// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorage implements Storage {
    private static final String TYPENAMEFORNONENTITY = "__type_name_";
    private final Object syncroot = new Object();
    private ObjectMapper objectMapper;
    private Map<String, JsonNode> memory;
    private Logger logger = LoggerFactory.getLogger(MemoryStorage.class);
    private int _eTag = 0;

    public MemoryStorage() {
        this(null);
    }

    public MemoryStorage(Map<String, JsonNode> values) {
        objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();
        objectMapper.enableDefaultTyping();

        memory = values != null  ? values : new ConcurrentHashMap<>();
    }

    @Override
    public CompletableFuture<Map<String, Object>> read(String[] keys) {
        if (keys == null) {
            throw new IllegalArgumentException("keys cannot be null");
        }

        Map<String, Object> storeItems = new ConcurrentHashMap<>(keys.length);
        synchronized (this.syncroot) {
            for (String key : keys) {
                if (memory.containsKey(key)) {
                    Object state = memory.get(key);
                    if (state != null) {
                        try {
                            if (!(state instanceof JsonNode))
                                throw new RuntimeException("DictionaryRead failed: entry not JsonNode");

                            JsonNode stateNode = (JsonNode) state;

                            // Check if type info is set for the class
                            if (!(stateNode.hasNonNull(TYPENAMEFORNONENTITY))) {
                                logger.error("Read failed: Type info not present for " + key);
                                throw new RuntimeException(String.format("Read failed: Type info not present for key " + key));
                            }
                            String clsName = stateNode.get(TYPENAMEFORNONENTITY).textValue();

                            // Load the class info
                            Class<?> cls;
                            try {
                                cls = Class.forName(clsName);
                            } catch (ClassNotFoundException e) {
                                logger.error("Read failed: Could not load class {}", clsName);
                                throw new RuntimeException(String.format("Read failed: Could not load class %s", clsName));
                            }

                            // Populate dictionary
                            storeItems.put(key, objectMapper.treeToValue(stateNode, cls));
                        } catch (JsonProcessingException e) {
                            logger.error("Read failed: {}", e.toString());
                            throw new RuntimeException(String.format("Read failed: %s", e.toString()));
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(storeItems);
    }

    @Override
    public CompletableFuture<Void> write(Map<String, Object> changes) {
        synchronized (this.syncroot) {
            for (Map.Entry<String, Object> change : changes.entrySet()) {
                Object newValue = change.getValue();

                String oldStateETag = null;
                if (memory.containsKey(change.getKey())) {
                    JsonNode oldState = memory.get(change.getKey());
                    if (oldState.has("eTag")) {
                        JsonNode eTagToken = oldState.get("eTag");
                        oldStateETag = eTagToken.asText();
                    }
                }

                // Dictionary stores Key:JsonNode (with type information held within the JsonNode)
                JsonNode newState = objectMapper.valueToTree(newValue);
                ((ObjectNode) newState).put(TYPENAMEFORNONENTITY, newValue.getClass().getTypeName());

                // Set ETag if applicable
                if (newValue instanceof StoreItem) {
                    StoreItem newStoreItem = (StoreItem) newValue;
                    if (oldStateETag != null
                        && !StringUtils.equals(newStoreItem.getETag(), "*")
                        && !StringUtils.equals(newStoreItem.getETag(), oldStateETag)) {

                        String msg = String.format("Etag conflict. Original: %s, Current: %s",
                            newStoreItem.getETag(), oldStateETag);
                        logger.error(msg);
                        throw new RuntimeException(msg);
                    }
                    Integer newTag = _eTag++;
                    ((ObjectNode) newState).put("eTag", newTag.toString());
                }

                memory.put(change.getKey(), newState);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> delete(String[] keys) {
        if (keys == null) {
            throw new IllegalArgumentException("keys cannot be null");
        }

        synchronized (this.syncroot) {
            for (String key : keys) {
                memory.remove(key);
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
