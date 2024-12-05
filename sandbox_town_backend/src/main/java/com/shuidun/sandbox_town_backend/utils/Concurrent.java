package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.mixin.GameCache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

// 并发工具类
// 未来可能直接使用parallelStream进行替换，parallelStream会自动使用ForkJoinPool
public class Concurrent {
    // 在线程池中执行任务
    public static <T> void executeInThreadPool(Collection<T> items, Consumer<T> consumer) {
        List<CompletableFuture<Void>> futures = items.stream()
                .map(item -> CompletableFuture.runAsync(() -> consumer.accept(item), GameCache.executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // 在线程池中执行任务并收集结果
    public static <T, R> List<R> executeInThreadPoolWithOutput(Collection<T> items, Function<T, R> function) {
        List<CompletableFuture<R>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> function.apply(item), GameCache.executor))
                .toList();

        // 等待所有任务完成并收集结果
        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
