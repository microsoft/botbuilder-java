package com.microsoft.bot.connector;

import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncHelper {
    public static <T> CompletableFuture<List<T>> completableFutureFromObservable(Observable<T> observable) {
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        observable
            .doOnError(future::completeExceptionally)
            .toList()
            .forEach(future::complete);
        return future;
    }

    public static <T> CompletableFuture<T> completableSingleFutureFromObservable(Observable<T> observable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        observable
            .doOnError(future::completeExceptionally)
            .single();
        return future;
    }
}
