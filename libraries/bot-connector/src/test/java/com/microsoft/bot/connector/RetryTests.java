// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.Retry;
import com.microsoft.bot.connector.authentication.RetryException;
import com.microsoft.bot.connector.authentication.RetryParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class RetryTests {
    @Test
    public void Retry_NoRetryWhenTaskSucceeds() {
        FaultyClass faultyClass = new FaultyClass();
        faultyClass.exceptionToThrow = null;

        Retry.run(() ->
            faultyClass.faultyTask(),
            ((e, integer) -> faultyClass.exceptionHandler(e, integer)))
            .join();

        Assert.assertNull(faultyClass.exceptionReceived);
        Assert.assertEquals(1, faultyClass.callCount);
    }

    @Test
    public void Retry_RetryThenSucceed() {
        FaultyClass faultyClass = new FaultyClass();
        faultyClass.exceptionToThrow = new IllegalArgumentException();
        faultyClass.triesUntilSuccess = 3;

        Retry.run(() ->
            faultyClass.faultyTask(),
            ((e, integer) -> faultyClass.exceptionHandler(e, integer)))
            .join();

        Assert.assertNotNull(faultyClass.exceptionReceived);
        Assert.assertEquals(3, faultyClass.callCount);
    }

    @Test
    public void Retry_RetryUntilFailure() {
        FaultyClass faultyClass = new FaultyClass();
        faultyClass.exceptionToThrow = new IllegalArgumentException();
        faultyClass.triesUntilSuccess = 12;

        try {
            Retry.run(() ->
                faultyClass.faultyTask(),
                ((e, integer) -> faultyClass.exceptionHandler(e, integer)))
                .join();
            Assert.fail("Should have thrown a RetryException because it exceeded max retry");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof RetryException);
            Assert.assertEquals(RetryParams.MAX_RETRIES, faultyClass.callCount);
            Assert.assertTrue(RetryParams.MAX_RETRIES == ((RetryException) e.getCause()).getExceptions().size());
        }
    }

    private static class FaultyClass {
        RuntimeException exceptionToThrow;
        RuntimeException exceptionReceived;
        int latestRetryCount = 0;
        int callCount = 0;
        int triesUntilSuccess = 0;

        CompletableFuture<String> faultyTask() {
            callCount++;

            if (callCount < triesUntilSuccess && exceptionToThrow != null) {
                CompletableFuture<String> result = new CompletableFuture<>();
                result.completeExceptionally(exceptionToThrow);
                return result;
            }

            return CompletableFuture.completedFuture(null);
        }

        RetryParams exceptionHandler(RuntimeException e, int currentRetryCount) {
            exceptionReceived = e;
            latestRetryCount = currentRetryCount;
            return RetryParams.defaultBackOff(currentRetryCount);
        }
    }
}
