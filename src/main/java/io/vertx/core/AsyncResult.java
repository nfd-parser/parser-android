package io.vertx.core;

public interface AsyncResult<T> {
    T result();
    Throwable cause();
    boolean succeeded();
    boolean failed();

    static <T> AsyncResult<T> success(T result) {
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return result;
            }

            @Override
            public Throwable cause() {
                return null;
            }

            @Override
            public boolean succeeded() {
                return true;
            }

            @Override
            public boolean failed() {
                return false;
            }
        };
    }

    static <T> AsyncResult<T> failure(Throwable cause) {
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return null;
            }

            @Override
            public Throwable cause() {
                return cause;
            }

            @Override
            public boolean succeeded() {
                return false;
            }

            @Override
            public boolean failed() {
                return true;
            }
        };
    }
}
