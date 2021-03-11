// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.skills.BotFrameworkSkill;
import com.microsoft.bot.builder.TypedInvokeResponse;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryBase;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryOptions;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;

/**
 * A {@link BotFrameworkHttpClient} specialized for Skills that encapsulates
 * Conversation ID generation.
 */
public class SkillHttpClient extends BotFrameworkHttpClient {

    private final SkillConversationIdFactoryBase conversationIdFactory;

    /**
     * Initializes a new instance of the {@link SkillHttpClient} class.
     *
     * @param credentialProvider    An instance of {@link CredentialProvider}.
     * @param conversationIdFactory An instance of a class derived from
     *                              {@link SkillConversationIdFactoryBase}.
     * @param channelProvider       An instance of {@link ChannelProvider}.
     */
    public SkillHttpClient(CredentialProvider credentialProvider, SkillConversationIdFactoryBase conversationIdFactory,
            ChannelProvider channelProvider) {
        super(credentialProvider, channelProvider);
        this.conversationIdFactory = conversationIdFactory;
    }

    /**
     * Uses the SkillConversationIdFactory to create or retrieve a Skill
     * Conversation Id, and sends the activity.
     *
     * @param originatingAudience The oauth audience scope, used during token
     *                            retrieval. (Either
     *                            https://api.getbotframework().com or bot app id.)
     * @param fromBotId           The MicrosoftAppId of the bot sending the
     *                            activity.
     * @param toSkill             The skill to create the conversation Id for.
     * @param callbackUrl         The callback Url for the skill host.
     * @param activity            The activity to send.
     * @param type                Type of T required due to type erasure of generics
     *                            in Java.
     * @param <T>                 Type of expected TypedInvokeResponse.
     *
     * @return task with invokeResponse.
     */
    public <T extends Object> CompletableFuture<TypedInvokeResponse<T>> postActivity(String originatingAudience,
            String fromBotId, BotFrameworkSkill toSkill, URI callbackUrl, Activity activity, Class<T> type) {
        return getSkillConversationId(originatingAudience, fromBotId, toSkill, activity)
                .thenCompose(skillConversationId -> {
                    return postActivity(fromBotId, toSkill.getAppId(), toSkill.getSkillEndpoint(), callbackUrl,
                            skillConversationId, activity, type);
                });

    }

    private CompletableFuture<String> getSkillConversationId(String originatingAudience, String fromBotId,
            BotFrameworkSkill toSkill, Activity activity) {
        try {
            SkillConversationIdFactoryOptions options = new SkillConversationIdFactoryOptions();
            options.setFromBotOAuthScope(originatingAudience);
            options.setFromBotId(fromBotId);
            options.setActivity(activity);
            options.setBotFrameworkSkill(toSkill);
            return conversationIdFactory.createSkillConversationId(options);
        } catch (Exception ex) {
            // Attempt to create the ID using deprecated method.
            return conversationIdFactory.createSkillConversationId(activity.getConversationReference());
        }
    }

    /**
     * Forwards an activity to a skill (bot).
     *
     * @param fromBotId   The MicrosoftAppId of the bot sending the activity.
     * @param toSkill     An instance of {@link BotFrameworkSkill} .
     * @param callbackUrl The callback Uri.
     * @param activity    activity to forward.
     * @param type        type of T
     * @param <T>         Type of expected TypedInvokeResponse.
     *
     * @return task with optional invokeResponse of type T.
     */
    public <T extends Object> CompletableFuture<TypedInvokeResponse<T>> postActivity(String fromBotId,
            BotFrameworkSkill toSkill, URI callbackUrl, Activity activity, Class<T> type) {
        String originatingAudience = getChannelProvider() != null && getChannelProvider().isGovernment()
                ? GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE
                : AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE;
        return postActivity(originatingAudience, fromBotId, toSkill, callbackUrl, activity, type);
    }
}
