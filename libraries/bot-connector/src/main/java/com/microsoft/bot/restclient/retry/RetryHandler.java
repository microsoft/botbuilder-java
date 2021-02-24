// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient.retry;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * An instance of this interceptor placed in the request pipeline handles retriable errors.
 */
public final class RetryHandler implements Interceptor {
    /**
     * Represents the default number of retries.
     */
    private static final int DEFAULT_NUMBER_OF_ATTEMPTS = 3;
    /**
     * Represents the default value that will be used to calculate a random
     * delta in the exponential delay between retries.
     */
    private static final int DEFAULT_BACKOFF_DELTA = 1000 * 10;
    /**
     * Represents the default maximum backoff time.
     */
    private static final int DEFAULT_MAX_BACKOFF = 1000 * 10;
    /**
     * Represents the default minimum backoff time.
     */
    private static final int DEFAULT_MIN_BACKOFF = 1000;

    /**
     * The retry strategy to use.
     */
    private final RetryStrategy retryStrategy;

    /**
     * @return the strategy used by this handler
     */
    public RetryStrategy strategy() {
        return retryStrategy;
    }

    /**
     * Initialized an instance of {@link RetryHandler} class.
     * Sets default retry strategy base on Exponential Backoff.
     */
    public RetryHandler() {
        this.retryStrategy = new ExponentialBackoffRetryStrategy(
                DEFAULT_NUMBER_OF_ATTEMPTS,
                DEFAULT_MIN_BACKOFF,
                DEFAULT_MAX_BACKOFF,
                DEFAULT_BACKOFF_DELTA);
    }

    /**
     * Initialized an instance of {@link RetryHandler} class.
     *
     * @param retryStrategy retry strategy to use.
     */
    public RetryHandler(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // try the request
        Response response = chain.proceed(request);

        int tryCount = 0;
        while (retryStrategy.shouldRetry(tryCount, response)) {
            tryCount++;
            if (response.body() != null) {
                response.body().close();
            }
            // retry the request
            response = chain.proceed(request);
        }

        // otherwise just pass the original response on
        return response;
    }
}
