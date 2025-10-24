package io.vertx.core;

public class AsyncResultImpl<T> implements AsyncResult<T> {
    private final T result;
    private final Throwable cause;
    private final boolean succeeded;

    public AsyncResultImpl(T result) {
        this.result = result;
        this.cause = null;
        this.succeeded = true;
    }

    public AsyncResultImpl(Throwable cause) {
        this.result = null;
        this.cause = cause;
        this.succeeded = false;
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public boolean succeeded() {
        return succeeded;
    }

    @Override
    public boolean failed() {
        return !succeeded;
    }
}

