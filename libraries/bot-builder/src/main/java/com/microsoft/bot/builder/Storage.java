// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the interface for a storage layer.
 */
public interface Storage {
    /**
     * Reads storage items from storage.
     *
     * @param keys keys of the items to read
     * @return A task that represents the work queued to execute. If the activities
     *         are successfully sent, the task result contains the items read,
     *         indexed by key.
     */
    CompletableFuture<Map<String, Object>> read(String[] keys);

    /**
     * Writes storage items to storage.
     *
     * @param changes The items to write, indexed by key.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> write(Map<String, Object> changes);

    /**
     * Deletes storage items from storage.
     *
     * @param keys keys of the items to delete
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> delete(String[] keys);
}
