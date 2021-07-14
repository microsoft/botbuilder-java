// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.CallerIdConstants;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * This class inherited all the implementations of {@link SkillHandler}
 * class since we needed similar code for {@link CloudSkillHandler}.
 * The {@link CloudSkillHandler} class differs from {@link SkillHandler}
 * class only in authentication by making use of {@link BotFrameworkAuthentication} class.
 * This class is internal since it is only used in skill handler classes.
 */
public class SkillHandlerImpl {

    private final String skillConversationReferenceKey;
    private final BotAdapter adapter;
    private final Bot bot;
    private final SkillConversationIdFactoryBase conversationIdFactory;
    private final Supplier<String> getOAuthScope;
    private Logger logger = LoggerFactory.getLogger(SkillHandlerImpl.class);

    public SkillHandlerImpl(
        String withSkillConversationReferenceKey,
        BotAdapter withAdapter,
        Bot withBot,
        SkillConversationIdFactoryBase withConversationIdFactory,
        Supplier<String> withGetOAuthScope) {
        if (StringUtils.isBlank(withSkillConversationReferenceKey)) {
            throw new IllegalArgumentException("skillConversationReferenceKey cannot be null");
        }
        if (withAdapter == null) {
            throw new IllegalArgumentException("adapter cannot be null");
        }

        if (withBot == null) {
            throw new IllegalArgumentException("bot cannot be null");
        }

        if (withConversationIdFactory == null) {
            throw new IllegalArgumentException("withConversationIdFactory cannot be null");
        }

        this.skillConversationReferenceKey = withSkillConversationReferenceKey;
        this.adapter = withAdapter;
        this.bot = withBot;
        this.conversationIdFactory = withConversationIdFactory;
        this.getOAuthScope = withGetOAuthScope;
    }

    /**
     *
     * @param claimsIdentity claimsIdentity for the bot, should have
     *                       AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId conversationId.
     * @param activity       Activity to send.
     *
     * @return Task for a resource response.
     */
    public CompletableFuture<ResourceResponse> onSendToConversation(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        Activity activity) {
        return processActivity(claimsIdentity, conversationId, null, activity);
    }

    /**
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation ID.
     * @param activityId      activityId the reply is to (OPTIONAL).
     * @param activity        Activity to send.
     *
     * @return Task for a resource response.
     */
    public CompletableFuture<ResourceResponse> onReplyToActivity(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String activityId,
        Activity activity) {
        return processActivity(claimsIdentity, conversationId, activityId, activity);
    }

    /**
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation ID.
     * @param activityId      activityId to delete.
     *
     * @return Task with void value.
     */
    public CompletableFuture<Void> onDeleteActivity(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String activityId) {

        SkillConversationReference skillConversationReference = getSkillConversationReference(conversationId).join();

        BotCallbackHandler callback = turnContext -> {
            turnContext.getTurnState().add(this.skillConversationReferenceKey, skillConversationReference);
            return turnContext.deleteActivity(activityId);
        };

        return adapter.continueConversation(claimsIdentity,
            skillConversationReference.getConversationReference(),
            skillConversationReference.getOAuthScope(),
            callback);
    }

    /**
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation ID.
     * @param activityId      activityId the reply is to (OPTIONAL).
     * @param activity        Activity to send.
     *
     * @return Task for a resource response.
     */
    public CompletableFuture<ResourceResponse> onUpdateActivity(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String activityId,
        Activity activity) {
        SkillConversationReference skillConversationReference = getSkillConversationReference(conversationId).join();

        AtomicReference<ResourceResponse> resourceResponse = new AtomicReference<ResourceResponse>();

        BotCallbackHandler callback = turnContext -> {
            turnContext.getTurnState().add(this.skillConversationReferenceKey, skillConversationReference);
            activity.applyConversationReference(skillConversationReference.getConversationReference());
            turnContext.getActivity().setId(activityId);
            String callerId = String.format("%s%s",
                CallerIdConstants.BOT_TO_BOT_PREFIX,
                JwtTokenValidation.getAppIdFromClaims(claimsIdentity.claims()));
            turnContext.getActivity().setCallerId(callerId);
            resourceResponse.set(turnContext.updateActivity(activity).join());
            return CompletableFuture.completedFuture(null);
        };

        this.adapter.continueConversation(claimsIdentity,
            skillConversationReference.getConversationReference(),
            skillConversationReference.getOAuthScope(),
            callback);

        if (resourceResponse.get() != null) {
            return CompletableFuture.completedFuture(resourceResponse.get());
        } else {
            return CompletableFuture.completedFuture(new ResourceResponse(UUID.randomUUID().toString()));
        }
    }

    private static void applySkillActivityToTurnContext(TurnContext turnContext, Activity activity) {
        // adapter.ContinueConversation() sends an event activity with ContinueConversation in the name.
        // this warms up the incoming middlewares but once that's done and we hit the custom callback,
        // we need to swap the values back to the ones received from the skill so the bot gets the actual activity.
        turnContext.getActivity().setChannelData(activity.getChannelData());
        turnContext.getActivity().setCode(activity.getCode());
        turnContext.getActivity().setEntities(activity.getEntities());
        turnContext.getActivity().setLocale(activity.getLocale());
        turnContext.getActivity().setLocalTimestamp(activity.getLocalTimestamp());
        turnContext.getActivity().setName(activity.getName());
        for (Map.Entry<String, JsonNode> entry : activity.getProperties().entrySet()) {
            turnContext.getActivity().setProperties(entry.getKey(), entry.getValue());
        }
        turnContext.getActivity().setRelatesTo(activity.getRelatesTo());
        turnContext.getActivity().setReplyToId(activity.getReplyToId());
        turnContext.getActivity().setTimestamp(activity.getTimestamp());
        turnContext.getActivity().setText(activity.getText());
        turnContext.getActivity().setType(activity.getType());
        turnContext.getActivity().setValue(activity.getValue());
    }

    private CompletableFuture<SkillConversationReference> getSkillConversationReference(String conversationId) {

        SkillConversationReference skillConversationReference;
        try {
            skillConversationReference = this.conversationIdFactory.
                getSkillConversationReference(conversationId).join();
        } catch (NotImplementedException ex) {
            if (this.logger != null) {
                this.logger.warn("Got NotImplementedException when trying to call "
                    + "GetSkillConversationReference() on the ConversationIdFactory,"
                    + " attempting to use deprecated GetConversationReference() method instead.");
            }

            // Attempt to get SkillConversationReference using deprecated method.
            // this catch should be removed once we remove the deprecated method.
            // We need to use the deprecated method for backward compatibility.
            ConversationReference conversationReference =
                this.conversationIdFactory.getConversationReference(conversationId).join();
            skillConversationReference = new SkillConversationReference();
            skillConversationReference.setConversationReference(conversationReference);
            skillConversationReference.setOAuthScope(this.getOAuthScope.get());
        }

        if (skillConversationReference == null) {
            if (this.logger != null) {
                this.logger.warn(
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
            turnContext.getTurnState().add(this.skillConversationReferenceKey, skillConversationReference);
            activity.applyConversationReference(skillConversationReference.getConversationReference());
            turnContext.getActivity().setId(replyToActivityId);
            String callerId = String.format("%s%s",
                CallerIdConstants.BOT_TO_BOT_PREFIX,
                JwtTokenValidation.getAppIdFromClaims(claimsIdentity.claims()));
            turnContext.getActivity().setCallerId(callerId);

            switch (activity.getType()) {
                case ActivityTypes.END_OF_CONVERSATION:
                    this.conversationIdFactory.deleteConversationReference(conversationId).join();
                    this.sendToBot(activity, turnContext);
                    break;
                case ActivityTypes.EVENT:
                    this.sendToBot(activity, turnContext);
                    break;
                case ActivityTypes.COMMAND:
                case ActivityTypes.COMMAND_RESULT:
                    if (activity.getName().startsWith("application/")) {
                        // Send to channel and capture the resource response
                        // for the SendActivityCall so we can return it.
                        resourceResponse.set(turnContext.sendActivity(activity).join());
                    } else {
                        this.sendToBot(activity, turnContext);
                    }

                    break;
                default:
                    // Capture the resource response for the SendActivityCall so we can return it.
                    resourceResponse.set(turnContext.sendActivity(activity).join());
                    break;
            }
            return CompletableFuture.completedFuture(null);
        };

        this.adapter.continueConversation(claimsIdentity,
            skillConversationReference.getConversationReference(),
            skillConversationReference.getOAuthScope(),
            callback).join();

        if (resourceResponse.get() != null) {
            return CompletableFuture.completedFuture(resourceResponse.get());
        } else {
            return CompletableFuture.completedFuture(new ResourceResponse(UUID.randomUUID().toString()));
        }
    }

    private CompletableFuture<Void> sendToBot(Activity activity, TurnContext turnContext) {
        this.applySkillActivityToTurnContext(turnContext, activity);
        this.bot.onTurn(turnContext).join();
        return CompletableFuture.completedFuture(null);
    }
}
