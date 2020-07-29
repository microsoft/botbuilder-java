// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Asyc and CompletableFuture helpers methods.
 */
public final class Async {
    private Async() {

    }

    /**
     * Executes a block and throws a completion exception if needed.
     *
     * @param supplier The block to execute.
     * @param <T> The type of the return value.
     * @return The return value.
     */
    public static <T> T tryThrow(ThrowSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (CompletionException ce) {
            throw ce;
        } catch (Throwable t) {
            throw new CompletionException(t);
        }
    }

    /**
     * Executes a block and returns a CompletableFuture with either the return
     * value or the exception (completeExceptionally).
     *
     * @param supplier The block to execute.
     * @param <T> The type of the CompletableFuture value.
     * @return The CompletableFuture
     */
    public static <T> CompletableFuture<T> tryCompletion(ThrowSupplier<T> supplier) {
        CompletableFuture<T> result = new CompletableFuture<>();

        try {
            result.complete(supplier.get());
        } catch (Throwable t) {
            result.completeExceptionally(t);
        }

        return result;
    }
}
