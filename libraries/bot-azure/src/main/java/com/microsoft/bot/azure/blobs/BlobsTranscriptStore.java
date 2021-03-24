// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure.blobs;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.bot.builder.BotAssert;
import com.microsoft.bot.builder.PagedResult;
import com.microsoft.bot.builder.TranscriptInfo;
import com.microsoft.bot.builder.TranscriptStore;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The blobs transcript store stores transcripts in an Azure Blob container.
 * Each activity is stored as json blob in structure of
 * container/{channelId]/{conversationId}/{Timestamp.ticks}-{activity.id}.json.
 */
public class BlobsTranscriptStore implements TranscriptStore {

    // Containers checked for creation.
    private static final HashSet<String> CHECKED_CONTAINERS = new HashSet<String>();

    private final Integer milisecondsTimeout = 2000;
    private final Integer retryTimes = 3;
    private final Integer longRadix = 16;
    private final Integer multipleProductValue = 10_000_000;

    private final ObjectMapper jsonSerializer;
    private BlobContainerClient containerClient;

    /**
     * Initializes a new instance of the {@link BlobsTranscriptStore} class.
     * 
     * @param dataConnectionString Azure Storage connection string.
     * @param containerName        Name of the Blob container where entities will be
     *                             stored.
     */
    public BlobsTranscriptStore(String dataConnectionString, String containerName) {
        if (StringUtils.isBlank(dataConnectionString)) {
            throw new IllegalArgumentException("dataConnectionString");
        }

        if (StringUtils.isBlank(containerName)) {
            throw new IllegalArgumentException("containerName");
        }

        jsonSerializer = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .findAndRegisterModules();

        // Triggers a check for the existence of the container
        containerClient = this.getContainerClient(dataConnectionString, containerName);
    }

    /**
     * Log an activity to the transcript.
     * 
     * @param activity Activity being logged.
     * @return A CompletableFuture that represents the work queued to execute.
     */
    public CompletableFuture<Void> logActivity(Activity activity) {
        BotAssert.activityNotNull(activity);

        switch (activity.getType()) {
            case ActivityTypes.MESSAGE_UPDATE:
                Activity updatedActivity = null;
                try {
                    updatedActivity =
                        jsonSerializer.readValue(jsonSerializer.writeValueAsString(activity), Activity.class);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                updatedActivity.setType(ActivityTypes.MESSAGE); // fixup original type (should be Message)
                Activity finalUpdatedActivity = updatedActivity;
                innerReadBlob(activity).thenAccept(activityAndBlob -> {
                    if (activityAndBlob != null && activityAndBlob.getLeft() != null) {
                        finalUpdatedActivity.setLocalTimestamp(activityAndBlob.getLeft().getLocalTimestamp());
                        finalUpdatedActivity.setTimestamp(activityAndBlob.getLeft().getTimestamp());
                        logActivityToBlobClient(finalUpdatedActivity, activityAndBlob.getRight(), true)
                            .thenApply(task -> CompletableFuture.completedFuture(null));
                    } else {
                        // The activity was not found, so just add a record of this update.
                        this.innerLogActivity(finalUpdatedActivity)
                            .thenApply(task -> CompletableFuture.completedFuture(null));
                    }
                });

                return CompletableFuture.completedFuture(null);

            case ActivityTypes.MESSAGE_DELETE:
                innerReadBlob(activity).thenAccept(activityAndBlob -> {
                    if (activityAndBlob != null && activityAndBlob.getLeft() != null) {
                        ChannelAccount from = new ChannelAccount();
                        from.setId("deleted");
                        from.setRole(activityAndBlob.getLeft().getFrom().getRole());
                        ChannelAccount recipient = new ChannelAccount();
                        recipient.setId("deleted");
                        recipient.setRole(activityAndBlob.getLeft().getRecipient().getRole());

                        // tombstone the original message
                        Activity tombstonedActivity = new Activity(ActivityTypes.MESSAGE_DELETE);
                        tombstonedActivity.setId(activityAndBlob.getLeft().getId());
                        tombstonedActivity.setFrom(from);
                        tombstonedActivity.setRecipient(recipient);
                        tombstonedActivity.setLocale(activityAndBlob.getLeft().getLocale());
                        tombstonedActivity.setLocalTimestamp(activityAndBlob.getLeft().getTimestamp());
                        tombstonedActivity.setTimestamp(activityAndBlob.getLeft().getTimestamp());
                        tombstonedActivity.setChannelId(activityAndBlob.getLeft().getChannelId());
                        tombstonedActivity.setConversation(activityAndBlob.getLeft().getConversation());
                        tombstonedActivity.setServiceUrl(activityAndBlob.getLeft().getServiceUrl());
                        tombstonedActivity.setReplyToId(activityAndBlob.getLeft().getReplyToId());

                        logActivityToBlobClient(tombstonedActivity, activityAndBlob.getRight(), true)
                            .thenApply(task -> CompletableFuture.completedFuture(null));
                    }
                });

                return CompletableFuture.completedFuture(null);

            default:
                this.innerLogActivity(activity).thenApply(task -> CompletableFuture.completedFuture(null));
                return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Get activities for a conversation (Aka the transcript).
     * 
     * @param channelId         The ID of the channel the conversation is in.
     * @param conversationId    The ID of the conversation.
     * @param continuationToken The continuation token (if available).
     * @param startDate         A cutoff date. Activities older than this date are
     *                          not included.
     * @return PagedResult of activities.
     */
    public CompletableFuture<PagedResult<Activity>> getTranscriptActivities(
        String channelId,
        String conversationId,
        @Nullable String continuationToken,
        OffsetDateTime startDate
    ) {
        if (startDate == null) {
            startDate = OffsetDateTime.MIN;
        }

        final int pageSize = 20;

        if (StringUtils.isBlank(channelId)) {
            throw new IllegalArgumentException("Missing channelId");
        }

        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("Missing conversationId");
        }

        PagedResult<Activity> pagedResult = new PagedResult<Activity>();

        String token = null;
        List<BlobItem> blobs = new ArrayList<BlobItem>();
        do {
            String prefix = String.format("%s/%s/", sanitizeKey(channelId), sanitizeKey(conversationId));
            Iterable<PagedResponse<BlobItem>> resultSegment = containerClient
                .listBlobsByHierarchy("/", this.getOptionsWithMetadata(prefix), null)
                .iterableByPage(token);
            token = null;
            for (PagedResponse<BlobItem> blobPage : resultSegment) {
                for (BlobItem blobItem : blobPage.getValue()) {
                    OffsetDateTime parseDateTime = OffsetDateTime.parse(blobItem.getMetadata().get("Timestamp"));
                    if (parseDateTime.isAfter(startDate) || parseDateTime.isEqual(startDate)) {
                        if (continuationToken != null) {
                            if (blobItem.getName().equals(continuationToken)) {
                                // we found continuation token
                                continuationToken = null;
                            }
                        } else {
                            blobs.add(blobItem);
                            if (blobs.size() == pageSize) {
                                break;
                            }
                        }
                    }
                }

                // Get the continuation token and loop until it is empty.
                token = blobPage.getContinuationToken();
            }
        } while (!StringUtils.isBlank(token) && blobs.size() < pageSize);

        pagedResult.setItems(blobs.stream().map(bl -> {
            BlobClient blobClient = containerClient.getBlobClient(bl.getName());
            return this.getActivityFromBlobClient(blobClient);
        }).map(t -> t.join()).collect(Collectors.toList()));

        if (pagedResult.getItems().size() == pageSize) {
            pagedResult.setContinuationToken(blobs.get(blobs.size() - 1).getName());
        }

        return CompletableFuture.completedFuture(pagedResult);
    }

    /**
     * List conversations in the channelId.
     * 
     * @param channelId         The ID of the channel.
     * @param continuationToken The continuation token (if available).
     * @return A CompletableFuture that represents the work queued to execute.
     */
    public CompletableFuture<PagedResult<TranscriptInfo>> listTranscripts(
        String channelId,
        @Nullable String continuationToken
    ) {
        final int pageSize = 20;

        if (StringUtils.isBlank(channelId)) {
            throw new IllegalArgumentException("Missing channelId");
        }

        String token = null;

        List<TranscriptInfo> conversations = new ArrayList<TranscriptInfo>();
        do {
            String prefix = String.format("%s/", sanitizeKey(channelId));
            Iterable<PagedResponse<BlobItem>> resultSegment = containerClient
                .listBlobsByHierarchy("/", this.getOptionsWithMetadata(prefix), null)
                .iterableByPage(token);
            token = null;
            for (PagedResponse<BlobItem> blobPage : resultSegment) {
                for (BlobItem blobItem : blobPage.getValue()) {
                    // Unescape the Id we escaped when we saved it
                    String conversationId = new String();
                    String lastName = Arrays.stream(blobItem.getName().split("/"))
                        .reduce((first, second) -> second.length() > 0 ? second : first)
                        .get();
                    try {
                        conversationId = URLDecoder.decode(lastName, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }
                    TranscriptInfo conversation =
                        new TranscriptInfo(conversationId, channelId, blobItem.getProperties().getCreationTime());
                    if (continuationToken != null) {
                        if (StringUtils.equals(conversation.getId(), continuationToken)) {
                            // we found continuation token
                            continuationToken = null;
                        }

                        // skip record
                    } else {
                        conversations.add(conversation);
                        if (conversations.size() == pageSize) {
                            break;
                        }
                    }
                }
            }
        } while (!StringUtils.isBlank(token) && conversations.size() < pageSize);

        PagedResult<TranscriptInfo> pagedResult = new PagedResult<TranscriptInfo>();
        pagedResult.setItems(conversations);

        if (pagedResult.getItems().size() == pageSize) {
            pagedResult.setContinuationToken(pagedResult.getItems().get(pagedResult.getItems().size() - 1).getId());
        }

        return CompletableFuture.completedFuture(pagedResult);
    }

    /**
     * Delete a specific conversation and all of it's activities.
     * 
     * @param channelId      The ID of the channel the conversation is in.
     * @param conversationId The ID of the conversation to delete.
     * @return A CompletableFuture that represents the work queued to execute.
     */
    public CompletableFuture<Void> deleteTranscript(String channelId, String conversationId) {
        if (StringUtils.isBlank(channelId)) {
            throw new IllegalArgumentException("Missing channelId");
        }

        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("Missing conversationId");
        }

        String token = null;
        do {
            String prefix = String.format("%s/%s/", sanitizeKey(channelId), sanitizeKey(conversationId));
            Iterable<PagedResponse<BlobItem>> resultSegment = containerClient
                .listBlobsByHierarchy("/", this.getOptionsWithMetadata(prefix), null)
                .iterableByPage(token);
            token = null;

            for (PagedResponse<BlobItem> blobPage : resultSegment) {
                for (BlobItem blobItem : blobPage.getValue()) {
                    BlobClient blobClient = containerClient.getBlobClient(blobItem.getName());
                    if (blobClient.exists()) {
                        try {
                            blobClient.delete();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    // Get the continuation token and loop until it is empty.
                    token = blobPage.getContinuationToken();
                }
            }
        } while (!StringUtils.isBlank(token));

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Pair<Activity, BlobClient>> innerReadBlob(Activity activity) {
        int i = 0;
        while (true) {
            try {
                String token = null;
                do {
                    String prefix = String.format(
                        "%s/%s/",
                        sanitizeKey(activity.getChannelId()),
                        sanitizeKey(activity.getConversation().getId())
                    );
                    Iterable<PagedResponse<BlobItem>> resultSegment = containerClient
                        .listBlobsByHierarchy("/", this.getOptionsWithMetadata(prefix), null)
                        .iterableByPage(token);
                    token = null;
                    for (PagedResponse<BlobItem> blobPage : resultSegment) {
                        for (BlobItem blobItem : blobPage.getValue()) {
                            if (blobItem.getMetadata().get("Id").equals(activity.getId())) {
                                BlobClient blobClient = containerClient.getBlobClient(blobItem.getName());
                                return this.getActivityFromBlobClient(
                                    blobClient
                                ).thenApply(blobActivity -> new Pair<Activity, BlobClient>(blobActivity, blobClient));
                            }
                        }

                        // Get the continuation token and loop until it is empty.
                        token = blobPage.getContinuationToken();
                    }
                } while (!StringUtils.isBlank(token));

                return CompletableFuture.completedFuture(null);
            } catch (HttpResponseException ex) {
                if (ex.getResponse().getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
                    // additional retry logic,
                    // even though this is a read operation blob storage can return 412 if there is
                    // contention
                    if (i++ < retryTimes) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(milisecondsTimeout);
                            continue;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    throw ex;
                }
                // This break will finish the while when the catch if condition is false
                break;
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Activity> getActivityFromBlobClient(BlobClient blobClient) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        blobClient.download(content);
        String contentString = new String(content.toByteArray());
        try {
            return CompletableFuture.completedFuture(jsonSerializer.readValue(contentString, Activity.class));
        } catch (IOException ex) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> innerLogActivity(Activity activity) {
        String blobName = this.getBlobName(activity);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return logActivityToBlobClient(activity, blobClient, null);
    }

    private CompletableFuture<Void> logActivityToBlobClient(
        Activity activity,
        BlobClient blobClient,
        Boolean overwrite
    ) {
        if (overwrite == null) {
            overwrite = false;
        }
        String activityJson = null;
        try {
            activityJson = jsonSerializer.writeValueAsString(activity);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        InputStream data = new ByteArrayInputStream(activityJson.getBytes(StandardCharsets.UTF_8));

        try {
            blobClient.upload(data, data.available(), overwrite);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("Id", activity.getId());
        if (activity.getFrom() != null) {
            metaData.put("FromId", activity.getFrom().getId());
        }

        if (activity.getRecipient() != null) {
            metaData.put("RecipientId", activity.getRecipient().getId());
        }
        metaData.put("Timestamp", activity.getTimestamp().toString());

        blobClient.setMetadata(metaData);

        return CompletableFuture.completedFuture(null);
    }

    private String getBlobName(Activity activity) {
        String blobName = String.format(
            "%s/%s/%s-%s.json",
            sanitizeKey(activity.getChannelId()),
            sanitizeKey(activity.getConversation().getId()),
            this.formatTicks(activity.getTimestamp()),
            sanitizeKey(activity.getId())
        );

        return blobName;
    }

    private String sanitizeKey(String key) {
        // Blob Name rules: case-sensitive any url char
        try {
            return URLEncoder.encode(key, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private BlobContainerClient getContainerClient(String dataConnectionString, String containerName) {
        containerName = containerName.toLowerCase();
        containerClient = new BlobContainerClientBuilder().connectionString(dataConnectionString)
            .containerName(containerName)
            .buildClient();
        if (!CHECKED_CONTAINERS.contains(containerName)) {
            CHECKED_CONTAINERS.add(containerName);
            if (!containerClient.exists()) {
                try {
                    containerClient.create();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return containerClient;
    }

    /**
     * Formats a timestamp in a way that is consistent with the C# SDK.
     * 
     * @param dateTime The dateTime used to get the ticks
     * @return The String representing the ticks.
     */
    private String formatTicks(OffsetDateTime dateTime) {
        final Instant begin = ZonedDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        final Instant end = dateTime.toInstant();
        long secsDiff = Math.subtractExact(end.getEpochSecond(), begin.getEpochSecond());
        long totalHundredNanos = Math.multiplyExact(secsDiff, multipleProductValue);
        final Long ticks = Math.addExact(totalHundredNanos, (end.getNano() - begin.getNano()) / 100);
        return Long.toString(ticks, longRadix);
    }

    private ListBlobsOptions getOptionsWithMetadata(String prefix) {
        BlobListDetails details = new BlobListDetails();
        details.setRetrieveMetadata(true);
        ListBlobsOptions options = new ListBlobsOptions();
        options.setDetails(details);
        options.setPrefix(prefix);
        return options;
    }
}
