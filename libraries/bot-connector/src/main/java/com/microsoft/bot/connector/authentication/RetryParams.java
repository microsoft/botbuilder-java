// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import java.time.Duration;

public class RetryParams {
    private static final int MAX_RETRIES = 10;
    private static Duration MAX_DELAY = Duration.ofSeconds(10);
    private static Duration DEFAULT_BACKOFF_TIME = Duration.ofMillis(50);

    private boolean shouldRetry = true;
    private long retryAfter;

    public static RetryParams stopRetrying() {
        return new RetryParams() {{
           setShouldRetry(false);
        }};
    }

    public static RetryParams defaultBackOff(int retryCount) {
        return retryCount < MAX_RETRIES ? new RetryParams(DEFAULT_BACKOFF_TIME.toMillis()) : stopRetrying();
    }

    public RetryParams() {

    }

    public RetryParams(long retryAfter) {
        if (retryAfter > MAX_DELAY.toMillis()) {
            setRetryAfter(MAX_DELAY.toMillis());
        } else {
            setRetryAfter(retryAfter);
        }
    }

    public boolean getShouldRetry() {
        return shouldRetry;
    }

    public void setShouldRetry(boolean withShouldRetry) {
        this.shouldRetry = withShouldRetry;
    }

    public long getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(long withRetryAfter) {
        this.retryAfter = withRetryAfter;
    }
}
