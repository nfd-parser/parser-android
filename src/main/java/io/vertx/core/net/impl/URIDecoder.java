package io.vertx.core.net.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class URIDecoder {
    public static String decodeURIComponent(String encoded) {
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return encoded;
        }
    }
}
