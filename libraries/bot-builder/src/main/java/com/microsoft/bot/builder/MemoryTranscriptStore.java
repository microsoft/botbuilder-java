// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.schema.Activity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The memory transcript store stores transcripts in volatile memory in a Dictionary.
 *
 * Because this uses an unbounded volatile dictionary this should only be used for unit tests or
 * non-production environments.
 */
public class MemoryTranscriptStore implements TranscriptStore {
    private HashMap<String, HashMap<String, ArrayList<Activity>>> channels = new HashMap<String, HashMap<String, ArrayList<Activity>>>();

    /**
     * Emulate C# SkipWhile.
     * Stateful
     *
     * @param func1 predicate to apply
     * @param <T>   type
     * @return if the predicate condition is true
     */
    public static <T> Predicate<T> skipwhile(Function<? super T, Object> func1) {
        final boolean[] started = {false};
        return t -> started[0] || (started[0] = (boolean) func1.apply(t));
    }

    /**
     * Logs an activity to the transcript.
     *
     * @param activity The activity to log.
     * @return A CompletableFuture that represents the work queued to execute.
     */
    @Override
    public final CompletableFuture<Void> logActivity(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("activity cannot be null for LogActivity()");
        }

        synchronized (this.channels) {
            HashMap<String, ArrayList<Activity>> channel;
            if (!channels.containsKey(activity.getChannelId())) {
                channel = new HashMap<String, ArrayList<Activity>>();
                channels.put(activity.getChannelId(), channel);
            } else {
                channel = channels.get(activity.getChannelId());
            }

            ArrayList<Activity> transcript;

            if (!channel.containsKey(activity.getConversation().getId())) {
                transcript = new ArrayList<Activity>();
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
     * @param continuationToken
     * @param startDate         A cutoff date. Activities older than this date are not included.
     * @return A task that represents the work queued to execute.
     * If the task completes successfully, the result contains the matching activities.
     */
    @Override
    public CompletableFuture<PagedResult<Activity>> getTranscriptActivities(String channelId,
                                                                            String conversationId,
                                                                            String continuationToken,
                                                                            OffsetDateTime startDate) {
        if (channelId == null) {
            throw new IllegalArgumentException(String.format("missing %1$s", "channelId"));
        }

        if (conversationId == null) {
            throw new IllegalArgumentException(String.format("missing %1$s", "conversationId"));
        }

        PagedResult<Activity> pagedResult = new PagedResult<Activity>();
        synchronized (channels) {
            if (channels.containsKey(channelId)) {
                HashMap<String, ArrayList<Activity>> channel = channels.get(channelId);
                if (channel.containsKey(conversationId)) {
                    ArrayList<Activity> transcript = channel.get(conversationId);
                    if (continuationToken != null) {
                        List<Activity> items = transcript.stream()
                            .sorted(Comparator.comparing(Activity::getTimestamp))
                            .filter(a -> a.getTimestamp().compareTo(startDate) >= 0)
                            .filter(skipwhile(a -> !a.getId().equals(continuationToken)))
                            .skip(1)
                            .limit(20)
                            .collect(Collectors.toList());

                        pagedResult.setItems(items.toArray(new Activity[items.size()]));

                        if (pagedResult.getItems().length == 20) {
                            pagedResult.setContinuationToken(items.get(items.size() - 1).getId());
                        }
                    } else {
                        List<Activity> items = transcript.stream()
                            .sorted(Comparator.comparing(Activity::getTimestamp))
                            .filter(a -> a.getTimestamp().compareTo((startDate == null)
                                ? OffsetDateTime.MIN
                                : startDate) >= 0)
                            .limit(20)
                            .collect(Collectors.toList());

                        pagedResult.setItems(items.toArray(new Activity[items.size()]));

                        if (items.size() == 20) {
                            pagedResult.setContinuationToken(items.get(items.size() - 1).getId());
                        }
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
            throw new IllegalArgumentException(String.format("%1$s should not be null", "channelId"));
        }

        if (conversationId == null) {
            throw new IllegalArgumentException(String.format("%1$s should not be null", "conversationId"));
        }

        synchronized (this.channels) {
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
     * @param continuationToken
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<PagedResult<TranscriptInfo>> listTranscripts(String channelId, String continuationToken) {
        if (channelId == null) {
            throw new IllegalArgumentException(String.format("missing %1$s", "channelId"));
        }

        PagedResult<TranscriptInfo> pagedResult = new PagedResult<TranscriptInfo>();
        synchronized (channels) {
            if (channels.containsKey(channelId)) {
                HashMap<String, ArrayList<Activity>> channel = channels.get(channelId);
                if (continuationToken != null) {
                    List<TranscriptInfo> items = channel.entrySet().stream()
                        .map(c -> {
                            OffsetDateTime offsetDateTime;

                            if (c.getValue().stream().findFirst().isPresent()) {
                                offsetDateTime = c.getValue().stream().findFirst().get().getTimestamp();
                            } else {
                                offsetDateTime = OffsetDateTime.now();
                            }

                            return new TranscriptInfo(c.getKey(), channelId, offsetDateTime);
                        })
                        .sorted(Comparator.comparing(TranscriptInfo::getCreated))
                        .filter(skipwhile(c -> !c.getId().equals(continuationToken)))
                        .skip(1)
                        .limit(20)
                        .collect(Collectors.toList());

                    pagedResult.setItems(items.toArray(new TranscriptInfo[items.size()]));
                    if (items.size() == 20) {
                        pagedResult.setContinuationToken(items.get(items.size() - 1).getId());
                    }
                } else {

                    List<TranscriptInfo> items = channel.entrySet().stream()
                        .map(c -> {
                            OffsetDateTime offsetDateTime;
                            if (c.getValue().stream().findFirst().isPresent()) {
                                offsetDateTime = c.getValue().stream().findFirst().get().getTimestamp();
                            } else {
                                offsetDateTime = OffsetDateTime.now();
                            }
                            return new TranscriptInfo(c.getKey(), channelId, offsetDateTime);
                        })
                        .sorted(Comparator.comparing(TranscriptInfo::getCreated))
                        .limit(20)
                        .collect(Collectors.toList());

                    pagedResult.setItems(items.toArray(new TranscriptInfo[items.size()]));
                    if (items.size() == 20) {
                        pagedResult.setContinuationToken(items.get(items.size() - 1).getId());
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(pagedResult);
    }
}
