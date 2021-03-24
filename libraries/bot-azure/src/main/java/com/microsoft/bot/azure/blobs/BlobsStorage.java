// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure.blobs;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.StoreItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Implements {@link Storage} using Azure Storage Blobs. This class uses a
 * single Azure Storage Blob Container. Each entity or {@link StoreItem} is
 * serialized into a JSON string and stored in an individual text blob. Each
 * blob is named after the store item key, which is encoded so that it conforms
 * a valid blob name. an entity is an {@link StoreItem}, the storage object will
 * set the entity's {@link StoreItem} property value to the blob's ETag upon
 * read. Afterward, an {@link BlobRequestConditions} with the ETag value will be
 * generated during Write. New entities start with a null ETag.
 */
public class BlobsStorage implements Storage {

    private ObjectMapper objectMapper;
    private final BlobContainerClient containerClient;

    private final Integer millisecondsTimeout = 2000;
    private final Integer retryTimes = 8;

    /**
     * Initializes a new instance of the {@link BlobsStorage} class.
     * 
     * @param dataConnectionString Azure Storage connection string.
     * @param containerName        Name of the Blob container where entities will be
     *                             stored.
     */
    public BlobsStorage(String dataConnectionString, String containerName) {
        if (StringUtils.isBlank(dataConnectionString)) {
            throw new IllegalArgumentException("dataConnectionString is required.");
        }

        if (StringUtils.isBlank(containerName)) {
            throw new IllegalArgumentException("containerName is required.");
        }

        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules()
            .enableDefaultTyping();

        containerClient = new BlobContainerClientBuilder().connectionString(dataConnectionString)
            .containerName(containerName)
            .buildClient();
    }

    /**
     * Deletes entity blobs from the configured container.
     * 
     * @param keys An array of entity keys.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> delete(String[] keys) {
        if (keys == null) {
            throw new IllegalArgumentException("The 'keys' parameter is required.");
        }

        for (String key : keys) {
            String blobName = getBlobName(key);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (blobClient.exists()) {
                try {
                    blobClient.delete();
                } catch (BlobStorageException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Retrieve entities from the configured blob container.
     * 
     * @param keys An array of entity keys.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Map<String, Object>> read(String[] keys) {
        if (keys == null) {
            throw new IllegalArgumentException("The 'keys' parameter is required.");
        }

        if (!containerClient.exists()) {
            try {
                containerClient.create();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        Map<String, Object> items = new HashMap<>();

        for (String key : keys) {
            String blobName = getBlobName(key);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            innerReadBlob(blobClient).thenAccept(value -> {
                if (value != null) {
                    items.put(key, value);
                }
            });
        }
        return CompletableFuture.completedFuture(items);
    }

    /**
     * Stores a new entity in the configured blob container.
     * 
     * @param changes The changes to write to storage.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> write(Map<String, Object> changes) {
        if (changes == null) {
            throw new IllegalArgumentException("The 'changes' parameter is required.");
        }

        if (!containerClient.exists()) {
            try {
                containerClient.create();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (Map.Entry<String, Object> keyValuePair : changes.entrySet()) {
            Object newValue = keyValuePair.getValue();
            StoreItem storeItem = newValue instanceof StoreItem ? (StoreItem) newValue : null;

            // "*" eTag in StoreItem converts to null condition for AccessCondition
            boolean isNullOrEmpty =
                storeItem == null || StringUtils.isBlank(storeItem.getETag()) || storeItem.getETag().equals("*");
            BlobRequestConditions accessCondition =
                !isNullOrEmpty ? new BlobRequestConditions().setIfMatch(storeItem.getETag()) : null;

            String blobName = getBlobName(keyValuePair.getKey());
            BlobClient blobReference = containerClient.getBlobClient(blobName);
            try {
                String json = objectMapper.writeValueAsString(newValue);
                InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
                // verify the corresponding length
                blobReference.uploadWithResponse(
                    stream,
                    stream.available(),
                    null,
                    null,
                    null,
                    null,
                    accessCondition,
                    null,
                    Context.NONE
                );
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    StringBuilder sb =
                        new StringBuilder("An error occurred while trying to write an object. The underlying ");
                    sb.append(BlobErrorCode.INVALID_BLOCK_LIST);
                    sb.append(
                        " error is commonly caused due to "
                            + "concurrently uploading an object larger than 128MB in size."
                    );

                    throw new HttpResponseException(sb.toString(), e.getResponse());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private static String getBlobName(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("The 'key' parameter is required.");
        }

        String blobName;
        try {
            blobName = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("The key could not be encoded");
        }

        return blobName;
    }

    private CompletableFuture<Object> innerReadBlob(BlobClient blobReference) {
        Integer i = 0;
        while (true) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                blobReference.download(outputStream);
                String contentString = outputStream.toString();

                Object obj;
                // We are doing this try/catch because we are receiving String or HashMap
                try {
                    // We need to deserialize to an Object class since there are contentString which
                    // has an Object type
                    obj = objectMapper.readValue(contentString, Object.class);
                } catch (MismatchedInputException ex) {
                    // In case of the contentString has the structure of a HashMap,
                    // we need to deserialize it to a HashMap object
                    obj = objectMapper.readValue(contentString, HashMap.class);
                }

                if (obj instanceof StoreItem) {
                    String eTag = blobReference.getProperties().getETag();
                    ((StoreItem) obj).setETag(eTag);
                }

                return CompletableFuture.completedFuture(obj);
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
                    // additional retry logic,
                    // even though this is a read operation blob storage can return 412 if there is
                    // contention
                    if (i++ < retryTimes) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(millisecondsTimeout);
                            continue;
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                    throw e;
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
