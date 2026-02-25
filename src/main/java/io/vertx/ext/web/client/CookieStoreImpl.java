package io.vertx.ext.web.client;

import io.netty.handler.codec.http.cookie.Cookie;
import okhttp3.Cookie.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CookieStoreImpl implements CookieStore {
    private final Map<String, List<okhttp3.Cookie>> store = new ConcurrentHashMap<>();

    @Override
    public void put(Cookie cookie) {
        String domain = cookie.domain();
        if (domain == null || domain.isEmpty()) {
            domain = "localhost";
        }
        
        // Convert io.netty.handler.codec.http.cookie.Cookie to OkHttp Cookie
        Builder builder = new Builder()
                .name(cookie.name())
                .value(cookie.value())
                .domain(domain)
                .path(cookie.path() != null && !cookie.path().isEmpty() ? cookie.path() : "/");
        
        if (cookie.isSecure()) {
            builder.secure();
        }
        if (cookie.isHttpOnly()) {
            builder.httpOnly();
        }
        
        okhttp3.Cookie okHttpCookie = builder.build();
        store.computeIfAbsent(domain, k -> new ArrayList<>()).add(okHttpCookie);
    }

    @Override
    public List<okhttp3.Cookie> get(String domain) {
        return store.getOrDefault(domain, new ArrayList<>());
    }

    @Override
    public Map<String, List<okhttp3.Cookie>> getAll() {
        return new ConcurrentHashMap<>(store);
    }
}
