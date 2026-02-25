package cn.qaiu.parser.impl;

import cn.qaiu.entity.FileInfo;
import cn.qaiu.entity.ShareLinkInfo;
import cn.qaiu.parser.PanBase;
import cn.qaiu.util.DateTimeUtils;
import cn.qaiu.util.HeaderUtils;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QQ闪传 <br>
 * 只能客户端上传 支持Android QQ 9.2.5, MACOS QQ 6.9.78，可生成分享链接，通过浏览器下载，支持超大文件，有效期默认7天（暂时没找到续期方法）。<br>
 */
public class QQscTool extends PanBase {

    Logger LOG = LoggerFactory.getLogger(QQscTool.class);

    private static final String API_URL = "https://qfile.qq.com/http2rpc/gotrpc/noauth/trpc.qqntv2.richmedia.InnerProxy/BatchDownload";
    private static final String FILE_LIST_API_URL = "https://qfile.qq.com/http2rpc/gotrpc/noauth/trpc.file.FileFlashTrans/GetFileList";
    private static final String PROGRESS_API_URL = "https://qfile.qq.com/http2rpc/gotrpc/noauth/trpc.file.FileFlashTrans/GetTransProgress";

    private static final MultiMap HEADERS = HeaderUtils.parseHeaders("""
            Accept-Encoding: gzip, deflate
            Accept-Language: zh-CN,zh;q=0.9
            Connection: keep-alive
            Cookie: uin=9000002; p_uin=9000002; qqweb_env=
            DNT: 1
            Origin: https://qfile.qq.com
            Referer: https://qfile.qq.com/q/Xolxtv5b4O
            Sec-Fetch-Dest: empty
            Sec-Fetch-Mode: cors
            Sec-Fetch-Site: same-origin
            User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36 Edg/138.0.0.0
            accept: application/json
            content-type: application/json
            sec-ch-ua: "Not)A;Brand";v="8", "Chromium";v="138", "Microsoft Edge";v="138"
            sec-ch-ua-mobile: ?0
            sec-ch-ua-platform: "macOS"
            x-oidb: {"uint32_command":"0x9248", "uint32_service_type":"4"}
            """);

    private String filesetId;
    private String deviceId;

    public QQscTool(ShareLinkInfo shareLinkInfo) {
        super(shareLinkInfo);
    }

    public Future<String> parse() {
        // 第一步：先请求分享URL获取deviceId
        initDeviceId().compose(v -> {
            // 第二步：获取文件列表信息
            return getFileListInfo();
        }).onSuccess(fileListResult -> {
            JsonObject data = fileListResult.getJsonObject("data");
            JsonArray fileLists = data.getJsonArray("file_lists");
            
            if (fileLists == null || fileLists.isEmpty()) {
                promise.fail("未能获取到文件列表");
                return;
            }
            
            JsonObject firstList = fileLists.getJsonObject(0);
            JsonArray fileList = firstList.getJsonArray("file_list");
            
            if (fileList == null || fileList.isEmpty()) {
                promise.fail("文件列表为空");
                return;
            }
            
            // 判断是否为目录分享（多文件）
            boolean isDirectory = fileList.size() > 1;
            
            if (isDirectory) {
                LOG.info("检测到目录分享，包含 {} 个文件", fileList.size());
                // 保存必要信息供 parseFileList 使用
                shareLinkInfo.getOtherParam().put("filesetId", filesetId);
                shareLinkInfo.getOtherParam().put("deviceId", deviceId);
                shareLinkInfo.getOtherParam().put("isDirectory", true);
                
                // 目录分享不直接返回下载链接，提示使用 parseFileList
                promise.fail("这是一个目录分享，包含 " + fileList.size() + " 个文件，请使用 parseFileList 方法获取文件列表");
            } else {
                LOG.info("检测到单文件分享");
                // 单文件分享
                JsonObject fileItem = fileList.getJsonObject(0);
                FileInfo fileInfo = extractFileInfo(fileItem);
                shareLinkInfo.getOtherParam().put("fileInfo", fileInfo);
                shareLinkInfo.getOtherParam().put("isDirectory", false);
                
                // 获取剩余时长和时间信息
                fetchLeftTime().onSuccess(timeInfo -> {
                    if (fileInfo.getExtParameters() == null) {
                        fileInfo.setExtParameters(new HashMap<>());
                    }
                    fileInfo.getExtParameters().put("leftTimeDays", timeInfo.getDouble("leftTimeDays"));
                    
                    // 设置时间信息（秒级时间戳）
                    String createTime = timeInfo.getString("createTime", "0");
                    String updateTime = timeInfo.getString("updateTime", "0");
                    if (!"0".equals(createTime)) {
                        fileInfo.setCreateTime(DateTimeUtils.formatSecondsStringToDateTime(createTime));
                    }
                    if (!"0".equals(updateTime)) {
                        // 毫秒时间戳转换为 yyyy-MM-dd HH:mm:ss
                        fileInfo.setUpdateTime(DateTimeUtils.formatMillisStringToDateTime(updateTime));
                    }
                    
                    getDownloadUrl(fileItem).onSuccess(promise::complete)
                            .onFailure(promise::fail);
                }).onFailure(e -> {
                    LOG.warn("获取剩余时长失败: {}", e.getMessage());
                    // 即使获取剩余时长失败，也继续返回下载链接
                    getDownloadUrl(fileItem).onSuccess(promise::complete)
                            .onFailure(promise::fail);
                });
            }
        }).onFailure(e -> {
            LOG.error("获取文件列表失败", e);
            promise.fail(e);
        });

        return promise.future();
    }
    
    /**
     * 初始化deviceId（从分享URL获取cookie）
     */
    private Future<Void> initDeviceId() {
        return clientSession.getAbs(shareLinkInfo.getShareUrl()).send()
                .compose(result -> {
                    if (result.statusCode() == 200) {
                        String htmlJs = result.bodyAsString();
                        LOG.debug("获取到的HTML内容: {}", htmlJs);
                        
                        String fileUUID = getFileUUID(htmlJs);
                        if (fileUUID == null) {
                            return Future.failedFuture("未能提取到文件UUID");
                        }
                        
                        // 保存 filesetId 用于后续查询
                        this.filesetId = fileUUID;
                        LOG.info("提取到的文件UUID: {}", fileUUID);
                        
                        // 从cookie中提取deviceId
                        List<String> cookies = result.cookies();
                        for (String cookie : cookies) {
                            if (cookie.startsWith("deviceId=")) {
                                this.deviceId = cookie.split(";")[0].split("=", 2)[1];
                                LOG.info("获取到deviceId: {}", this.deviceId);
                                break;
                            }
                        }
                        
                        return Future.succeededFuture();
                    } else {
                        return Future.failedFuture("请求失败，状态码: " + result.statusCode());
                    }
                });
    }
    
    /**
     * 获取文件列表信息
     */
    private Future<JsonObject> getFileListInfo() {
        if (filesetId == null) {
            return Future.failedFuture("filesetId 未初始化");
        }
        
        // 构建请求获取文件列表
        JsonObject reqInfo = new JsonObject()
                .put("parent_id", "")
                .put("req_depth", 1)
                .put("count", 28)
                .putNull("pagination_info")
                .put("filter_condition", new JsonObject().put("file_category", 0))
                .put("sort_conditions", new JsonArray().add(
                        new JsonObject().put("sort_field", 0).put("sort_order", 0)
                ));
        
        JsonObject requestBody = new JsonObject()
                .put("fileset_id", filesetId)
                .put("req_infos", new JsonArray().add(reqInfo))
                .put("support_folder_status", true)
                .put("scene_type", 103);
        
        MultiMap headers = buildHeaders("0x93d4");
        
        return clientSession.postAbs(FILE_LIST_API_URL)
                .putHeaders(headers)
                .sendJsonObject(requestBody)
                .compose(result -> {
                    if (result.statusCode() == 200) {
                        JsonObject body = asJson(result);
                        LOG.debug("文件列表API响应: {}", body.encodePrettily());
                        
                        if (!body.containsKey("retcode") || body.getInteger("retcode") != 0) {
                            return Future.failedFuture("API请求失败，错误信息: " + body.encodePrettily());
                        }
                        return Future.succeededFuture(body);
                    } else {
                        return Future.failedFuture("API请求失败，状态码: " + result.statusCode());
                    }
                });
    }
    
    /**
     * 从文件项中提取文件信息
     */
    private FileInfo extractFileInfo(JsonObject fileItem) {
        FileInfo fileInfo = new FileInfo();
        
        // 基本信息
        fileInfo.setFileName(fileItem.getString("name"));
        fileInfo.setFileId(fileItem.getString("cli_fileid"));
        fileInfo.setSize(Long.parseLong(fileItem.getString("file_size", "0")));
        fileInfo.setSizeStr(formatFileSize(fileInfo.getSize()));
        
        // 文件类型
        Integer fileType = fileItem.getInteger("file_type");
        fileInfo.setFileType(getFileTypeName(fileType));
        
        // Hash信息
        fileInfo.setHash(fileItem.getString("file_md5"));
        
        // 时间信息
        String updateTimestamp = fileItem.getString("update_timestamp", "0");
        if (!"0".equals(updateTimestamp) && updateTimestamp.length() > 10) {
            // 毫秒时间戳转换为秒
            fileInfo.setUpdateTime(updateTimestamp.substring(0, updateTimestamp.length() - 3));
        }
        
        // 扩展参数
        Map<String, Object> extParams = new HashMap<>();
        extParams.put("file_sha1", fileItem.getString("file_sha1"));
        extParams.put("fileset_id", fileItem.getString("fileset_id"));
        extParams.put("is_cloud_file", fileItem.getBoolean("is_cloud_file", false));
        extParams.put("physical_id", fileItem.getJsonObject("physical", new JsonObject()).getString("id"));
        fileInfo.setExtParameters(extParams);
        
        // 设置网盘类型
        fileInfo.setPanType("qqsc");
        
        LOG.debug("提取文件信息: {}", fileInfo.getFileName());
        return fileInfo;
    }
    
    /**
     * 解析文件列表（目录分享）
     */
    @Override
    public Future<List<FileInfo>> parseFileList() {
        Promise<List<FileInfo>> promise = Promise.promise();
        
        // 从 otherParam 中获取已保存的信息
        String savedFilesetId = (String) shareLinkInfo.getOtherParam().get("filesetId");
        String savedDeviceId = (String) shareLinkInfo.getOtherParam().get("deviceId");
        
        if (savedFilesetId != null && savedDeviceId != null) {
            // 已有信息，直接获取文件列表
            this.filesetId = savedFilesetId;
            this.deviceId = savedDeviceId;
            fetchFileListData(promise);
        } else {
            // 需要先初始化
            initDeviceId().compose(v -> getFileListInfo())
                    .onSuccess(fileListResult -> {
                        // 保存信息供后续使用
                        shareLinkInfo.getOtherParam().put("filesetId", filesetId);
                        shareLinkInfo.getOtherParam().put("deviceId", deviceId);
                        fetchFileListData(promise);
                    })
                    .onFailure(promise::fail);
        }
        
        return promise.future();
    }
    
    /**
     * 获取文件列表数据
     */
    private void fetchFileListData(Promise<List<FileInfo>> promise) {
        getFileListInfo().onSuccess(fileListResult -> {
            JsonObject data = fileListResult.getJsonObject("data");
            JsonArray fileLists = data.getJsonArray("file_lists");
            
            if (fileLists == null || fileLists.isEmpty()) {
                promise.fail("未能获取到文件列表");
                return;
            }
            
            JsonObject firstList = fileLists.getJsonObject(0);
            JsonArray fileList = firstList.getJsonArray("file_list");
            
            if (fileList == null || fileList.isEmpty()) {
                promise.complete(new ArrayList<>());
                return;
            }
            
            LOG.info("获取到 {} 个文件", fileList.size());
            List<FileInfo> result = new ArrayList<>();
            
            // 先获取剩余时长和时间信息
            fetchLeftTime().onComplete(leftTimeResult -> {
                JsonObject timeInfo = leftTimeResult.succeeded() ? leftTimeResult.result() : null;
                Double leftTimeDays = timeInfo != null ? timeInfo.getDouble("leftTimeDays") : null;
                String createTime = timeInfo != null ? timeInfo.getString("createTime", "0") : "0";
                String updateTime = timeInfo != null ? timeInfo.getString("updateTime", "0") : "0";
                
                for (int i = 0; i < fileList.size(); i++) {
                    JsonObject fileItem = fileList.getJsonObject(i);
                    FileInfo fileInfo = extractFileInfo(fileItem);
                    
                    // 添加剩余时长
                    if (leftTimeDays != null) {
                        if (fileInfo.getExtParameters() == null) {
                            fileInfo.setExtParameters(new HashMap<>());
                        }
                        fileInfo.getExtParameters().put("leftTimeDays", leftTimeDays);
                    }
                    
                    // 设置时间信息
                    if (!"0".equals(createTime)) {
                        fileInfo.setCreateTime(DateTimeUtils.formatSecondsStringToDateTime(createTime));
                    }
                    if (!"0".equals(updateTime)) {
                        // 毫秒时间戳转换为 yyyy-MM-dd HH:mm:ss
                        fileInfo.setUpdateTime(DateTimeUtils.formatMillisStringToDateTime(updateTime));
                    }
                    
                    // 设置下载解析URL（用于 parseById）
                    Map<String, Object> params = new HashMap<>();
                    params.put("physicalId", fileItem.getJsonObject("physical", new JsonObject()).getString("id"));
                    params.put("fileName", fileItem.getString("name"));
                    params.put("filesetId", filesetId);
                    
                    JsonObject paramJson = new JsonObject(params);
                    String encodedParam = cn.qaiu.util.CommonUtils.urlBase64Encode(paramJson.encode());
                    fileInfo.setParserUrl(String.format("/v2/redirectUrl/%s/%s", 
                            shareLinkInfo.getType(), encodedParam));
                    
                    result.add(fileInfo);
                }
                
                promise.complete(result);
            });
        }).onFailure(promise::fail);
    }
    
    /**
     * 根据文件ID获取下载链接
     */
    @Override
    public Future<String> parseById() {
        Promise<String> promise = Promise.promise();
        
        // 从 paramJson 中提取参数
        JsonObject paramJson = (JsonObject) shareLinkInfo.getOtherParam().get("paramJson");
        if (paramJson == null) {
            promise.fail("缺少必要的参数");
            return promise.future();
        }
        
        String physicalId = paramJson.getString("physicalId");
        String fileName = paramJson.getString("fileName");
        String filesetId = paramJson.getString("filesetId");
        
        if (physicalId == null || filesetId == null) {
            promise.fail("缺少必要的参数: physicalId 或 filesetId");
            return promise.future();
        }
        
        // 构建下载请求
        String jsonTemplate = """
                {"req_head":{"agent":8},"download_info":[{"batch_id":"%s","scene":{"business_type":4,"app_type":22,"scene_type":5},"index_node":{"file_uuid":"%s"},"url_type":2,"download_scene":0}],"scene_type":103}
                """;
        
        String formatted = jsonTemplate.formatted(filesetId, physicalId);
        JsonObject requestBody = new JsonObject(formatted);
        
        client.postAbs(API_URL)
                .putHeaders(HEADERS)
                .sendJsonObject(requestBody)
                .onSuccess(result -> {
                    if (result.statusCode() == 200) {
                        JsonObject body = asJson(result);
                        LOG.debug("下载API响应: {}", body.encodePrettily());
                        
                        if (!body.containsKey("retcode") || body.getInteger("retcode") != 0) {
                            promise.fail("API请求失败，错误信息: " + body.encodePrettily());
                            return;
                        }
                        
                        JsonArray downloadRsp = body.getJsonObject("data").getJsonArray("download_rsp");
                        if (downloadRsp != null && !downloadRsp.isEmpty()) {
                            String url = downloadRsp.getJsonObject(0).getString("url");
                            if (fileName != null) {
                                url = url + "&filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                            }
                            promise.complete(url);
                        } else {
                            promise.fail("API响应中缺少 download_rsp");
                        }
                    } else {
                        promise.fail("API请求失败，状态码: " + result.statusCode());
                    }
                })
                .onFailure(promise::fail);
        
        return promise.future();
    }
    
    /**
     * 获取下载URL
     */
    private Future<String> getDownloadUrl(JsonObject fileItem) {
        String physicalId = fileItem.getJsonObject("physical", new JsonObject()).getString("id");
        String fileName = fileItem.getString("name");
        
        if (physicalId == null) {
            return Future.failedFuture("未能获取到physical ID");
        }
        
        String jsonTemplate = """
                {"req_head":{"agent":8},"download_info":[{"batch_id":"%s","scene":{"business_type":4,"app_type":22,"scene_type":5},"index_node":{"file_uuid":"%s"},"url_type":2,"download_scene":0}],"scene_type":103}
                """;
        
        String formatted = jsonTemplate.formatted(filesetId, physicalId);
        JsonObject requestBody = new JsonObject(formatted);
        
        return client.postAbs(API_URL)
                .putHeaders(HEADERS)
                .sendJsonObject(requestBody)
                .compose(result -> {
                    if (result.statusCode() == 200) {
                        JsonObject body = asJson(result);
                        LOG.debug("下载API响应内容: {}", body.encodePrettily());
                        
                        if (!body.containsKey("retcode") || body.getInteger("retcode") != 0) {
                            return Future.failedFuture("API请求失败，错误信息: " + body.encodePrettily());
                        }
                        
                        JsonArray downloadRsp = body.getJsonObject("data").getJsonArray("download_rsp");
                        if (downloadRsp != null && !downloadRsp.isEmpty()) {
                            String url = downloadRsp.getJsonObject(0).getString("url");
                            if (fileName != null) {
                                url = url + "&filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                            }
                            return Future.succeededFuture(url);
                        } else {
                            return Future.failedFuture("API响应中缺少 download_rsp");
                        }
                    } else {
                        return Future.failedFuture("API请求失败，状态码: " + result.statusCode());
                    }
                });
    }
    
    /**
     * 获取分享剩余时长和文件信息（转换为天数）
     */
    private Future<JsonObject> fetchLeftTime() {
        if (filesetId == null) {
            return Future.failedFuture("filesetId 未初始化");
        }
        
        JsonObject requestBody = new JsonObject()
                .put("fileset_ids", new JsonArray().add(filesetId))
                .put("scene_type", 103);
        
        MultiMap headers = buildHeaders("0x93d9");
        
        return clientSession.postAbs(PROGRESS_API_URL)
                .putHeaders(headers)
                .sendJsonObject(requestBody)
                .compose(result -> {
                    if (result.statusCode() == 200) {
                        JsonObject body = asJson(result);
                        LOG.debug("剩余时长API响应: {}", body.encodePrettily());
                        
                        if (!body.containsKey("retcode") || body.getInteger("retcode") != 0) {
                            return Future.failedFuture("API请求失败，错误信息: " + body.encodePrettily());
                        }
                        
                        JsonArray transProgress = body.getJsonObject("data", new JsonObject())
                                .getJsonArray("trans_progress");
                        
                        if (transProgress != null && !transProgress.isEmpty()) {
                            JsonObject progressItem = transProgress.getJsonObject(0);
                            JsonObject fileset = progressItem.getJsonObject("fileset", new JsonObject());
                            String leftTimeStr = fileset.getString("left_time", "0");
                            String createTime = fileset.getString("create_time", "0");
                            String updateTimestamp = fileset.getString("update_timestamp", "0");
                            
                            try {
                                long leftTimeSeconds = Long.parseLong(leftTimeStr);
                                // 将秒转换为天数，保留2位小数
                                double leftTimeDays = Math.round(leftTimeSeconds / 86400.0 * 100.0) / 100.0;
                                LOG.info("剩余时长: {} 秒 = {} 天", leftTimeSeconds, leftTimeDays);
                                
                                // 返回包含多个字段的对象
                                JsonObject timeResult = new JsonObject()
                                        .put("leftTimeDays", leftTimeDays)
                                        .put("createTime", createTime)
                                        .put("updateTime", updateTimestamp);
                                
                                return Future.succeededFuture(timeResult);
                            } catch (NumberFormatException e) {
                                return Future.failedFuture("解析剩余时长失败: " + leftTimeStr);
                            }
                        } else {
                            return Future.failedFuture("API响应中缺少 trans_progress");
                        }
                    } else {
                        return Future.failedFuture("API请求失败，状态码: " + result.statusCode());
                    }
                });
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
    
    /**
     * 构建请求头（包含deviceId和x-oidb）
     */
    private MultiMap buildHeaders(String oidbCommand) {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        headers.addAll(HEADERS);
        headers.set("x-oidb", "{\"uint32_command\":\"" + oidbCommand + "\", \"uint32_service_type\":\"1\"}");
        
        // 构建完整的Cookie头（包含deviceId和其他必需的cookie）
        // 注意：curl示例中没有x-device-id头，只在Cookie中包含deviceId
        if (deviceId != null) {
            // Cookie顺序：deviceId, env, uin, p_uin, qqweb_env
            String cookieValue = "deviceId=" + deviceId + "; env=; uin=9000002; p_uin=9000002; qqweb_env=";
            headers.set("Cookie", cookieValue);
        }
        
        return headers;
    }
    
    /**
     * 根据文件类型代码获取类型名称
     */
    private String getFileTypeName(Integer fileType) {
        if (fileType == null) {
            return "unknown";
        }
        return switch (fileType) {
            case 1 -> "image";
            case 2 -> "video";
            case 3 -> "audio";
            case 4 -> "archive";
            case 5 -> "document";
            default -> "file";
        };
    }

    String getFileUUID(String htmlJs) {
        // 使用正则表达式匹配UUID格式: 8-4-4-4-12
        // 例如: ed0e9bb6-f450-4122-8946-3f0b8f60c0a6
        // fileset_id在script标签中，且前面没有buildId
        
        // 先提取script标签内容
        Pattern scriptPattern = Pattern.compile("<script[^>]*>(.*?)</script>", Pattern.DOTALL);
        Matcher scriptMatcher = scriptPattern.matcher(htmlJs);
        
        String scriptContent = "";
        while (scriptMatcher.find()) {
            scriptContent = scriptMatcher.group(1);
            // 在script内容中查找UUID，确保前面没有buildId
            Pattern uuidPattern = Pattern.compile("(?<!buildId[\"':=\\s]{0,10})([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})");
            Matcher uuidMatcher = uuidPattern.matcher(scriptContent);
            
            while (uuidMatcher.find()) {
                String uuid = uuidMatcher.group(1);
                // 额外验证：确保这个UUID前面确实没有buildId
                int uuidPos = scriptContent.indexOf(uuid);
                String before = scriptContent.substring(Math.max(0, uuidPos - 30), uuidPos);
                if (!before.contains("buildId")) {
                    LOG.info("提取到的fileset_id: {}", uuid);
                    return uuid;
                }
                LOG.debug("跳过buildId相关的UUID: {}", uuid);
            }
        }
        
        LOG.error("未找到符合条件的fileset_id");
        return null;
    }

    public static String extractFileNameFromTitle(String content) {
        // 匹配<title>和</title>之间的内容
        Pattern pattern = Pattern.compile("<title>(.*?)</title>");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String fullTitle = matcher.group(1);
            // 按 "｜" 分割，取前半部分
            int sepIndex = fullTitle.indexOf("｜");
            if (sepIndex != -1) {
                return fullTitle.substring(0, sepIndex);
            }
            return fullTitle; // 如果没有分隔符，就返回全部
        }
        return null;
    }
}

