package io.netty.handler.codec.http.cookie;

public class Cookie {
    private String name;
    private String value;
    private String domain;
    private String path;
    private long maxAge;
    private boolean secure;
    private boolean httpOnly;

    public Cookie(String name, String value) {
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

    public Cookie setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String path() {
        return path;
    }

    public Cookie setPath(String path) {
        this.path = path;
        return this;
    }

    public long maxAge() {
        return maxAge;
    }

    public Cookie setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public Cookie setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public Cookie setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }
}
