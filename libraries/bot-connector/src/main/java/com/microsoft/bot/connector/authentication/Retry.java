// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Will retry a call for a configurable number of times with backoff.
 *
 * @see RetryParams
 */
public final class Retry {
    private Retry() {

    }

    /**
     * Runs a task with retry.
     *
     * @param task                  The task to run.
     * @param retryExceptionHandler Called when an exception happens.
     * @param <TResult>             The type of the result.
     * @return A CompletableFuture that is complete when 'task' returns
     *         successfully.
     * @throws RetryException If the task doesn't complete successfully.
     */
    public static <TResult> CompletableFuture<TResult> run(
        Supplier<CompletableFuture<TResult>> task,
        BiFunction<RuntimeException, Integer, RetryParams> retryExceptionHandler
    ) {
        return runInternal(task, retryExceptionHandler, 1, new ArrayList<>());
    }

    private static <TResult> CompletableFuture<TResult> runInternal(
        Supplier<CompletableFuture<TResult>> task,
        BiFunction<RuntimeException, Integer, RetryParams> retryExceptionHandler,
        final Integer retryCount,
        final List<Throwable> exceptions
    ) {
        AtomicReference<RetryParams> retry = new AtomicReference<>();

        return task.get()
            .exceptionally((t) -> {
                exceptions.add(t);
                retry.set(retryExceptionHandler.apply(new RetryException(t), retryCount));
                return null;
            })
            .thenCompose(taskResult -> {
                CompletableFuture<TResult> result = new CompletableFuture<>();

                if (retry.get() == null) {
                    result.complete(taskResult);
                    return result;
                }

                if (retry.get().getShouldRetry()) {
                    try {
                        Thread.sleep(withBackOff(retry.get().getRetryAfter(), retryCount));
                    } catch (InterruptedException e) {
                        throw new RetryException(e);
                    }

                    return runInternal(task, retryExceptionHandler, retryCount + 1, exceptions);
                }

                result.completeExceptionally(new RetryException("Exceeded retry count", exceptions));

                return result;
            });
    }

    private static final double BACKOFF_MULTIPLIER = 1.1;

    private static long withBackOff(long delay, int retryCount) {
        double result = delay * Math.pow(BACKOFF_MULTIPLIER, retryCount - 1);
        return (long) Math.min(result, Long.MAX_VALUE);
    }
}
