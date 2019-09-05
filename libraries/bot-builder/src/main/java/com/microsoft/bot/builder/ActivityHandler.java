// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
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
    public CompletableFuture<Void> onTurnAsync(TurnContext turnContext) {
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
            case MESSAGE:
                return onMessageActivityAsync(turnContext);
            case CONVERSATION_UPDATE:
                return onConversationUpdateActivityAsync(turnContext);
            case MESSAGE_REACTION:
                return onMessageReactionActivityAsync(turnContext);
            case EVENT:
                return onEventActivityAsync(turnContext);

            default:
                return onUnrecognizedActivityAsync(turnContext);
        }
    }

    /**
     * Invoked when a message activity is received from the user when the base behavior of
     * {@link #onTurnAsync(TurnContext)} is used.
     *
     * If overridden, this could potentially contain conversational logic.
     * By default, this method does nothing.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> onMessageActivityAsync(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a conversation update activity is received from the channel when the base behavior of
     * {@link #onTurnAsync(TurnContext)} is used.
     *
     * Conversation update activities are useful when it comes to responding to users being added to or removed
     * from the conversation.
     *
     * For example, a bot could respond to a user being added by greeting the user.
     * By default, this method will call {@link #onMembersAddedAsync(List, TurnContext)} if any users have been added,
     * or {@link #onMembersRemovedAsync(List, TurnContext)} if any users have been removed. The method checks the member
     * ID so that it only responds to updates regarding members other than the bot itself.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> onConversationUpdateActivityAsync(TurnContext turnContext) {
        if (turnContext.getActivity().getMembersAdded() != null) {
            if (turnContext.getActivity().getMembersAdded().stream()
                .anyMatch(m -> StringUtils.equals(m.getId(), turnContext.getActivity().getId()))) {

                return onMembersAddedAsync(turnContext.getActivity().getMembersAdded(), turnContext);
            }
        } else if (turnContext.getActivity().getMembersRemoved() != null) {
            if (turnContext.getActivity().getMembersRemoved().stream()
                .anyMatch(m -> StringUtils.equals(m.getId(), turnContext.getActivity().getId()))) {

                return onMembersRemovedAsync(turnContext.getActivity().getMembersRemoved(), turnContext);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when members other than this bot (like a user) are added to the conversation when the base behavior of
     * {@link #onConversationUpdateActivityAsync(TurnContext)} is used.
     *
     * If overridden, this could potentially send a greeting message to the user instead of waiting for the user to
     * send a message first.
     *
     * By default, this method does nothing.
     *
     * @param membersAdded A list of all the users that have been added in the conversation update.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMembersAddedAsync(List<ChannelAccount> membersAdded, TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when members other than this bot (like a user) are removed from the conversation when the base
     * behavior of {@link #onConversationUpdateActivityAsync(TurnContext)} is used.
     *
     * This method could optionally be overridden to perform actions related to users leaving a group conversation.
     *
     * By default, this method does nothing.
     *
     * @param membersRemoved A list of all the users that have been removed in the conversation update.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMembersRemovedAsync(List<ChannelAccount> membersRemoved, TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event activity is received from the connector when the base behavior of
     * {@link #onTurnAsync(TurnContext)} is used.
     *
     * Message reactions correspond to the user adding a 'like' or 'sad' etc. (often an emoji) to a
     * previously sent activity. Message reactions are only supported by a few channels.
     *
     * The activity that the message reaction corresponds to is indicated in the replyToId property.
     * The value of this property is the activity id of a previously sent activity given back to the
     * bot as the response from a send call.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture onMessageReactionActivityAsync(TurnContext turnContext) {
        CompletableFuture task = null;

        if (turnContext.getActivity().getReactionsAdded() != null) {
            task = onReactionsAddedAsync(turnContext.getActivity().getReactionsAdded(), turnContext);
        }

        if (turnContext.getActivity().getReactionsRemoved() != null) {
            if (task != null) {
                task.thenApply((result) -> onReactionsRemovedAsync(
                    turnContext.getActivity().getReactionsRemoved(), turnContext));
            } else {
                task = onReactionsRemovedAsync(turnContext.getActivity().getReactionsRemoved(), turnContext);
            }
        }

        return task == null ? CompletableFuture.completedFuture(null) : task;
    }

    /**
     * Called when there have been Reactions added that reference a previous Activity.
     *
     * @param messageReactions The list of reactions added.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture onReactionsAddedAsync(List<MessageReaction> messageReactions,
                                                      TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when there have been Reactions removed that reference a previous Activity.
     *
     * @param messageReactions The list of reactions removed.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture onReactionsRemovedAsync(List<MessageReaction> messageReactions,
                                                        TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event activity is received from the connector when the base behavior of
     * {@link #onTurnAsync(TurnContext)} is used.
     *
     * Event activities can be used to communicate many different things.
     *
     * By default, this method will call {@link #onTokenResponseEventAsync(TurnContext)} if the
     * activity's name is "tokens/response" or {@link #onEventAsync(TurnContext)} otherwise.
     * "tokens/response" event can be triggered by an {@link com.microsoft.bot.schema.OAuthCard}.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture onEventActivityAsync(TurnContext turnContext) {
        if (StringUtils.equals(turnContext.getActivity().getName(), "tokens/response")) {
            return onTokenResponseEventAsync(turnContext);
        }

        return onEventAsync(turnContext);
    }

    /**
     * Invoked when a "tokens/response" event is received when the base behavior of
     * {@link #onEventActivityAsync(TurnContext)} is used.
     *
     * If using an OAuthPrompt, override this method to forward this {@link Activity} to the current dialog.
     *
     * By default, this method does nothing.
     *
     * @param turnContext
     * @return
     */
    protected CompletableFuture onTokenResponseEventAsync(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event other than <c>tokens/response</c> is received when the base behavior of
     * {@link #onEventActivityAsync(TurnContext)} is used.
     *
     * This method could optionally be overridden if the bot is meant to handle miscellaneous events.
     *
     * By default, this method does nothing.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture onEventAsync(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an activity other than a message, conversation update, or event is received when the base behavior of
     * {@link #onTurnAsync(TurnContext)} is used.
     *
     * If overridden, this could potentially respond to any of the other activity types like
     * {@link com.microsoft.bot.schema.ActivityTypes#CONTACT_RELATION_UPDATE} or
     * {@link com.microsoft.bot.schema.ActivityTypes#END_OF_CONVERSATION}.
     *
     * By default, this method does nothing.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture onUnrecognizedActivityAsync(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }
}
