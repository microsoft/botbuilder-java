// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.TypedInvokeResponse;
import com.microsoft.bot.schema.Activity;

/**
 * A Bot Framework client.
 */
public abstract class BotFrameworkClient {

    /**
     * Forwards an activity to a skill (bot).
     *
     * NOTE: Forwarding an activity to a skill will flush UserState and
     * ConversationState changes so that skill has accurate state.
     *
     * @param fromBotId       The MicrosoftAppId of the bot sending the
     *                        activity.
     * @param toBotId         The MicrosoftAppId of the bot receiving
     *                        the activity.
     * @param toUrl           The URL of the bot receiving the activity.
     * @param serviceUrl      The callback Url for the skill host.
     * @param conversationId  A conversation ID to use for the
     *                        conversation with the skill.
     * @param activity        The {@link Activity} to send to forward.
     *
     * @return   task with optional invokeResponse.
     */
    public abstract CompletableFuture<InvokeResponse> postActivity(
        String fromBotId,
        String toBotId,
        URI toUrl,
        URI serviceUrl,
        String conversationId,
        Activity activity);

    /**
     * Forwards an activity to a skill (bot).
     *
     * NOTE: Forwarding an activity to a skill will flush UserState and
     * ConversationState changes so that skill has accurate state.
     *
     * @param fromBotId       The MicrosoftAppId of the bot sending the
     *                        activity.
     * @param toBotId         The MicrosoftAppId of the bot receiving
     *                        the activity.
     * @param toUrl           The URL of the bot receiving the activity.
     * @param serviceUrl      The callback Url for the skill host.
     * @param conversationId  A conversation ID to use for the
     *                        conversation with the skill.
     * @param activity        The {@link Activity} to send to forward.
     * @param <T>             The type for the TypedInvokeResponse body to contain.
     *
     * @return   task with optional invokeResponse.
     */
    public abstract <T extends Object> CompletableFuture<TypedInvokeResponse<T>> postTypedActivity(
        String fromBotId,
        String toBotId,
        URI toUrl,
        URI serviceUrl,
        String conversationId,
        Activity activity);
}
