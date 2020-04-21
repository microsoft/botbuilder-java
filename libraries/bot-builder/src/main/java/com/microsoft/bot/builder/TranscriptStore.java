// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Transcript logger stores activities for conversations for recall.
 */
public interface TranscriptStore extends TranscriptLogger {

    /**
     * Gets from the store activities that match a set of criteria.
     *
     * @param channelId      The ID of the channel the conversation is in.
     * @param conversationId The ID of the conversation.
     * @return A task that represents the work queued to execute. If the task
     *         completes successfully, the result contains the matching activities.
     */
    default CompletableFuture<PagedResult<Activity>> getTranscriptActivities(
        String channelId,
        String conversationId
    ) {
        return getTranscriptActivities(channelId, conversationId, null);
    }

    /**
     * Gets from the store activities that match a set of criteria.
     *
     * @param channelId         The ID of the channel the conversation is in.
     * @param conversationId    The ID of the conversation.
     * @param continuationToken The continuation token (if available).
     * @return A task that represents the work queued to execute. If the task
     *         completes successfully, the result contains the matching activities.
     */
    default CompletableFuture<PagedResult<Activity>> getTranscriptActivities(
        String channelId,
        String conversationId,
        String continuationToken
    ) {
        return getTranscriptActivities(channelId, conversationId, continuationToken, null);
    }

    /**
     * Gets from the store activities that match a set of criteria.
     *
     * @param channelId         The ID of the channel the conversation is in.
     * @param conversationId    The ID of the conversation.
     * @param continuationToken The continuation token (if available).
     * @param startDate         A cutoff date. Activities older than this date are
     *                          not included.
     * @return A task that represents the work queued to execute. If the task
     *         completes successfully, the result contains the matching activities.
     */
    CompletableFuture<PagedResult<Activity>> getTranscriptActivities(
        String channelId,
        String conversationId,
        String continuationToken,
        OffsetDateTime startDate
    );

    /**
     * Gets the conversations on a channel from the store.
     *
     * @param channelId The ID of the channel.
     * @return A task that represents the work queued to execute.
     */
    default CompletableFuture<PagedResult<TranscriptInfo>> listTranscripts(String channelId) {
        return listTranscripts(channelId, null);
    }

    /**
     * Gets the conversations on a channel from the store.
     *
     * @param channelId         The ID of the channel.
     * @param continuationToken The continuation token (if available).
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<PagedResult<TranscriptInfo>> listTranscripts(
        String channelId,
        String continuationToken
    );

    /**
     * Deletes conversation data from the store.
     *
     * @param channelId      The ID of the channel the conversation is in.
     * @param conversationId The ID of the conversation to delete.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> deleteTranscript(String channelId, String conversationId);
}
