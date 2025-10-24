package io.vertx.core.net;

public class ProxyOptions {
    private String host;
    private int port;
    private String username;
    private String password;
    private ProxyType type = ProxyType.HTTP;

    public ProxyOptions() {}

    public ProxyOptions(ProxyOptions other) {
        this.host = other.host;
        this.port = other.port;
        this.username = other.username;
        this.password = other.password;
        this.type = other.type;
    }

    public String getHost() {
        return host;
    }

    public ProxyOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ProxyOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ProxyOptions setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ProxyOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    public ProxyType getType() {
        return type;
    }

    public ProxyOptions setType(ProxyType type) {
        this.type = type;
        return this;
    }
}