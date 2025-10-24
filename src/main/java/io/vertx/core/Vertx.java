package io.vertx.core;

import io.vertx.ext.web.client.WebClient;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class Vertx {
    private static final ConcurrentHashMap<String, WorkerExecutor> workerExecutors = new ConcurrentHashMap<>();
    private static final AtomicInteger pendingTasks = new AtomicInteger(0);
    private static final AtomicInteger instanceCount = new AtomicInteger(0);
    private static Thread eventLoopThread;
    private static CountDownLatch shutdownLatch;
    
    public Vertx() {
        int count = instanceCount.incrementAndGet();
        if (count == 1) {
            // 第一个实例启动事件循环
            startEventLoop();
        }
    }
    
    public static Vertx vertx() {
        return new Vertx();
    }

    /**
     * 启动内部事件循环
     */
    private static synchronized void startEventLoop() {
        if (shutdownLatch == null || shutdownLatch.getCount() == 0) {
            shutdownLatch = new CountDownLatch(1);
            eventLoopThread = new Thread(() -> {
                try {
                    // 事件循环一直运行，直到收到关闭信号
                    shutdownLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "vertx-event-loop");
            
            // 设置为非守护线程，这样JVM不会立即退出
            eventLoopThread.setDaemon(false);
            eventLoopThread.start();
        }
    }

    public WebClient createHttpClient() {
        return WebClient.create();
    }

    public WorkerExecutor createSharedWorkerExecutor(String name) {
        return workerExecutors.computeIfAbsent(name, k -> WorkerExecutor.create());
    }

    /**
     * 注册一个待处理的任务
     */
    public static void registerPendingTask() {
        pendingTasks.incrementAndGet();
    }

    /**
     * 标记一个任务完成
     */
    public static void taskCompleted() {
        pendingTasks.decrementAndGet();
    }

    /**
     * 等待所有异步任务完成（用于测试）
     * @param timeout 超时时间（秒）
     * @return 是否所有任务都完成
     */
    public static boolean awaitTermination(long timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout * 1000;
        while (pendingTasks.get() > 0) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                return false; // 超时
            }
            Thread.sleep(Math.min(remaining, 100));
        }
        return true;
    }

    /**
     * 获取当前待处理任务数
     */
    public static int getPendingTaskCount() {
        return pendingTasks.get();
    }

    /**
     * 检查事件循环是否在运行
     */
    public static boolean isRunning() {
        return shutdownLatch != null && shutdownLatch.getCount() > 0;
    }

    /**
     * 关闭Vertx实例和事件循环
     */
    public void close() {
        int remaining = instanceCount.decrementAndGet();
        if (remaining == 0) {
            // 最后一个实例关闭时，停止事件循环
            synchronized (Vertx.class) {
                // 关闭所有worker executors
                workerExecutors.values().forEach(WorkerExecutor::close);
                workerExecutors.clear();
                
                // 停止事件循环
                if (shutdownLatch != null && shutdownLatch.getCount() > 0) {
                    shutdownLatch.countDown();
                    
                    // 等待事件循环线程结束
                    if (eventLoopThread != null) {
                        try {
                            eventLoopThread.join(5000); // 最多等待5秒
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }
}
