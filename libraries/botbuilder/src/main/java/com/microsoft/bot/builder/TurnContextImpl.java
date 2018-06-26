package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.BotAssert;
import com.microsoft.bot.builder.DeleteActivityHandler;
import com.microsoft.bot.builder.SendActivitiesHandler;
import com.microsoft.bot.builder.ServiceKeyAlreadyRegisteredException;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextServiceCollection;
import com.microsoft.bot.builder.TurnContextServiceCollectionImpl;
import com.microsoft.bot.builder.UpdateActivityHandler;
import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;
import static com.microsoft.bot.schema.models.ActivityTypes.MESSAGE;
import static com.microsoft.bot.schema.models.ActivityTypes.TRACE;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

/**
 * Provides context for a turn of a bot.
 * <remarks>Context provides information needed to process an incoming activity.
 * The context object is created by a {@link BotAdapter} and persists for the
 * length of the turn.</remarks>
 * {@linkalso IBot}
 * {@linkalso IMiddleware}
 */
public class TurnContextImpl implements TurnContext, AutoCloseable {
    private final BotAdapter _adapter;
    private final ActivityImpl _activity;
    private Boolean _responded = false;

    private final List<SendActivitiesHandler> _onSendActivities = new ArrayList<SendActivitiesHandler>();
    private final List<UpdateActivityHandler> _onUpdateActivity = new ArrayList<UpdateActivityHandler>();
    private final List<DeleteActivityHandler> _onDeleteActivity = new ArrayList<DeleteActivityHandler>();

    private final TurnContextServiceCollection _turnServices;

    /**
     * Creates a context object.
     * @param adapter The adapter creating the context.
     * @param activity The incoming activity for the turn;
     * or {@code null} for a turn for a proactive message.
     * @throws IllegalArgumentException {@code activity} or
     * {@code adapter} is {@code null}.
     * <remarks>For use by bot adapter implementations only.</remarks>
     */
    public TurnContextImpl(BotAdapter adapter, ActivityImpl activity) {
        if (adapter == null)
            throw new IllegalArgumentException("adapter");
        _adapter = adapter;
        if (activity == null)
             throw new IllegalArgumentException("activity");
        _activity = activity;

        _turnServices = new TurnContextServiceCollectionImpl();
        }



    /**
     * Adds a response handler for send activity operations.
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}.
     * <remarks>When the context's {@link SendActivity(IActivity)}
     * or {@link SendActivities(IActivity[])} methods are called,
     * the adapter calls the registered handlers in the order in which they were
     * added to the context object.
     * </remarks>
     */
    public TurnContext OnSendActivities(SendActivitiesHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("handler");

        _onSendActivities.add(handler);
        return this;
        }

    /**
     * Adds a response handler for update activity operations.
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}.
     * <remarks>When the context's {@link UpdateActivity(IActivity)} is called,
     * the adapter calls the registered handlers in the order in which they were
     * added to the context object.
     * </remarks>
     */
    public TurnContext OnUpdateActivity(UpdateActivityHandler handler) {
        if (handler == null)
        throw new IllegalArgumentException("handler");

        _onUpdateActivity.add(handler);
        return this;
        }

    /**
     * Adds a response handler for delete activity operations.
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}.
     * <remarks>When the context's {@link DeleteActivity(string)} is called,
     * the adapter calls the registered handlers in the order in which they were
     * added to the context object.
     * </remarks>
     */
    public TurnContext OnDeleteActivity(DeleteActivityHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("handler");

        _onDeleteActivity.add(handler);
        return this;
    }

    /**
     * Gets the bot adapter that created this context object.
     */
    public BotAdapter getAdapter() {
        return this._adapter;
    }

    /**
     * Gets the services registered on this context object.
     */
    public TurnContextServiceCollection getServices() {
        return this._turnServices;
    }

    /**
     * Gets the activity associated with this turn; or {@code null} when processing
     * a proactive message.
     */
    public ActivityImpl getActivity() {
        return this._activity;
    }

    /**
     * Indicates whether at least one response was sent for the current turn.
     * @return {@code true} if at least one response was sent for the current turn.
     * @throws IllegalArgumentException You attempted to set the value to {@code false}.
     */
    public boolean getResponded() {
        return this._responded;
    }
    public void setResponded(boolean responded) {
        if (responded == false)
        {
            throw new IllegalArgumentException("TurnContext: cannot set 'responded' to a value of 'false'.");
        }
        this._responded = true;
    }

/**
 * Sends a message activity to the sender of the incoming activity.
 * @param textReplyToSend The text of the message to send.
 * @param speak Optional, text to be spoken by your bot on a speech-enabled
 * channel.
 * @param inputHint Optional, indicates whether your bot is accepting,
 * expecting, or ignoring user input after the message is delivered to the client.
 * One of: "acceptingInput", "ignoringInput", or "expectingInput".
 * Default is null.
 * @return A task that represents the work queued to execute.
 * @throws IllegalArgumentException 
 * {@code textReplyToSend} is {@code null} or whitespace.
 * <remarks>If the activity is successfully sent, the task result contains
 * a {@link ResourceResponse} object containing the ID that the receiving
 * channel assigned to the activity.
 * <p>See the channel's documentation for limits imposed upon the contents of
 * {@code textReplyToSend}.</p>
 * <p>To control various characteristics of your bot's speech such as voice,
 * rate, volume, pronunciation, and pitch, specify {@code speak} in
 * Speech Synthesis Markup Language (SSML) format.</p>
 * </remarks>
 */
    @Override
    public CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend) throws Exception {
            return SendActivity(textReplyToSend, null, null);
    }
    @Override
    public CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak) throws Exception {
        return SendActivity(textReplyToSend, speak, null);
    }
    @Override
    public CompletableFuture<ResourceResponse> SendActivity(String textReplyToSend, String speak, String inputHint) throws Exception {
        if (StringUtils.isEmpty(textReplyToSend))
            throw new IllegalArgumentException("textReplyToSend");

        ActivityImpl activityToSend = (ActivityImpl) new ActivityImpl()
                                        .withType(MESSAGE)
                                        .withText(textReplyToSend);
        if (speak != null)
            activityToSend.withSpeak(speak);

        if (StringUtils.isNotEmpty(inputHint))
            activityToSend.withInputHint(InputHints.fromString(inputHint));

        return completedFuture(await(SendActivity(activityToSend)));
    }

    /**
     * Sends an activity to the sender of the incoming activity.
     * @param activity The activity to send.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException {@code activity} is {@code null}.
     * <remarks>If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.</remarks>
     */
    @Override
    public CompletableFuture<ResourceResponse> SendActivity(ActivityImpl activity) throws Exception {
        if (activity == null)
            throw new IllegalArgumentException("activity");

        ActivityImpl[] activities = {activity };
        ResourceResponse[] responses = await(SendActivities(activities));
        if (responses == null || responses.length == 0)  {
            // It's possible an interceptor prevented the activity from having been sent.
            // Just return an empty response in that case.
            return completedFuture(null);
        }
        else {
            return completedFuture(responses[0]);
        }
    }

    /**
     * Sends a set of activities to the sender of the incoming activity.
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute.
     * <remarks>If the activities are successfully sent, the task result contains
     * an array of {@link ResourceResponse} objects containing the IDs that
     * the receiving channel assigned to the activities.</remarks>
     */
    @Override
    public CompletableFuture<ResourceResponse[]> SendActivities(ActivityImpl[] activities) throws Exception {
        // Bind the relevant Conversation Reference properties, such as URLs and
        // ChannelId's, to the activities we're about to send.
        ConversationReference cr = GetConversationReference(this._activity);
        for (Activity a : activities) {
            ApplyConversationReference(a, cr);
        }

        // Convert the IActivities to Activies.
        // Activity[] activityArray = Array.ConvertAll(activities, (input) => (Activity)input);
        List<Activity> activityArray = Arrays.stream(activities).map(input -> (Activity) input).collect(toList());


        // Create the list used by the recursive methods.
        List<Activity> activityList = new ArrayList<Activity>(activityArray);

        Callable<CompletableFuture<ResourceResponse[]>> ActuallySendStuff = () ->  {
            // Are the any non-trace activities to send?
            // The thinking here is that a Trace event isn't user relevant data
            // so the "Responded" flag should not be set by Trace messages being
            // sent out.
            boolean sentNonTraceActivities = false;
            if (!activityList.stream().anyMatch((a) -> a.type() == TRACE)) {
                sentNonTraceActivities = true;
            }
            // Send from the list, which may have been manipulated via the event handlers.
            // Note that 'responses' was captured from the root of the call, and will be
            // returned to the original caller.
            ResourceResponse[] responses = new ResourceResponse[0];
            try {
                responses = await(this.getAdapter().SendActivities(this,  activityList.toArray(new Activity[activityList.size()])));
            } catch (ServiceKeyAlreadyRegisteredException e) {
                // TODO: Log error
                return completedFuture(null);
            }
            if (responses != null && responses.length == activityList.size())  {
                // stitch up activity ids
                for (int i = 0; i < responses.length; i++) {
                    ResourceResponse response = responses[i];
                    Activity activity = activityList.get(i);
                    activity.withId(response.id());
                }
            }

            // If we actually sent something (that's not Trace), set the flag.
            if (sentNonTraceActivities) {
                this.setResponded(true);
            }
            return completedFuture(responses);
        };

        List<Activity> act_list = new ArrayList<>(activityList);
        return completedFuture(await(SendActivitiesInternal(act_list, _onSendActivities.iterator(), ActuallySendStuff)));
    }

    /**
     * Replaces an existing activity.
     * @param activity New replacement activity.
     * @return A task that represents the work queued to execute.
     * @throws Microsoft.Bot.Schema.ErrorResponseException 
     * The HTTP operation failed and the response contained additional information.
     * @throws System.AggregateException 
     * One or more exceptions occurred during the operation.
     * <remarks>If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     * <p>Before calling this, set the ID of the replacement activity to the ID
     * of the activity to replace.</p></remarks>
     */
    @Override
    public ResourceResponse UpdateActivity(ActivityImpl activity) throws Exception {
        ActivityImpl a = (ActivityImpl) activity;

        Callable<CompletableFuture<ResourceResponse>> ActuallyUpdateStuff = () -> {
            return completedFuture(await(this.getAdapter().UpdateActivity(this, a)));
        };

        return await(UpdateActivityInternal(a, _onUpdateActivity.iterator(), ActuallyUpdateStuff));
    }

    /**
     * Deletes an existing activity.
     * @param activityId The ID of the activity to delete.
     * @return A task that represents the work queued to execute.
     * @throws Microsoft.Bot.Schema.ErrorResponseException 
     * The HTTP operation failed and the response contained additional information.
     */
    public CompletableFuture DeleteActivity(String activityId) throws Exception {
        if (StringUtils.isWhitespace(activityId) || activityId == null)
            throw new IllegalArgumentException("activityId");

        ConversationReference cr = GetConversationReference(this.getActivity());
        cr.withActivityId(activityId);

        Callable<CompletableFuture> ActuallyDeleteStuff = () -> {
            await(this.getAdapter().DeleteActivity(this, cr));
            return completedFuture(null);
        };

        await(DeleteActivityInternal(cr, _onDeleteActivity.iterator(), ActuallyDeleteStuff));
        return completedFuture(null);
    }

    /**
     * Deletes an existing activity.
     * @param conversationReference The conversation containing the activity to delete.
     * @return A task that represents the work queued to execute.
     * @throws Microsoft.Bot.Schema.ErrorResponseException 
     * The HTTP operation failed and the response contained additional information.
     * <remarks>The conversation reference's {@link ConversationReference.ActivityId}
     * indicates the activity in the conversation to delete.</remarks>
     */
    public CompletableFuture DeleteActivity(ConversationReference conversationReference) throws Exception {
        if (conversationReference == null)
            throw new IllegalArgumentException("conversationReference");

        Callable<CompletableFuture> ActuallyDeleteStuff = () -> {
            return completedFuture(await(this.getAdapter().DeleteActivity(this, conversationReference)));
        };

        await(DeleteActivityInternal(conversationReference, _onDeleteActivity.iterator(), ActuallyDeleteStuff));
        return completedFuture(null);
    }

    private CompletableFuture<ResourceResponse[]> SendActivitiesInternal(
        List<Activity> activities,
        Iterator<SendActivitiesHandler> sendHandlers,
        Callable<CompletableFuture<ResourceResponse[]>> callAtBottom) throws Exception {
        if (activities == null)
            throw new IllegalArgumentException("activities");
        if (sendHandlers == null)
            throw new IllegalArgumentException("sendHandlers");

        if (false == sendHandlers.hasNext()) { // No middleware to run.
            if (callAtBottom != null)
                return completedFuture(await(callAtBottom.call()));
            return completedFuture(new ResourceResponse[0]);
        }

        // Default to "No more Middleware after this".
        Callable<CompletableFuture<ResourceResponse[]>> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.
            //Iterable<SendActivitiesHandler> remaining = sendHandlers.Skip(1);
            //Iterator<SendActivitiesHandler> remaining = sendHandlers.iterator();
            if (sendHandlers.hasNext())
                sendHandlers.next();
            return completedFuture(await(SendActivitiesInternal(activities, sendHandlers, callAtBottom)));
            };

        // Grab the current middleware, which is the 1st element in the array, and execute it
        SendActivitiesHandler caller = sendHandlers.next();
        return completedFuture(await(caller.handle(this, activities, next)));
    }

    //         private async Task<ResourceResponse> UpdateActivityInternal(Activity activity,
    //            IEnumerable<UpdateActivityHandler> updateHandlers,
    //            Func<Task<ResourceResponse>> callAtBottom)
    //        {
    //            BotAssert.ActivityNotNull(activity);
    //            if (updateHandlers == null)
    //                throw new ArgumentException(nameof(updateHandlers));
    //
    //            if (updateHandlers.Count() == 0) // No middleware to run.
    //            {
    //                if (callAtBottom != null)
    //                {
    //                    return await callAtBottom();
    //                }
    //
    //                return null;
    //            }
    //
    //          /**
    //           */ Default to "No more Middleware after this".
    //           */
    //            async Task<ResourceResponse> next()
    //            {
    //              /**
    //               */ Remove the first item from the list of middleware to call,
    //               */ so that the next call just has the remaining items to worry about.
    //               */
    //                IEnumerable<UpdateActivityHandler> remaining = updateHandlers.Skip(1);
    //                var result = await UpdateActivityInternal(activity, remaining, callAtBottom).ConfigureAwait(false);
    //                activity.Id = result.Id;
    //                return result;
    //            }
    //
    //          /**
    //           */ Grab the current middleware, which is the 1st element in the array, and execute it
    //           */
    //            UpdateActivityHandler toCall = updateHandlers.First();
    //            return await toCall(this, activity, next);
    //        }
    private CompletableFuture<ResourceResponse> UpdateActivityInternal(ActivityImpl activity,
            Iterator<UpdateActivityHandler> updateHandlers,
            Callable<CompletableFuture<ResourceResponse>> callAtBottom) throws Exception {
        BotAssert.ActivityNotNull(activity);
        if (updateHandlers == null)
            throw new IllegalArgumentException("updateHandlers");

        if (false == updateHandlers.hasNext()) { // No middleware to run.
            if (callAtBottom != null) {
                return completedFuture(await(callAtBottom.call()));
            }
            return completedFuture(null);
        }

        // Default to "No more Middleware after this".
        Callable<CompletableFuture<ResourceResponse[]>> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.
            if (updateHandlers.hasNext())
                updateHandlers.next();
            ResourceResponse result = null;
            result = await(UpdateActivityInternal(activity, updateHandlers, callAtBottom));
            activity.withId(result.id());
            return completedFuture(new ResourceResponse[] { result });
        };

        // Grab the current middleware, which is the 1st element in the array, and execute it
        UpdateActivityHandler toCall = updateHandlers.next();
        return completedFuture(await(toCall.handle(this, activity, next)));
    }



    private CompletableFuture DeleteActivityInternal(ConversationReference cr,
            Iterator<DeleteActivityHandler> updateHandlers,
            Callable<CompletableFuture> callAtBottom) throws Exception {
        BotAssert.ConversationReferenceNotNull(cr);
        if (updateHandlers == null)
            throw new IllegalArgumentException("updateHandlers");

        if (updateHandlers.hasNext() == false) { // No middleware to run.
            if (callAtBottom != null) {
                await(callAtBottom.call());
            }
            return completedFuture(null);
        }

        // Default to "No more Middleware after this".
        Callable<CompletableFuture> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.
            if (updateHandlers.hasNext())
                updateHandlers.next();

            await(DeleteActivityInternal(cr, updateHandlers, callAtBottom));
            return completedFuture(null);
        };

        // Grab the current middleware, which is the 1st element in the array, and execute it.
        DeleteActivityHandler toCall = updateHandlers.next();
        await(toCall.handle(this, cr, next));
        return completedFuture(null);
    }

    /**
     * Creates a conversation reference from an activity.
     * @param activity The activity.
     * @return A conversation reference for the conversation that contains the activity.
     * @throws IllegalArgumentException 
     * {@code activity} is {@code null}.
     */
    public static ConversationReference GetConversationReference(ActivityImpl activity) {
        BotAssert.ActivityNotNull(activity);

        ConversationReference r = new ConversationReference()
                .withActivityId(activity.id())
                .withUser(activity.from())
                .withBot(activity.recipient())
                .withConversation(activity.conversation())
                .withChannelId(activity.channelId())
                .withServiceUrl(activity.serviceUrl());

        return r;
    }

    /**
     * Updates an activity with the delivery information from an existing
     * conversation reference.
     * @param activity The activity to update.
     * @param reference The conversation reference.
     * @param isIncoming (Optional) {@code true} to treat the activity as an
     * incoming activity, where the bot is the recipient; otherwaire {@code false}.
     * Default is {@code false}, and the activity will show the bot as the sender.
     * <remarks>Call {@link GetConversationReference(Activity)} on an incoming
     * activity to get a conversation reference that you can then use to update an
     * outgoing activity with the correct delivery information.
     * <p>The {@link SendActivity(IActivity)} and {@link SendActivities(IActivity[])}
     * methods do this for you.</p>
     * </remarks>
     */
    public static Activity ApplyConversationReference(Activity activity, ConversationReference reference) {
        return ApplyConversationReference(activity, reference, false);
    }
    public static Activity ApplyConversationReference(Activity activity, ConversationReference reference, boolean isIncoming) {
        activity.withChannelId(reference.channelId());
        activity.withServiceUrl(reference.serviceUrl());
        activity.withConversation(reference.conversation());

        if (isIncoming) {
            activity.withFrom(reference.user());
            activity.withRecipient(reference.bot());
            if (reference.activityId() != null)
                activity.withId(reference.activityId());
        }
        else  { // Outgoing
            activity.withFrom(reference.bot());
            activity.withRecipient(reference.user());
            if (reference.activityId() != null)
                activity.withReplyToId(reference.activityId());
        }
        return activity;
    }

    public void close() throws Exception {
        _turnServices.close();
    }
}
