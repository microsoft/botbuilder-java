package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.microsoft.bot.schema.models.Activity;
import org.joda.time.DateTime;

import java.util.concurrent.CompletableFuture;

/**
 * Transcript logger stores activities for conversations for recall.
 */
public interface TranscriptStore extends TranscriptLogger {
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

    CompletableFuture<PagedResult<Activity>> GetTranscriptActivitiesAsync(String channelId, String conversationId, String continuationToken);

    CompletableFuture<PagedResult<Activity>> GetTranscriptActivitiesAsync(String channelId, String conversationId);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: Task<PagedResult<IActivity>> GetTranscriptActivitiesAsync(string channelId, string conversationId, string continuationToken = null, DateTime startDate = default(DateTime));
    CompletableFuture<PagedResult<Activity>> GetTranscriptActivitiesAsync(String channelId, String conversationId, String continuationToken, DateTime localStartDate);

    /**
     * Gets the conversations on a channel from the store.
     *
     * @param channelId         The ID of the channel.
     * @param continuationToken
     * @return A task that represents the work queued to execute.
     */

    CompletableFuture<PagedResult<Transcript>> ListTranscriptsAsync(String channelId);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: Task<PagedResult<Transcript>> ListTranscriptsAsync(string channelId, string continuationToken = null);
    CompletableFuture<PagedResult<Transcript>> ListTranscriptsAsync(String channelId, String continuationToken);

    /**
     * Deletes conversation data from the store.
     *
     * @param channelId      The ID of the channel the conversation is in.
     * @param conversationId The ID of the conversation to delete.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture DeleteTranscriptAsync(String channelId, String conversationId);
}
