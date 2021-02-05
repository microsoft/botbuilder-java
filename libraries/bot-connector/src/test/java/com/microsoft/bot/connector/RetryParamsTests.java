// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.RetryParams;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class RetryParamsTests {
    @Test
    public void RetryParams_StopRetryingValidation() {
        RetryParams retryParams = RetryParams.stopRetrying();
        Assert.assertFalse(retryParams.getShouldRetry());
    }

    @Test
    public void RetryParams_DefaultBackOffShouldRetryOnFirstRetry() {
        RetryParams retryParams = RetryParams.defaultBackOff(0);

        Assert.assertTrue(retryParams.getShouldRetry());
        Assert.assertEquals(50, retryParams.getRetryAfter());
    }

    @Test
    public void RetryParams_DefaultBackOffShouldNotRetryAfter5Retries() {
        RetryParams retryParams = RetryParams.defaultBackOff(10);
        Assert.assertFalse(retryParams.getShouldRetry());
    }

    @Test
    public void RetryParams_DelayOutOfBounds() {
        RetryParams retryParams = new RetryParams(Duration.ofSeconds(11).toMillis());
        Assert.assertEquals(Duration.ofSeconds(10).toMillis(), retryParams.getRetryAfter());
    }
}
