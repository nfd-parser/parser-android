package io.netty.handler.codec.http.cookie;

public class ServerCookieEncoder {
    public static String encode(DefaultCookie cookie) {
        return cookie.name() + "=" + cookie.value();
    }
}