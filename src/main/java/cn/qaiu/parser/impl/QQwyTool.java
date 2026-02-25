package cn.qaiu.parser.impl;

import cn.qaiu.entity.FileInfo;
import cn.qaiu.entity.ShareLinkInfo;
import cn.qaiu.parser.PanBase;
import cn.qaiu.util.FileSizeConverter;
import cn.qaiu.util.HeaderUtils;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 腾讯微云解析器 (share.weiyun.com)
 * 认证方式: cookie (有效期1个月)
 * 域名规则: https://share.weiyun.com/{分享key}
 */
public class QQwyTool extends PanBase {
    
    public static final String SHARE_URL_PREFIX = "https://share.weiyun.com/";
    
    private static final String VIEW_API_URL = "https://share.weiyun.com/webapp/json/weiyunShare/WeiyunShareView";
    private static final String DIR_LIST_API_URL = "https://share.weiyun.com/webapp/json/weiyunShareNoLogin/WeiyunShareDirList";
    private static final String DOWNLOAD_API_URL = "https://share.weiyun.com/webapp/json/weiyunShare/WeiyunShareBatchDownload";
    
    private final MultiMap header = HeaderUtils.parseHeaders("""
            User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36 Edg/144.0.0.0
            Content-Type: application/json;charset=UTF-8
            Accept: application/json, text/plain, */*
            Origin: https://share.weiyun.com
            Referer: https://share.weiyun.com/
            Cache-Control: no-cache
            Pragma: no-cache
            DNT: 1
            Sec-Fetch-Dest: empty
            Sec-Fetch-Mode: cors
            Sec-Fetch-Site: same-origin
            """);
    
    private String wyctoken = "";
    private String gToken = "";
    
    public QQwyTool(ShareLinkInfo shareLinkInfo) {
        super(shareLinkInfo);
        // 从认证配置中获取 cookie
        if (shareLinkInfo.getOtherParam() != null && shareLinkInfo.getOtherParam().containsKey("auths")) {
            MultiMap auths = (MultiMap) shareLinkInfo.getOtherParam().get("auths");
            String cookie = auths.get("cookie");
            if (cookie != null && !cookie.isEmpty()) {
                header.add(HttpHeaders.COOKIE, cookie);
                extractTokensFromCookie(cookie);
            }
        }
    }
    
    /**
     * 从Cookie中提取wyctoken和g_tk
     */
    private void extractTokensFromCookie(String cookie) {
        Pattern wyctokenPattern = Pattern.compile("wyctoken=([^;\\s]+)");
        Matcher matcher = wyctokenPattern.matcher(cookie);
        if (matcher.find()) {
            wyctoken = matcher.group(1);
            gToken = wyctoken; // g_tk使用wyctoken的值
        }
    }
    
    @Override
    public Future<String> parse() {
        String shareKey = shareLinkInfo.getShareKey();
        if (shareKey == null || shareKey.isEmpty()) {
            fail("分享key为空");
            return promise.future();
        }
        
        // 更新Referer
        header.set("Referer", SHARE_URL_PREFIX + shareKey);
        
        // 步骤1: 如果没有wyctoken，先获取
        if (wyctoken.isEmpty()) {
            getWyctoken(shareKey).onSuccess(token -> {
                wyctoken = token;
                gToken = token;
                // 获取文件信息
                getFileInfo(shareKey);
            }).onFailure(err -> fail(err, "获取wyctoken失败"));
        } else {
            // 直接获取文件信息
            getFileInfo(shareKey);
        }
        
        return promise.future();
    }
    
    @Override
    public Future<List<FileInfo>> parseFileList() {
        Promise<List<FileInfo>> listPromise = Promise.promise();
        
        String shareKey = shareLinkInfo.getShareKey();
        if (shareKey == null || shareKey.isEmpty()) {
            listPromise.fail("分享key为空");
            return listPromise.future();
        }
        
        // 更新Referer
        header.set("Referer", SHARE_URL_PREFIX + shareKey);
        
        // 获取目录ID参数（如果有）
        String dirKey = (String) shareLinkInfo.getOtherParam().get("dirKey");
        String dirName = (String) shareLinkInfo.getOtherParam().get("dirName");
        
        // 步骤1: 如果没有wyctoken，先获取
        if (wyctoken.isEmpty()) {
            getWyctoken(shareKey).onSuccess(token -> {
                wyctoken = token;
                gToken = token;
                // 解析目录
                parseDirectory(shareKey, dirKey, dirName, listPromise);
            }).onFailure(err -> {
                log.error("获取wyctoken失败", err);
                listPromise.fail(err);
            });
        } else {
            // 直接解析目录
            parseDirectory(shareKey, dirKey, dirName, listPromise);
        }
        
        return listPromise.future();
    }
    
    /**
     * 获取wyctoken
     */
    private Future<String> getWyctoken(String shareKey) {
        Promise<String> tokenPromise = Promise.promise();
        String shareUrl = SHARE_URL_PREFIX + shareKey;
        
        clientNoRedirects.getAbs(shareUrl)
            .putHeaders(header)
            .send(ar -> {
                if (ar.succeeded()) {
                    var response = ar.result();
                    // 从Set-Cookie中提取wyctoken
                    List<String> cookies = response.cookies();
                    for (String cookie : cookies) {
                        if (cookie.startsWith("wyctoken=")) {
                            String token = cookie.substring(9).split(";")[0];
                            log.info("获取到wyctoken: {}", token);
                            // 更新cookie header
                            String currentCookie = header.get(HttpHeaders.COOKIE);
                            if (currentCookie != null && !currentCookie.isEmpty()) {
                                header.set(HttpHeaders.COOKIE, currentCookie + "; wyctoken=" + token);
                            } else {
                                header.add(HttpHeaders.COOKIE, "wyctoken=" + token);
                            }
                            tokenPromise.complete(token);
                            return;
                        }
                    }
                    tokenPromise.fail("未找到wyctoken");
                } else {
                    tokenPromise.fail(ar.cause());
                }
            });
        
        return tokenPromise.future();
    }
    
    /**
     * 获取文件信息
     */
    private void getFileInfo(String shareKey) {
        String sharePwd = shareLinkInfo.getSharePassword() != null ? shareLinkInfo.getSharePassword() : "";
        long seq = System.currentTimeMillis() / 1000;
        double r = Math.random();
        
        String apiUrl = VIEW_API_URL + "?refer=chrome_mac&g_tk=" + gToken + "&r=" + r;
        
        // 构建请求body
        JsonObject reqHeader = new JsonObject()
            .put("seq", seq)
            .put("type", 1)
            .put("cmd", 12002)
            .put("appid", 30113)
            .put("version", 3)
            .put("major_version", 3)
            .put("minor_version", 3)
            .put("fix_version", 3)
            .put("wx_openid", "")
            .put("qq_openid", "")
            .put("user_flag", 0)
            .put("env_id", "");
        
        JsonObject shareViewReq = new JsonObject()
            .put("share_pwd", sharePwd)
            .put("share_key", shareKey);
        
        JsonObject extReqHead = new JsonObject()
            .put("token_info", new JsonObject()
                .put("token_type", 3)
                .put("login_key_type", 1540))
            .put("language_info", new JsonObject()
                .put("language_type", 2052));
        
        JsonObject reqMsgBody = new JsonObject()
            .put("ext_req_head", extReqHead)
            .put(".weiyun.WeiyunShareViewMsgReq_body", shareViewReq);
        
        JsonObject requestBody = new JsonObject()
            .put("req_header", reqHeader.encode())
            .put("req_body", new JsonObject()
                .put("ReqMsg_body", reqMsgBody).encode());
        
        client.postAbs(apiUrl)
            .putHeaders(header)
            .sendJsonObject(requestBody, ar -> {
                if (ar.succeeded()) {
                    var response = ar.result();
                    JsonObject respJson = response.bodyAsJsonObject();
                    
                    if (respJson != null && respJson.containsKey("data")) {
                        JsonObject data = respJson.getJsonObject("data");
                        JsonObject rspHeader = data.getJsonObject("rsp_header");
                        
                        if (rspHeader.getInteger("retcode") == 0) {
                            JsonObject rspBody = data.getJsonObject("rsp_body")
                                .getJsonObject("RspMsg_body");
                            parseFileList(rspBody, shareKey, sharePwd);
                        } else {
                            fail("获取文件信息失败: " + rspHeader.getString("retmsg"));
                        }
                    } else {
                        fail("响应格式错误");
                    }
                } else {
                    fail(ar.cause(), "请求文件信息失败");
                }
            });
    }
    
    /**
     * 解析文件列表
     */
    private void parseFileList(JsonObject rspBody, String shareKey, String sharePwd) {
        JsonArray fileList = rspBody.getJsonArray("file_list");
        JsonArray dirList = rspBody.getJsonArray("dir_list");
        
        if (fileList == null || fileList.isEmpty()) {
            if (dirList != null && !dirList.isEmpty()) {
                fail("暂不支持文件夹解析，请直接分享文件");
            } else {
                fail("未找到文件");
            }
            return;
        }
        
        List<FileInfo> fileInfoList = new ArrayList<>();
        
        // 解析文件列表
        for (int i = 0; i < fileList.size(); i++) {
            JsonObject file = fileList.getJsonObject(i);
            FileInfo fileInfo = new FileInfo();
            
            String fileId = file.getString("file_id");
            String fileName = file.getString("file_name");
            Long fileSize = file.getLong("file_size");
            String pdirKey = file.getString("pdir_key");
            
            fileInfo.setFileName(fileName);
            fileInfo.setSize(fileSize);
            fileInfo.setSizeStr(FileSizeConverter.convertToReadableSize(fileSize));
            
            // 存储用于下载的信息到extParameters
            Map<String, Object> extParams = new java.util.HashMap<>();
            extParams.put("file_id", fileId);
            extParams.put("filename", fileName);
            extParams.put("file_size", fileSize);
            extParams.put("pdir_key", pdirKey);
            
            fileInfo.setExtParameters(extParams);
            fileInfoList.add(fileInfo);
        }
        
        // 批量获取下载链接
        getDownloadUrls(fileInfoList, shareKey, sharePwd);
    }
    
    /**
     * 获取下载链接
     */
    private void getDownloadUrls(List<FileInfo> fileInfoList, String shareKey, String sharePwd) {
        long seq = System.currentTimeMillis() / 1000;
        double r = Math.random();
        
        String apiUrl = DOWNLOAD_API_URL + "?refer=chrome_mac&g_tk=" + gToken + "&r=" + r;
        
        // 构建文件列表
        JsonArray fileListArray = new JsonArray();
        for (FileInfo fileInfo : fileInfoList) {
            Map<String, Object> extParams = fileInfo.getExtParameters();
            JsonObject fileItem = new JsonObject()
                .put("pdir_key", extParams.get("pdir_key"))
                .put("file_id", extParams.get("file_id"))
                .put("filename", extParams.get("filename"))
                .put("file_size", extParams.get("file_size"));
            fileListArray.add(fileItem);
        }
        
        // 构建请求body
        JsonObject reqHeader = new JsonObject()
            .put("seq", seq)
            .put("type", 1)
            .put("cmd", 12024)
            .put("appid", 30113)
            .put("version", 3)
            .put("major_version", 3)
            .put("minor_version", 3)
            .put("fix_version", 3)
            .put("wx_openid", "")
            .put("qq_openid", "")
            .put("user_flag", 0)
            .put("env_id", "")
            .put("device_info", "{\"browser\":\"chrome\"}");
        
        JsonObject downloadReq = new JsonObject()
            .put("share_key", shareKey)
            .put("pwd", sharePwd)
            .putNull("file_owner")
            .put("download_type", 0)
            .put("file_list", fileListArray);
        
        JsonObject extReqHead = new JsonObject()
            .put("token_info", new JsonObject()
                .put("token_type", 3)
                .put("login_key_type", 1540))
            .put("language_info", new JsonObject()
                .put("language_type", 2052));
        
        JsonObject reqMsgBody = new JsonObject()
            .put("ext_req_head", extReqHead)
            .put(".weiyun.WeiyunShareBatchDownloadMsgReq_body", downloadReq);
        
        JsonObject requestBody = new JsonObject()
            .put("req_header", reqHeader.encode())
            .put("req_body", new JsonObject()
                .put("ReqMsg_body", reqMsgBody).encode());
        
        client.postAbs(apiUrl)
            .putHeaders(header)
            .sendJsonObject(requestBody, ar -> {
                if (ar.succeeded()) {
                    var response = ar.result();
                    JsonObject respJson = response.bodyAsJsonObject();
                    
                    if (respJson != null && respJson.containsKey("data")) {
                        JsonObject data = respJson.getJsonObject("data");
                        JsonObject rspHeader = data.getJsonObject("rsp_header");
                        
                        if (rspHeader.getInteger("retcode") == 0) {
                            JsonObject rspBody = data.getJsonObject("rsp_body")
                                .getJsonObject("RspMsg_body");
                            JsonArray downloadFileList = rspBody.getJsonArray("file_list");
                            
                            // 提取file_sha用于构建FTN5K cookie
                            String fileSha = null;
                            
                            // 更新文件信息的下载链接
                            for (int i = 0; i < downloadFileList.size(); i++) {
                                JsonObject downloadFile = downloadFileList.getJsonObject(i);
                                if (downloadFile.getInteger("retcode") == 0) {
                                    String httpsDownloadUrl = downloadFile.getString("https_download_url");
                                    String fileId = downloadFile.getString("file_id");
                                    
                                    // 提取file_sha（用于FTN5K cookie）
                                    if (fileSha == null) {
                                        String sha = downloadFile.getString("file_sha");
                                        if (sha != null && sha.length() >= 8) {
                                            fileSha = sha.substring(sha.length() - 8);
                                        }
                                    }
                                    
                                    // 找到对应的fileInfo并设置下载链接
                                    for (FileInfo fileInfo : fileInfoList) {
                                        Map<String, Object> extParams = fileInfo.getExtParameters();
                                        if (extParams != null && fileId.equals(extParams.get("file_id"))) {
                                            fileInfo.setFilePath(httpsDownloadUrl);
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            // 构建返回结果（带下载header）
                            buildResultWithHeaders(fileInfoList, fileSha);
                        } else {
                            fail("获取下载链接失败: " + rspHeader.getString("retmsg"));
                        }
                    } else {
                        fail("响应格式错误");
                    }
                } else {
                    fail(ar.cause(), "请求下载链接失败");
                }
            });
    }
    
    /**
     * 解析目录（用于parseFileList）
     */
    private void parseDirectory(String shareKey, String dirKey, String dirName, Promise<List<FileInfo>> listPromise) {
        // 如果没有指定目录，先获取根目录信息
        if (dirKey == null || dirKey.isEmpty()) {
            getFileInfoForDirectory(shareKey, listPromise);
        } else {
            // 直接获取指定目录的文件列表
            getDirFileList(shareKey, dirKey, dirName, listPromise);
        }
    }
    
    /**
     * 获取分享的根目录信息（用于目录解析）
     */
    private void getFileInfoForDirectory(String shareKey, Promise<List<FileInfo>> listPromise) {
        String sharePwd = shareLinkInfo.getSharePassword() != null ? shareLinkInfo.getSharePassword() : "";
        long seq = System.currentTimeMillis() / 1000;
        double r = Math.random();
        
        String apiUrl = VIEW_API_URL + "?refer=chrome_mac&g_tk=" + gToken + "&r=" + r;
        
        // 构建请求body（同parse方法）
        JsonObject reqHeader = new JsonObject()
            .put("seq", seq)
            .put("type", 1)
            .put("cmd", 12002)
            .put("appid", 30113)
            .put("version", 3)
            .put("major_version", 3)
            .put("minor_version", 3)
            .put("fix_version", 3)
            .put("wx_openid", "")
            .put("qq_openid", "")
            .put("user_flag", 0)
            .put("env_id", "");
        
        JsonObject shareViewReq = new JsonObject()
            .put("share_pwd", sharePwd)
            .put("share_key", shareKey);
        
        JsonObject extReqHead = new JsonObject()
            .put("token_info", new JsonObject()
                .put("token_type", 3)
                .put("login_key_type", 1540))
            .put("language_info", new JsonObject()
                .put("language_type", 2052));
        
        JsonObject reqMsgBody = new JsonObject()
            .put("ext_req_head", extReqHead)
            .put(".weiyun.WeiyunShareViewMsgReq_body", shareViewReq);
        
        JsonObject requestBody = new JsonObject()
            .put("req_header", reqHeader.encode())
            .put("req_body", new JsonObject()
                .put("ReqMsg_body", reqMsgBody).encode());
        
        client.postAbs(apiUrl)
            .putHeaders(header)
            .sendJsonObject(requestBody, ar -> {
                if (ar.succeeded()) {
                    var response = ar.result();
                    JsonObject respJson = response.bodyAsJsonObject();
                    
                    if (respJson != null && respJson.containsKey("data")) {
                        JsonObject data = respJson.getJsonObject("data");
                        JsonObject rspHeader = data.getJsonObject("rsp_header");
                        
                        if (rspHeader.getInteger("retcode") == 0) {
                            JsonObject rspBody = data.getJsonObject("rsp_body")
                                .getJsonObject("RspMsg_body");
                            
                            JsonArray dirList = rspBody.getJsonArray("dir_list");
                            JsonArray fileList = rspBody.getJsonArray("file_list");
                            
                            // 如果有目录，需要递归获取
                            if (dirList != null && !dirList.isEmpty()) {
                                // 取第一个目录的信息
                                JsonObject firstDir = dirList.getJsonObject(0);
                                String dirKey = firstDir.getString("dir_key");
                                String dirName = firstDir.getString("dir_name");
                                
                                // 递归获取目录内容
                                getDirFileList(shareKey, dirKey, dirName, listPromise);
                            } else if (fileList != null && !fileList.isEmpty()) {
                                // 根目录直接是文件，转换为FileInfo列表
                                List<FileInfo> allFiles = convertToFileInfoList(fileList, rspBody.getString("pdir_key", ""));
                                listPromise.complete(allFiles);
                            } else {
                                listPromise.fail("未找到文件或目录");
                            }
                        } else {
                            listPromise.fail("获取文件信息失败: " + rspHeader.getString("retmsg"));
                        }
                    } else {
                        listPromise.fail("响应格式错误");
                    }
                } else {
                    log.error("请求文件信息失败", ar.cause());
                    listPromise.fail(ar.cause());
                }
            });
    }
    
    /**
     * 获取目录文件列表
     */
    private void getDirFileList(String shareKey, String dirKey, String dirName, Promise<List<FileInfo>> listPromise) {
        String sharePwd = shareLinkInfo.getSharePassword() != null ? shareLinkInfo.getSharePassword() : "";
        long seq = System.currentTimeMillis() / 1000;
        double r = Math.random();
        
        String apiUrl = DIR_LIST_API_URL + "?refer=chrome_mac&g_tk=" + gToken + "&r=" + r;
        
        // 构建请求body
        JsonObject reqHeader = new JsonObject()
            .put("seq", seq)
            .put("type", 1)
            .put("cmd", 12031)
            .put("appid", 30113)
            .put("version", 3)
            .put("major_version", 3)
            .put("minor_version", 3)
            .put("fix_version", 3)
            .put("wx_openid", "")
            .put("qq_openid", "")
            .put("user_flag", 0)
            .put("env_id", "")
            .put("device_info", "{\"browser\":\"chrome\"}");
        
        JsonObject dirListReq = new JsonObject()
            .put("share_key", shareKey)
            .put("share_pwd", sharePwd)
            .put("dir_key", dirKey)
            .put("dir_name", dirName != null ? dirName : "")
            .put("get_type", 0)
            .put("start", 0)
            .put("count", 100)
            .put("get_abstract_url", true);
        
        JsonObject extReqHead = new JsonObject()
            .put("token_info", new JsonObject()
                .put("token_type", 3)
                .put("login_key_type", 1540))
            .put("language_info", new JsonObject()
                .put("language_type", 2052));
        
        JsonObject reqMsgBody = new JsonObject()
            .put("ext_req_head", extReqHead)
            .put(".weiyun.WeiyunShareDirListMsgReq_body", dirListReq);
        
        JsonObject requestBody = new JsonObject()
            .put("req_header", reqHeader.encode())
            .put("req_body", new JsonObject()
                .put("ReqMsg_body", reqMsgBody).encode());
        
        client.postAbs(apiUrl)
            .putHeaders(header)
            .sendJsonObject(requestBody, ar -> {
                if (ar.succeeded()) {
                    var response = ar.result();
                    JsonObject respJson = response.bodyAsJsonObject();
                    
                    if (respJson != null && respJson.containsKey("data")) {
                        JsonObject data = respJson.getJsonObject("data");
                        JsonObject rspHeader = data.getJsonObject("rsp_header");
                        
                        if (rspHeader.getInteger("retcode") == 0) {
                            JsonObject rspBody = data.getJsonObject("rsp_body")
                                .getJsonObject("RspMsg_body");
                            
                            JsonArray fileList = rspBody.getJsonArray("file_list");
                            JsonArray subDirList = rspBody.getJsonArray("dir_list");
                            
                            List<FileInfo> allFiles = new ArrayList<>();
                            
                            // 处理当前目录的文件
                            if (fileList != null && !fileList.isEmpty()) {
                                allFiles.addAll(convertToFileInfoList(fileList, dirKey));
                            }
                            
                            // 处理子目录（递归）
                            if (subDirList != null && !subDirList.isEmpty()) {
                                collectFilesFromSubDirs(shareKey, subDirList, allFiles, listPromise);
                            } else {
                                // 没有子目录，直接返回当前文件列表
                                listPromise.complete(allFiles);
                            }
                        } else {
                            listPromise.fail("获取目录列表失败: " + rspHeader.getString("retmsg"));
                        }
                    } else {
                        listPromise.fail("响应格式错误");
                    }
                } else {
                    log.error("请求目录列表失败", ar.cause());
                    listPromise.fail(ar.cause());
                }
            });
    }
    
    /**
     * 递归收集子目录中的文件
     */
    private void collectFilesFromSubDirs(String shareKey, JsonArray subDirList, 
                                         List<FileInfo> collectedFiles, Promise<List<FileInfo>> finalPromise) {
        if (subDirList.isEmpty()) {
            finalPromise.complete(collectedFiles);
            return;
        }
        
        // 递归处理每个子目录
        List<Future<List<FileInfo>>> futures = new ArrayList<>();
        
        for (int i = 0; i < subDirList.size(); i++) {
            JsonObject dir = subDirList.getJsonObject(i);
            String subDirKey = dir.getString("dir_key");
            String subDirName = dir.getString("dir_name");
            
            Promise<List<FileInfo>> subPromise = Promise.promise();
            getDirFileList(shareKey, subDirKey, subDirName, subPromise);
            futures.add(subPromise.future());
        }
        
        // 等待所有子目录处理完成
        Future.all(futures).onSuccess(compositeFuture -> {
            for (int i = 0; i < futures.size(); i++) {
                List<FileInfo> subFiles = futures.get(i).result();
                if (subFiles != null) {
                    collectedFiles.addAll(subFiles);
                }
            }
            finalPromise.complete(collectedFiles);
        }).onFailure(err -> {
            log.error("收集子目录文件失败", err);
            finalPromise.fail(err);
        });
    }
    
    /**
     * 将JsonArray转换为FileInfo列表
     */
    private List<FileInfo> convertToFileInfoList(JsonArray fileList, String pdirKey) {
        List<FileInfo> result = new ArrayList<>();
        
        for (int i = 0; i < fileList.size(); i++) {
            JsonObject file = fileList.getJsonObject(i);
            FileInfo fileInfo = new FileInfo();
            
            String fileId = file.getString("file_id");
            String fileName = file.getString("filename", file.getString("file_name"));
            Long fileSize = file.getLong("file_size");
            String filePdirKey = file.getString("pdir_key", pdirKey);
            
            fileInfo.setFileName(fileName);
            fileInfo.setFileId(fileId);
            fileInfo.setSize(fileSize);
            fileInfo.setSizeStr(FileSizeConverter.convertToReadableSize(fileSize));
            
            // 存储用于下载的信息到extParameters
            Map<String, Object> extParams = new java.util.HashMap<>();
            extParams.put("file_id", fileId);
            extParams.put("filename", fileName);
            extParams.put("file_size", fileSize);
            extParams.put("pdir_key", filePdirKey);
            
            fileInfo.setExtParameters(extParams);
            result.add(fileInfo);
        }
        
        return result;
    }
    
    /**
     * 构建返回结果（带下载header）
     */
    private void buildResultWithHeaders(List<FileInfo> fileInfoList, String fileSha) {
        if (fileInfoList.isEmpty()) {
            fail("未获取到文件信息");
            return;
        }
        
        // 构建下载请求头
        Map<String, String> downloadHeaders = new java.util.HashMap<>();
        
        // 设置FTN5K cookie（file_sha的后8位）
        if (fileSha != null && !fileSha.isEmpty()) {
            downloadHeaders.put(HttpHeaders.COOKIE.toString(), "FTN5K=" + fileSha);
        }
        
        // 设置其他必要的header
        downloadHeaders.put(HttpHeaders.USER_AGENT.toString(),
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36 Edg/144.0.0.0");
        downloadHeaders.put(HttpHeaders.REFERER.toString(), "https://share.weiyun.com/");
        
        if (fileInfoList.size() == 1) {
            // 单文件直接返回下载链接（带header）
            FileInfo fileInfo = fileInfoList.get(0);
            String downloadUrl = fileInfo.getFilePath();
            if (downloadUrl != null && !downloadUrl.isEmpty()) {
                completeWithMeta(downloadUrl, downloadHeaders);
            } else {
                fail("未获取到下载链接");
            }
        } else {
            // 多文件返回列表
            JsonArray resultArray = new JsonArray();
            for (FileInfo fileInfo : fileInfoList) {
                String downloadUrl = fileInfo.getFilePath();
                if (downloadUrl != null && !downloadUrl.isEmpty()) {
                    JsonObject fileJson = new JsonObject()
                        .put("fileName", fileInfo.getFileName())
                        .put("size", fileInfo.getSize())
                        .put("sizeStr", fileInfo.getSizeStr())
                        .put("url", downloadUrl);
                    resultArray.add(fileJson);
                }
            }
            promise.complete(resultArray.encode());
        }
    }
}
