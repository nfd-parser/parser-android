package io.vertx.core;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicBoolean;

public class Future<T> {
    private final CompletableFuture<T> delegate = new CompletableFuture<>();
    private final AtomicBoolean taskRegistered = new AtomicBoolean(false);
    private final AtomicBoolean taskCompleted = new AtomicBoolean(false);

    public Future() {
        // 注册待处理任务
        registerTask();
    }

    private void registerTask() {
        if (taskRegistered.compareAndSet(false, true)) {
            Vertx.registerPendingTask();
        }
    }

    private void markCompleted() {
        if (taskCompleted.compareAndSet(false, true)) {
            Vertx.taskCompleted();
        }
    }

    public static <T> Future<T> succeededFuture(T value) {
        Future<T> f = new Future<>();
        f.complete(value);
        return f;
    }

    public static Future<Void> succeededFuture() {
        Future<Void> f = new Future<>();
        f.complete(null);
        return f;
    }

    public static <T> Future<T> failedFuture(Throwable e) {
        Future<T> f = new Future<>();
        f.fail(e);
        return f;
    }

    public static <T> Future<T> failedFuture(String message) {
        Future<T> f = new Future<>();
        f.fail(new RuntimeException(message));
        return f;
    }

    @SuppressWarnings("rawtypes")
    public static Future<Void> all(java.util.List<? extends Future<?>> futures) {
        Future<Void> combined = new Future<>();
        if (futures == null || futures.isEmpty()) {
            combined.complete(null);
            return combined;
        }
        java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(futures.size());
        java.util.concurrent.atomic.AtomicBoolean failed = new java.util.concurrent.atomic.AtomicBoolean(false);
        for (Future<?> f : futures) {
            f.onComplete(result -> {
                if (result.failed() && failed.compareAndSet(false, true)) {
                    combined.fail(result.cause());
                } else if (!failed.get() && remaining.decrementAndGet() == 0) {
                    combined.complete(null);
                }
            });
        }
        return combined;
    }

    public void complete(T value) {
        delegate.complete(value);
        markCompleted();
    }

    public void fail(Throwable e) {
        delegate.completeExceptionally(e);
        markCompleted();
    }

    public Future<T> onSuccess(Handler<T> handler) {
        delegate.thenAccept(result -> {
            try {
                // Type erasure compatibility: HttpResponse<String> and HttpResponse<Buffer> are the same at runtime
                handler.handle(result);
            } catch (Throwable e) {
                // If handler throws exception, CompletableFuture will automatically fail the future
                // This catch ensures the exception is logged and doesn't propagate to the thread pool
                System.err.println("WARNING: Success handler threw exception: " + e.getMessage());
                e.printStackTrace();
                // Re-throw to let CompletableFuture handle it
                throw new RuntimeException(e);
            }
        });
        return this;
    }
    
    // Accept any Handler type for HttpResponse compatibility (relies on type erasure)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Future<T> onSuccessUnchecked(Handler handler) {
        delegate.thenAccept(result -> {
            try {
                // Due to type erasure, HttpResponse<Buffer> and HttpResponse<String> are identical at runtime
                handler.handle(result);
            } catch (Throwable e) {
                // If handler throws exception, CompletableFuture will automatically fail the future
                // This catch ensures the exception is logged and doesn't propagate to the thread pool
                System.err.println("WARNING: Success handler threw exception: " + e.getMessage());
                e.printStackTrace();
                // Re-throw to let CompletableFuture handle it
                throw new RuntimeException(e);
            }
        });
        return this;
    }

    public Future<T> onFailure(Handler<Throwable> handler) {
        delegate.exceptionally(e -> {
            try {
                handler.handle(e);
            } catch (Exception ex) {
                // If failure handler also throws, log it but don't fail the future again
                System.err.println("WARNING: Failure handler threw exception: " + ex.getMessage());
                ex.printStackTrace();
            }
            return null;
        });
        return this;
    }

    public void onComplete(Handler<AsyncResult<T>> handler) {
        delegate.whenComplete((result, error) -> {
            try {
                if (error != null) {
                    handler.handle(AsyncResult.failure(error));
                } else {
                    handler.handle(AsyncResult.success(result));
                }
            } catch (Throwable e) {
                // If complete handler throws exception, log it
                // The future is already completed, so we can't fail it again
                System.err.println("WARNING: Complete handler threw exception: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 将Future的完成结果传递给Promise
     * @param promise 要完成的Promise
     */
    public void onComplete(Promise<T> promise) {
        onComplete(promise::handle);
    }

    public <U> Future<U> map(Function<T, U> mapper) {
        Future<U> mappedFuture = new Future<>();
        delegate.whenComplete((result, error) -> {
            if (error != null) {
                mappedFuture.fail(error);
            } else {
                try {
                    U mappedResult = mapper.apply(result);
                    mappedFuture.complete(mappedResult);
                } catch (Exception e) {
                    mappedFuture.fail(e);
                }
            }
        });
        return mappedFuture;
    }

    public <U> Future<U> compose(Function<T, Future<U>> mapper) {
        Future<U> composedFuture = new Future<>();
        delegate.whenComplete((result, error) -> {
            if (error != null) {
                composedFuture.fail(error);
            } else {
                try {
                    Future<U> nextFuture = mapper.apply(result);
                    nextFuture.onComplete(composedFuture::handle);
                } catch (Exception e) {
                    composedFuture.fail(e);
                }
            }
        });
        return composedFuture;
    }

    public Future<T> recover(Function<Throwable, Future<T>> mapper) {
        Future<T> recoveredFuture = new Future<>();
        delegate.whenComplete((result, error) -> {
            if (error != null) {
                try {
                    Future<T> recoveryFuture = mapper.apply(error);
                    recoveryFuture.onComplete(recoveredFuture::handle);
                } catch (Exception e) {
                    recoveredFuture.fail(e);
                }
            } else {
                recoveredFuture.complete(result);
            }
        });
        return recoveredFuture;
    }

    public Future<T> otherwise(Function<Throwable, T> mapper) {
        Future<T> otherwiseFuture = new Future<>();
        delegate.whenComplete((result, error) -> {
            if (error != null) {
                try {
                    T otherwiseResult = mapper.apply(error);
                    otherwiseFuture.complete(otherwiseResult);
                } catch (Exception e) {
                    otherwiseFuture.fail(e);
                }
            } else {
                otherwiseFuture.complete(result);
            }
        });
        return otherwiseFuture;
    }

    public <U> Future<U> transform(Function<AsyncResult<T>, AsyncResult<U>> transformer) {
        Future<U> transformedFuture = new Future<>();
        delegate.whenComplete((result, error) -> {
            AsyncResult<T> asyncResult = error != null ? AsyncResult.failure(error) : AsyncResult.success(result);
            try {
                AsyncResult<U> transformedResult = transformer.apply(asyncResult);
                if (transformedResult.succeeded()) {
                    transformedFuture.complete(transformedResult.result());
                } else {
                    transformedFuture.fail(transformedResult.cause());
                }
            } catch (Exception e) {
                transformedFuture.fail(e);
            }
        });
        return transformedFuture;
    }

    public T result() {
        return delegate.getNow(null);
    }

    public Throwable cause() {
        try {
            delegate.join();
            return null;
        } catch (java.util.concurrent.CompletionException e) {
            // 解包CompletionException，返回原始异常
            return e.getCause() != null ? e.getCause() : e;
        } catch (Exception e) {
            return e;
        }
    }

    public boolean succeeded() {
        return delegate.isDone() && !delegate.isCompletedExceptionally();
    }

    public boolean failed() {
        return delegate.isCompletedExceptionally();
    }

    public boolean isComplete() {
        return delegate.isDone();
    }

    public CompletableFuture<T> toCompletionStage() {
        return delegate;
    }

    public CompletableFuture<T> toCompletableFuture() {
        return delegate;
    }

    // Helper method for handling AsyncResult
    public void handle(AsyncResult<T> result) {
        if (result.succeeded()) {
            complete(result.result());
        } else {
            fail(result.cause());
        }
    }
}
