// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncHelper {
    private AsyncHelper() {
    }

    /**
     * Creates a CompletableFuture from an RxJava Observable.
     *
     * Because Observables are n+, this results in a List<T> return values.
     *
     * @param observable The Observable to convert.
     * @param <T> The return type of the Observable.
     * @return A CompletableFuture of List<T>.
     */
    public static <T> CompletableFuture<List<T>> completableFutureFromObservable(Observable<T> observable) {
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        observable
            .doOnError(future::completeExceptionally)
            .toList()
            .forEach(future::complete);
        return future;
    }

    /**
     * Creates a CompletableFuture from an Rx Java Observable, enforcing a single
     * result of type T.
     *
     * @param observable The Observable to convert.
     * @param <T> The returns type.
     * @return A CompletableFutre of type T.
     */
    public static <T> CompletableFuture<T> completableSingleFutureFromObservable(Observable<T> observable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        observable
            .doOnError(future::completeExceptionally)
            .single();
        return future;
    }
}
