package io.vertx.ext.web.client;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import okhttp3.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CookieStoreImpl implements CookieStore {
    private final Map<String, List<Cookie>> store = new ConcurrentHashMap<>();

    @Override
    public void put(DefaultCookie cookie) {
        String domain = cookie.domain();
        if (domain == null || domain.isEmpty()) {
            domain = "/";
        }
        
        // Convert DefaultCookie to OkHttp Cookie
        Cookie.Builder builder = new Cookie.Builder()
                .name(cookie.name())
                .value(cookie.value())
                .domain(cookie.domain() != null && !cookie.domain().isEmpty() ? cookie.domain() : "localhost")
                .path(cookie.path() != null && !cookie.path().isEmpty() ? cookie.path() : "/");
        
        if (cookie.isSecure()) {
            builder.secure();
        }
        if (cookie.isHttpOnly()) {
            builder.httpOnly();
        }
        
        Cookie okHttpCookie = builder.build();
        store.computeIfAbsent(domain, k -> new ArrayList<>()).add(okHttpCookie);
    }

    @Override
    public List<Cookie> get(String domain) {
        return store.getOrDefault(domain, new ArrayList<>());
    }

    @Override
    public Map<String, List<Cookie>> getAll() {
        return new ConcurrentHashMap<>(store);
    }
}

