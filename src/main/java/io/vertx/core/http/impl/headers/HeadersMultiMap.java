package io.vertx.core.http.impl.headers;

import io.vertx.core.MultiMap;

public class HeadersMultiMap extends MultiMap {
    public static HeadersMultiMap httpHeaders() {
        return new HeadersMultiMap();
    }
}
