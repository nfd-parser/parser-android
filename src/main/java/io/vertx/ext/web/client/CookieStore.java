package io.vertx.ext.web.client;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import okhttp3.Cookie;

import java.util.List;
import java.util.Map;

public interface CookieStore {
    void put(DefaultCookie cookie);
    List<Cookie> get(String domain);
    Map<String, List<Cookie>> getAll();
}

