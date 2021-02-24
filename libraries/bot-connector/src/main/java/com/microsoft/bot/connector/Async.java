// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import java.util.concurrent.CompletableFuture;

/**
 * Asyc and CompletableFuture helpers methods.
 */
public final class Async {
    private Async() {

    }

    /**
     * Executes a block and returns a CompletableFuture with either the return
     * value or the exception (completeExceptionally).
     *
     * @param supplier The block to execute.
     * @param <T> The type of the CompletableFuture value.
     * @return The CompletableFuture
     */
    public static <T> CompletableFuture<T> wrapBlock(ThrowSupplier<T> supplier) {
        CompletableFuture<T> result = new CompletableFuture<>();

        try {
            result.complete(supplier.get());
        } catch (Throwable t) {
            result.completeExceptionally(t);
        }

        return result;
    }

    /**
     * Executes a block that returns a CompletableFuture, and catches any exceptions in order
     * to properly return a completed exceptionally result.
     *
     * @param supplier The block to execute.
     * @param <T> The type of the CompletableFuture value.
     * @return The CompletableFuture
     */
    public static <T> CompletableFuture<T> tryCompletable(ThrowSupplier<CompletableFuture<T>> supplier) {
        CompletableFuture<T> result = new CompletableFuture<>();

        try {
            return supplier.get();
        } catch (Throwable t) {
            result.completeExceptionally(t);
        }

        return result;
    }

    /**
     * Constructs a CompletableFuture completed exceptionally.
     * @param ex The exception.
     * @param <T> Type of CompletableFuture.
     * @return A CompletableFuture with the exception.
     */
    public static <T> CompletableFuture<T> completeExceptionally(Throwable ex) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(ex);
        return result;
    }
}
