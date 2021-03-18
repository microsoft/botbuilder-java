// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.ChannelServiceHandler;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.CallerIdConstants;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Bot Framework Handler for skills.
 */
public class SkillHandler extends ChannelServiceHandler {

    /**
     * The skill conversation reference.
     */
    public static final String SKILL_CONVERSATION_REFERENCE_KEY =
                "com.microsoft.bot.builder.skills.SkillConversationReference";

    private final BotAdapter adapter;
    private final Bot bot;
    private final SkillConversationIdFactoryBase conversationIdFactory;

    /**
     * The slf4j Logger to use. Note that slf4j is configured by providing Log4j
     * dependencies in the POM, and corresponding Log4j configuration in the
     * 'resources' folder.
     */
    private Logger logger = LoggerFactory.getLogger(SkillHandler.class);

    /**
     * Initializes a new instance of the {@link SkillHandler} class, using a
     * credential provider.
     *
     * @param adapter                An instance of the {@link BotAdapter} that will handle the request.
     * @param bot                    The {@link IBot} instance.
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

        this.adapter = adapter;
        this.bot = bot;
        this.conversationIdFactory = conversationIdFactory;
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
     * @return   task for a resource response.
     */
    @Override
    protected CompletableFuture<ResourceResponse> onSendToConversation(
                ClaimsIdentity claimsIdentity,
                String conversationId,
                Activity activity) {
        return processActivity(claimsIdentity, conversationId, null, activity);
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
     * @return   task for a resource response.
     */
    @Override
    protected CompletableFuture<ResourceResponse> onReplyToActivity(
                    ClaimsIdentity claimsIdentity,
                    String conversationId,
                    String activityId,
                    Activity activity) {
        return processActivity(claimsIdentity, conversationId, activityId, activity);
    }

    /**
     */
    @Override
    protected CompletableFuture<Void> onDeleteActivity(
                        ClaimsIdentity claimsIdentity,
                        String conversationId,
                        String activityId) {

        SkillConversationReference skillConversationReference =  getSkillConversationReference(conversationId).join();

        BotCallbackHandler callback = turnContext -> {
            turnContext.getTurnState().add(SKILL_CONVERSATION_REFERENCE_KEY, skillConversationReference);
            return turnContext.deleteActivity(activityId);
        };

        return adapter.continueConversation(claimsIdentity,
                                             skillConversationReference.getConversationReference(),
                                             skillConversationReference.getOAuthScope(),
                                             callback);
    }

    /**
     */
    @Override
    protected CompletableFuture<ResourceResponse> onUpdateActivity(
                    ClaimsIdentity claimsIdentity,
                    String conversationId,
                    String activityId,
                    Activity activity) {
        SkillConversationReference skillConversationReference =  getSkillConversationReference(conversationId).join();

        AtomicReference<ResourceResponse> resourceResponse = new AtomicReference<ResourceResponse>();

        BotCallbackHandler callback = turnContext -> {
            turnContext.getTurnState().add(SKILL_CONVERSATION_REFERENCE_KEY, skillConversationReference);
            activity.applyConversationReference(skillConversationReference.getConversationReference());
            turnContext.getActivity().setId(activityId);
            String callerId = String.format("%s%s",
                                            CallerIdConstants.BOT_TO_BOT_PREFIX,
                                            JwtTokenValidation.getAppIdFromClaims(claimsIdentity.claims()));
            turnContext.getActivity().setCallerId(callerId);
            resourceResponse.set(turnContext.updateActivity(activity).join());
            return CompletableFuture.completedFuture(null);
        };

        adapter.continueConversation(claimsIdentity,
                                      skillConversationReference.getConversationReference(),
                                      skillConversationReference.getOAuthScope(),
                                      callback);

        if (resourceResponse.get() != null) {
            return CompletableFuture.completedFuture(resourceResponse.get());
        } else {
            return CompletableFuture.completedFuture(new ResourceResponse(UUID.randomUUID().toString()));
        }
    }

    private static void applyEoCToTurnContextActivity(TurnContext turnContext, Activity endOfConversationActivity) {
        // transform the turnContext.Activity to be the EndOfConversation.
        turnContext.getActivity().setType(endOfConversationActivity.getType());
        turnContext.getActivity().setText(endOfConversationActivity.getText());
        turnContext.getActivity().setCode(endOfConversationActivity.getCode());

        turnContext.getActivity().setReplyToId(endOfConversationActivity.getReplyToId());
        turnContext.getActivity().setValue(endOfConversationActivity.getValue());
        turnContext.getActivity().setEntities(endOfConversationActivity.getEntities());
        turnContext.getActivity().setLocale(endOfConversationActivity.getLocale());
        turnContext.getActivity().setLocalTimestamp(endOfConversationActivity.getLocalTimestamp());
        turnContext.getActivity().setTimestamp(endOfConversationActivity.getTimestamp());
        turnContext.getActivity().setChannelData(endOfConversationActivity.getChannelData());
        for (Map.Entry<String, JsonNode> entry : endOfConversationActivity.getProperties().entrySet()) {
            turnContext.getActivity().setProperties(entry.getKey(), entry.getValue());
        }
    }

    private static void applyEventToTurnContextActivity(TurnContext turnContext, Activity eventActivity) {
        // transform the turnContext.Activity to be the EventActivity.
        turnContext.getActivity().setType(eventActivity.getType());
        turnContext.getActivity().setName(eventActivity.getName());
        turnContext.getActivity().setValue(eventActivity.getValue());
        turnContext.getActivity().setRelatesTo(eventActivity.getRelatesTo());

        turnContext.getActivity().setReplyToId(eventActivity.getReplyToId());
        turnContext.getActivity().setValue(eventActivity.getValue());
        turnContext.getActivity().setEntities(eventActivity.getEntities());
        turnContext.getActivity().setLocale(eventActivity.getLocale());
        turnContext.getActivity().setLocalTimestamp(eventActivity.getLocalTimestamp());
        turnContext.getActivity().setTimestamp(eventActivity.getTimestamp());
        turnContext.getActivity().setChannelData(eventActivity.getChannelData());
        for (Map.Entry<String, JsonNode> entry : eventActivity.getProperties().entrySet()) {
            turnContext.getActivity().setProperties(entry.getKey(), entry.getValue());
        }
    }

    private CompletableFuture<SkillConversationReference> getSkillConversationReference(String conversationId) {

        SkillConversationReference skillConversationReference;
        try {
            skillConversationReference =  conversationIdFactory.getSkillConversationReference(conversationId).join();
        } catch (NotImplementedException ex) {
            if (logger != null) {
                logger.warn("Got NotImplementedException when trying to call "
                            + "GetSkillConversationReference() on the ConversationIdFactory,"
                            + " attempting to use deprecated GetConversationReference() method instead.");
            }

            // Attempt to get SkillConversationReference using deprecated method.
            // this catch should be removed once we remove the deprecated method.
            // We need to use the deprecated method for backward compatibility.
            ConversationReference conversationReference =
                    conversationIdFactory.getConversationReference(conversationId).join();
            skillConversationReference = new SkillConversationReference();
            skillConversationReference.setConversationReference(conversationReference);
            if (getChannelProvider() != null && getChannelProvider().isGovernment()) {
                skillConversationReference.setOAuthScope(
                        GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);
            } else {
                skillConversationReference.setOAuthScope(
                        AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);
            }
        }

        if (skillConversationReference == null) {
            if (logger != null) {
                logger.warn(
                    String.format("Unable to get skill conversation reference for conversationId %s.", conversationId)
                );
            }
            throw new RuntimeException("Key not found");
        }

        return CompletableFuture.completedFuture(skillConversationReference);
    }

    private CompletableFuture<ResourceResponse> processActivity(
                    ClaimsIdentity claimsIdentity,
                    String conversationId,
                    String replyToActivityId,
                    Activity activity) {

        SkillConversationReference skillConversationReference = getSkillConversationReference(conversationId).join();

        AtomicReference<ResourceResponse> resourceResponse = new AtomicReference<ResourceResponse>();

        BotCallbackHandler callback = turnContext -> {
            turnContext.getTurnState().add(SKILL_CONVERSATION_REFERENCE_KEY, skillConversationReference);
            activity.applyConversationReference(skillConversationReference.getConversationReference());
            turnContext.getActivity().setId(replyToActivityId);
            String callerId = String.format("%s%s",
                                            CallerIdConstants.BOT_TO_BOT_PREFIX,
                                            JwtTokenValidation.getAppIdFromClaims(claimsIdentity.claims()));
            turnContext.getActivity().setCallerId(callerId);

            switch (activity.getType()) {
                case ActivityTypes.END_OF_CONVERSATION:
                     conversationIdFactory.deleteConversationReference(conversationId).join();
                    applyEoCToTurnContextActivity(turnContext, activity);
                     bot.onTurn(turnContext).join();
                    break;
                case ActivityTypes.EVENT:
                    applyEventToTurnContextActivity(turnContext, activity);
                     bot.onTurn(turnContext).join();
                    break;
                default:
                    resourceResponse.set(turnContext.sendActivity(activity).join());
                    break;
            }
            return CompletableFuture.completedFuture(null);
        };

         adapter.continueConversation(claimsIdentity,
                                       skillConversationReference.getConversationReference(),
                                       skillConversationReference.getOAuthScope(),
                                       callback).join();

        if (resourceResponse.get() != null) {
            return CompletableFuture.completedFuture(resourceResponse.get());
        } else {
            return CompletableFuture.completedFuture(new ResourceResponse(UUID.randomUUID().toString()));
        }
    }
}

