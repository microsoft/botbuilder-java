// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import com.microsoft.bot.connector.ExecutorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Retry {
    public static <TResult> CompletableFuture<TResult> run(
        Supplier<CompletableFuture<TResult>> task,
        BiFunction<RuntimeException, Integer, RetryParams> retryExceptionHandler) {

        CompletableFuture<TResult> result = new CompletableFuture<>();

        ExecutorFactory.getExecutor().execute(() -> {
            RetryParams retry = RetryParams.stopRetrying();
            List<Throwable> exceptions = new ArrayList<>();
            int currentRetryCount = 0;

            do {
                try {
                    result.complete(task.get().join());
                } catch (Throwable t) {
                    exceptions.add(t);
                    retry = retryExceptionHandler.apply(new RetryException(t), currentRetryCount);
                }

                if (retry.getShouldRetry()) {
                    currentRetryCount++;
                    try {
                        Thread.sleep(withBackoff(retry.getRetryAfter(), currentRetryCount));
                    } catch (InterruptedException e) {
                        throw new RetryException(e);
                    }
                }
            } while (retry.getShouldRetry());

            result.completeExceptionally(new RetryException("Exceeded retry count", exceptions));
        });

        return result;
    }

    private static long withBackoff(long delay, int retryCount) {
        double result = delay * Math.pow(1.1, retryCount - 1);
        return (long) Math.min(result, Long.MAX_VALUE);
    }
}
