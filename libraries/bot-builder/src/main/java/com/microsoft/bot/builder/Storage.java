package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Storage
{
    /**
     * Read StoreItems from storage
     * @param keys keys of the storeItems to read
     * @return StoreItem dictionary
     */
    CompletableFuture<Map<String, ? extends Object>> Read(String... keys) throws JsonProcessingException;

    /**
     * Write StoreItems to storage
     * @param changes 
     */
    CompletableFuture Write(Map<String, ? extends Object> changes) throws Exception;

    /**
     * Delete StoreItems from storage
     * @param keys keys of the storeItems to delete
     */
    CompletableFuture Delete(String... keys);
}



