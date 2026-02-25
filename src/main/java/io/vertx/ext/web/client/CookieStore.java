package io.vertx.ext.web.client;

import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.util.List;
import java.util.Map;

public interface CookieStore {
    void put(io.netty.handler.codec.http.cookie.Cookie cookie);
    List<okhttp3.Cookie> get(String domain);
    Map<String, List<okhttp3.Cookie>> getAll();
}
