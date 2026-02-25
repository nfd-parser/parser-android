package cn.qaiu.parser.impl;

import cn.qaiu.entity.ShareLinkInfo;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QQ邮箱云盘解析器 (wx.mail.qq.com)
 * 支持通过API直接获取下载链接
 */
public class QQwTool extends QQTool {

    public QQwTool(ShareLinkInfo shareLinkInfo) {
        super(shareLinkInfo);
    }

    @Override
    public Future<String> parse() {
        // 尝试从URL中提取参数，调用API获取下载链接
        String shareUrl = shareLinkInfo.getStandardUrl();
        
        // 从URL中提取 k 和 sid 参数
        Map<String, String> params = extractUrlParams(shareUrl);
        String k = params.get("k");
        String r = generateRandomParam();
        String sid = params.get("sid");
        
        if (k != null) {
            // 调用API获取文件信息
            fetchFileInfoViaApi(k, r, sid);
        } else {
            // 降级到HTML解析
            parseFromHtml();
        }

        return promise.future();
    }

    /**
     * 从URL中提取参数
     */
    private Map<String, String> extractUrlParams(String url) {
        Map<String, String> result = new HashMap<>();
        try {
            QueryStringDecoder decoder = new QueryStringDecoder(url, StandardCharsets.UTF_8);
            Map<String, List<String>> params = decoder.parameters();
            
            if (params.containsKey("k")) {
                result.put("k", params.get("k").get(0));
            }
            if (params.containsKey("sid")) {
                result.put("sid", params.get("sid").get(0));
            }
        } catch (Exception e) {
            log.error("提取URL参数失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 生成随机参数r
     * r参数的特征：
     * - 是一个长数字串（20-30位数字）
     * - 由当前时间戳（毫秒）和随机数组合而成
     * - 格式: [时间戳毫秒数][随机数]
     * 
     * 示例: 16709828195361769486379771
     *      1670982819536 (时间戳12位) + 176948637977 (随机数14位)
     */
    public static String generateRandomParam() {
        // 获取当前时间戳（毫秒），转换为字符串后取最后12位
        long currentTimeMs = System.currentTimeMillis();
        String timestampPart = String.valueOf(currentTimeMs);
        
        // 生成14位随机数字
        SecureRandom random = new SecureRandom();
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            randomPart.append(random.nextInt(10));
        }
        
        // 组合得到最终的r参数
        return timestampPart + randomPart.toString();
    }

    /**
     * 通过API获取文件信息
     */
    private void fetchFileInfoViaApi(String k, String r, String sid) {
        String apiUrl = "https://wx.mail.qq.com/s?k=" + k + "&r=" + r + "&sid=" + (sid != null ? sid : "");
        
        MultiMap headers = getApiHeaders();
        
        client.postAbs(apiUrl)
                .putHeaders(headers)
                .sendBuffer(io.vertx.core.buffer.Buffer.buffer("f=json"))
                .onSuccess(res -> {
                    try {
                        JsonObject response = res.bodyAsJsonObject();
                        JsonObject body = response.getJsonObject("body");
                        
                        if (body != null && body.containsKey("url")) {
                            String url = body.getString("url");
                            String processedUrl = url.replace("\\x26", "&");
                            
                            // 提取文件信息并存储到 ShareLinkInfo 的 otherParam 中
                            extractAndStoreFileInfo(body);
                            
                            log.info("QQw API 获取下载链接成功: " + processedUrl.substring(0, Math.min(60, processedUrl.length())) + "...");
                            promise.complete(processedUrl);
                        } else {
                            log.warn("API 返回中没有找到 url 字段，尝试HTML解析");
                            parseFromHtml();
                        }
                    } catch (Exception e) {
                        log.warn("解析API响应失败: " + e.getMessage() + "，尝试HTML解析");
                        parseFromHtml();
                    }
                })
                .onFailure(err -> {
                    log.warn("API 请求失败: " + err.getMessage() + "，尝试HTML解析");
                    parseFromHtml();
                });
    }

    /**
     * 从API响应的body中提取文件信息并存储
     */
    private void extractAndStoreFileInfo(JsonObject body) {
        try {
            Map<String, Object> fileInfoMap = new HashMap<>();
            
            // 提取文件名
            if (body.containsKey("name")) {
                fileInfoMap.put("fileName", body.getString("name"));
            }
            
            // 提取文件大小
            if (body.containsKey("size")) {
                fileInfoMap.put("fileSize", body.getLong("size"));
            }
            
            // 提取文件ID
            if (body.containsKey("fileid")) {
                fileInfoMap.put("fileId", body.getString("fileid"));
            }
            
            // 提取过期时间
            if (body.containsKey("expired_time")) {
                fileInfoMap.put("expiredTime", body.getLong("expired_time"));
            }
            
            // 提取SHA哈希
            if (body.containsKey("sha")) {
                fileInfoMap.put("sha", body.getString("sha"));
            }
            
            // 提取MD5哈希
            if (body.containsKey("md5")) {
                fileInfoMap.put("md5", body.getString("md5"));
            }
            
            // 提取完整的body对象供后续使用
            fileInfoMap.put("apiBody", body.copy());
            
            // 存储到 ShareLinkInfo 的 otherParam 中
            if (shareLinkInfo.getOtherParam() == null) {
                shareLinkInfo.setOtherParam(new HashMap<>());
            }
            shareLinkInfo.getOtherParam().put("qqwFileInfo", fileInfoMap);
            
            log.debug("QQw 文件信息已提取: fileName=" + fileInfoMap.get("fileName") + 
                    ", fileSize=" + fileInfoMap.get("fileSize") + 
                    ", sha=" + fileInfoMap.get("sha"));
            
        } catch (Exception e) {
            log.warn("提取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * HTML页面解析降级方案
     */
    private void parseFromHtml() {
        client.getAbs(shareLinkInfo.getStandardUrl()).send().onSuccess(res -> {
            String html = res.bodyAsString();
            String url = extractVariables(html).get("url");
            if (url != null) {
                String url302 = url.replace("\\x26", "&");
                promise.complete(url302);
            } else {
                fail("分享链接解析失败, 可能是链接失效");
            }
        }).onFailure(handleFail());
    }

    /**
     * 获取API请求头
     */
    private MultiMap getApiHeaders() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        headers.set("accept", "application/json, text/plain, */*");
        headers.set("accept-language", "zh-CN,zh;q=0.9");
        headers.set("cache-control", "no-cache");
        headers.set("content-type", "application/x-www-form-urlencoded");
        headers.set("dnt", "1");
        headers.set("origin", "https://wx.mail.qq.com");
        headers.set("pragma", "no-cache");
        headers.set("referer", "https://wx.mail.qq.com/");
        headers.set("sec-ch-ua", "\"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"144\", \"Microsoft Edge\";v=\"144\"");
        headers.set("sec-ch-ua-mobile", "?0");
        headers.set("sec-ch-ua-platform", "\"macOS\"");
        headers.set("sec-fetch-dest", "empty");
        headers.set("sec-fetch-mode", "cors");
        headers.set("sec-fetch-site", "same-origin");
        headers.set("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36 Edg/144.0.0.0");
        return headers;
    }

    /**
     * 从HTML中提取变量
     */
    private Map<String, String> extractVariables(String jsCode) {
        Map<String, String> variables = new HashMap<>();

        // 正则表达式匹配 var 变量定义
        String regex = "var\\s+(\\w+)\\s*=\\s*([\"']?)([^\"';\\s]+)\\2\n";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsCode);

        while (matcher.find()) {
            String variableName = matcher.group(1); // 变量名
            String variableValue = matcher.group(3); // 变量值
            variables.put(variableName, variableValue);
        }

        return variables;
    }
}
