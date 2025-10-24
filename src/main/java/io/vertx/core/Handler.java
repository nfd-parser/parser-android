package io.vertx.core;

public interface Handler<T> {
    void handle(T event);
}
