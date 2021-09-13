// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.connector.Async;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.AdaptiveCardInvokeResponse;
import com.microsoft.bot.schema.AdaptiveCardInvokeValue;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.MessageReaction;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.SignInConstants;

/**
 * An implementation of the {@link Bot} interface intended for further
 * subclassing. Derive from this class to plug in code to handle particular
 * {@link Activity} types. Pre and post processing of Activities can be plugged
 * in by deriving and calling the base class implementation.
 */
public class ActivityHandler implements Bot {

    /**
     * Called by the adapter (for example, a {@link BotFrameworkAdapter}) at runtime
     * in order to process an inbound {@link Activity}.
     *
     * <p>
     * This method calls other methods in this class based on the type of the
     * activity to process, which allows a derived class to provide type-specific
     * logic in a controlled way.
     * </p>
     *
     * <p>
     * In a derived class, override this method to add logic that applies to all
     * activity types. Add logic to apply before the type-specific logic before the
     * call to the base class {@link Bot#onTurn(TurnContext)} method. Add logic to
     * apply after the type-specific logic after the call to the base class
     * {@link Bot#onTurn(TurnContext)} method.
     * </p>
     *
     * @param turnContext The context object for this turn. Provides information
     *                    about the incoming activity, and other data needed to
     *                    process the activity.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "TurnContext cannot be null."
            ));
        }

        if (turnContext.getActivity() == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext must have a non-null Activity."
            ));
        }

        if (turnContext.getActivity().getType() == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext.getActivity must have a non-null Type."
            ));
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

            case ActivityTypes.INSTALLATION_UPDATE:
                return onInstallationUpdate(turnContext);

            case ActivityTypes.COMMAND:
                return onCommandActivity(turnContext);

            case ActivityTypes.COMMAND_RESULT:
                return onCommandResultActivity(turnContext);

            case ActivityTypes.END_OF_CONVERSATION:
                return onEndOfConversationActivity(turnContext);

            case ActivityTypes.TYPING:
                return onTypingActivity(turnContext);

            case ActivityTypes.INVOKE:
                return onInvokeActivity(turnContext).thenCompose(invokeResponse -> {
                    // If OnInvokeActivityAsync has already sent an InvokeResponse, do not send
                    // another one.
                    if (
                        invokeResponse != null && turnContext.getTurnState()
                            .get(BotFrameworkAdapter.INVOKE_RESPONSE_KEY) == null
                    ) {

                        Activity activity = new Activity(ActivityTypes.INVOKE_RESPONSE);
                        activity.setValue(invokeResponse);

                        return turnContext.sendActivity(activity);
                    }

                    CompletableFuture<ResourceResponse> noAction = new CompletableFuture<>();
                    noAction.complete(null);
                    return noAction;
                }).thenApply(response -> null);

            default:
                return onUnrecognizedActivityType(turnContext);
        }
    }

    /**
     * Override this in a derived class to provide logic specific to
     * {@link ActivityTypes#MESSAGE} activities, such as the conversational logic.
     * <p>
     * When the {@link #onTurn(TurnContext)} method receives a message activity, it
     * calls this method.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a conversation update activity is received from the channel when
     * the base behavior of {@link #onTurn(TurnContext)} is used.
     * <p>
     * Conversation update activities are useful when it comes to responding to
     * users being added to or removed from the conversation.
     * <p>
     * For example, a bot could respond to a user being added by greeting the user.
     * By default, this method will call {@link #onMembersAdded(List, TurnContext)}
     * if any users have been added or {@link #onMembersRemoved(List, TurnContext)}
     * if any users have been removed. The method checks the member ID so that it
     * only responds to updates regarding members other than the bot itself.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        Activity activity = turnContext.getActivity();

        if (
            activity.getMembersAdded() != null && activity.getRecipient() != null
                && activity.getMembersAdded()
                    .stream()
                    .anyMatch(m -> !StringUtils.equals(m.getId(), activity.getRecipient().getId()))
        ) {
            return onMembersAdded(activity.getMembersAdded(), turnContext);
        } else if (
            activity.getMembersRemoved() != null && activity.getRecipient() != null
                && activity.getMembersRemoved()
                    .stream()
                    .anyMatch(m -> !StringUtils.equals(m.getId(), activity.getRecipient().getId()))
        ) {
            return onMembersRemoved(activity.getMembersRemoved(), turnContext);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic for when members other than
     * the bot join the conversation, such as your bot's welcome logic.
     *
     * <p>
     * When the {@link #onConversationUpdateActivity(TurnContext)} method receives a
     * conversation update activity that indicates one or more users other than the
     * bo are joining the conversation, it calls this method.
     * </p>
     *
     * @param membersAdded A list of all the members added to the conversation, as
     *                     described by the conversation update activity.
     * @param turnContext  The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic for when members other than
     * the bot leave the conversation, such as your bot's good-bye logic.
     *
     * <p>
     * When the {@link #onConversationUpdateActivity(TurnContext)} method receives a
     * conversation update activity that indicates one or more users other than the
     * bot are leaving the conversation, it calls this method.
     * </p>
     *
     * @param membersRemoved A list of all the members removed from the
     *                       conversation, as described by the conversation update
     *                       activity.
     * @param turnContext    The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMembersRemoved(
        List<ChannelAccount> membersRemoved,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event activity is received from the connector when the base
     * behavior of {@link #onTurn(TurnContext)} is used.
     *
     * <p>
     * Message reactions correspond to the user adding a 'like' or 'sad' etc. (often
     * an emoji) to a previously sent activity. Message reactions are only supported
     * by a few channels.
     * </p>
     *
     * <p>
     * The activity that the message reaction corresponds to is indicated in the
     * replyToId property. The value of this property is the activity id of a
     * previously sent activity given back to the bot as the response from a send
     * call.
     * </p>
     *
     * <p>
     * When the {@link #onTurn(TurnContext)} method receives a message reaction
     * activity, it calls this method. If the message reaction indicates that
     * reactions were added to a message, it calls
     * {@link #onReactionsAdded(List, TurnContext)}. If the message reaction
     * indicates that reactions were removed from a message, it calls
     * {@link #onReactionsRemoved(List, TurnContext)}.
     * </p>
     *
     * <p>
     * In a derived class, override this method to add logic that applies to all
     * message reaction activities. Add logic to apply before the reactions added or
     * removed logic before the call to the base class method. Add logic to apply
     * after the reactions added or removed logic after the call to the base class.
     * </p>
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onMessageReactionActivity(TurnContext turnContext) {
        CompletableFuture<Void> task = null;

        if (turnContext.getActivity().getReactionsAdded() != null) {
            task = onReactionsAdded(turnContext.getActivity().getReactionsAdded(), turnContext);
        }

        if (turnContext.getActivity().getReactionsRemoved() != null) {
            if (task != null) {
                task.thenApply(
                    result -> onReactionsRemoved(
                        turnContext.getActivity().getReactionsRemoved(), turnContext
                    )
                );
            } else {
                task = onReactionsRemoved(
                    turnContext.getActivity().getReactionsRemoved(), turnContext
                );
            }
        }

        return task == null ? CompletableFuture.completedFuture(null) : task;
    }

    /**
     * Override this in a derived class to provide logic for when reactions to a
     * previous activity are added to the conversation.
     *
     * <p>
     * Message reactions correspond to the user adding a 'like' or 'sad' etc. (often
     * an emoji) to a previously sent message on the conversation. Message reactions
     * are supported by only a few channels. The activity that the message is in
     * reaction to is identified by the activity's {@link Activity#getReplyToId()}
     * property. The value of this property is the activity ID of a previously sent
     * activity. When the bot sends an activity, the channel assigns an ID to it,
     * which is available in the
     * {@link com.microsoft.bot.schema.ResourceResponse#getId} of the result.
     * </p>
     *
     * @param messageReactions The list of reactions added.
     * @param turnContext      The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onReactionsAdded(
        List<MessageReaction> messageReactions,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic for when reactions to a
     * previous activity are removed from the conversation.
     *
     * <p>
     * Message reactions correspond to the user adding a 'like' or 'sad' etc. (often
     * an emoji) to a previously sent message on the conversation. Message reactions
     * are supported by only a few channels. The activity that the message is in
     * reaction to is identified by the activity's {@link Activity#getReplyToId()}
     * property. The value of this property is the activity ID of a previously sent
     * activity. When the bot sends an activity, the channel assigns an ID to it,
     * which is available in the
     * {@link com.microsoft.bot.schema.ResourceResponse#getId} of the result.
     * </p>
     *
     * @param messageReactions The list of reactions removed.
     * @param turnContext      The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onReactionsRemoved(
        List<MessageReaction> messageReactions,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event activity is received from the connector when the base
     * behavior of {@link #onTurn(TurnContext)} is used.
     *
     * <p>
     * Event activities can be used to communicate many different things.
     * </p>
     *
     * <p>
     * By default, this method will call {@link #onTokenResponseEvent(TurnContext)}
     * if the activity's name is "tokens/response" or {@link #onEvent(TurnContext)}
     * otherwise. "tokens/response" event can be triggered by an
     * {@link com.microsoft.bot.schema.OAuthCard}.
     * </p>
     *
     * <p>
     * When the {@link #onTurn(TurnContext)} method receives an event activity, it
     * calls this method.
     * </p>
     *
     * <p>
     * If the event {@link Activity#getName} is `tokens/response`, it calls
     * {@link #onTokenResponseEvent(TurnContext)} otherwise, it calls
     * {@link #onEvent(TurnContext)}.
     * </p>
     *
     * <p>
     * In a derived class, override this method to add logic that applies to all
     * event activities. Add logic to apply before the specific event-handling logic
     * before the call to the base class method. Add logic to apply after the
     * specific event-handling logic after the call to the base class method.
     * </p>
     *
     * <p>
     * Event activities communicate programmatic information from a client or
     * channel to a bot. The meaning of an event activity is defined by the
     * {@link Activity#getName} property, which is meaningful within the scope of a
     * channel. A `tokens/response` event can be triggered by an
     * {@link com.microsoft.bot.schema.OAuthCard} or an OAuth prompt.
     * </p>
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
     * Invoked when an invoke activity is received from the connector when the base
     * behavior of onTurn is used.
     * <p>
     * Invoke activities can be used to communicate many different things. By
     * default, this method will call onSignInInvokeAsync if the activity's name is
     * 'signin/verifyState' or 'signin/tokenExchange'.
     * <p>
     * A 'signin/verifyState' or 'signin/tokenExchange' invoke can be triggered by
     * an OAuthCard.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<InvokeResponse> onInvokeActivity(TurnContext turnContext) {
        if (StringUtils.equals(turnContext.getActivity().getName(), "adaptiveCard/action")) {
            AdaptiveCardInvokeValue invokeValue = null;
            try {
                invokeValue = getAdaptiveCardInvokeValue(turnContext.getActivity());
            } catch (InvokeResponseException e) {
                return Async.completeExceptionally(e);
            }
            return onAdaptiveCardInvoke(turnContext, invokeValue).thenApply(result -> createInvokeResponse(result));
        }

        if (
            StringUtils.equals(
                turnContext.getActivity().getName(), SignInConstants.VERIFY_STATE_OPERATION_NAME
            ) || StringUtils.equals(
                turnContext.getActivity().getName(), SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME
            )
        ) {
            return onSignInInvoke(turnContext).thenApply(aVoid -> createInvokeResponse(null))
                .exceptionally(ex -> {
                    if (
                        ex instanceof CompletionException
                            && ex.getCause() instanceof InvokeResponseException
                    ) {
                        InvokeResponseException ire = (InvokeResponseException) ex.getCause();
                        return new InvokeResponse(ire.statusCode, ire.body);
                    } else if (ex instanceof InvokeResponseException) {
                        InvokeResponseException ire = (InvokeResponseException) ex;
                        return new InvokeResponse(ire.statusCode, ire.body);
                    }
                    return new InvokeResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, null);
                });
        }

        CompletableFuture<InvokeResponse> result = new CompletableFuture<>();
        result.complete(new InvokeResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED, null));
        return result;
    }

    /**
     * Invoked when a 'signin/verifyState' or 'signin/tokenExchange' event is
     * received when the base behavior of onInvokeActivity is used.
     * <p>
     * If using an OAuthPrompt, override this method to forward this Activity to the
     * current dialog. By default, this method does nothing.
     * <p>
     * When the onInvokeActivity method receives an Invoke with a name of
     * `tokens/response`, it calls this method.
     * <p>
     * If your bot uses the OAuthPrompt, forward the incoming Activity to the
     * current dialog.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onSignInInvoke(TurnContext turnContext) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        result.completeExceptionally(
            new InvokeResponseException(HttpURLConnection.HTTP_NOT_IMPLEMENTED)
        );
        return result;
    }

    /**
     * Creates a success InvokeResponse with the specified body.
     *
     * @param body The body to return in the invoke response.
     * @return The InvokeResponse object.
     */
    protected InvokeResponse createInvokeResponse(Object body) {
        return new InvokeResponse(HttpURLConnection.HTTP_OK, body);
    }

    /**
     * Invoked when a "tokens/response" event is received when the base behavior of
     * {@link #onEventActivity(TurnContext)} is used.
     *
     * <p>
     * If using an OAuthPrompt, override this method to forward this
     * {@link Activity} to the current dialog.
     * </p>
     *
     * <p>
     * By default, this method does nothing.
     * </p>
     * <p>
     * When the {@link #onEventActivity(TurnContext)} method receives an event with
     * a {@link Activity#getName()} of `tokens/response`, it calls this method.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTokenResponseEvent(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an event other than tokens/response is received when the base
     * behavior of {@link #onEventActivity(TurnContext)} is used.
     *
     * <p>
     * This method could optionally be overridden if the bot is meant to handle
     * miscellaneous events.
     * </p>
     *
     * <p>
     * By default, this method does nothing.
     * </p>
     * <p>
     * When the {@link #onEventActivity(TurnContext)} method receives an event with
     * a {@link Activity#getName()} other than `tokens/response`, it calls this
     * method.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onEvent(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic specific to
     * ActivityTypes.InstallationUpdate activities.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onInstallationUpdate(TurnContext turnContext) {
        String action = turnContext.getActivity().getAction();
        if (StringUtils.isEmpty(action)) {
            return CompletableFuture.completedFuture(null);
        }

        switch (action) {
            case "add":
            case "add-upgrade":
                return onInstallationUpdateAdd(turnContext);

            case "remove":
            case "remove-upgrade":
                return onInstallationUpdateRemove(turnContext);

            default:
                return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Invoked when a command activity is received when the base behavior of
     * {@link ActivityHandler#onTurn(TurnContext)} is used. Commands are requests to perform an
     * action and receivers typically respond with one or more commandResult
     * activities. Receivers are also expected to explicitly reject unsupported
     * command activities.
     *
     * @param turnContext  A strongly-typed context Object for this
     *                     turn.
     *
     * @return   A task that represents the work queued to execute.
     *
     * When the {@link ActivityHandler#onTurn(TurnContext)} method receives a command activity,
     * it calls this method. In a derived class, override this method to add
     * logic that applies to all comand activities. Add logic to apply before
     * the specific command-handling logic before the call to the base class
     * {@link ActivityHandler#onCommandActivity(TurnContext)} method. Add
     * logic to apply after the specific command-handling logic after the call
     * to the base class
     * {@link ActivityHandler#onCommandActivity(TurnContext)} method. Command
     * activities communicate programmatic information from a client or channel
     * to a bot. The meaning of an command activity is defined by the
     * name property, which is meaningful within the scope of a channel.
     */
    protected CompletableFuture<Void> onCommandActivity(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a CommandResult activity is received when the
     * base behavior of {@link ActivityHandler#onTurn(TurnContext)} is used. CommandResult
     * activities can be used to communicate the result of a command execution.
     *
     * @param turnContext  A strongly-typed context Object for this
     *                     turn.
     *
     * @return   A task that represents the work queued to execute.
     *
     * When the {@link ActivityHandler#onTurn(TurnContext)} method receives a CommandResult
     * activity, it calls this method. In a derived class, override this method
     * to add logic that applies to all comand activities. Add logic to apply
     * before the specific CommandResult-handling logic before the call to the
     * base class
     * {@link ActivityHandler#onCommandResultActivity(TurnContext)}
     * method. Add logic to apply after the specific CommandResult-handling
     * logic after the call to the base class
     * {@link ActivityHandler#onCommandResultActivity(TurnContext)}
     * method. CommandResult activities communicate programmatic information
     * from a client or channel to a bot. The meaning of an CommandResult
     * activity is defined by the name property,
     * which is meaningful within the scope of a channel.
     */
    protected CompletableFuture<Void> onCommandResultActivity(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic specific to ActivityTypes.InstallationUpdate
     * activities with 'action' set to 'add'.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onInstallationUpdateAdd(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic specific to ActivityTypes.InstallationUpdate
     * activities with 'action' set to 'remove'.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onInstallationUpdateRemove(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when the bot is sent an Adaptive Card Action Execute.
     *
     * @param turnContext  A strongly-typed context Object for this
     *                     turn.
     * @param invokeValue  A stringly-typed Object from the incoming
     *                     activity's Value.
     *
     * @return   A task that represents the work queued to execute.
     *
     * When the {@link OnInvokeActivity(TurnContext(InvokeActivity))} method
     * receives an Invoke with a {@link InvokeActivity.name} of
     * `adaptiveCard/action`, it calls this method.
     */
    protected CompletableFuture<AdaptiveCardInvokeResponse> onAdaptiveCardInvoke(
        TurnContext turnContext, AdaptiveCardInvokeValue invokeValue) {
        return Async.completeExceptionally(new InvokeResponseException(HttpURLConnection.HTTP_NOT_IMPLEMENTED));
    }

    /**
     * Override this in a derived class to provide logic specific to
     * ActivityTypes.END_OF_CONVERSATION activities.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onEndOfConversationActivity(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic specific to
     * ActivityTypes.Typing activities.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTypingActivity(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when an activity other than a message, conversation update, or event
     * is received when the base behavior of {@link #onTurn(TurnContext)} is used.
     *
     * <p>
     * If overridden, this could potentially respond to any of the other activity
     * types like
     * {@link com.microsoft.bot.schema.ActivityTypes#CONTACT_RELATION_UPDATE} or
     * {@link com.microsoft.bot.schema.ActivityTypes#END_OF_CONVERSATION}.
     * </p>
     *
     * <p>
     * By default, this method does nothing.
     * </p>
     *
     * <p>
     * When the {@link #onTurn(TurnContext)} method receives an activity that is not
     * a message, conversation update, message reaction, or event activity, it calls
     * this method.
     * </p>
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onUnrecognizedActivityType(TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    private AdaptiveCardInvokeValue getAdaptiveCardInvokeValue(Activity activity) throws InvokeResponseException {
        if (activity.getValue() == null) {
            AdaptiveCardInvokeResponse response = createAdaptiveCardInvokeErrorResponse(
                HttpURLConnection.HTTP_BAD_REQUEST, "BadRequest", "Missing value property");
            throw new InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response);
        }

        Object obj = activity.getValue();
        JsonNode node = null;
        if (obj instanceof JsonNode) {
            node = (JsonNode) obj;
        } else {
            AdaptiveCardInvokeResponse response = createAdaptiveCardInvokeErrorResponse(
                HttpURLConnection.HTTP_BAD_REQUEST, "BadRequest", "Value property instanceof not properly formed");
            throw new InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response);
        }

        AdaptiveCardInvokeValue invokeValue = Serialization.treeToValue(node, AdaptiveCardInvokeValue.class);
        if (invokeValue == null) {
            AdaptiveCardInvokeResponse response = createAdaptiveCardInvokeErrorResponse(
                HttpURLConnection.HTTP_BAD_REQUEST, "BadRequest", "Value property instanceof not properly formed");
            throw new InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response);
        }

        if (invokeValue.getAction() == null) {
            AdaptiveCardInvokeResponse response = createAdaptiveCardInvokeErrorResponse(
                HttpURLConnection.HTTP_BAD_REQUEST, "BadRequest", "Missing action property");
            throw new InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response);
        }

        if (!invokeValue.getAction().getType().equals("Action.Execute")) {
            AdaptiveCardInvokeResponse response = createAdaptiveCardInvokeErrorResponse(
                HttpURLConnection.HTTP_BAD_REQUEST, "NotSupported",
                    String.format("The action '%s'is not supported.", invokeValue.getAction().getType()));
            throw new InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response);
        }

        return invokeValue;
    }

    private AdaptiveCardInvokeResponse createAdaptiveCardInvokeErrorResponse(
        Integer statusCode,
        String code,
        String message
    ) {
        AdaptiveCardInvokeResponse adaptiveCardInvokeResponse = new AdaptiveCardInvokeResponse();
        adaptiveCardInvokeResponse.setStatusCode(statusCode);
        adaptiveCardInvokeResponse.setType("application/vnd.getmicrosoft().error");
        com.microsoft.bot.schema.Error error = new com.microsoft.bot.schema.Error();
        error.setCode(code);
        error.setMessage(message);
        adaptiveCardInvokeResponse.setValue(error);
        return adaptiveCardInvokeResponse;
    }


    /**
     * InvokeResponse Exception.
     */
    protected class InvokeResponseException extends Exception {

        private int statusCode;
        private Object body;

        /**
         * Initializes new instance with HTTP status code value.
         *
         * @param withStatusCode The HTTP status code.
         */
        public InvokeResponseException(int withStatusCode) {
            this(withStatusCode, null);
        }

        /**
         * Initializes new instance with HTTP status code value.
         *
         * @param withStatusCode The HTTP status code.
         * @param withBody       The body. Can be null.
         */
        public InvokeResponseException(int withStatusCode, Object withBody) {
            statusCode = withStatusCode;
            body = withBody;
        }

        /**
         * Returns an InvokeResponse based on this exception.
         *
         * @return The InvokeResponse value.
         */
        public InvokeResponse createInvokeResponse() {
            return new InvokeResponse(statusCode, body);
        }
    }
}
