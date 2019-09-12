// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.MessageReaction;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of the IBot interface intended for further subclassing.
 * Derive from this class to plug in code to handle particular Activity types.
 * Pre and post processing of Activities can be plugged in by deriving and calling
 * the base class implementation.
 */
public class ActivityHandler implements Bot {
    /**
     * The OnTurnAsync function is called by the Adapter (for example, the {@link BotFrameworkAdapter} at
     * runtime in order to process an inbound Activity.
     *
     * @param turnContext The context object for this turn. Provides information about the
     *                    incoming activity, and other data needed to process the activity.
     * @return
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        if (turnContext == null) {
            throw new IllegalArgumentException("TurnContext cannot be null.");
        }

        if (turnContext.getActivity() == null) {
            throw new IllegalArgumentException("turnContext must have a non-null Activity.");
        }

        if (turnContext.getActivity().getType() == null) {
            throw new IllegalArgumentException("turnContext.getActivity must have a non-null Type.");
        }

        switch (turnContext.getActivity().getType()) {
            case ActivityTypes.MESSAGE:
                return onMessageActivity(turnContext);
            case ActivityTypes.CONVERSATION_UPDATE:
                return onConversationUpdateActivity(turnContext);
            case ActivityTypes.MESSAGE_REACTION:
                return onMessageReactionActivity(turnContext);
            case ActivityTypes.EVENT:
                return onEventActivity(turnContext);

            default:
                return onUnrecognizedActivityType(turnContext);
        }
    }

    /**
     * Invoked when a message activity is received from the user when the base behavior of
     * {@link #onTurn(TurnContext)} is used.
     * <p>
     * If overridden, this could potentially contain conversational logic.
     * By default, this method does nothing.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a conversation update activity is received from the channel when the base behavior of
     * {@link #onTurn(TurnContext)} is used.
     * <p>
     * Conversation update activities are useful when it comes to responding to users being added to or removed
     * from the conversation.
     * <p>
     * For example, a bot could respond to a user being added by greeting the user.
     * By default, this method will call {@link #onMembersAdded(List, TurnContext)} if any users have been added,
     * or {@link #onMembersRemoved(List, TurnContext)} if any users have been removed. The method checks the member
     * ID so that it only responds to updates regarding members other than the bot itself.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        Activity activity = turnContext.getActivity();

        if (activity.getMembersAdded() != null) {
            if (activity.getRecipient() != null && activity.getMembersAdded().stream()
                .anyMatch(m -> !StringUtils.equals(m.getId(), activity.getRecipient().getId()))) {

                return onMembersAdded(activity.getMembersAdded(), turnContext);
            }
        } else if (activity.getRecipient() != null && activity.getMembersRemoved() != null) {
            if (activity.getMembersRemoved().stream()
                .anyMatch(m -> !StringUtils.equals(m.getId(), activity.getRecipient().getId()))) {

                return onMembersRemoved(activity.getMembersRemoved(), turnContext);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when members other than this bot (like a user) are added to the conversation when the base behavior of
     * {@link #onConversationUpdateActivity(TurnContext)} is used.
     * <p>
     * If overridden, this could potentially send a greeting message to the user instead of waiting for the user to
     * send a message first.
     * <p>
     * By default, this method does nothing.
     *
     * @param membersAdded A list of all the users that have been added in the conversation update.
     * @param turnContext  The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when members other than this bot (like a user) are removed from the conversation when the base
     * behavior of {@link #onConversationUpdateActivity(TurnContext)} is used.
     * <p>
     * This method could optionally be overridden to perform actions related to users leaving a group conversation.
     * <p>
     * By default, this method does nothing.
     *
     * @param membersRemoved A list of all the users that have been removed in the conversation update.
     * @param turnContext    The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMembersRemoved(List<ChannelAccount> membersRemoved, TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event activity is received from the connector when the base behavior of
     * {@link #onTurn(TurnContext)} is used.
     * <p>
     * Message reactions correspond to the user adding a 'like' or 'sad' etc. (often an emoji) to a
     * previously sent activity. Message reactions are only supported by a few channels.
     * <p>
     * The activity that the message reaction corresponds to is indicated in the replyToId property.
     * The value of this property is the activity id of a previously sent activity given back to the
     * bot as the response from a send call.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMessageReactionActivity(TurnContext turnContext) {
        CompletableFuture task = null;

        if (turnContext.getActivity().getReactionsAdded() != null) {
            task = onReactionsAdded(turnContext.getActivity().getReactionsAdded(), turnContext);
        }

        if (turnContext.getActivity().getReactionsRemoved() != null) {
            if (task != null) {
                task.thenApply((result) -> onReactionsRemoved(
                    turnContext.getActivity().getReactionsRemoved(), turnContext));
            } else {
                task = onReactionsRemoved(turnContext.getActivity().getReactionsRemoved(), turnContext);
            }
        }

        return task == null ? CompletableFuture.completedFuture(null) : task;
    }

    /**
     * Called when there have been Reactions added that reference a previous Activity.
     *
     * @param messageReactions The list of reactions added.
     * @param turnContext      The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onReactionsAdded(List<MessageReaction> messageReactions,
                                                      TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when there have been Reactions removed that reference a previous Activity.
     *
     * @param messageReactions The list of reactions removed.
     * @param turnContext      The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onReactionsRemoved(List<MessageReaction> messageReactions,
                                                        TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event activity is received from the connector when the base behavior of
     * {@link #onTurn(TurnContext)} is used.
     * <p>
     * Event activities can be used to communicate many different things.
     * <p>
     * By default, this method will call {@link #onTokenResponseEvent(TurnContext)} if the
     * activity's name is "tokens/response" or {@link #onEvent(TurnContext)} otherwise.
     * "tokens/response" event can be triggered by an {@link com.microsoft.bot.schema.OAuthCard}.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onEventActivity(TurnContext turnContext) {
        if (StringUtils.equals(turnContext.getActivity().getName(), "tokens/response")) {
            return onTokenResponseEvent(turnContext);
        }

        return onEvent(turnContext);
    }

    /**
     * Invoked when a "tokens/response" event is received when the base behavior of
     * {@link #onEventActivity(TurnContext)} is used.
     * <p>
     * If using an OAuthPrompt, override this method to forward this {@link Activity} to the current dialog.
     * <p>
     * By default, this method does nothing.
     *
     * @param turnContext
     * @return
     */
    protected CompletableFuture<Void> onTokenResponseEvent(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event other than tokens/response is received when the base behavior of
     * {@link #onEventActivity(TurnContext)} is used.
     * <p>
     * This method could optionally be overridden if the bot is meant to handle miscellaneous events.
     * <p>
     * By default, this method does nothing.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onEvent(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an activity other than a message, conversation update, or event is received when the base behavior of
     * {@link #onTurn(TurnContext)} is used.
     * <p>
     * If overridden, this could potentially respond to any of the other activity types like
     * {@link com.microsoft.bot.schema.ActivityTypes#CONTACT_RELATION_UPDATE} or
     * {@link com.microsoft.bot.schema.ActivityTypes#END_OF_CONVERSATION}.
     * <p>
     * By default, this method does nothing.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onUnrecognizedActivityType(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }
}
