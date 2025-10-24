package io.vertx.ext.web.client;

import io.vertx.core.MultiMap;
import io.vertx.core.net.ProxyOptions;

public class WebClientOptions {
    private boolean followRedirects = true;
    private int connectTimeout = 60000;
    private int readTimeout = 60000;
    private int writeTimeout = 60000;
    private boolean userAgentEnabled = true;
    private String userAgent = "Parser-Android/1.0";
    private MultiMap defaultHeaders = MultiMap.caseInsensitiveMultiMap();
    private ProxyOptions proxyOptions;

    public WebClientOptions() {}

    public WebClientOptions(WebClientOptions other) {
        this.followRedirects = other.followRedirects;
        this.connectTimeout = other.connectTimeout;
        this.readTimeout = other.readTimeout;
        this.writeTimeout = other.writeTimeout;
        this.userAgentEnabled = other.userAgentEnabled;
        this.userAgent = other.userAgent;
        this.defaultHeaders = new MultiMap();
        this.defaultHeaders.putAll(other.defaultHeaders);
        this.proxyOptions = other.proxyOptions;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public WebClientOptions setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public WebClientOptions setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public WebClientOptions setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public WebClientOptions setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public boolean isUserAgentEnabled() {
        return userAgentEnabled;
    }

    public WebClientOptions setUserAgentEnabled(boolean userAgentEnabled) {
        this.userAgentEnabled = userAgentEnabled;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public WebClientOptions setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public MultiMap getDefaultHeaders() {
        return defaultHeaders;
    }

    public WebClientOptions setDefaultHeaders(MultiMap defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    public WebClientOptions setProxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }
}