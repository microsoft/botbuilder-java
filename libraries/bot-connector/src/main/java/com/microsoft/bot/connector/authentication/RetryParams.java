// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import java.time.Duration;

/**
 * State for Retry.
 */
public class RetryParams {
    public static final int MAX_RETRIES = 10;
    private static final Duration MAX_DELAY = Duration.ofSeconds(10);
    private static final Duration DEFAULT_BACKOFF_TIME = Duration.ofMillis(50);

    private boolean shouldRetry = true;
    private long retryAfter;

    /**
     * Helper to create a RetryParams with a shouldRetry of false.
     * 
     * @return A RetryParams that returns false for {@link #getShouldRetry()}.
     */
    public static RetryParams stopRetrying() {
        RetryParams retryParams = new RetryParams();
        retryParams.setShouldRetry(false);
        return retryParams;
    }

    /**
     * Helper to create a RetryParams with the default backoff time.
     * 
     * @param retryCount The number of times retry has happened.
     * @return A RetryParams object with the proper backoff time.
     */
    public static RetryParams defaultBackOff(int retryCount) {
        return retryCount < MAX_RETRIES
            ? new RetryParams(DEFAULT_BACKOFF_TIME.toMillis())
            : stopRetrying();
    }

    /**
     * Default Retry options.
     */
    public RetryParams() {

    }

    /**
     * RetryParams with the specified delay.
     * 
     * @param withRetryAfter Delay in milliseconds.
     */
    public RetryParams(long withRetryAfter) {
        setRetryAfter(Math.min(withRetryAfter, MAX_DELAY.toMillis()));
    }

    /**
     * Indicates whether a retry should happen.
     * 
     * @return True if a retry should occur.
     */
    public boolean getShouldRetry() {
        return shouldRetry;
    }

    /**
     * Sets whether a retry should happen.
     * 
     * @param withShouldRetry True for a retry.
     */
    public void setShouldRetry(boolean withShouldRetry) {
        this.shouldRetry = withShouldRetry;
    }

    /**
     * Retry delay.
     * 
     * @return Delay in milliseconds.
     */
    public long getRetryAfter() {
        return retryAfter;
    }

    /**
     * Sets the retry delay.
     * 
     * @param withRetryAfter Delay in milliseconds.
     */
    public void setRetryAfter(long withRetryAfter) {
        this.retryAfter = withRetryAfter;
    }
}
