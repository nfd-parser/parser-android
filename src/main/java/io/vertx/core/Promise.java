package io.vertx.core;

public class Promise<T> {
    private final Future<T> future = new Future<>();

    public static <T> Promise<T> promise() {
        return new Promise<>();
    }

    public Future<T> future() {
        return future;
    }

    public void complete(T value) {
        future.complete(value);
    }

    public void fail(String msg) {
        fail(new RuntimeException(msg));
    }

    public void fail(Throwable e) {
        future.fail(e);
    }

    // Handler support for compatibility
    public void handle(AsyncResult<T> result) {
        if (result.succeeded()) {
            complete(result.result());
        } else {
            fail(result.cause());
        }
    }
}
