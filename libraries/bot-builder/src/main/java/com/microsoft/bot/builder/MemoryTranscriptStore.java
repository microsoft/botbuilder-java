// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.codepoetics.protonpack.StreamUtils;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.Activity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The memory transcript store stores transcripts in volatile memory in a
 * Dictionary.
 *
 * <p>
 * Because this uses an unbounded volatile dictionary this should only be used
 * for unit tests or non-production environments.
 * </p>
 */
public class MemoryTranscriptStore implements TranscriptStore {
    /**
     * Numbers of results in a paged request.
     */
    private static final int PAGE_SIZE = 20;

    /**
     * Sync object for locking.
     */
    private final Object sync = new Object();

    /**
     * Map of channel transcripts.
     */
    private HashMap<String, HashMap<String, ArrayList<Activity>>> channels = new HashMap<>();

    /**
     * Logs an activity to the transcript.
     *
     * @param activity The activity to log.
     * @return A CompletableFuture that represents the work queued to execute.
     */
    @Override
    public final CompletableFuture<Void> logActivity(Activity activity) {
        if (activity == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException("activity cannot be null for LogActivity()"));
        }

        synchronized (sync) {
            HashMap<String, ArrayList<Activity>> channel;
            if (!channels.containsKey(activity.getChannelId())) {
                channel = new HashMap<>();
                channels.put(activity.getChannelId(), channel);
            } else {
                channel = channels.get(activity.getChannelId());
            }

            ArrayList<Activity> transcript;

            if (!channel.containsKey(activity.getConversation().getId())) {
                transcript = new ArrayList<>();
                channel.put(activity.getConversation().getId(), transcript);
            } else {
                transcript = channel.get(activity.getConversation().getId());
            }

            transcript.add(activity);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Gets from the store activities that match a set of criteria.
     *
     * @param channelId         The ID of the channel the conversation is in.
     * @param conversationId    The ID of the conversation.
     * @param continuationToken The continuation token from the previous page of
     *                          results.
     * @param startDate         A cutoff date. Activities older than this date are
     *                          not included.
     * @return A task that represents the work queued to execute. If the task
     *         completes successfully, the result contains the matching activities.
     */
    @Override
    public CompletableFuture<PagedResult<Activity>> getTranscriptActivities(
        String channelId,
        String conversationId,
        String continuationToken,
        OffsetDateTime startDate
    ) {
        if (channelId == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException(String.format("missing %1$s", "channelId")));
        }

        if (conversationId == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException(String.format("missing %1$s", "conversationId")));
        }

        PagedResult<Activity> pagedResult = new PagedResult<>();
        synchronized (sync) {
            if (channels.containsKey(channelId)) {
                HashMap<String, ArrayList<Activity>> channel = channels.get(channelId);
                if (channel.containsKey(conversationId)) {
                    OffsetDateTime effectiveStartDate = (startDate == null)
                        ? OffsetDateTime.MIN
                        : startDate;

                    ArrayList<Activity> transcript = channel.get(conversationId);

                    Stream<Activity> stream = transcript.stream()
                        .sorted(Comparator.comparing(Activity::getTimestamp))
                        .filter(a -> a.getTimestamp().compareTo(effectiveStartDate) >= 0);

                    if (continuationToken != null) {
                        stream = StreamUtils
                            .skipWhile(stream, a -> !a.getId().equals(continuationToken))
                            .skip(1);
                    }

                    List<Activity> items = stream.limit(PAGE_SIZE).collect(Collectors.toList());

                    pagedResult.setItems(items);
                    if (pagedResult.getItems().size() == PAGE_SIZE) {
                        pagedResult.setContinuationToken(items.get(items.size() - 1).getId());
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(pagedResult);
    }

    /**
     * Deletes conversation data from the store.
     *
     * @param channelId      The ID of the channel the conversation is in.
     * @param conversationId The ID of the conversation to delete.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> deleteTranscript(String channelId, String conversationId) {
        if (channelId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                String.format("%1$s should not be null", "channelId")
            ));
        }

        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                String.format("%1$s should not be null", "conversationId")
            ));
        }

        synchronized (sync) {
            if (this.channels.containsKey(channelId)) {
                HashMap<String, ArrayList<Activity>> channel = this.channels.get(channelId);
                channel.remove(conversationId);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Gets the conversations on a channel from the store.
     *
     * @param channelId         The ID of the channel.
     * @param continuationToken The continuation token from the previous page of
     *                          results.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<PagedResult<TranscriptInfo>> listTranscripts(
        String channelId,
        String continuationToken
    ) {
        if (channelId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(String.format(
                "missing %1$s", "channelId"
            )));
        }

        PagedResult<TranscriptInfo> pagedResult = new PagedResult<>();
        synchronized (sync) {
            if (channels.containsKey(channelId)) {
                HashMap<String, ArrayList<Activity>> channel = channels.get(channelId);
                Stream<TranscriptInfo> stream = channel.entrySet().stream().map(c -> {
                    OffsetDateTime offsetDateTime;

                    if (c.getValue().stream().findFirst().isPresent()) {
                        offsetDateTime = c.getValue().stream().findFirst().get().getTimestamp();
                    } else {
                        offsetDateTime = OffsetDateTime.now();
                    }

                    return new TranscriptInfo(c.getKey(), channelId, offsetDateTime);
                }).sorted(Comparator.comparing(TranscriptInfo::getCreated));

                if (continuationToken != null) {
                    stream = StreamUtils
                        .skipWhile(stream, c -> !c.getId().equals(continuationToken))
                        .skip(1);
                }

                List<TranscriptInfo> items = stream.limit(PAGE_SIZE).collect(Collectors.toList());

                pagedResult.setItems(items);
                if (items.size() == PAGE_SIZE) {
                    pagedResult.setContinuationToken(items.get(items.size() - 1).getId());
                }
            }
        }

        return CompletableFuture.completedFuture(pagedResult);
    }
}
