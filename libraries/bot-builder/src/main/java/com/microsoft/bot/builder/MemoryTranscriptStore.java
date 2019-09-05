package com.microsoft.bot.builder;


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.schema.Activity;
import org.joda.time.DateTime;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
 * <p>
 * <p>
 * Because this uses an unbounded volitile dictionary this should only be used for unit tests or non-production environments.
 */
public class MemoryTranscriptStore implements TranscriptStore {
    private HashMap<String, HashMap<String, ArrayList<Activity>>> channels = new HashMap<String, HashMap<String, ArrayList<Activity>>>();

    /**
     * Logs an activity to the transcript.
     *
     * @param activity The activity to log.
     * @return A CompletableFuture that represents the work queued to execute.
     */
    @Override
    public final CompletableFuture<Void> logActivityAsync(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null for LogActivity()");
        }

        synchronized (this.channels) {
            HashMap<String, ArrayList<Activity>> channel;
            if (!this.channels.containsKey(activity.getChannelId())) {
                channel = new HashMap<String, ArrayList<Activity>>();
                this.channels.put(activity.getChannelId(), channel);
            } else {
                channel = this.channels.get(activity.getChannelId());
            }

            ArrayList<Activity> transcript = null;

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
    public CompletableFuture<PagedResult<Activity>> getTranscriptActivitiesAsync(String channelId,
                                                                                 String conversationId,
                                                                                 String continuationToken,
                                                                                 DateTime startDate) {
        if (channelId == null) {
            throw new IllegalArgumentException(String.format("missing %1$s", "channelId"));
        }

        if (conversationId == null) {
            throw new IllegalArgumentException(String.format("missing %1$s", "conversationId"));
        }

        return CompletableFuture.supplyAsync(() -> {
            PagedResult<Activity> pagedResult = new PagedResult<Activity>();
            synchronized (channels) {
                HashMap<String, ArrayList<Activity>> channel;
                if (!channels.containsKey(channelId)) {
                    return pagedResult;
                }
                channel = channels.get(channelId);
                ArrayList<Activity> transcript;

                if (!channel.containsKey(conversationId)) {
                    return pagedResult;
                }
                transcript = channel.get(conversationId);
                if (continuationToken != null) {
                    List<Activity> items = transcript.stream()
                            .sorted(Comparator.comparing(Activity::getTimestamp))
                            .filter(a -> a.getTimestamp().compareTo(startDate) >= 0)
                            .filter(skipwhile(a -> !a.getId().equals(continuationToken)))
                            .skip(1)
                            .limit(20)
                            .collect(Collectors.toList());

                    pagedResult.items(items.toArray(new Activity[items.size()]));

                    if (pagedResult.getItems().length == 20) {
                        pagedResult.withContinuationToken(items.get(items.size() - 1).getId());
                    }
                } else {
                    List<Activity> items = transcript.stream()
                            .sorted(Comparator.comparing(Activity::getTimestamp))
                            .filter(a -> a.getTimestamp().compareTo((startDate == null) ? new DateTime(Long.MIN_VALUE) : startDate) >= 0)
                            .limit(20)
                            .collect(Collectors.toList());
                    pagedResult.items(items.toArray(new Activity[items.size()]));
                    if (items.size() == 20) {
                        pagedResult.withContinuationToken(items.get(items.size() - 1).getId());
                    }
                }
            }

            return pagedResult;

        }, ExecutorFactory.getExecutor());
    }

    /**
     * Deletes conversation data from the store.
     *
     * @param channelId      The ID of the channel the conversation is in.
     * @param conversationId The ID of the conversation to delete.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> deleteTranscriptAsync(String channelId, String conversationId) {
        if (channelId == null) {
            throw new IllegalArgumentException(String.format("%1$s should not be null", "channelId"));
        }

        if (conversationId == null) {
            throw new IllegalArgumentException(String.format("%1$s should not be null", "conversationId"));
        }

        return CompletableFuture.runAsync(() -> {
            synchronized (this.channels) {
                if (!this.channels.containsKey(channelId)) {
                    return;
                }
                HashMap<String, ArrayList<Activity>> channel = this.channels.get(channelId);
                if (channel.containsKey(conversationId)) {
                    channel.remove(conversationId);
                }
            }
        }, ExecutorFactory.getExecutor());
    }

    /**
     * Gets the conversations on a channel from the store.
     *
     * @param channelId         The ID of the channel.
     * @param continuationToken
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<PagedResult<Transcript>> listTranscriptsAsync(String channelId, String continuationToken) {
        if (channelId == null) {
            throw new IllegalArgumentException(String.format("missing %1$s", "channelId"));
        }

        return CompletableFuture.supplyAsync(() -> {
            PagedResult<Transcript> pagedResult = new PagedResult<Transcript>();
            synchronized (channels) {

                if (!channels.containsKey(channelId)) {
                    return pagedResult;
                }

                HashMap<String, ArrayList<Activity>> channel = channels.get(channelId);
                if (continuationToken != null) {
                    List<Transcript> items = channel.entrySet().stream()
                            .map(c -> {
                                        OffsetDateTime offsetDateTime = null;
                                        if (c.getValue().stream().findFirst().isPresent()) {
                                            DateTime dt = c.getValue().stream().findFirst().get().getTimestamp();
                                            // convert to DateTime to OffsetDateTime
                                            Instant instant = Instant.ofEpochMilli(dt.getMillis());
                                            ZoneOffset offset = ZoneId.of(dt.getZone().getID()).getRules().getOffset(instant);
                                            offsetDateTime = instant.atOffset(offset);
                                        } else {
                                            offsetDateTime = OffsetDateTime.now();
                                        }
                                        return new Transcript()
                                                .withChannelId(channelId)
                                                .withId(c.getKey())
                                                .withCreated(offsetDateTime);
                                    }
                            )
                            .sorted(Comparator.comparing(Transcript::getCreated))
                            .filter(skipwhile(c -> !c.getId().equals(continuationToken)))
                            .skip(1)
                            .limit(20)
                            .collect(Collectors.toList());
                    pagedResult.items(items.toArray(new Transcript[items.size()]));
                    if (items.size() == 20) {
                        pagedResult.withContinuationToken(items.get(items.size() - 1).getId());
                    }
                } else {

                    List<Transcript> items = channel.entrySet().stream()
                            .map(c -> {
                                        OffsetDateTime offsetDateTime = null;
                                        if (c.getValue().stream().findFirst().isPresent()) {
                                            DateTime dt = c.getValue().stream().findFirst().get().getTimestamp();
                                            // convert to DateTime to OffsetDateTime
                                            Instant instant = Instant.ofEpochMilli(dt.getMillis());
                                            ZoneOffset offset = ZoneId.of(dt.getZone().getID()).getRules().getOffset(instant);
                                            offsetDateTime = instant.atOffset(offset);
                                        } else {
                                            offsetDateTime = OffsetDateTime.now();
                                        }
                                        return new Transcript()
                                                .withChannelId(channelId)
                                                .withId(c.getKey())
                                                .withCreated(offsetDateTime);
                                    }
                            )
                            .sorted(Comparator.comparing(Transcript::getCreated))
                            .limit(20)
                            .collect(Collectors.toList());
                    pagedResult.items(items.toArray(new Transcript[items.size()]));
                    if (items.size() == 20) {
                        pagedResult.withContinuationToken(items.get(items.size() - 1).getId());
                    }
                }
            }
            return pagedResult;
        }, ExecutorFactory.getExecutor());
    }

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

}
