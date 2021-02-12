package com.microsoft.bot.connector;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.Assert;
import org.junit.Test;

public class AsyncTests {
    @Test()
    public void AsyncTryCompletionShouldCompleteExceptionally() {
        CompletableFuture<Void> result = Async.tryCompletable(() -> {
           throw new IllegalArgumentException("test");
        });

        Assert.assertTrue(result.isCompletedExceptionally());
    }

    @Test
    public void AsyncTryCompletionShouldComplete() {
        CompletableFuture<Boolean> result = Async.tryCompletable(() -> CompletableFuture.completedFuture(true));
        Assert.assertTrue(result.join());
    }

    @Test
    public void AsyncWrapBlockShouldCompleteExceptionally() {
        CompletableFuture<Void> result = Async.wrapBlock(() -> {
            throw new IllegalArgumentException("test");
        });

        Assert.assertTrue(result.isCompletedExceptionally());
    }

    @Test
    public void AsyncWrapBlockShouldComplete() {
        CompletableFuture<Boolean> result = Async.wrapBlock(() -> true);
        Assert.assertTrue(result.join());
    }
}
