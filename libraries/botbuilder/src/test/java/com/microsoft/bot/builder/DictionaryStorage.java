package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.schema.models.Entity;

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
    private final Map<String, Object> memory;
    private final Object syncroot = new Object();
    private int _eTag = 0;
    private final String typeNameForNonEntity = "__type_name_";

    public DictionaryStorage() {
            this(null);
    }
    public DictionaryStorage(Map<String, Object> dictionary ) {
        DictionaryStorage.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndRegisterModules();
        this.memory = (dictionary != null) ? dictionary : new HashMap<String, Object>();
    }

    public CompletableFuture Delete(String[] keys) {
        synchronized (this.syncroot) {
                for (String key : keys)  {
                        Object o = this.memory.get(key);
                        this.memory.remove(o);
                }
        }
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Map<String, ?>> Read(String[] keys) throws JsonProcessingException {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> storeItems = new HashMap<String, Object>(keys.length);
            synchronized (this.syncroot) {
                for (String key : keys) {
                    if (this.memory.containsKey(key)) {
                        Object state = this.memory.get(key);
                        if (state != null) {
                            try {
                                if (!(state instanceof JsonNode))
                                    throw new RuntimeException("DictionaryRead failed: entry not JsonNode");
                                JsonNode stateNode = (JsonNode) state;
                                // Check if type info is set for the class
                                if (!(stateNode.hasNonNull(this.typeNameForNonEntity))) {
                                    throw new RuntimeException(String.format("DictionaryRead failed: Type info not present"));
                                }
                                String clsName = stateNode.get(this.typeNameForNonEntity).textValue();

                                // Load the class info
                                Class<?> cls;
                                try {
                                    cls = Class.forName(clsName);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(String.format("DictionaryRead failed: Could not load class %s", clsName));
                                }

                                // Populate dictionary
                                storeItems.put(key,DictionaryStorage.objectMapper.treeToValue(stateNode, cls ));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                                throw new RuntimeException(String.format("DictionaryRead failed: %s", e.toString()));
                            }
                        }
                    }

                }
            }

            return storeItems;
        });
    }

    @Override
    public CompletableFuture Write(Map<String, ?> changes) throws Exception {
        synchronized (this.syncroot) {
            for (Map.Entry change : changes.entrySet()) {
                Object newValue = change.getValue();

                String oldStateETag = null; // default(string);
                if (this.memory.containsValue(change.getKey())) {
                    Map oldState = (Map) this.memory.get(change.getKey());
                    if (oldState.containsValue("eTag")) {
                        Map.Entry eTagToken = (Map.Entry) oldState.get("eTag");
                        oldStateETag = (String) eTagToken.getValue();
                    }

                }
                // Dictionary stores Key:JsonNode (with type information held within the JsonNode)
                JsonNode newState = DictionaryStorage.objectMapper.valueToTree(newValue);
                ((ObjectNode)newState).put(this.typeNameForNonEntity, newValue.getClass().getTypeName());

                // Set ETag if applicable
                if (newValue instanceof StoreItem) {
                    StoreItem newStoreItem = (StoreItem) newValue;
                    if(oldStateETag != null && newStoreItem.geteTag() != "*" &&
                        newStoreItem.geteTag() != oldStateETag) {
                        throw new Exception(String.format("Etag conflict.\r\n\r\nOriginal: %s\r\nCurrent: %s",
                                newStoreItem.geteTag(), oldStateETag));
                    }
                    Integer newTag = _eTag++;
                    ((ObjectNode)newState).put("eTag", newTag.toString());
                }

                this.memory.put((String)change.getKey(), newState);
            }
        }
        return completedFuture(null);
    }

}

