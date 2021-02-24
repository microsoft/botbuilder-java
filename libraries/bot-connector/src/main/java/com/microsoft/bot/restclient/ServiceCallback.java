// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient;

/**
 * The callback used for client side asynchronous operations.
 *
 * @param <T> the type of the response
 */
public interface ServiceCallback<T> {
    /**
     * Override this method to handle REST call failures.
     *
     * @param t the exception thrown from the pipeline.
     */
    void failure(Throwable t);

    /**
     * Override this method to handle successful REST call results.
     *
     * @param result the result object.
     */
    void success(T result);
}
