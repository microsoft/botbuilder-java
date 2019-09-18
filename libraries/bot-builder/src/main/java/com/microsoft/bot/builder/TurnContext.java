// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.bot.schema.ResourceResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides context for a turn of a bot.
 * Context provides information needed to process an incoming activity.
 * The context object is created by a {@link BotAdapter} and persists for the
 * length of the turn.
 * {@linkalso Bot}
 * {@linkalso Middleware}
 */
public interface TurnContext {
    /**
     * Sends a trace activity to the {@link BotAdapter} for logging purposes.
     *
     * @param turnContext The context for the current turn.
     * @param name        The value to assign to the activity's {@link Activity#getName} property.
     * @param value       The value to assign to the activity's {@link Activity#getValue} property.
     * @param valueType   The value to assign to the activity's {@link Activity#getValueType} property.
     * @param label       The value to assign to the activity's {@link Activity#getLabel} property.
     * @return A task that represents the work queued to execute. If the adapter is being hosted in the Emulator,
     * the task result contains a {@link ResourceResponse} object with the original trace activity's ID; otherwise,
     * it contains a {@link ResourceResponse} object containing the ID that the receiving channel assigned to the
     * activity.
     */
    static CompletableFuture<ResourceResponse> traceActivity(
        TurnContext turnContext,
        String name,
        Object value,
        String valueType,
        String label) {

        return turnContext.sendActivity(turnContext.getActivity().createTrace(name, value, valueType, label));
    }


    /**
     * Gets the bot adapter that created this context object.
     */
    BotAdapter getAdapter();

    /**
     * Gets the services registered on this context object.
     */
    TurnContextStateCollection getTurnState();

    /**
     * Incoming request
     */
    Activity getActivity();

    /**
     * Indicates whether at least one response was sent for the current turn.
     *
     * @return {@code true} if at least one response was sent for the current turn.
     */
    boolean getResponded();

    /**
     * Sends a message activity to the sender of the incoming activity.
     *
     * @param textReplyToSend The text of the message to send.
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     * <p>See the channel's documentation for limits imposed upon the contents of
     * {@code textReplyToSend}.</p>
     * <p>To control various characteristics of your bot's speech such as voice,
     * rate, volume, pronunciation, and pitch, specify {@code speak} in
     * Speech Synthesis Markup Language (SSML) format.</p>
     */
    CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend);

    /**
     * Sends a message activity to the sender of the incoming activity.
     *
     * @param textReplyToSend The text of the message to send.
     * @param speak           Optional, text to be spoken by your bot on a speech-enabled
     *                        channel.
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     * <p>See the channel's documentation for limits imposed upon the contents of
     * {@code textReplyToSend}.</p>
     * <p>To control various characteristics of your bot's speech such as voice,
     * rate, volume, pronunciation, and pitch, specify {@code speak} in
     * Speech Synthesis Markup Language (SSML) format.</p>
     */
    CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak);

    /**
     * Sends a message activity to the sender of the incoming activity.
     *
     * @param textReplyToSend The text of the message to send.
     * @param speak           Optional, text to be spoken by your bot on a speech-enabled
     *                        channel.
     * @param inputHint       Optional, indicates whether your bot is accepting,
     *                        expecting, or ignoring user input after the message is delivered to the client.
     *                        One of: "acceptingInput", "ignoringInput", or "expectingInput".
     *                        Default is "acceptingInput".
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     * <p>See the channel's documentation for limits imposed upon the contents of
     * {@code textReplyToSend}.</p>
     * <p>To control various characteristics of your bot's speech such as voice,
     * rate, volume, pronunciation, and pitch, specify {@code speak} in
     * Speech Synthesis Markup Language (SSML) format.</p>
     */
    CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak, InputHints inputHint);

    /**
     * Sends an activity to the sender of the incoming activity.
     *
     * @param activity The activity to send.
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     */
    CompletableFuture<ResourceResponse> sendActivity(Activity activity);

    /**
     * Sends a set of activities to the sender of the incoming activity.
     *
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute.
     * If the activities are successfully sent, the task result contains
     * an array of {@link ResourceResponse} objects containing the IDs that
     * the receiving channel assigned to the activities.
     */
    CompletableFuture<ResourceResponse[]> sendActivities(List<Activity> activities);

    /**
     * Replaces an existing activity.
     *
     * @param withActivity New replacement activity.
     * @return A task that represents the work queued to execute.
     * If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     * <p>Before calling this, set the ID of the replacement activity to the ID
     * of the activity to replace.</p>
     */
    CompletableFuture<ResourceResponse> updateActivity(Activity withActivity);

    /**
     * Deletes an existing activity.
     *
     * @param activityId The ID of the activity to delete.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> deleteActivity(String activityId);

    /**
     * Deletes an existing activity.
     *
     * @param conversationReference The conversation containing the activity to delete.
     * @return A task that represents the work queued to execute.
     * The conversation reference's {@link ConversationReference#getActivityId}
     * indicates the activity in the conversation to delete.
     */
    CompletableFuture<Void> deleteActivity(ConversationReference conversationReference);

    /**
     * Adds a response handler for send activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * When the context's {@link #sendActivity(Activity)}
     * or {@link #sendActivities(List<Activity>)} methods are called,
     * the adapter calls the registered handlers in the order in which they were
     * added to the context object.
     */
    TurnContext onSendActivities(SendActivitiesHandler handler);

    /**
     * Adds a response handler for update activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * When the context's {@link #updateActivity(Activity)} is called,
     * the adapter calls the registered handlers in the order in which they were
     * added to the context object.
     */
    TurnContext onUpdateActivity(UpdateActivityHandler handler);

    /**
     * Adds a response handler for delete activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws NullPointerException {@code handler} is {@code null}.
     *                              When the context's {@link #deleteActivity(String)} is called,
     *                              the adapter calls the registered handlers in the order in which they were
     *                              added to the context object.
     */
    TurnContext onDeleteActivity(DeleteActivityHandler handler);
}
