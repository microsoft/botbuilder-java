package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ConversationReference;
import com.microsoft.bot.schema.models.ConversationReferenceHelper;
import com.microsoft.bot.schema.models.ResourceResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Represents a bot adapter that can connect a bot to a service endpoint.
 * This class is abstract.
 * <remarks>The bot adapter encapsulates authentication processes and sends
 * activities to and receives activities from the Bot Connector Service. When your
 * bot receives an activity, the adapter creates a context object, passes it to your
 * bot's application logic, and sends responses back to the user's channel.
 * <p>Use {@link Use(IMiddleware)} to add {@link IMiddleware} objects
 * to your adapter’s middleware collection. The adapter processes and directs
 * incoming activities in through the bot middleware pipeline to your bot’s logic
 * and then back out again. As each activity flows in and out of the bot, each piece
 * of middleware can inspect or act upon the activity, both before and after the bot
 * logic runs.</p>
 * </remarks>
 * {@linkalso ITurnContext}
 * {@linkalso IActivity}
 * {@linkalso IBot}
 * {@linkalso IMiddleware}
 */
public abstract class BotAdapter {
    /**
     * The collection of middleware in the adapter's pipeline.
     */
    protected final MiddlewareSet _middlewareSet = new MiddlewareSet();

    /**
     * Creates a default adapter.
     */
    public BotAdapter() {
        super();
    }

    /**
     * Adds middleware to the adapter's pipeline.
     * @param middleware The middleware to add.
     * @return The updated adapter object.
     * <remarks>Middleware is added to the adapter at initialization time.
     * For each turn, the adapter calls middleware in the order in which you added it.
     * </remarks>
     */
    public BotAdapter Use(Middleware middleware) {
        _middlewareSet.Use(middleware);
        return this;
    }

    /**
     * When overridden in a derived class, sends activities to the conversation.
     * @param context The context object for the turn.
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute.
     * <remarks>If the activities are successfully sent, the task result contains
     * an array of {@link ResourceResponse} objects containing the IDs that
     * the receiving channel assigned to the activities.</remarks>
     * {@linkalso ITurnContext.OnSendActivities(SendActivitiesHandler)}
     */
    public abstract CompletableFuture<ResourceResponse[]> SendActivities(TurnContext context, Activity[] activities) throws InterruptedException, ServiceKeyAlreadyRegisteredException;

    /**
     * When overridden in a derived class, replaces an existing activity in the
     * conversation.
     * @param context The context object for the turn.
     * @param activity New replacement activity.
     * @return A task that represents the work queued to execute.
     * <remarks>If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     * <p>Before calling this, set the ID of the replacement activity to the ID
     * of the activity to replace.</p></remarks>
     * {@linkalso ITurnContext.OnUpdateActivity(UpdateActivityHandler)}
     */
    public abstract CompletableFuture<ResourceResponse> UpdateActivity(TurnContext context, Activity activity);

    /**
     * When overridden in a derived class, deletes an existing activity in the
     * conversation.
     * @param context The context object for the turn.
     * @param reference Conversation reference for the activity to delete.
     * @return A task that represents the work queued to execute.
     * <remarks>The {@link ConversationReference.ActivityId} of the conversation
     * reference identifies the activity to delete.</remarks>
     * {@linkalso ITurnContext.OnDeleteActivity(DeleteActivityHandler)}
     */
    public abstract CompletableFuture DeleteActivity(TurnContext context, ConversationReference reference);


    /**
     * Starts activity processing for the current bot turn.
     * @param context The turn's context object.
     * @param callback A callback method to run at the end of the pipeline.
     * @param cancelToken A cancellation token for the task.
     * @return A task that represents the work queued to execute.
     * @throws NullPointerException 
     * {@code context} is null.
     * <remarks>The adapter calls middleware in the order in which you added it.
     * The adapter passes in the context object for the turn and a next delegate,
     * and the middleware calls the delegate to pass control to the next middleware
     * in the pipeline. Once control reaches the end of the pipeline, the adapter calls
     * the {@code callback} method. If a middleware component doesn’t call
     * the next delegate, the adapter does not call  any of the subsequent middleware’s
     * {@link IMiddleware.OnTurn(ITurnContext, MiddlewareSet.NextDelegate)}
     * methods or the callback method, and the pipeline short circuits.
     * <p>When the turn is initiated by a user activity (reactive messaging), the
     * callback method will be a reference to the bot's
     * {@link IBot.OnTurn(ITurnContext)} method. When the turn is
     * initiated by a call to {@link ContinueConversation(ConversationReference, Func{ITurnContext, Task})}
     * (proactive messaging), the callback method is the callback method that was provided in the call.</p>
     * </remarks>
     */
    // TODO: daveta CancellationToken
    //    protected CompletableFuture RunPipeline(TurnContext context, Function<TurnContext, CompletableFuture> callback = null, AtomicReference<CompletableFuture> cancelToken = null)
    protected CompletableFuture RunPipeline(TurnContext context, Function<TurnContext, CompletableFuture> callback) throws Exception, ServiceKeyAlreadyRegisteredException {
        BotAssert.ContextNotNull(context);

        // Call any registered Middleware Components looking for ReceiveActivity()
        if (context.getActivity() != null) {
            await(_middlewareSet.ReceiveActivityWithStatus(context, callback));
        }
        else {
            // call back to caller on proactive case
            if (callback != null) {
                await(callback.apply(context));
            }
        }
        return completedFuture(null);
    }


    /**
     * Creates a conversation on the specified channel.
     * @param channelId The ID of the channel.
     * @param callback A method to call when the new conversation is available.
     * @return A task that represents the work queued to execute.
     * @throws UnsupportedOperationException 
     * <remarks>No base implementation is provided.</remarks>
     */
    public CompletableFuture CreateConversation(String channelId, Function<TurnContext, CompletableFuture> callback)
    {
        throw new UnsupportedOperationException("Adapter does not support CreateConversation with this arguments");
    }

    /**
     * Sends a proactive message to a conversation.
     * @param botAppId The application ID of the bot. This paramter is ignored in
     * single tenant the Adpters (Console, Test, etc) but is critical to the BotFrameworkAdapter
     * which is multi-tenant aware. 
     * @param reference A reference to the conversation to continue.
     * @param callback The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute.
     * <remarks>Call this method to proactively send a message to a conversation.
     * Most channels require a user to initaiate a conversation with a bot
     * before the bot can send activities to the user.</remarks>
     * {@linkalso RunPipeline(ITurnContext, Func{ITurnContext, Task}, CancellationTokenSource)}
     */
    public CompletableFuture ContinueConversation(String botId, ConversationReference reference, Function<TurnContext, CompletableFuture> callback) throws Exception, ServiceKeyAlreadyRegisteredException {

        ConversationReferenceHelper conv = new ConversationReferenceHelper(reference);
        ActivityImpl activity = conv.GetPostToBotMessage();

        try (TurnContextImpl context = new TurnContextImpl(this, activity))
        {
            return RunPipeline(context, callback);
        }
    }
}
