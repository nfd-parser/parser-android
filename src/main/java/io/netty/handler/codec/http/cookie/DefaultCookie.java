package io.netty.handler.codec.http.cookie;

public class DefaultCookie {
    private final String name;
    private final String value;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;

    public DefaultCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String domain() {
        return domain;
    }

    public DefaultCookie setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String path() {
        return path;
    }

    public DefaultCookie setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public DefaultCookie setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public DefaultCookie setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }
}