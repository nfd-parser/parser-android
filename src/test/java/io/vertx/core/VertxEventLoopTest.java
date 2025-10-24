package io.vertx.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 测试Vertx事件循环机制
 */
public class VertxEventLoopTest {

    @Before
    public void setUp() {
        // 确保每个测试开始时任务计数为0
        while (Vertx.getPendingTaskCount() > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @After
    public void tearDown() {
        // 清理，确保没有遗留任务
        try {
            Vertx.awaitTermination(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testSingleTaskRegistration() {
        System.out.println("=== 测试单个任务注册 ===");
        
        int initialCount = Vertx.getPendingTaskCount();
        assertEquals("初始任务数应为0", 0, initialCount);

        Future<String> future = new Future<>();
        int afterCreate = Vertx.getPendingTaskCount();
        assertEquals("创建Future后任务数应为1", 1, afterCreate);

        future.complete("test");
        // 等待一小段时间让任务完成
        sleep(50);
        
        int afterComplete = Vertx.getPendingTaskCount();
        assertEquals("完成Future后任务数应为0", 0, afterComplete);
        
        System.out.println("✓ 单个任务注册测试通过");
    }

    @Test
    public void testMultipleTasksRegistration() {
        System.out.println("=== 测试多个任务注册 ===");
        
        assertEquals("初始任务数应为0", 0, Vertx.getPendingTaskCount());

        Future<String> future1 = new Future<>();
        Future<String> future2 = new Future<>();
        Future<String> future3 = new Future<>();

        assertEquals("创建3个Future后任务数应为3", 3, Vertx.getPendingTaskCount());

        future1.complete("task1");
        sleep(50);
        assertEquals("完成1个任务后剩余2个", 2, Vertx.getPendingTaskCount());

        future2.complete("task2");
        sleep(50);
        assertEquals("完成2个任务后剩余1个", 1, Vertx.getPendingTaskCount());

        future3.complete("task3");
        sleep(50);
        assertEquals("完成3个任务后剩余0个", 0, Vertx.getPendingTaskCount());
        
        System.out.println("✓ 多个任务注册测试通过");
    }

    @Test
    public void testAwaitTerminationSuccess() throws InterruptedException {
        System.out.println("=== 测试等待任务完成（成功） ===");
        
        Future<String> future = new Future<>();
        assertEquals("任务数应为1", 1, Vertx.getPendingTaskCount());

        // 异步完成任务
        new Thread(() -> {
            sleep(200);
            future.complete("done");
        }).start();

        long startTime = System.currentTimeMillis();
        boolean completed = Vertx.awaitTermination(5);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue("任务应该在超时前完成", completed);
        assertTrue("任务应该在200-500ms内完成", elapsedTime >= 200 && elapsedTime < 500);
        assertEquals("完成后任务数应为0", 0, Vertx.getPendingTaskCount());
        
        System.out.println("✓ 等待任务完成测试通过（耗时: " + elapsedTime + "ms）");
    }

    @Test
    public void testAwaitTerminationTimeout() throws InterruptedException {
        System.out.println("=== 测试等待任务超时 ===");
        
        Future<String> future = new Future<>();
        assertEquals("任务数应为1", 1, Vertx.getPendingTaskCount());

        long startTime = System.currentTimeMillis();
        boolean completed = Vertx.awaitTermination(1); // 1秒超时
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertFalse("任务应该超时", completed);
        assertTrue("超时时间应该接近1秒", elapsedTime >= 1000 && elapsedTime < 1200);
        assertEquals("超时后任务数仍为1", 1, Vertx.getPendingTaskCount());

        // 清理：完成任务
        future.complete("cleanup");
        sleep(50);
        
        System.out.println("✓ 等待任务超时测试通过（耗时: " + elapsedTime + "ms）");
    }

    @Test
    public void testAwaitTerminationWithNoTasks() throws InterruptedException {
        System.out.println("=== 测试无任务时立即返回 ===");
        
        assertEquals("初始任务数应为0", 0, Vertx.getPendingTaskCount());

        long startTime = System.currentTimeMillis();
        boolean completed = Vertx.awaitTermination(5);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue("无任务时应立即完成", completed);
        assertTrue("应该在很短时间内返回", elapsedTime < 100);
        
        System.out.println("✓ 无任务时立即返回测试通过（耗时: " + elapsedTime + "ms）");
    }

    @Test
    public void testConcurrentTasks() throws InterruptedException {
        System.out.println("=== 测试并发任务 ===");
        
        int taskCount = 10;
        AtomicInteger completedCount = new AtomicInteger(0);

        // 创建多个并发任务
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            new Thread(() -> {
                Future<String> future = new Future<>();
                sleep(50 + (taskId * 10)); // 错开完成时间
                future.complete("task-" + taskId);
                completedCount.incrementAndGet();
            }).start();
        }

        // 等待所有任务注册
        sleep(100);

        long startTime = System.currentTimeMillis();
        boolean completed = Vertx.awaitTermination(5);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue("所有任务应该完成", completed);
        assertEquals("所有任务都应该被完成", taskCount, completedCount.get());
        assertEquals("完成后任务数应为0", 0, Vertx.getPendingTaskCount());
        
        System.out.println("✓ 并发任务测试通过（" + taskCount + "个任务，耗时: " + elapsedTime + "ms）");
    }

    @Test
    public void testFailedFuture() {
        System.out.println("=== 测试失败的Future ===");
        
        Future<String> future = new Future<>();
        assertEquals("任务数应为1", 1, Vertx.getPendingTaskCount());

        future.fail(new RuntimeException("test error"));
        sleep(50);
        
        assertEquals("失败的Future也应标记为完成", 0, Vertx.getPendingTaskCount());
        
        System.out.println("✓ 失败的Future测试通过");
    }

    @Test
    public void testSucceededFutureFactory() {
        System.out.println("=== 测试succeededFuture工厂方法 ===");
        
        int before = Vertx.getPendingTaskCount();
        Future<String> future = Future.succeededFuture("test");
        sleep(50);
        int after = Vertx.getPendingTaskCount();
        
        assertEquals("succeededFuture应该已完成", before, after);
        assertTrue("Future应该是成功状态", future.succeeded());
        
        System.out.println("✓ succeededFuture工厂方法测试通过");
    }

    @Test
    public void testFailedFutureFactory() {
        System.out.println("=== 测试failedFuture工厂方法 ===");
        
        int before = Vertx.getPendingTaskCount();
        Future<String> future = Future.failedFuture(new RuntimeException("error"));
        sleep(50);
        int after = Vertx.getPendingTaskCount();
        
        assertEquals("failedFuture应该已完成", before, after);
        assertTrue("Future应该是失败状态", future.failed());
        
        System.out.println("✓ failedFuture工厂方法测试通过");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

