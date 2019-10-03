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
        FaultyClass faultyClass = new FaultyClass() {{
           exceptionToThrow = null;
        }};

        String result = Retry.run(() ->
            faultyClass.faultyTask(),
            ((e, integer) -> faultyClass.exceptionHandler(e, integer)))
            .join();

        Assert.assertNull(faultyClass.exceptionReceived);
        Assert.assertEquals(1, faultyClass.callCount);
    }

    @Test
    public void Retry_RetryThenSucceed() {
        FaultyClass faultyClass = new FaultyClass() {{
            exceptionToThrow = new IllegalArgumentException();
            triesUntilSuccess = 3;
        }};

        String result = Retry.run(() ->
                faultyClass.faultyTask(),
            ((e, integer) -> faultyClass.exceptionHandler(e, integer)))
            .join();

        Assert.assertNotNull(faultyClass.exceptionReceived);
        Assert.assertEquals(3, faultyClass.callCount);
    }

    @Test
    public void Retry_RetryUntilFailure() {
        FaultyClass faultyClass = new FaultyClass() {{
            exceptionToThrow = new IllegalArgumentException();
            triesUntilSuccess = 12;
        }};

        try {
            Retry.run(() ->
                    faultyClass.faultyTask(),
                ((e, integer) -> faultyClass.exceptionHandler(e, integer)))
                .join();
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof RetryException);
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
                throw exceptionToThrow;
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
