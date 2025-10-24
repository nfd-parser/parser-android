package io.vertx.core;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * 测试Vertx事件循环生命周期
 */
public class VertxEventLoopLifecycleTest {

    @Test
    public void testEventLoopStartsAutomatically() {
        System.out.println("=== 测试事件循环自动启动 ===");
        
        assertFalse("初始时事件循环应未运行", Vertx.isRunning());
        
        Vertx vertx = Vertx.vertx();
        
        // 等待一小段时间让事件循环启动
        sleep(100);
        
        assertTrue("创建Vertx实例后事件循环应运行", Vertx.isRunning());
        
        // 清理
        vertx.close();
        sleep(100);
        
        assertFalse("关闭后事件循环应停止", Vertx.isRunning());
        
        System.out.println("✓ 事件循环自动启动测试通过");
    }

    @Test
    public void testMultipleVertxInstances() {
        System.out.println("=== 测试多个Vertx实例共享事件循环 ===");
        
        Vertx vertx1 = Vertx.vertx();
        sleep(100);
        assertTrue("第一个实例启动事件循环", Vertx.isRunning());
        
        Vertx vertx2 = Vertx.vertx();
        sleep(100);
        assertTrue("第二个实例时事件循环仍在运行", Vertx.isRunning());
        
        // 关闭第一个实例
        vertx1.close();
        sleep(100);
        assertTrue("关闭第一个实例后，事件循环仍应运行", Vertx.isRunning());
        
        // 关闭第二个实例
        vertx2.close();
        sleep(100);
        assertFalse("关闭所有实例后，事件循环应停止", Vertx.isRunning());
        
        System.out.println("✓ 多个Vertx实例测试通过");
    }

    @Test
    public void testEventLoopKeepsApplicationRunning() {
        System.out.println("=== 测试事件循环保持应用运行 ===");
        
        AtomicBoolean taskCompleted = new AtomicBoolean(false);
        
        Vertx vertx = Vertx.vertx();
        
        // 创建一个延迟完成的Future
        Future<String> future = new Future<>();
        
        // 在后台线程中延迟完成Future
        new Thread(() -> {
            sleep(500);
            future.complete("done");
            taskCompleted.set(true);
        }).start();
        
        future.onSuccess(result -> {
            System.out.println("任务完成: " + result);
        });
        
        // 等待任务完成
        assertTrue("事件循环应在运行", Vertx.isRunning());
        
        try {
            Vertx.awaitTermination(2);
        } catch (InterruptedException e) {
            fail("等待被中断");
        }
        
        assertTrue("任务应该完成", taskCompleted.get());
        
        // 清理
        vertx.close();
        sleep(100);
        
        System.out.println("✓ 事件循环保持应用运行测试通过");
    }

    @Test
    public void testRecreateSameVertxAfterClose() throws InterruptedException {
        System.out.println("=== 测试关闭后可以重新创建Vertx ===");
        
        // 第一次创建和关闭
        Vertx vertx1 = Vertx.vertx();
        sleep(100);
        assertTrue("第一次创建后事件循环应运行", Vertx.isRunning());
        
        vertx1.close();
        sleep(100);
        assertFalse("关闭后事件循环应停止", Vertx.isRunning());
        
        // 第二次创建
        Vertx vertx2 = Vertx.vertx();
        sleep(100);
        assertTrue("第二次创建后事件循环应重新运行", Vertx.isRunning());
        
        // 清理
        vertx2.close();
        sleep(100);
        
        System.out.println("✓ 重新创建Vertx测试通过");
    }

    @Test
    public void testTaskCountTracking() throws InterruptedException {
        System.out.println("=== 测试任务计数追踪 ===");
        
        Vertx vertx = Vertx.vertx();
        
        assertEquals("初始任务数应为0", 0, Vertx.getPendingTaskCount());
        
        Future<String> future1 = new Future<>();
        Future<String> future2 = new Future<>();
        
        sleep(50);
        assertEquals("创建2个Future后任务数应为2", 2, Vertx.getPendingTaskCount());
        
        future1.complete("task1");
        sleep(50);
        assertEquals("完成1个任务后剩余1个", 1, Vertx.getPendingTaskCount());
        
        future2.complete("task2");
        sleep(50);
        assertEquals("完成所有任务后任务数应为0", 0, Vertx.getPendingTaskCount());
        
        // 清理
        vertx.close();
        
        System.out.println("✓ 任务计数追踪测试通过");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

