// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder;

/**
 * A method that can participate in send activity events for the current turn.
 * @param context The context object for the turn.
 * @param activities The activities to send.
 * @param next The delegate to call to continue event processing.
 * @return A task that represents the work queued to execute.
 * A handler calls the {@code next} delegate to pass control to
 * the next registered handler. If a handler doesn’t call the next delegate,
 * the adapter does not call any of the subsequent handlers and does not send the
 * {@code activities}.
 *
 * {@linkalso BotAdapter}
 * {@linkalso UpdateActivityHandler}
 * {@linkalso DeleteActivityHandler}
 */

import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ConversationReference;
import com.microsoft.bot.schema.models.ResourceResponse;

import java.util.concurrent.CompletableFuture;

//public delegate Task DeleteActivityHandler(TurnContext context, ConversationReference reference, Func<Task> next);

/**
 * Provides context for a turn of a bot.
 * Context provides information needed to process an incoming activity.
 * The context object is created by a {@link BotAdapter} and persists for the 
 * length of the turn.
 * {@linkalso Bot}
 * {@linkalso Middleware}
 */
public interface TurnContext
{
    /**
     * Gets the bot adapter that created this context object.
     */
    BotAdapter getAdapter();

    /**
     * Gets the services registered on this context object.
     */
    TurnContextServiceCollection getServices();

    /**
     * Incoming request
     */
    Activity getActivity();



    /**
     * Indicates whether at least one response was sent for the current turn.
     * @return {@code true} if at least one response was sent for the current turn.
     */
    boolean getResponded();
    void setResponded(boolean responded);

    /**
     * Sends a message activity to the sender of the incoming activity.
     * @param textReplyToSend The text of the message to send.
     * @param speak Optional, text to be spoken by your bot on a speech-enabled 
     * channel.
     * @param inputHint Optional, indicates whether your bot is accepting, 
     * expecting, or ignoring user input after the message is delivered to the client.
     * One of: "acceptingInput", "ignoringInput", or "expectingInput".
     * Default is "acceptingInput".
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving 
     * channel assigned to the activity.
     * <p>See the channel's documentation for limits imposed upon the contents of 
     * {@code textReplyToSend}.</p>
     * <p>To control various characteristics of your bot's speech such as voice, 
     * rate, volume, pronunciation, and pitch, specify {@code speak} in 
     * Speech Synthesis Markup Language (SSML) format.</p>
     *
     */
    CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend) throws Exception;
    CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak) throws Exception;
    //CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak = null, String inputHint = InputHints.AcceptingInput);
    CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak, String inputHint) throws Exception;
    
    /**
     * Sends an activity to the sender of the incoming activity.
     * @param activity The activity to send.
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving 
     * channel assigned to the activity.
     */
    CompletableFuture<ResourceResponse> SendActivity(Activity activity) throws Exception;

    /**
     * Sends a set of activities to the sender of the incoming activity.
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute.
     * If the activities are successfully sent, the task result contains
     * an array of {@link ResourceResponse} objects containing the IDs that 
     * the receiving channel assigned to the activities.
     */
    CompletableFuture<ResourceResponse[]> SendActivities(Activity[] activities) throws Exception;

    /**
     * Replaces an existing activity. 
     * @param activity New replacement activity.        
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving 
     * channel assigned to the activity.
     * <p>Before calling this, set the ID of the replacement activity to the ID
     * of the activity to replace.</p>
     */
    ResourceResponse UpdateActivity(Activity activity) throws Exception;

    /**
     * Deletes an existing activity.
     * @param activityId The ID of the activity to delete.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture DeleteActivity(String activityId) throws Exception;

    /**
     * Deletes an existing activity.
     * @param conversationReference The conversation containing the activity to delete.
     * @return A task that represents the work queued to execute.
     * The conversation reference's {@link ConversationReference.ActivityId}
     * indicates the activity in the conversation to delete.
     */
    CompletableFuture DeleteActivity(ConversationReference conversationReference) throws Exception;

    /**
     * Adds a response handler for send activity operations.
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * When the context's {@link SendActivity(Activity)}
     * or {@link SendActivities(Activity[])} methods are called,
     * the adapter calls the registered handlers in the order in which they were 
     * added to the context object.
     *
     */
    TurnContext OnSendActivities(SendActivitiesHandler handler);

    /**
     * Adds a response handler for update activity operations.
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * When the context's {@link UpdateActivity(Activity)} is called,
     * the adapter calls the registered handlers in the order in which they were 
     * added to the context object.
     *
     */
    TurnContext OnUpdateActivity(UpdateActivityHandler handler);

    /**
     * Adds a response handler for delete activity operations.
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws NullPointerException {@code handler} is {@code null}.
     * When the context's {@link DeleteActivity(String)} is called,
     * the adapter calls the registered handlers in the order in which they were 
     * added to the context object.
     *
     */
    TurnContext OnDeleteActivity(DeleteActivityHandler handler);
}
