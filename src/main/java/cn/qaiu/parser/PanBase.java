package cn.qaiu.parser;

import cn.qaiu.WebClientVertxInit;
import cn.qaiu.entity.ShareLinkInfo;
import cn.qaiu.util.HttpResponseHelper;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.WebClientSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 解析器抽象类包含promise, HTTP Client, 默认失败方法等;
 * 新增网盘解析器需要继承该类. <br>
 * <h2>实现类命名规则: </h2>
 * <p>{网盘标识}Tool, 网盘标识不超过5个字符, 可以取网盘名称首字母缩写或拼音首字母, <br>
 * 音乐类型的解析以M开头, 例如网易云音乐Mne</p>
 */
public abstract class PanBase implements IPanTool {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected Promise<String> promise = Promise.promise();

    /**
     * Http client
     */
    protected WebClient client = WebClient.create(WebClientVertxInit.get(),
            new WebClientOptions());

    /**
     * Http client session (会话管理, 带cookie请求)
     */
    protected WebClientSession clientSession = WebClientSession.create(client);

    /**
     * Http client 不自动跳转
     */
    protected WebClient clientNoRedirects = WebClient.create(WebClientVertxInit.get(),
            new WebClientOptions().setFollowRedirects(false));

    protected ShareLinkInfo shareLinkInfo;

    /**
     * 子类重写此构造方法不需要添加额外逻辑
     * 如:
     * <blockquote><pre>
     *  public XxTool(String key, String pwd) {
     *      super(key, pwd);
     *  }
     * </pre></blockquote>
     *
     * @param shareLinkInfo 分享链接信息
     */
    public PanBase(ShareLinkInfo shareLinkInfo) {
        this.shareLinkInfo = shareLinkInfo;
        if (shareLinkInfo.getOtherParam().containsKey("proxy")) {
            JsonObject proxy = (JsonObject) shareLinkInfo.getOtherParam().get("proxy");
            ProxyOptions proxyOptions = new ProxyOptions()
                    .setType(ProxyType.valueOf(proxy.getString("type").toUpperCase()))
                    .setHost(proxy.getString("host"))
                    .setPort(proxy.getInteger("port"));
            if (StringUtils.isNotEmpty(proxy.getString("username"))) {
                proxyOptions.setUsername(proxy.getString("username"));
            }
            if (StringUtils.isNotEmpty(proxy.getString("password"))) {
                proxyOptions.setPassword(proxy.getString("password"));
            }
            this.client = WebClient.create(WebClientVertxInit.get(),
                    new WebClientOptions()
                            .setUserAgentEnabled(false)
                            .setProxyOptions(proxyOptions));

            this.clientSession = WebClientSession.create(client);
            this.clientNoRedirects = WebClient.create(WebClientVertxInit.get(),
                    new WebClientOptions().setFollowRedirects(false)
                            .setUserAgentEnabled(false)
                            .setProxyOptions(proxyOptions));
        }
    }

    protected PanBase() {
    }

    protected String baseMsg() {
        if (shareLinkInfo.getShareUrl() != null) {
            return shareLinkInfo.getPanName() + "-" + shareLinkInfo.getType() + ": url=" + shareLinkInfo.getShareUrl();
        }
        return shareLinkInfo.getPanName() + "-" + shareLinkInfo.getType() + ": key=" + shareLinkInfo.getShareKey() +
                ";pwd=" + shareLinkInfo.getSharePassword();
    }


    /**
     * 失败时生成异常消息
     *
     * @param t        异常实例
     * @param errorMsg 提示消息
     * @param args     log参数变量
     */
    protected void fail(Throwable t, String errorMsg, Object... args) {
        try {
            // 判断是否已经完成
            if (promise.future().isComplete()) {
                log.warn("Promise 已经完成, 无法再次失败: {}, {}", errorMsg, promise.future().cause());
                return;
            }
            String s = String.format(errorMsg.replaceAll("\\{}", "%s"), args);
            log.error("解析异常: " + s, t.fillInStackTrace());
            promise.fail(baseMsg() + ": 解析异常: " + s + " -> " + t);
        } catch (Exception e) {
            log.error("ErrorMsg format fail. The parameter has been discarded", e);
            log.error("解析异常: " + errorMsg, t.fillInStackTrace());
            if (promise.future().isComplete()) {
                log.warn("ErrorMsg format. Promise 已经完成, 无法再次失败: {}", errorMsg);
                return;
            }
            promise.fail(baseMsg() + ": 解析异常: " + errorMsg + " -> " + t);
        }
    }

    /**
     * 失败时生成异常消息
     *
     * @param errorMsg 提示消息
     * @param args     log参数变量
     */
    protected void fail(String errorMsg, Object... args) {
        try {
            // 判断是否已经完成
            if (promise.future().isComplete()) {
                log.warn("Promise 已经完成, 无法再次失败: {}, {}", errorMsg, promise.future().cause());
                return;
            }
            String s = String.format(errorMsg.replaceAll("\\{}", "%s"), args);
            promise.fail(baseMsg() + " - 解析异常: " + s);
        } catch (Exception e) {
            if (promise.future().isComplete()) {
                log.warn("ErrorMsg format. Promise 已经完成, 无法再次失败: {}", errorMsg);
                return;
            }
            log.error("ErrorMsg format fail. The parameter has been discarded", e);
            promise.fail(baseMsg() + " - 解析异常: " + errorMsg);
        }
    }

    protected void fail() {
        fail("");
    }

    /**
     * 生成失败Future的处理器
     *
     * @param errorMsg 提示消息
     * @return Handler
     */
    protected Handler<Throwable> handleFail(String errorMsg) {
        return t -> fail(baseMsg() + " - 请求异常 {}: -> {}", errorMsg, t.fillInStackTrace());
    }

    protected Handler<Throwable> handleFail() {
        return handleFail("");
    }


    /**
     * bodyAsJsonObject的封装, 会自动处理异常
     *
     * @param res HttpResponse
     * @return JsonObject
     */
    protected JsonObject asJson(HttpResponse<?> res) {
        return HttpResponseHelper.asJson(res);
    }

    /**
     * body To text的封装, 会自动处理异常, 会自动解压gzip
     * @param res HttpResponse
     * @return String
     */
    protected String asText(HttpResponse<?> res) {
        return HttpResponseHelper.asText(res);
    }

    protected void complete(String url) {
        // 自动将直链存储到 otherParam 中，以便客户端链接生成器使用
        shareLinkInfo.getOtherParam().put("downloadUrl", url);
        promise.complete(url);
    }

    /**
     * 完成解析并存储下载元数据
     * 
     * @param url 下载直链
     * @param headers 请求头Map
     */
    protected void completeWithMeta(String url, Map<String, String> headers) {
        shareLinkInfo.getOtherParam().put("downloadUrl", url);
        if (headers != null && !headers.isEmpty()) {
            shareLinkInfo.getOtherParam().put("downloadHeaders", headers);
        }
        promise.complete(url);
    }

    /**
     * 完成解析并存储下载元数据（MultiMap版本）
     * 
     * @param url 下载直链
     * @param headers MultiMap格式的请求头
     */
    protected void completeWithMeta(String url, MultiMap headers) {
        Map<String, String> headerMap = new HashMap<>();
        if (headers != null) {
            headers.forEach((key, value) -> headerMap.put(key, value));
        }
        completeWithMeta(url, headerMap);
    }

    protected Future<String> future() {
        return promise.future();
    }

    /**
     * 调用下一个解析器, 通用域名解析
     */
    protected void nextParser() {
        Iterator<PanDomainTemplate> iterator = Arrays.asList(PanDomainTemplate.values()).iterator();
        while (iterator.hasNext()) {
            if (iterator.next().name().equalsIgnoreCase(shareLinkInfo.getType())) {
                if (iterator.hasNext()) {
                    PanDomainTemplate next = iterator.next();
                    log.debug("规则不匹配, 处理解析器转发: {} -> {}", shareLinkInfo.getPanName(), next.getDisplayName());
                    ParserCreate.fromType(next.name())
                            .fromAnyShareUrl(shareLinkInfo.getShareUrl())
                            .createTool()
                            .parse()
                            .onComplete(result -> {
                                if (result.succeeded()) {
                                    promise.complete(result.result());
                                } else {
                                    promise.fail(result.cause());
                                }
                            });
                } else {
                    fail("error: 没有下一个解析处理器");
                }
            }
        }
    }

    protected String getDomainName(){
        return shareLinkInfo.getOtherParam().getOrDefault("domainName", "").toString();
    }

    @Override
    public ShareLinkInfo getShareLinkInfo() {
        return shareLinkInfo;
    }
}
