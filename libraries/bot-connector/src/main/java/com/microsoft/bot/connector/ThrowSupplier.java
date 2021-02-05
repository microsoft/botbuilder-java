// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

/**
 * A Supplier that throws.
 * @param <T> The type of the Supplier return value.
 */
@FunctionalInterface
public interface ThrowSupplier<T> {
    /**
     * Gets a result.
     *
     * @return a result
     * @throws Throwable Any exception
     */
    T get() throws Throwable;
}
