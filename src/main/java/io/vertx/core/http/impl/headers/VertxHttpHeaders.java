package io.vertx.core.http.impl.headers;

import io.vertx.core.MultiMap;

public class VertxHttpHeaders extends MultiMap {
    public static VertxHttpHeaders httpHeaders() {
        return new VertxHttpHeaders();
    }
}