package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;


public class StorageExtensions {


    /// <summary>
    /// Storage extension to Read as strong typed StoreItem objects
    /// </summary>
    /// <typeparam name="StoreItemT"></typeparam>
    /// <param name="storage"></param>
    /// <param name="keys"></param>
    /// <returns></returns>
    @SuppressWarnings("unchecked")
    public static <StoreItemT extends Object> CompletableFuture<Map<String, StoreItemT>> Read(Storage storage, String... keys) throws JsonProcessingException {
        Map<String, ?> storeItems = await(storage.Read(keys));
        HashMap<String, StoreItemT> result = new HashMap<String, StoreItemT>();
        for (Map.Entry entry : storeItems.entrySet()) {
            StoreItemT tempVal;
            try {
                tempVal = (StoreItemT) entry.getValue();
            } catch(Exception ex) {
                // Skip - not an instance of StoreItemT (ugly)
                continue;
            }
            result.put((String)entry.getKey(), tempVal);
        }
        return CompletableFuture.completedFuture(result);
    }
}
