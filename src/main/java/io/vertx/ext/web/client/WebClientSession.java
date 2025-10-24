package io.vertx.ext.web.client;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import okhttp3.Cookie;

import java.util.List;

public class WebClientSession {
    private final WebClient client;
    private final MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    private final CookieStore cookieStore;

    public WebClientSession(WebClient client) {
        this.client = client;
        this.cookieStore = new CookieStoreImpl();
    }

    public WebClientSession(WebClient client, CookieStore cookieStore) {
        this.client = client;
        this.cookieStore = cookieStore;
    }

    public static WebClientSession create(WebClient client) {
        return new WebClientSession(client);
    }

    public static WebClientSession create(WebClient client, CookieStore cookieStore) {
        return new WebClientSession(client, cookieStore);
    }

    public CookieStore cookieStore() {
        return cookieStore;
    }

    public WebClientSession putHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    public WebClientSession putHeaders(MultiMap headers) {
        this.headers.putAll(headers);
        return this;
    }

    public WebClient.RequestBuilder<Buffer> getAbs(String url) {
        WebClient.RequestBuilder<Buffer> builder = client.getAbs(url);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> getAbs(io.vertx.uritemplate.UriTemplate template) {
        WebClient.RequestBuilder<Buffer> builder = client.getAbs(template);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> postAbs(String url) {
        WebClient.RequestBuilder<Buffer> builder = client.postAbs(url);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> postAbs(io.vertx.uritemplate.UriTemplate template) {
        WebClient.RequestBuilder<Buffer> builder = client.postAbs(template);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> putAbs(String url) {
        WebClient.RequestBuilder<Buffer> builder = client.putAbs(url);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> putAbs(io.vertx.uritemplate.UriTemplate template) {
        WebClient.RequestBuilder<Buffer> builder = client.putAbs(template);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> deleteAbs(String url) {
        WebClient.RequestBuilder<Buffer> builder = client.deleteAbs(url);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> deleteAbs(io.vertx.uritemplate.UriTemplate template) {
        WebClient.RequestBuilder<Buffer> builder = client.deleteAbs(template);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> patchAbs(String url) {
        WebClient.RequestBuilder<Buffer> builder = client.patchAbs(url);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> patchAbs(io.vertx.uritemplate.UriTemplate template) {
        WebClient.RequestBuilder<Buffer> builder = client.patchAbs(template);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> headAbs(String url) {
        WebClient.RequestBuilder<Buffer> builder = client.headAbs(url);
        headers.forEach(builder::putHeader);
        return builder;
    }

    public WebClient.RequestBuilder<Buffer> headAbs(io.vertx.uritemplate.UriTemplate template) {
        WebClient.RequestBuilder<Buffer> builder = client.headAbs(template);
        headers.forEach(builder::putHeader);
        return builder;
    }

    // Helper method to add cookies to the session
    public void addCookie(String name, String value, String domain) {
        io.netty.handler.codec.http.cookie.DefaultCookie cookie = 
            new io.netty.handler.codec.http.cookie.DefaultCookie(name, value);
        cookie.setDomain(domain);
        cookieStore.put(cookie);
    }

    // Helper method to get cookies for a domain
    public List<Cookie> getCookies(String domain) {
        return cookieStore.get(domain);
    }
}
