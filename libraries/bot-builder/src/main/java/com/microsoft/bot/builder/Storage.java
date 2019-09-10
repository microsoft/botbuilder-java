// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Storage {
    /**
     * Read StoreItems from storage
     *
     * @param keys keys of the storeItems to read
     * @return StoreItem dictionary
     */
    CompletableFuture<Map<String, Object>> read(String[] keys);

    /**
     * Write StoreItems to storage
     *
     * @param changes
     */
    CompletableFuture<Void> write(Map<String, Object> changes);

    /**
     * Delete StoreItems from storage
     *
     * @param keys keys of the storeItems to delete
     */
    CompletableFuture<Void> delete(String[] keys);
}



