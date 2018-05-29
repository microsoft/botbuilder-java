package com.microsoft.bot.builder.core.extensions;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Storage
{
    /// <summary>
    /// Read StoreItems from storage
    /// </summary>
    /// <param name="keys">keys of the storeItems to read</param>
    /// <returns>StoreItem dictionary</returns>
    CompletableFuture<Map<String, ? extends Object>> Read(String... keys);

    /// <summary>
    /// Write StoreItems to storage
    /// </summary>
    /// <param name="changes"></param>
    CompletableFuture Write(Map<String, ? extends Object> changes);

    /// <summary>
    /// Delete StoreItems from storage
    /// </summary>
    /// <param name="keys">keys of the storeItems to delete</param>
    CompletableFuture Delete(String... keys);
}



