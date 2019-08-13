package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class StorageExtensions {


    /**
     * Storage extension to Read as strong typed StoreItem objects
     *
     * @param StoreItemT
     * @param storage
     * @param keys
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <StoreItemT extends Object> CompletableFuture<Map<String, StoreItemT>> Read(Storage storage, String... keys) throws JsonProcessingException {
        Map<String, ?> storeItems = storage.Read(keys).join();
        HashMap<String, StoreItemT> result = new HashMap<String, StoreItemT>();
        for (Map.Entry entry : storeItems.entrySet()) {
            StoreItemT tempVal;
            try {
                tempVal = (StoreItemT) entry.getValue();
            } catch (Exception ex) {
                // Skip - not an instance of StoreItemT (ugly)
                continue;
            }
            result.put((String) entry.getKey(), tempVal);
        }
        return CompletableFuture.completedFuture(result);
    }
}
