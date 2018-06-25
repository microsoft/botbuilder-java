package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/// <summary>
/// Models IStorage around a dictionary
/// </summary>
public class DictionaryStorage implements Storage {
    private static ObjectMapper objectMapper;

    // TODO: Object needs to be defined
    private final Map<String, Object> _memory;
    private final Object _syncroot = new Object();
    private int _eTag = 0;

    public DictionaryStorage() {
            this(null);
    }
    public DictionaryStorage(Map<String, Object> dictionary ) {
        DictionaryStorage.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndRegisterModules();
        _memory = (dictionary != null) ? dictionary : new HashMap<String, Object>();
    }

    public CompletableFuture Delete(String[] keys) {
        synchronized (_syncroot) {
                for (String key : keys)  {
                        _memory.get(key);
                }
        }
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Map<String, ?>> Read(String[] keys) throws JsonProcessingException {
        HashMap storeItems = new HashMap<String, Object>(keys.length);
        synchronized (_syncroot) {
            for (String key : keys) {
                if (_memory.containsKey(key)) {
                    Object state = _memory.get(key);
                    if (state != null) {
                        storeItems.put(key, DictionaryStorage.objectMapper.writeValueAsString(state));
                    }
                }

            }
        }

        return completedFuture(storeItems);
    }

    @Override
    public CompletableFuture Write(Map<String, ?> changes) throws Exception {
        synchronized (_syncroot) {
            for (Map.Entry change : changes.entrySet()) {
                Object newValue = change.getValue();

                String oldStateETag = null; // default(string);
                if (_memory.containsValue(change.getKey())) {
                    Map oldState = (Map) _memory.get(change.getKey());
                    if (oldState.containsValue("eTag")) {
                        Map.Entry eTagToken = (Map.Entry) oldState.get("eTag");
                        oldStateETag = (String) eTagToken.getValue();
                    }

                }


                Map<String, Object> newState = DictionaryStorage.objectMapper.convertValue(newValue, HashMap.class);

                // Set ETag if applicable
                if (newValue instanceof StoreItem) {
                    StoreItem newStoreItem = (StoreItem) newValue;
                    if(oldStateETag != null && newStoreItem.geteTag() != "*" &&
                        newStoreItem.geteTag() != oldStateETag) {
                        throw new Exception(String.format("Etag conflict.\r\n\r\nOriginal: %s\r\nCurrent: %s",
                                newStoreItem.geteTag(), oldStateETag));
                    }
                    Integer newTag = _eTag++;
                    newState.put("eTag", newTag.toString());
                }

                _memory.put((String)change.getKey(), newState);
            }
        }
        return completedFuture(null);
    }

}

