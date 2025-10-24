package io.vertx.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class WorkerExecutor {
    private final Executor executor;

    public WorkerExecutor() {
        this.executor = Executors.newCachedThreadPool();
    }

    public WorkerExecutor(Executor executor) {
        this.executor = executor;
    }

    public static WorkerExecutor create() {
        return new WorkerExecutor();
    }

    public static WorkerExecutor create(Executor executor) {
        return new WorkerExecutor(executor);
    }

    public <T> Future<T> executeBlocking(Supplier<T> blockingCode) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(blockingCode, executor);
        Future<T> vertxFuture = new Future<>();
        
        future.whenComplete((result, error) -> {
            if (error != null) {
                vertxFuture.fail(error);
            } else {
                vertxFuture.complete(result);
            }
        });
        
        return vertxFuture;
    }

    public <T> Future<T> executeBlocking(Supplier<T> blockingCode, boolean ordered) {
        // For Android compatibility, ordered parameter is ignored
        return executeBlocking(blockingCode);
    }

    public void close() {
        if (executor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) executor).shutdown();
        }
    }
}