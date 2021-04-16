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
     * Initializes a new instance of the CosmosDbPartitionedStorage class. using the
     * provided CosmosDB credentials, database ID, and container ID.
     *
     * @param withCosmosDbStorageOptions Cosmos DB partitioned storage configuration
     *                                   options.
     */
    public CosmosDbPartitionedStorage(CosmosDbPartitionedStorageOptions withCosmosDbStorageOptions) {
        if (withCosmosDbStorageOptions == null) {
            throw new IllegalArgumentException("CosmosDbPartitionStorageOptions is required.");
        }

        if (withCosmosDbStorageOptions.getCosmosDbEndpoint() == null) {
            throw new IllegalArgumentException("Service EndPoint for CosmosDB is required: cosmosDbEndpoint");
        }

        if (StringUtils.isBlank(withCosmosDbStorageOptions.getAuthKey())) {
            throw new IllegalArgumentException("AuthKey for CosmosDB is required: authKey");
        }

        if (StringUtils.isBlank(withCosmosDbStorageOptions.getDatabaseId())) {
            throw new IllegalArgumentException("DatabaseId is required: databaseId");
        }

        if (StringUtils.isBlank(withCosmosDbStorageOptions.getContainerId())) {
            throw new IllegalArgumentException("ContainerId is required: containerId");
        }

        Boolean compatibilityMode = withCosmosDbStorageOptions.getCompatibilityMode();
        if (compatibilityMode == null) {
            withCosmosDbStorageOptions.setCompatibilityMode(true);
        }

        if (StringUtils.isNotBlank(withCosmosDbStorageOptions.getKeySuffix())) {
            if (withCosmosDbStorageOptions.getCompatibilityMode()) {
                throw new IllegalArgumentException(
                    "CompatibilityMode cannot be 'true' while using a KeySuffix: withCosmosDbStorageOptions"
                );
            }

            // In order to reduce key complexity, we do not allow invalid characters in a
            // KeySuffix
            // If the KeySuffix has invalid characters, the EscapeKey will not match
            String suffixEscaped = CosmosDbKeyEscape.escapeKey(withCosmosDbStorageOptions.getKeySuffix());
            if (!withCosmosDbStorageOptions.getKeySuffix().equals(suffixEscaped)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Cannot use invalid Row Key characters: %s %s",
                        withCosmosDbStorageOptions.getKeySuffix(),
                        "withCosmosDbStorageOptions"
                    )
                );
            }
        }

        cosmosDbStorageOptions = withCosmosDbStorageOptions;

        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules()
            .enableDefaultTyping();

        client = new DocumentClient(
            cosmosDbStorageOptions.getCosmosDbEndpoint(),
            cosmosDbStorageOptions.getAuthKey(),
            cosmosDbStorageOptions.getConnectionPolicy(),
            cosmosDbStorageOptions.getConsistencyLevel()
        );
    }

    /**
     * Reads storage items from storage.
     *
     * @param keys A collection of Ids for each item to be retrieved.
     * @return A dictionary containing the retrieved items.
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

        return getCollection().thenApply(collection -> {
            // Issue all of the reads at once
            List<CompletableFuture<Document>> documentFutures = new ArrayList<>();
            for (String key : keys) {
                documentFutures.add(
                    getDocumentById(
                        CosmosDbKeyEscape.escapeKey(
                            key,
                            cosmosDbStorageOptions.getKeySuffix(),
                            cosmosDbStorageOptions.getCompatibilityMode()
                        )
                    )
                );
            }

            // Map each returned Document to it's original value.
            Map<String, Object> storeItems = new HashMap<>();
            documentFutures.forEach(documentFuture -> {
                Document document = documentFuture.join();
                if (document != null) {
                    try {
                        // We store everything in a DocumentStoreItem. Get that.
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
                        logger.warn("Error reading from container", e);
                    }
                }
            });

            return storeItems;
        });
    }

    /**
     * Inserts or updates one or more items into the Cosmos DB container.
     *
     * @param changes A dictionary of items to be inserted or updated. The
     *                dictionary item key is used as the ID for the inserted /
     *                updated item.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> write(Map<String, Object> changes) {
        if (changes == null) {
            throw new IllegalArgumentException("changes");
        }

        if (changes.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return getCollection().thenApply(collection -> {
            for (Map.Entry<String, Object> change : changes.entrySet()) {
                try {
                    ObjectNode node = objectMapper.valueToTree(change.getValue());

                    // Remove etag from JSON object that was copied from StoreItem.
                    // The ETag information is updated as an _etag attribute in the document
                    // metadata.
                    node.remove("eTag");

                    DocumentStoreItem documentChange = new DocumentStoreItem();
                    documentChange.setId(
                        CosmosDbKeyEscape.escapeKey(
                            change.getKey(),
                            cosmosDbStorageOptions.getKeySuffix(),
                            cosmosDbStorageOptions.getCompatibilityMode()
                        )
                    );
                    documentChange.setReadId(change.getKey());
                    documentChange.setDocument(node.toString());
                    documentChange.setType(change.getValue().getClass().getTypeName());

                    Document document = new Document(objectMapper.writeValueAsString(documentChange));

                    RequestOptions options = new RequestOptions();
                    options.setPartitionKey(new PartitionKey(documentChange.partitionKey()));

                    if (change.getValue() instanceof StoreItem) {
                        String etag = ((StoreItem) change.getValue()).getETag();
                        if (!StringUtils.isEmpty(etag)) {
                            // if we have an etag, do opt. concurrency replace
                            AccessCondition condition = new AccessCondition();
                            condition.setType(AccessConditionType.IfMatch);
                            condition.setCondition(etag);

                            options.setAccessCondition(condition);
                        } else if (etag != null) {
                            logger.warn("write change, empty eTag: " + change.getKey());
                            continue;
                        }
                    }

                    client.upsertDocument(collection.getSelfLink(), document, options, true);

                } catch (JsonProcessingException | DocumentClientException e) {
                    logger.warn("Error upserting document: " + change.getKey(), e);
                    if (e instanceof DocumentClientException) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }

            return null;
        });
    }

    /**
     * Deletes one or more items from the Cosmos DB container.
     *
     * @param keys An array of Ids for the items to be deleted.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> delete(String[] keys) {
        if (keys == null) {
            throw new IllegalArgumentException("keys");
        }

        // issue the deletes in parallel
        return getCollection().thenCompose(collection -> Arrays.stream(keys).map(key -> {
            String escapedKey = CosmosDbKeyEscape
                .escapeKey(key, cosmosDbStorageOptions.getKeySuffix(), cosmosDbStorageOptions.getCompatibilityMode());
            return getDocumentById(escapedKey).thenApply(document -> {
                if (document != null) {
                    try {
                        RequestOptions options = new RequestOptions();
                        options.setPartitionKey(new PartitionKey(escapedKey));

                        client.deleteDocument(document.getSelfLink(), options);
                    } catch (DocumentClientException e) {
                        logger.warn("Unable to delete document", e);
                        throw new CompletionException(e);
                    }
                }

                return null;
            });
        }).collect(CompletableFutures.toFutureList()).thenApply(deleteResponses -> null));
    }

    private Database getDatabase() {
        if (databaseCache == null) {
            // Get the database if it exists
            List<Database> databaseList = client.queryDatabases(
                "SELECT * FROM root r WHERE r.id='" + cosmosDbStorageOptions.getDatabaseId() + "'",
                null
            ).getQueryIterable().toList();

            if (databaseList.size() > 0) {
                // Cache the database object so we won't have to query for it
                // later to retrieve the selfLink.
                databaseCache = databaseList.get(0);
            } else {
                // Create the database if it doesn't exist.
                try {
                    Database databaseDefinition = new Database();
                    databaseDefinition.setId(cosmosDbStorageOptions.getDatabaseId());

                    databaseCache = client.createDatabase(databaseDefinition, null).getResource();
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

                // Get the collection if it exists.
                List<DocumentCollection> collectionList = client.queryCollections(
                    getDatabase().getSelfLink(),
                    "SELECT * FROM root r WHERE r.id='" + cosmosDbStorageOptions.getContainerId() + "'",
                    null
                ).getQueryIterable().toList();

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

                        RequestOptions options = new RequestOptions();
                        options.setOfferThroughput(cosmosDbStorageOptions.getContainerThroughput());

                        collectionCache = client
                            .createCollection(getDatabase().getSelfLink(), collectionDefinition, options)
                            .getResource();
                    } catch (DocumentClientException e) {
                        // able to query or create the collection.
                        // Verify your connection, endpoint, and key.
                        logger.error("getCollection", e);
                        throw new RuntimeException("getCollection", e);
                    }
                }
                return CompletableFuture.completedFuture(collectionCache);
        }
    }

    private CompletableFuture<Document> getDocumentById(String id) {
        return getCollection().thenApply(collection -> {
            // Retrieve the document using the DocumentClient.
            List<Document> documentList = client
                .queryDocuments(collection.getSelfLink(), "SELECT * FROM root r WHERE r.id='" + id + "'", null)
                .getQueryIterable()
                .toList();

            if (documentList.size() > 0) {
                return documentList.get(0);
            } else {
                return null;
            }
        });
    }

    /**
     * Internal data structure for storing items in a CosmosDB Collection.
     */
    private static class DocumentStoreItem implements StoreItem {
        // PartitionKey path to be used for this document type
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
         *
         * @return The ID.
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the sanitized Id/Key used as PrimaryKey.
         *
         * @param withId The ID.
         */
        public void setId(String withId) {
            id = withId;
        }

        /**
         * Gets the un-sanitized Id/Key.
         *
         * @return The ID.
         */
        public String getReadId() {
            return readId;
        }

        /**
         * Sets the un-sanitized Id/Key.
         *
         * @param withReadId The ID.
         */
        public void setReadId(String withReadId) {
            readId = withReadId;
        }

        /**
         * Gets the persisted object.
         *
         * @return The item data.
         */
        public String getDocument() {
            return document;
        }

        /**
         * Sets the persisted object.
         *
         * @param withDocument The item data.
         */
        public void setDocument(String withDocument) {
            document = withDocument;
        }

        /**
         * Get ETag information for handling optimistic concurrency updates.
         *
         * @return The eTag value.
         */
        @Override
        public String getETag() {
            return eTag;
        }

        /**
         * Set ETag information for handling optimistic concurrency updates.
         *
         * @param withETag The eTag value.
         */
        @Override
        public void setETag(String withETag) {
            eTag = withETag;
        }

        /**
         * The type of the document data.
         *
         * @return The class name of the data being stored.
         */
        public String getType() {
            return type;
        }

        /**
         * The fully qualified class name of the data being stored.
         *
         * @param withType The class name of the data.
         */
        public void setType(String withType) {
            type = withType;
        }

        /**
         * The value used for the PartitionKey.
         *
         * @return In this case, the id field.
         */
        public String partitionKey() {
            return id;
        }
    }
}
