package com.devil.fission.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 异步工具类.
 *
 * @author Devil
 * @date Created in 2023/3/3 10:24
 */
public class Async {
    
    private static ExecutorService executor;
    
    static {
        create();
    }
    
    private Async() {
        throw new UnsupportedOperationException();
    }
    
    public static CompletableFuture<Void> run(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }
    
    public static List<CompletableFuture<Void>> run(Runnable... task) {
        List<CompletableFuture<Void>> list = new ArrayList<>();
        
        for (Runnable runnable : task) {
            list.add(CompletableFuture.runAsync(runnable, executor));
        }
        
        return list;
    }
    
    public static CompletableFuture<Object> anyOf(Runnable... task) {
        return CompletableFuture.anyOf(Arrays.stream(task).map(Async::run).toArray(CompletableFuture[]::new));
    }
    
    public static CompletableFuture<Void> allOf(Runnable... task) {
        return CompletableFuture.allOf(Arrays.stream(task).map(Async::run).toArray(CompletableFuture[]::new));
    }
    
    public static CompletableFuture<Void> combine(Runnable... task) {
        CompletableFuture<Void> run = run(task[0]);
        
        for (int i = 1; i < task.length; ++i) {
            run = run.thenCombineAsync(run(task[i]), (void1, void2) -> null, executor);
        }
        
        return run;
    }
    
    public static <U> CompletableFuture<U> supply(Supplier<U> task) {
        return CompletableFuture.supplyAsync(task, executor);
    }
    
    public static CompletableFuture<Object> anyOf(CompletableFuture<?>... tasks) {
        return CompletableFuture.anyOf(tasks);
    }
    
    public static CompletableFuture<Void> allOf(CompletableFuture<?>... tasks) {
        return CompletableFuture.allOf(tasks);
    }
    
    public static <U1, U2, V> CompletableFuture<? extends V> supplyCombine(Supplier<U1> task1, Supplier<U2> task2,
            BiFunction<? super U1, ? super U2, ? extends V> returnFn) {
        return supply(task1).thenCombineAsync(supply(task2), returnFn);
    }
    
    public static <U1, U2> CompletableFuture<U2> supplyComposeAsync(Supplier<U1> task1, Function<? super U1, ? extends CompletionStage<U2>> task2) {
        return supply(task1).thenComposeAsync(task2, executor);
    }
    
    public static boolean isSuccessFuture(CompletableFuture<?> future) {
        return future.isDone() && !future.isCompletedExceptionally() && !future.isCancelled();
    }
    
    public static boolean joinAndCheck(CompletableFuture<Void> future) {
        future.join();
        return future.isDone() && !future.isCompletedExceptionally() && !future.isCancelled();
    }
    
    public static void create() {
        int max = Runtime.getRuntime().availableProcessors();
        if (max <= 4) {
            max = 4;
        }
        
        if (max > 12) {
            max = 12;
        }
        
        executor = new ThreadPoolExecutor(4, max, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(256), new UserThreadFactory("machine-"));
    }
    
    public static void shutdown() {
        executor.shutdown();
    }
    
    static class UserThreadFactory implements ThreadFactory {
        
        private final String namePrefix;
        
        private final AtomicInteger nextId = new AtomicInteger(1);
        
        UserThreadFactory(String whatFeatureOfGroup) {
            namePrefix = whatFeatureOfGroup + "-async-";
        }
        
        @Override
        public Thread newThread(Runnable task) {
            String name = namePrefix + nextId.getAndIncrement();
            return new Thread(null, task, name, 0);
        }
        
    }
    
}
