package io.vertx.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * 测试Future功能
 */
public class FutureTest {

    @Before
    public void setUp() {
        // 清理之前的任务
        try {
            Vertx.awaitTermination(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @After
    public void tearDown() {
        // 清理测试产生的任务
        try {
            Vertx.awaitTermination(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testFutureComplete() {
        System.out.println("=== 测试Future完成 ===");
        
        Future<String> future = new Future<>();
        assertFalse("Future初始应为未完成状态", future.isComplete());
        assertFalse("Future初始应为非成功状态", future.succeeded());
        assertFalse("Future初始应为非失败状态", future.failed());

        future.complete("test result");
        sleep(50);

        assertTrue("Future应为完成状态", future.isComplete());
        assertTrue("Future应为成功状态", future.succeeded());
        assertFalse("Future不应为失败状态", future.failed());
        assertEquals("结果应正确", "test result", future.result());
        
        System.out.println("✓ Future完成测试通过");
    }

    @Test
    public void testFutureFail() {
        System.out.println("=== 测试Future失败 ===");
        
        Future<String> future = new Future<>();
        RuntimeException error = new RuntimeException("test error");

        future.fail(error);
        sleep(50);

        assertTrue("Future应为完成状态", future.isComplete());
        assertFalse("Future不应为成功状态", future.succeeded());
        assertTrue("Future应为失败状态", future.failed());
        assertNotNull("cause应不为null", future.cause());
        
        System.out.println("✓ Future失败测试通过");
    }

    @Test
    public void testOnSuccessHandler() {
        System.out.println("=== 测试onSuccess处理器 ===");
        
        AtomicReference<String> result = new AtomicReference<>();
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        Future<String> future = new Future<>();
        future.onSuccess(value -> {
            result.set(value);
            handlerCalled.set(true);
        });

        future.complete("success value");
        sleep(100);

        assertTrue("Success处理器应被调用", handlerCalled.get());
        assertEquals("结果应正确传递", "success value", result.get());
        
        System.out.println("✓ onSuccess处理器测试通过");
    }

    @Test
    public void testOnFailureHandler() {
        System.out.println("=== 测试onFailure处理器 ===");
        
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        Future<String> future = new Future<>();
        future.onFailure(throwable -> {
            error.set(throwable);
            handlerCalled.set(true);
        });

        RuntimeException testError = new RuntimeException("test error");
        future.fail(testError);
        sleep(100);

        assertTrue("Failure处理器应被调用", handlerCalled.get());
        assertEquals("错误应正确传递", testError, error.get());
        
        System.out.println("✓ onFailure处理器测试通过");
    }

    @Test
    public void testOnCompleteHandler() {
        System.out.println("=== 测试onComplete处理器 ===");
        
        AtomicReference<AsyncResult<String>> result = new AtomicReference<>();
        AtomicInteger callCount = new AtomicInteger(0);

        Future<String> future = new Future<>();
        future.onComplete(ar -> {
            result.set(ar);
            callCount.incrementAndGet();
        });

        future.complete("test");
        sleep(100);

        assertEquals("Complete处理器应被调用一次", 1, callCount.get());
        assertNotNull("AsyncResult应不为null", result.get());
        assertTrue("AsyncResult应为成功状态", result.get().succeeded());
        assertEquals("结果应正确", "test", result.get().result());
        
        System.out.println("✓ onComplete处理器测试通过");
    }

    @Test
    public void testChainedHandlers() {
        System.out.println("=== 测试链式处理器 ===");
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger completeCount = new AtomicInteger(0);

        Future<String> future = new Future<>();
        future.onSuccess(v -> successCount.incrementAndGet());
        future.onComplete(ar -> completeCount.incrementAndGet());
        future.onSuccess(v -> successCount.incrementAndGet());

        future.complete("test");
        sleep(100);

        assertEquals("Success处理器应被调用2次", 2, successCount.get());
        assertEquals("Complete处理器应被调用1次", 1, completeCount.get());
        
        System.out.println("✓ 链式处理器测试通过");
    }

    @Test
    public void testMapTransformation() {
        System.out.println("=== 测试map转换 ===");
        
        Future<Integer> future = new Future<>();
        Future<String> mappedFuture = future.map(num -> "Number: " + num);

        AtomicReference<String> result = new AtomicReference<>();
        mappedFuture.onSuccess(result::set);

        future.complete(42);
        sleep(100);

        assertEquals("map转换应正确", "Number: 42", result.get());
        assertTrue("原始Future应完成", future.isComplete());
        assertTrue("转换后的Future应完成", mappedFuture.isComplete());
        
        System.out.println("✓ map转换测试通过");
    }

    @Test
    public void testMapWithError() {
        System.out.println("=== 测试map转换错误处理 ===");
        
        Future<Integer> future = new Future<>();
        Future<String> mappedFuture = future.map(num -> {
            throw new RuntimeException("map error");
        });

        AtomicBoolean failureCalled = new AtomicBoolean(false);
        mappedFuture.onFailure(error -> failureCalled.set(true));

        future.complete(42);
        sleep(100);

        assertTrue("转换错误应触发失败", failureCalled.get());
        assertTrue("转换后的Future应失败", mappedFuture.failed());
        
        System.out.println("✓ map转换错误处理测试通过");
    }

    @Test
    public void testComposeTransformation() {
        System.out.println("=== 测试compose转换 ===");
        
        Future<Integer> future = new Future<>();
        Future<String> composedFuture = future.compose(num -> {
            Future<String> nextFuture = new Future<>();
            // 模拟异步操作
            new Thread(() -> {
                sleep(50);
                nextFuture.complete("Composed: " + num);
            }).start();
            return nextFuture;
        });

        AtomicReference<String> result = new AtomicReference<>();
        composedFuture.onSuccess(result::set);

        future.complete(99);
        sleep(200);

        assertEquals("compose转换应正确", "Composed: 99", result.get());
        assertTrue("组合后的Future应完成", composedFuture.isComplete());
        
        System.out.println("✓ compose转换测试通过");
    }

    @Test
    public void testRecovery() {
        System.out.println("=== 测试recover恢复 ===");
        
        Future<String> future = new Future<>();
        Future<String> recoveredFuture = future.recover(error -> {
            Future<String> recoveryFuture = new Future<>();
            recoveryFuture.complete("Recovered from: " + error.getMessage());
            return recoveryFuture;
        });

        AtomicReference<String> result = new AtomicReference<>();
        recoveredFuture.onSuccess(result::set);

        future.fail(new RuntimeException("original error"));
        sleep(100);

        assertEquals("recover应恢复错误", "Recovered from: original error", result.get());
        assertTrue("恢复后的Future应成功", recoveredFuture.succeeded());
        
        System.out.println("✓ recover恢复测试通过");
    }

    @Test
    public void testOtherwise() {
        System.out.println("=== 测试otherwise处理 ===");
        
        Future<String> future = new Future<>();
        Future<String> otherwiseFuture = future.otherwise(error -> "Default value");

        AtomicReference<String> result = new AtomicReference<>();
        otherwiseFuture.onSuccess(result::set);

        future.fail(new RuntimeException("error"));
        sleep(100);

        assertEquals("otherwise应提供默认值", "Default value", result.get());
        assertTrue("otherwise后的Future应成功", otherwiseFuture.succeeded());
        
        System.out.println("✓ otherwise处理测试通过");
    }

    @Test
    public void testSucceededFuture() {
        System.out.println("=== 测试succeededFuture工厂方法 ===");
        
        Future<String> future = Future.succeededFuture("immediate");
        
        assertTrue("应立即完成", future.isComplete());
        assertTrue("应为成功状态", future.succeeded());
        assertEquals("结果应正确", "immediate", future.result());
        
        System.out.println("✓ succeededFuture工厂方法测试通过");
    }

    @Test
    public void testFailedFuture() {
        System.out.println("=== 测试failedFuture工厂方法 ===");
        
        RuntimeException error = new RuntimeException("immediate error");
        Future<String> future = Future.failedFuture(error);
        
        assertTrue("应立即完成", future.isComplete());
        assertTrue("应为失败状态", future.failed());
        assertEquals("错误应正确", error, future.cause());
        
        System.out.println("✓ failedFuture工厂方法测试通过");
    }

    @Test
    public void testToCompletableFuture() {
        System.out.println("=== 测试toCompletableFuture转换 ===");
        
        Future<String> future = new Future<>();
        java.util.concurrent.CompletableFuture<String> cf = future.toCompletableFuture();

        assertNotNull("CompletableFuture应不为null", cf);
        assertFalse("初始应未完成", cf.isDone());

        future.complete("test");
        sleep(50);

        assertTrue("CompletableFuture应完成", cf.isDone());
        assertEquals("结果应正确", "test", cf.join());
        
        System.out.println("✓ toCompletableFuture转换测试通过");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
