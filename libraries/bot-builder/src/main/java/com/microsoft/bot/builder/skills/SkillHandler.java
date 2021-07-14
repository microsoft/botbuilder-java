// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.ChannelServiceHandler;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ResourceResponse;

/**
 * A Bot Framework Handler for skills.
 */
public class SkillHandler extends ChannelServiceHandler {

    /**
     * The skill conversation reference.
     */
    public static final String SKILL_CONVERSATION_REFERENCE_KEY =
                "com.microsoft.bot.builder.skills.SkillConversationReference";

    // Delegate that implements actual logic
    private final SkillHandlerImpl inner;

    /**
     * Initializes a new instance of the {@link SkillHandler} class, using a
     * credential provider.
     *
     * @param adapter                An instance of the {@link BotAdapter} that will handle the request.
     * @param bot                    The {@link Bot} instance.
     * @param conversationIdFactory  A {@link SkillConversationIdFactoryBase} to unpack the conversation ID and
     *                               map it to the calling bot.
     * @param credentialProvider     The credential provider.
     * @param authConfig             The authentication configuration.
     * @param channelProvider        The channel provider.
     *
     * Use a {@link MiddlewareSet} Object to add multiple middleware components
     * in the constructor. Use the Use({@link Middleware} ) method to add
     * additional middleware to the adapter after construction.
     */
    public SkillHandler(
        BotAdapter adapter,
        Bot bot,
        SkillConversationIdFactoryBase conversationIdFactory,
        CredentialProvider credentialProvider,
        AuthenticationConfiguration authConfig,
        ChannelProvider channelProvider
    ) {

        super(credentialProvider, authConfig, channelProvider);

        if (adapter == null) {
            throw new IllegalArgumentException("adapter cannot be null");
        }

        if (bot == null) {
            throw new IllegalArgumentException("bot cannot be null");
        }

        if (conversationIdFactory == null) {
            throw new IllegalArgumentException("conversationIdFactory cannot be null");
        }

        this.inner = new SkillHandlerImpl(
            SKILL_CONVERSATION_REFERENCE_KEY,
            adapter,
            bot,
            conversationIdFactory,
            () -> this.getChannelProvider() != null && this.getChannelProvider().isGovernment()
                ? GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE
                : AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);
    }

    /**
     * SendToConversation() API for Skill.
     *
     * This method allows you to send an activity to the end of a conversation.
     * This is slightly different from ReplyToActivity(). *
     * SendToConversation(conversationId) - will append the activity to the end
     * of the conversation according to the timestamp or semantics of the
     * channel. * ReplyToActivity(conversationId,ActivityId) - adds the
     * activity as a reply to another activity, if the channel supports it. If
     * the channel does not support nested replies, ReplyToActivity falls back
     * to SendToConversation. Use ReplyToActivity when replying to a specific
     * activity in the conversation. Use SendToConversation in all other cases.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  conversationId.
     * @param activity        Activity to send.
     *
     * @return Task for a resource response.
     */
    @Override
    protected CompletableFuture<ResourceResponse> onSendToConversation(
                ClaimsIdentity claimsIdentity,
                String conversationId,
                Activity activity) {
        return inner.onSendToConversation(claimsIdentity, conversationId, activity);
    }

    /**
     * ReplyToActivity() API for Skill.
     *
     * This method allows you to reply to an activity. This is slightly
     * different from SendToConversation(). *
     * SendToConversation(conversationId) - will append the activity to the end
     * of the conversation according to the timestamp or semantics of the
     * channel. * ReplyToActivity(conversationId,ActivityId) - adds the
     * activity as a reply to another activity, if the channel supports it. If
     * the channel does not support nested replies, ReplyToActivity falls back
     * to SendToConversation. Use ReplyToActivity when replying to a specific
     * activity in the conversation. Use SendToConversation in all other cases.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation ID.
     * @param activityId      activityId the reply is to (OPTIONAL).
     * @param activity        Activity to send.
     *
     * @return Task for a resource response.
     */
    @Override
    protected CompletableFuture<ResourceResponse> onReplyToActivity(
                    ClaimsIdentity claimsIdentity,
                    String conversationId,
                    String activityId,
                    Activity activity) {
        return inner.onReplyToActivity(claimsIdentity, conversationId, activityId, activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompletableFuture<Void> onDeleteActivity(
                        ClaimsIdentity claimsIdentity,
                        String conversationId,
                        String activityId) {
        return inner.onDeleteActivity(claimsIdentity, conversationId, activityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompletableFuture<ResourceResponse> onUpdateActivity(
                    ClaimsIdentity claimsIdentity,
                    String conversationId,
                    String activityId,
                    Activity activity) {
        return inner.onUpdateActivity(claimsIdentity, conversationId, activityId, activity);
    }
}

