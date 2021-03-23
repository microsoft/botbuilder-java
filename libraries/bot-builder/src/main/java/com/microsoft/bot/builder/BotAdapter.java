// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a bot adapter that can connect a bot to a service endpoint. This
 * class is abstract.
 * <p>
 * The bot adapter encapsulates authentication processes and sends activities to
 * and receives activities from the Bot Connector Service. When your bot
 * receives an activity, the adapter creates a context object, passes it to your
 * bot's application logic, and sends responses back to the user's channel.
 * </p>
 * <p>
 * Use {@link #use(Middleware)} to add {@link Middleware} objects to your
 * adapter’s middleware collection. The adapter processes and directs incoming
 * activities in through the bot middleware pipeline to your bot’s logic and
 * then back out again. As each activity flows in and out of the bot, each piece
 * of middleware can inspect or act upon the activity, both before and after the
 * bot logic runs.
 * </p>
 *
 * {@link TurnContext} {@link Activity} {@link Bot} {@link Middleware}
 */
public abstract class BotAdapter {
    /**
     * Key to store bot claims identity.
     */
    public static final String BOT_IDENTITY_KEY = "BotIdentity";

    /**
     * Key to store bot oauth scope.
     */
    public static final String OAUTH_SCOPE_KEY = "Microsoft.Bot.Builder.BotAdapter.OAuthScope";

    /**
     * Key to store bot oauth client.
     */
    public static final String OAUTH_CLIENT_KEY = "OAuthClient";

    /**
     * The collection of middleware in the adapter's pipeline.
     */
    private final MiddlewareSet middlewareSet = new MiddlewareSet();

    /**
     * Error handler that can catch exceptions in the middleware or application.
     */
    private OnTurnErrorHandler onTurnError;

    /**
     * Gets the error handler that can catch exceptions in the middleware or
     * application.
     *
     * @return An error handler that can catch exceptions in the middleware or
     *         application.
     */
    public OnTurnErrorHandler getOnTurnError() {
        return onTurnError;
    }

    /**
     * Sets the error handler that can catch exceptions in the middleware or
     * application.
     *
     * @param withTurnError An error handler that can catch exceptions in the
     *                      middleware or application.
     */
    public void setOnTurnError(OnTurnErrorHandler withTurnError) {
        onTurnError = withTurnError;
    }

    /**
     * Gets the collection of middleware in the adapter's pipeline.
     *
     * @return The middleware collection for the pipeline.
     */
    protected MiddlewareSet getMiddlewareSet() {
        return middlewareSet;
    }

    /**
     * Adds middleware to the adapter's pipeline.
     *
     * @param middleware The middleware to add.
     * @return The updated adapter object. Middleware is added to the adapter at
     *         initialization time. For each turn, the adapter calls middleware in
     *         the order in which you added it.
     */
    public BotAdapter use(Middleware middleware) {
        middlewareSet.use(middleware);
        return this;
    }

    /**
     * When overridden in a derived class, sends activities to the conversation.
     *
     * @param context    The context object for the turn.
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute. If the activities
     *         are successfully sent, the task result contains an array of
     *         {@link ResourceResponse} objects containing the IDs that the
     *         receiving channel assigned to the activities.
     *         {@link TurnContext#onSendActivities(SendActivitiesHandler)}
     */
    public abstract CompletableFuture<ResourceResponse[]> sendActivities(
        TurnContext context,
        List<Activity> activities
    );

    /**
     * When overridden in a derived class, replaces an existing activity in the
     * conversation.
     *
     * @param context  The context object for the turn.
     * @param activity New replacement activity.
     * @return A task that represents the work queued to execute. If the activity is
     *         successfully sent, the task result contains a
     *         {@link ResourceResponse} object containing the ID that the receiving
     *         channel assigned to the activity.
     *         <p>
     *         Before calling this, set the ID of the replacement activity to the ID
     *         of the activity to replace.
     *         </p>
     *         {@link TurnContext#onUpdateActivity(UpdateActivityHandler)}
     */
    public abstract CompletableFuture<ResourceResponse> updateActivity(
        TurnContext context,
        Activity activity
    );

    /**
     * When overridden in a derived class, deletes an existing activity in the
     * conversation.
     *
     * @param context   The context object for the turn.
     * @param reference Conversation reference for the activity to delete.
     * @return A task that represents the work queued to execute. The
     *         {@link ConversationReference#getActivityId} of the conversation
     *         reference identifies the activity to delete.
     *         {@link TurnContext#onDeleteActivity(DeleteActivityHandler)}
     */
    public abstract CompletableFuture<Void> deleteActivity(
        TurnContext context,
        ConversationReference reference
    );

    /**
     * Starts activity processing for the current bot turn.
     *
     * The adapter calls middleware in the order in which you added it. The adapter
     * passes in the context object for the turn and a next delegate, and the
     * middleware calls the delegate to pass control to the next middleware in the
     * pipeline. Once control reaches the end of the pipeline, the adapter calls the
     * {@code callback} method. If a middleware component does not call the next
     * delegate, the adapter does not call any of the subsequent middleware’s
     * {@link Middleware#onTurn(TurnContext, NextDelegate)} methods or the callback
     * method, and the pipeline short circuits.
     *
     * <p>
     * When the turn is initiated by a user activity (reactive messaging), the
     * callback method will be a reference to the bot's
     * {@link Bot#onTurn(TurnContext)} method. When the turn is initiated by a call
     * to
     * {@link #continueConversation(String, ConversationReference, BotCallbackHandler)}
     * (proactive messaging), the callback method is the callback method that was
     * provided in the call.
     * </p>
     *
     * @param context  The turn's context object.
     * @param callback A callback method to run at the end of the pipeline.
     * @return A task that represents the work queued to execute.
     * @throws NullPointerException {@code context} is null.
     */
    protected CompletableFuture<Void> runPipeline(
        TurnContext context,
        BotCallbackHandler callback
    ) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }

        // Call any registered Middleware Components looking for ReceiveActivity()
        if (context.getActivity() != null) {
            if (!StringUtils.isEmpty(context.getActivity().getLocale())) {

                Locale.setDefault(Locale.forLanguageTag(context.getActivity().getLocale()));

                context.setLocale(context.getActivity().getLocale());
            }

            return middlewareSet.receiveActivityWithStatus(context, callback)
                .exceptionally(exception -> {
                    if (onTurnError != null) {
                        return onTurnError.invoke(context, exception).join();
                    }

                    throw new CompletionException(exception);
                });
        } else {
            // call back to caller on proactive case
            if (callback != null) {
                return callback.invoke(context);
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * @param botAppId  The application ID of the bot. This parameter is ignored in
     *                  single tenant the Adapters (Console, Test, etc) but is
     *                  critical to the BotFrameworkAdapter which is multi-tenant
     *                  aware.
     * @param reference A reference to the conversation to continue.
     * @param callback  The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute. Call this method
     *         to proactively send a message to a conversation. Most channels
     *         require a user to initiate a conversation with a bot before the bot
     *         can send activities to the user.
     *
     *         {@link #runPipeline(TurnContext, BotCallbackHandler)}
     */
    public CompletableFuture<Void> continueConversation(
        String botAppId,
        ConversationReference reference,
        BotCallbackHandler callback
    ) {
        CompletableFuture<Void> pipelineResult = new CompletableFuture<>();

        try (TurnContextImpl context =
            new TurnContextImpl(this, reference.getContinuationActivity())) {
            pipelineResult = runPipeline(context, callback);
        } catch (Exception e) {
            pipelineResult.completeExceptionally(e);
        }

        return pipelineResult;
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param claimsIdentity A ClaimsIdentity reference for the conversation.
     * @param reference      A reference to the conversation to continue.
     * @param callback       The method to call for the result bot turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        ConversationReference reference,
        BotCallbackHandler callback
    ) {
        return Async.completeExceptionally(new NotImplementedException("continueConversation"));
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param claimsIdentity A ClaimsIdentity reference for the conversation.
     * @param reference      A reference to the conversation to continue.
     * @param audience       A value signifying the recipient of the proactive
     *                       message.
     * @param callback       The method to call for the result bot turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        ConversationReference reference,
        String audience,
        BotCallbackHandler callback
    ) {
        return Async.completeExceptionally(new NotImplementedException("continueConversation"));
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param botId The application ID of the bot. This parameter is ignored in single tenant
     *              the Adapters (Console, Test, etc) but is critical to the BotFrameworkAdapter
     *              which is multi-tenant aware.
     * @param continuationActivity  An Activity with the appropriate ConversationReference with
     *                              which to continue the conversation.
     * @param callback       The method to call for the result bot turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> continueConversation(
        String botId,
        Activity continuationActivity,
        BotCallbackHandler callback
    ) {
        return Async.completeExceptionally(new NotImplementedException("continueConversation"));
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param claimsIdentity A ClaimsIdentity for the conversation.
     * @param continuationActivity  An Activity with the appropriate ConversationReference with
     *                              which to continue the conversation.
     * @param callback       The method to call for the result bot turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        Activity continuationActivity,
        BotCallbackHandler callback
    ) {
        return Async.completeExceptionally(new NotImplementedException("continueConversation"));
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param claimsIdentity A ClaimsIdentity for the conversation.
     * @param continuationActivity  An Activity with the appropriate ConversationReference with
     *                              which to continue the conversation.
     * @param audience       A value signifying the recipient of the proactive
     *                       message.
     * @param callback       The method to call for the result bot turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        Activity continuationActivity,
        String audience,
        BotCallbackHandler callback
    ) {
        return Async.completeExceptionally(new NotImplementedException("continueConversation"));
    }
}
