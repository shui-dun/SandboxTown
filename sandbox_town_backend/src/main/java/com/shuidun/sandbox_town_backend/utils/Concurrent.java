package com.shuidun.sandbox_town_backend.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

// 并发工具类
// 未来可能直接使用parallelStream进行替换，parallelStream会自动使用ForkJoinPool
public class Concurrent {

    /** 线程池 */
    private static final ExecutorService executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), // 核心线程数
            Runtime.getRuntime().availableProcessors(), // 最大线程数
            60L, // 空闲线程存活时间
            TimeUnit.SECONDS, // 时间单位
            new LinkedBlockingQueue<>(100) // 阻塞队列
    );

    // 在线程池中执行任务
    public static <T> void executeInThreadPool(Collection<T> items, Consumer<T> consumer) {
        List<CompletableFuture<Void>> futures = items.stream()
                .map(item -> CompletableFuture.runAsync(() -> consumer.accept(item), executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // 在线程池中执行任务并收集结果
    public static <T, R> List<R> executeInThreadPoolWithOutput(Collection<T> items, Function<T, R> function) {
        List<CompletableFuture<R>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> function.apply(item), executor))
                .toList();

        // 等待所有任务完成并收集结果
        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
