package io.netty.handler.codec.http.cookie;

public class DefaultCookie extends Cookie {

    public DefaultCookie(String name, String value) {
        super(name, value);
    }

    @Override
    public DefaultCookie setDomain(String domain) {
        super.setDomain(domain);
        return this;
    }

    @Override
    public DefaultCookie setPath(String path) {
        super.setPath(path);
        return this;
    }

    @Override
    public DefaultCookie setSecure(boolean secure) {
        super.setSecure(secure);
        return this;
    }

    @Override
    public DefaultCookie setHttpOnly(boolean httpOnly) {
        super.setHttpOnly(httpOnly);
        return this;
    }
}