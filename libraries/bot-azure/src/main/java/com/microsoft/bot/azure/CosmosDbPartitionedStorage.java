// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.AccessCondition;
import com.microsoft.azure.documentdb.AccessConditionType;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.PartitionKey;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.StoreItem;
import com.microsoft.bot.connector.ExecutorFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Implements an CosmosDB based storage provider using partitioning for a bot.
 */
public class CosmosDbPartitionedStorage implements Storage {
    private Logger logger = LoggerFactory.getLogger(CosmosDbPartitionedStorage.class);
    private CosmosDbPartitionedStorageOptions cosmosDbStorageOptions;
    private ObjectMapper objectMapper;
    private final Object cacheSync = new Object();
    private DocumentClient client;
    private Database databaseCache;
    private DocumentCollection collectionCache;

    /**
     * Initializes a new instance of the CosmosDbPartitionedStorage class.
     * using the provided CosmosDB credentials, database ID, and container ID.
     *
     * @param withStorageOptions Cosmos DB partitioned storage configuration options.
     */
    public CosmosDbPartitionedStorage(CosmosDbPartitionedStorageOptions withStorageOptions) {
        if (withStorageOptions == null) {
            throw new IllegalArgumentException("cosmosDbPartitionStorageOptions");
        }

        if (StringUtils.isEmpty(withStorageOptions.getCosmosDbEndpoint())) {
            throw new IllegalArgumentException("cosmosDbEndpoint");
        }

        if (StringUtils.isEmpty(withStorageOptions.getAuthKey())) {
            throw new IllegalArgumentException("authKey");
        }

        if (StringUtils.isEmpty(withStorageOptions.getDatabaseId())) {
            throw new IllegalArgumentException("databaseId");
        }

        if (StringUtils.isEmpty(withStorageOptions.getContainerId())) {
            throw new IllegalArgumentException("containerId");
        }

        cosmosDbStorageOptions = withStorageOptions;

        objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules()
            .enableDefaultTyping();

        client = new DocumentClient(
            cosmosDbStorageOptions.getCosmosDbEndpoint(),
            cosmosDbStorageOptions.getAuthKey(),
            cosmosDbStorageOptions.getConnectionPolicy(),
            cosmosDbStorageOptions.getConsistencyLevel());
    }

    /**
     * Reads storage items from storage.
     *
     * @param keys keys of the items to read
     * @return A task that represents the work queued to execute. If the activities
     * are successfully sent, the task result contains the items read, indexed by key.
     */
    @Override
    public CompletableFuture<Map<String, Object>> read(String[] keys) {
        if (keys == null) {
            throw new IllegalArgumentException("keys");
        }

        if (keys.length == 0) {
            // No keys passed in, no result to return.
            return CompletableFuture.completedFuture(new HashMap<>());
        }

        return getCollection()
            .thenApplyAsync(collection -> {
                // issue all of the reads at once
                List<CompletableFuture<Document>> documentFutures = new ArrayList<>();
                for (String key : keys) {
                    documentFutures.add(getDocumentById(CosmosDbKeyEscape.escapeKey(key)));
                }

                // map each returned Document to it's original value.
                Map<String, Object> storeItems = new HashMap<>();
                documentFutures.forEach(documentFuture -> {
                    Document document = documentFuture.join();
                    if (document != null) {
                        try {
                            // we store everything in a DocumentStoreItem.  Get that.
                            JsonNode stateNode = objectMapper.readTree(document.toJson());
                            DocumentStoreItem storeItem = objectMapper.treeToValue(stateNode, DocumentStoreItem.class);

                            // DocumentStoreItem contains the original object.
                            JsonNode dataNode = objectMapper.readTree(storeItem.getDocument());
                            Object item = objectMapper.treeToValue(dataNode, Class.forName(storeItem.getType()));

                            if (item instanceof StoreItem) {
                                ((StoreItem) item).setETag(storeItem.getETag());
                            }
                            storeItems.put(storeItem.getReadId(), item);
                        } catch (IOException | ClassNotFoundException e) {
                            logger.warn("read", e);
                        }
                    }
                });

                return storeItems;
            }, ExecutorFactory.getExecutor());
    }

    /**
     * Writes storage items to storage.
     *
     * @param changes The items to write, indexed by key.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> write(Map<String, Object> changes) {
        if (changes == null) {
            throw new IllegalArgumentException("changes");
        }

        if (changes.size() == 0) {
            return CompletableFuture.completedFuture(null);
        }

        return getCollection()
            .thenApplyAsync(collection -> {
                for (Map.Entry<String, Object> change : changes.entrySet()) {
                    try {
                        ObjectNode node = objectMapper.valueToTree(change.getValue());

                        // Remove etag from JSON object that was copied from StoreItem.
                        // The ETag information is updated as an _etag attribute in the document metadata.
                        node.remove("eTag");

                        DocumentStoreItem storeItem = new DocumentStoreItem() {{
                            setId(CosmosDbKeyEscape.escapeKey(change.getKey()));
                            setReadId(change.getKey());
                            setDocument(node.toString());
                            setType(change.getValue().getClass().getTypeName());
                        }};

                        Document document = new Document(objectMapper.writeValueAsString(storeItem));

                        RequestOptions options = new RequestOptions();
                        options.setPartitionKey(new PartitionKey(storeItem.partitionKey()));

                        if (change.getValue() instanceof StoreItem) {
                            String eTag = ((StoreItem) change.getValue()).getETag();
                            if (!StringUtils.isEmpty(eTag)) {
                                AccessCondition condition = new AccessCondition();
                                condition.setType(AccessConditionType.IfMatch);
                                condition.setCondition(eTag);

                                options.setAccessCondition(condition);
                            } else if (eTag != null) {
                                logger.warn("write change, empty eTag: " + change.getKey());
                                continue;
                            }
                        }

                        client.upsertDocument(
                            collection.getSelfLink(),
                            document,
                            options,
                            true);

                    } catch (JsonProcessingException | DocumentClientException e) {
                        logger.warn("write change: " + change.getKey(), e);
                    }
                }

                return null;
            });
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
            throw new IllegalArgumentException("keys");
        }

        // issue the deletes in parallel
        return getCollection()
            .thenCompose(collection -> Arrays.stream(keys).map(key -> {
                String escapedKey = CosmosDbKeyEscape.escapeKey(key);
                return getDocumentById(escapedKey)
                    .thenApplyAsync(document -> {
                        if (document != null) {
                            try {
                                RequestOptions options = new RequestOptions();
                                options.setPartitionKey(new PartitionKey(escapedKey));

                                client.deleteDocument(document.getSelfLink(), options);
                            } catch (DocumentClientException e) {
                                throw new CompletionException(e);
                            }
                        }

                        return null;
                    }, ExecutorFactory.getExecutor());
            }).collect(CompletableFutures.toFutureList()).thenApply(deleteResponses -> null));
    }

    private Database getDatabase() {
        if (databaseCache == null) {
            // Get the database if it exists
            List<Database> databaseList = client
                .queryDatabases(
                    "SELECT * FROM root r WHERE r.id='" + cosmosDbStorageOptions.getDatabaseId()
                        + "'", null).getQueryIterable().toList();

            if (databaseList.size() > 0) {
                // Cache the database object so we won't have to query for it
                // later to retrieve the selfLink.
                databaseCache = databaseList.get(0);
            } else {
                // Create the database if it doesn't exist.
                try {
                    Database databaseDefinition = new Database();
                    databaseDefinition.setId(cosmosDbStorageOptions.getDatabaseId());

                    databaseCache = client.createDatabase(
                        databaseDefinition, null).getResource();
                } catch (DocumentClientException e) {
                    // able to query or create the collection.
                    // Verify your connection, endpoint, and key.
                    logger.error("getDatabase", e);
                    throw new RuntimeException(e);
                }
            }
        }

        return databaseCache;
    }

    private CompletableFuture<DocumentCollection> getCollection() {
        if (collectionCache != null) {
            return CompletableFuture.completedFuture(collectionCache);
        }

        synchronized (cacheSync) {
            if (collectionCache != null) {
                return CompletableFuture.completedFuture(collectionCache);
            }

            return CompletableFuture.supplyAsync(() -> {
                // Get the collection if it exists.
                List<DocumentCollection> collectionList = client
                    .queryCollections(
                        getDatabase().getSelfLink(),
                        "SELECT * FROM root r WHERE r.id='" + cosmosDbStorageOptions.getContainerId()
                            + "'", null).getQueryIterable().toList();

                if (collectionList.size() > 0) {
                    // Cache the collection object so we won't have to query for it
                    // later to retrieve the selfLink.
                    collectionCache = collectionList.get(0);
                } else {
                    // Create the collection if it doesn't exist.
                    try {
                        DocumentCollection collectionDefinition = new DocumentCollection();
                        collectionDefinition.setId(cosmosDbStorageOptions.getContainerId());

                        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
                        partitionKeyDefinition.setPaths(Collections.singleton(DocumentStoreItem.PARTITION_KEY_PATH));
                        collectionDefinition.setPartitionKey(partitionKeyDefinition);

                        RequestOptions options = new RequestOptions() {{
                            setOfferThroughput(cosmosDbStorageOptions.getContainerThroughput());
                        }};

                        collectionCache = client.createCollection(
                            getDatabase().getSelfLink(),
                            collectionDefinition, options).getResource();
                    } catch (DocumentClientException e) {
                        // able to query or create the collection.
                        // Verify your connection, endpoint, and key.
                        logger.error("getCollection", e);
                        throw new RuntimeException("getCollection", e);
                    }
                }

                return collectionCache;
            }, ExecutorFactory.getExecutor());
        }
    }

    private CompletableFuture<Document> getDocumentById(String id) {
        return getCollection()
            .thenApplyAsync(collection -> {
                // Retrieve the document using the DocumentClient.
                List<Document> documentList = client
                    .queryDocuments(collection.getSelfLink(),
                        "SELECT * FROM root r WHERE r.id='" + id + "'", null)
                    .getQueryIterable().toList();

                if (documentList.size() > 0) {
                    return documentList.get(0);
                } else {
                    return null;
                }
            }, ExecutorFactory.getExecutor());
    }

    /**
     * Internal data structure for storing items in a CosmosDB Collection.
     */
    private static class DocumentStoreItem implements StoreItem {
        public static final String PARTITION_KEY_PATH = "/id";

        @JsonProperty(value = "id")
        private String id;

        @JsonProperty(value = "realId")
        private String readId;

        @JsonProperty(value = "document")
        private String document;

        @JsonProperty(value = "_etag")
        private String eTag;

        @JsonProperty(value = "type")
        private String type;

        /**
         * Gets the sanitized Id/Key used as PrimaryKey.
         * @return The ID.
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the sanitized Id/Key used as PrimaryKey.
         * @param withId The ID.
         */
        public void setId(String withId) {
            id = withId;
        }

        /**
         * Gets the un-sanitized Id/Key.
         * @return The ID.
         */
        public String getReadId() {
            return readId;
        }

        /**
         * Sets the un-sanitized Id/Key.
         * @param withReadId The ID.
         */
        public void setReadId(String withReadId) {
            readId = withReadId;
        }

        /**
         * Gets the persisted object.
         * @return The item data.
         */
        public String getDocument() {
            return document;
        }

        /**
         * Sets the persisted object.
         * @param withDocument The item data.
         */
        public void setDocument(String withDocument) {
            document = withDocument;
        }

        /**
         * Get eTag for concurrency.
         * @return The eTag value.
         */
        @Override
        public String getETag() {
            return eTag;
        }

        /**
         * Set eTag for concurrency.
         * @param withETag The eTag value.
         */
        @Override
        public void setETag(String withETag) {
            eTag = withETag;
        }

        /**
         * The type of the document data.
         * @return The class name of the data being stored.
         */
        public String getType() {
            return type;
        }

        /**
         * The fully qualified class name of the data being stored.
         * @param withType The class name of the data.
         */
        public void setType(String withType) {
            type = withType;
        }

        /**
         * The value used for the PartitionKey.
         * @return In this case, the id field.
         */
        public String partitionKey() {
            return id;
        }
    }
}
