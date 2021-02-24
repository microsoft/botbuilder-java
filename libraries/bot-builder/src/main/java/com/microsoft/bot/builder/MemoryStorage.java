// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.connector.Async;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A storage layer that uses an in-memory dictionary.
 */
public class MemoryStorage implements Storage {
    /**
     * Special field for holding the type information for the top level object being
     * stored.
     */
    private static final String TYPENAMEFORNONENTITY = "__type_name_";

    /**
     * Concurrency sync.
     */
    private final Object syncroot = new Object();

    /**
     * To/From JSON.
     */
    private ObjectMapper objectMapper;

    /**
     * The internal map for storage.
     */
    private Map<String, JsonNode> memory;

    /**
     * The... ummm... logger.
     */
    private Logger logger = LoggerFactory.getLogger(MemoryStorage.class);

    /**
     * eTag counter.
     */
    private int eTag = 0;

    /**
     * Initializes a new instance of the MemoryStorage class.
     */
    public MemoryStorage() {
        this(null);
    }

    /**
     * Initializes a new instance of the MemoryStorage class.
     *
     * @param values A pre-existing dictionary to use; or null to use a new one.
     */
    public MemoryStorage(Map<String, JsonNode> values) {
        objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();
        objectMapper.enableDefaultTyping();

        memory = values != null ? values : new ConcurrentHashMap<>();
    }

    /**
     * Reads storage items from storage.
     *
     * @param keys keys of the items to read
     * @return A task that represents the work queued to execute. If the activities
     *         are successfully sent, the task result contains the items read,
     *         indexed by key.
     */
    @Override
    public CompletableFuture<Map<String, Object>> read(String[] keys) {
        if (keys == null) {
            return Async.completeExceptionally(new IllegalArgumentException("keys cannot be null"));
        }

        Map<String, Object> storeItems = new ConcurrentHashMap<>(keys.length);
        synchronized (this.syncroot) {
            for (String key : keys) {
                if (memory.containsKey(key)) {
                    JsonNode stateNode = memory.get(key);
                    if (stateNode != null) {
                        try {
                            // Check if type info is set for the class
                            if (!(stateNode.hasNonNull(TYPENAMEFORNONENTITY))) {
                                logger.error("Read failed: Type info not present for " + key);
                                return Async.completeExceptionally(new RuntimeException(
                                    String
                                        .format("Read failed: Type info not present for key " + key)
                                ));
                            }
                            String clsName = stateNode.get(TYPENAMEFORNONENTITY).textValue();

                            // Load the class info
                            Class<?> cls;
                            try {
                                cls = Class.forName(clsName);
                            } catch (ClassNotFoundException e) {
                                logger.error("Read failed: Could not load class {}", clsName);
                                return Async.completeExceptionally(new RuntimeException(
                                    String.format("Read failed: Could not load class %s", clsName)
                                ));
                            }

                            // Populate dictionary
                            storeItems.put(key, objectMapper.treeToValue(stateNode, cls));
                        } catch (JsonProcessingException e) {
                            logger.error("Read failed: {}", e.toString());
                            return Async.completeExceptionally(new RuntimeException(
                                String.format("Read failed: %s", e.toString())
                            ));
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(storeItems);
    }

    /**
     * Writes storage items to storage.
     *
     * @param changes The items to write, indexed by key.
     * @return A task that represents the work queued to execute.
     */
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

                // Dictionary stores Key:JsonNode (with type information held within the
                // JsonNode)
                JsonNode newState = objectMapper.valueToTree(newValue);
                ((ObjectNode) newState)
                    .put(TYPENAMEFORNONENTITY, newValue.getClass().getTypeName());

                // Set ETag if applicable
                if (newValue instanceof StoreItem) {
                    StoreItem newStoreItem = (StoreItem) newValue;
                    if (
                        oldStateETag != null && !StringUtils.equals(newStoreItem.getETag(), "*")
                            && !StringUtils.equals(newStoreItem.getETag(), oldStateETag)
                    ) {
                        String msg = String.format(
                            "eTag conflict. Original: %s, Current: %s", newStoreItem.getETag(),
                            oldStateETag
                        );
                        logger.error(msg);
                        return Async.completeExceptionally(new RuntimeException(msg));
                    }
                    int newTag = eTag++;
                    ((ObjectNode) newState).put("eTag", Integer.toString(newTag));
                }

                memory.put(change.getKey(), newState);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Deletes storage items from storage.
     *
     * @param keys keys of the items to delete
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> delete(String[] keys) {
        if (keys == null) {
            return Async.completeExceptionally(new IllegalArgumentException("keys cannot be null"));
        }

        synchronized (this.syncroot) {
            for (String key : keys) {
                memory.remove(key);
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
