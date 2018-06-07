// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder.core;

/// <summary>
/// A method that can participate in send activity events for the current turn.
/// </summary>
/// <param name="context">The context object for the turn.</param>
/// <param name="activities">The activities to send.</param>
/// <param name="next">The delegate to call to continue event processing.</param>
/// <returns>A task that represents the work queued to execute.</returns>
/// <remarks>A handler calls the <paramref name="next"/> delegate to pass control to 
/// the next registered handler. If a handler doesn’t call the next delegate,
/// the adapter does not call any of the subsequent handlers and does not send the
/// <paramref name="activities"/>.
/// </remarks>
/// <seealso cref="BotAdapter"/>
/// <seealso cref="UpdateActivityHandler"/>
/// <seealso cref="DeleteActivityHandler"/>

import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ConversationReference;
import com.microsoft.bot.schema.models.ResourceResponse;

import java.util.concurrent.CompletableFuture;

//public delegate Task DeleteActivityHandler(ITurnContext context, ConversationReference reference, Func<Task> next);

/// <summary>
/// Provides context for a turn of a bot.
/// </summary>
/// <remarks>Context provides information needed to process an incoming activity.
/// The context object is created by a <see cref="BotAdapter"/> and persists for the 
/// length of the turn.</remarks>
/// <seealso cref="IBot"/>
/// <seealso cref="IMiddleware"/>
public interface TurnContext
{
    /// <summary>
    /// Gets the bot adapter that created this context object.
    /// </summary>
    BotAdapter getAdapter();

    /// <summary>
    /// Gets the services registered on this context object.
    /// </summary>
    TurnContextServiceCollection getServices();

    /// <summary>
    /// Incoming request
    /// </summary>
    Activity getActivity();

    /// <summary>
    /// Indicates whether at least one response was sent for the current turn.
    /// </summary>
    /// <value><c>true</c> if at least one response was sent for the current turn.</value>
    boolean getResponded();
    void setResponded(boolean responded);

    /// <summary>
    /// Sends a message activity to the sender of the incoming activity.
    /// </summary>
    /// <param name="textReplyToSend">The text of the message to send.</param>
    /// <param name="speak">Optional, text to be spoken by your bot on a speech-enabled 
    /// channel.</param>
    /// <param name="inputHint">Optional, indicates whether your bot is accepting, 
    /// expecting, or ignoring user input after the message is delivered to the client.
    /// One of: "acceptingInput", "ignoringInput", or "expectingInput".
    /// Default is "acceptingInput".</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>If the activity is successfully sent, the task result contains
    /// a <see cref="ResourceResponse"/> object containing the ID that the receiving 
    /// channel assigned to the activity.
    /// <para>See the channel's documentation for limits imposed upon the contents of 
    /// <paramref name="textReplyToSend"/>.</para>
    /// <para>To control various characteristics of your bot's speech such as voice, 
    /// rate, volume, pronunciation, and pitch, specify <paramref name="speak"/> in 
    /// Speech Synthesis Markup Language (SSML) format.</para>
    /// </remarks>
    CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend) throws Exception;
    CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak) throws Exception;
    //CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak = null, String inputHint = InputHints.AcceptingInput);
    CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak, String inputHint) throws Exception;
    
    /// <summary>
    /// Sends an activity to the sender of the incoming activity.
    /// </summary>
    /// <param name="activity">The activity to send.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>If the activity is successfully sent, the task result contains
    /// a <see cref="ResourceResponse"/> object containing the ID that the receiving 
    /// channel assigned to the activity.</remarks>
    CompletableFuture<ResourceResponse> SendActivity(Activity activity) throws Exception;

    /// <summary>
    /// Sends a set of activities to the sender of the incoming activity.
    /// </summary>
    /// <param name="activities">The activities to send.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>If the activities are successfully sent, the task result contains
    /// an array of <see cref="ResourceResponse"/> objects containing the IDs that 
    /// the receiving channel assigned to the activities.</remarks>
    CompletableFuture<ResourceResponse[]> SendActivities(Activity[] activities) throws Exception;

    /// <summary>
    /// Replaces an existing activity. 
    /// </summary>
    /// <param name="activity">New replacement activity.</param>        
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>If the activity is successfully sent, the task result contains
    /// a <see cref="ResourceResponse"/> object containing the ID that the receiving 
    /// channel assigned to the activity.
    /// <para>Before calling this, set the ID of the replacement activity to the ID
    /// of the activity to replace.</para></remarks>
    ResourceResponse UpdateActivity(Activity activity) throws Exception;

    /// <summary>
    /// Deletes an existing activity.
    /// </summary>
    /// <param name="activityId">The ID of the activity to delete.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    CompletableFuture DeleteActivity(String activityId) throws Exception;

    /// <summary>
    /// Deletes an existing activity.
    /// </summary>
    /// <param name="conversationReference">The conversation containing the activity to delete.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>The conversation reference's <see cref="ConversationReference.ActivityId"/> 
    /// indicates the activity in the conversation to delete.</remarks>
    CompletableFuture DeleteActivity(ConversationReference conversationReference) throws Exception;

    /// <summary>
    /// Adds a response handler for send activity operations.
    /// </summary>
    /// <param name="handler">The handler to add to the context object.</param>
    /// <returns>The updated context object.</returns>
    /// <remarks>When the context's <see cref="SendActivity(IActivity)"/> 
    /// or <see cref="SendActivities(IActivity[])"/> methods are called, 
    /// the adapter calls the registered handlers in the order in which they were 
    /// added to the context object.
    /// </remarks>
    TurnContext OnSendActivities(SendActivitiesHandler handler);

    /// <summary>
    /// Adds a response handler for update activity operations.
    /// </summary>
    /// <param name="handler">The handler to add to the context object.</param>
    /// <returns>The updated context object.</returns>
    /// <remarks>When the context's <see cref="UpdateActivity(IActivity)"/> is called, 
    /// the adapter calls the registered handlers in the order in which they were 
    /// added to the context object.
    /// </remarks>
    TurnContext OnUpdateActivity(UpdateActivityHandler handler);

    /// <summary>
    /// Adds a response handler for delete activity operations.
    /// </summary>
    /// <param name="handler">The handler to add to the context object.</param>
    /// <returns>The updated context object.</returns>
    /// <exception cref="ArgumentNullException"><paramref name="handler"/> is <c>null</c>.</exception>
    /// <remarks>When the context's <see cref="DeleteActivity(String)"/> is called, 
    /// the adapter calls the registered handlers in the order in which they were 
    /// added to the context object.
    /// </remarks>
    TurnContext OnDeleteActivity(DeleteActivityHandler handler);
}
