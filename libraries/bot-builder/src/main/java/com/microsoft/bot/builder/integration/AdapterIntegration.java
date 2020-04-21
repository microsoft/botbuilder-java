// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.integration;

import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;

import java.util.concurrent.CompletableFuture;

/**
 * An interface that defines the contract between web service integration pieces
 * and the bot adapter.
 */
public interface AdapterIntegration {
    /**
     * Creates a turn context and runs the middleware pipeline for an incoming
     * activity.
     *
     * @param authHeader The HTTP authentication header of the request.
     * @param activity   The incoming activity.
     * @param callback   The code to run at the end of the adapter's middleware
     *                   pipeline.
     * @return A task that represents the work queued to execute. If the activity
     *         type was 'Invoke' and the corresponding key (channelId + activityId)
     *         was found then an InvokeResponse is returned, otherwise null is
     *         returned.
     */
    CompletableFuture<InvokeResponse> processActivity(
        String authHeader,
        Activity activity,
        BotCallbackHandler callback
    );

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * _channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param botId     The application ID of the bot. This parameter is ignored in
     *                  single tenant the Adapters (Console, Test, etc) but is
     *                  critical to the BotFrameworkAdapter which is multi-tenant
     *                  aware.
     * @param reference A reference to the conversation to continue.
     * @param callback  The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> continueConversation(
        String botId,
        ConversationReference reference,
        BotCallbackHandler callback
    );
}
