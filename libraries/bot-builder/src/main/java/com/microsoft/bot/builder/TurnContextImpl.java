package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.bot.schema.ResourceResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.microsoft.bot.schema.ActivityTypes.MESSAGE;
import static com.microsoft.bot.schema.ActivityTypes.TRACE;
import static java.util.stream.Collectors.toList;

/**
 * Provides context for a turn of a bot.
 * Context provides information needed to process an incoming activity.
 * The context object is created by a {@link BotAdapter} and persists for the
 * length of the turn.
 * {@linkalso Bot}
 * {@linkalso Middleware}
 */
public class TurnContextImpl implements TurnContext, AutoCloseable {
    private final BotAdapter adapter;
    private final Activity activity;
    private final List<SendActivitiesHandler> onSendActivities = new ArrayList<SendActivitiesHandler>();
    private final List<UpdateActivityHandler> onUpdateActivity = new ArrayList<UpdateActivityHandler>();
    private final List<DeleteActivityHandler> onDeleteActivity = new ArrayList<DeleteActivityHandler>();
    private final TurnContextStateCollection turnState;
    private Boolean responded = false;

    /**
     * Creates a context object.
     *
     * @param adapter  The adapter creating the context.
     * @param activity The incoming activity for the turn;
     *                 or {@code null} for a turn for a proactive message.
     * @throws IllegalArgumentException {@code activity} or
     *                                  {@code adapter} is {@code null}.
     *                                  For use by bot adapter implementations only.
     */
    public TurnContextImpl(BotAdapter adapter, Activity activity) {
        if (adapter == null)
            throw new IllegalArgumentException("adapter");
        this.adapter = adapter;
        if (activity == null)
            throw new IllegalArgumentException("activity");
        this.activity = activity;

        turnState = new TurnContextStateCollection();
    }

    /**
     * Creates a conversation reference from an activity.
     *
     * @param activity The activity.
     * @return A conversation reference for the conversation that contains the activity.
     * @throws IllegalArgumentException {@code activity} is {@code null}.
     */
    public static ConversationReference getConversationReference(Activity activity) {
        BotAssert.activityNotNull(activity);

        ConversationReference r = new ConversationReference() {{
            setActivityId(activity.getId());
            setUser(activity.getFrom());
            setBot(activity.getRecipient());
            setConversation(activity.getConversation());
            setChannelId(activity.getChannelId());
            setServiceUrl(activity.getServiceUrl());
        }};

        return r;
    }

    /**
     * Updates an activity with the delivery information from an existing
     * conversation reference.
     *
     * @param activity  The activity to update.
     * @param reference The conversation reference.
     */
    public static Activity applyConversationReference(Activity activity, ConversationReference reference) {
        return applyConversationReference(activity, reference, false);
    }

    /**
     * Updates an activity with the delivery information from an existing
     * conversation reference.
     *
     * @param activity   The activity to update.
     * @param reference  The conversation reference.
     * @param isIncoming (Optional) {@code true} to treat the activity as an
     *                   incoming activity, where the bot is the recipient; otherwaire {@code false}.
     *                   Default is {@code false}, and the activity will show the bot as the sender.
     *                   Call {@link #getConversationReference(Activity)} on an incoming
     *                   activity to get a conversation reference that you can then use to update an
     *                   outgoing activity with the correct delivery information.
     *                   <p>The {@link #sendActivity(Activity)} and {@link #sendActivities(Activity[])}
     *                   methods do this for you.</p>
     */
    public static Activity applyConversationReference(Activity activity,
                                                      ConversationReference reference,
                                                      boolean isIncoming) {

        activity.setChannelId(reference.getChannelId());
        activity.setServiceUrl(reference.getServiceUrl());
        activity.setConversation(reference.getConversation());

        if (isIncoming) {
            activity.setFrom(reference.getUser());
            activity.setRecipient(reference.getBot());
            if (reference.getActivityId() != null)
                activity.setId(reference.getActivityId());
        } else { // Outgoing
            activity.setFrom(reference.getBot());
            activity.setRecipient(reference.getUser());
            if (reference.getActivityId() != null)
                activity.setReplyToId(reference.getActivityId());
        }
        return activity;
    }

    /**
     * Adds a response handler for send activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}.
     *                                  When the context's {@link #sendActivity(Activity)}
     *                                  or {@link #sendActivities(Activity[])} methods are called,
     *                                  the adapter calls the registered handlers in the order in which they were
     *                                  added to the context object.
     */
    @Override
    public TurnContext onSendActivities(SendActivitiesHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("handler");

        this.onSendActivities.add(handler);
        return this;
    }

    /**
     * Adds a response handler for update activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}.
     *                                  When the context's {@link #updateActivity(Activity)} is called,
     *                                  the adapter calls the registered handlers in the order in which they were
     *                                  added to the context object.
     */
    @Override
    public TurnContext onUpdateActivity(UpdateActivityHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("handler");

        this.onUpdateActivity.add(handler);
        return this;
    }

    /**
     * Adds a response handler for delete activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}.
     *                                  When the context's {@link #deleteActivity(String)} is called,
     *                                  the adapter calls the registered handlers in the order in which they were
     *                                  added to the context object.
     */
    @Override
    public TurnContext onDeleteActivity(DeleteActivityHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("handler");

        this.onDeleteActivity.add(handler);
        return this;
    }

    /**
     * Gets the bot adapter that created this context object.
     */
    public BotAdapter getAdapter() {
        return this.adapter;
    }

    /**
     * Gets the services registered on this context object.
     */
    public TurnContextStateCollection getTurnState() {
        return this.turnState;
    }

    /**
     * Gets the activity associated with this turn; or {@code null} when processing
     * a proactive message.
     */
    @Override
    public Activity getActivity() {
        return this.activity;
    }

    /**
     * Indicates whether at least one response was sent for the current turn.
     *
     * @return {@code true} if at least one response was sent for the current turn.
     * @throws IllegalArgumentException You attempted to set the value to {@code false}.
     */
    @Override
    public boolean getResponded() {
        return this.responded;
    }

    private void setResponded(boolean responded) {
        if (responded == false) {
            throw new IllegalArgumentException("TurnContext: cannot set 'responded' to a value of 'false'.");
        }
        this.responded = true;
    }

    /**
     * Sends a message activity to the sender of the incoming activity.
     *
     * @param textReplyToSend The text of the message to send.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException {@code textReplyToSend} is {@code null} or whitespace.
     *                                  If the activity is successfully sent, the task result contains
     *                                  a {@link ResourceResponse} object containing the ID that the receiving
     *                                  channel assigned to the activity.
     *                                  <p>See the channel's documentation for limits imposed upon the contents of
     *                                  {@code textReplyToSend}.</p>
     *                                  <p>To control various characteristics of your bot's speech such as voice,
     *                                  rate, volume, pronunciation, and pitch, specify {@code speak} in
     *                                  Speech Synthesis Markup Language (SSML) format.</p>
     */
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend) {
        return sendActivity(textReplyToSend, null, null);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak) {
        return sendActivity(textReplyToSend, speak, null);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak, String inputHint) {
        if (StringUtils.isEmpty(textReplyToSend))
            throw new IllegalArgumentException("textReplyToSend");

        Activity activityToSend = new Activity(MESSAGE) {{
            setText(textReplyToSend);
        }};
        if (speak != null)
            activityToSend.setSpeak(speak);

        if (StringUtils.isNotEmpty(inputHint))
            activityToSend.setInputHint(InputHints.fromString(inputHint));

        return sendActivity(activityToSend);
    }

    /**
     * Sends an activity to the sender of the incoming activity.
     *
     * @param activity The activity to send.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException {@code activity} is {@code null}.
     *                                  If the activity is successfully sent, the task result contains
     *                                  a {@link ResourceResponse} object containing the ID that the receiving
     *                                  channel assigned to the activity.
     */
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("activity");
        }

        Activity[] activities = {activity};
        return sendActivities(activities)
            .thenApply(resourceResponses -> {
                if (resourceResponses == null || resourceResponses.length == 0) {
                    return null;
                }
                return resourceResponses[0];
            });
    }

    /**
     * Sends a set of activities to the sender of the incoming activity.
     *
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute.
     * If the activities are successfully sent, the task result contains
     * an array of {@link ResourceResponse} objects containing the IDs that
     * the receiving channel assigned to the activities.
     */
    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(Activity[] activities) {
        // Bind the relevant Conversation Reference properties, such as URLs and
        // ChannelId's, to the activities we're about to send.
        ConversationReference cr = getConversationReference(this.activity);
        for (Activity a : activities) {
            applyConversationReference(a, cr);
        }

        // Convert the IActivities to Activities.
        List<Activity> activityArray = Arrays.stream(activities).map(input -> input).collect(toList());

        // Create the list used by the recursive methods.
        List<Activity> activityList = new ArrayList<Activity>(activityArray);

        Supplier<CompletableFuture<ResourceResponse[]>> actuallySendStuff = () -> {
            // Send from the list, which may have been manipulated via the event handlers.
            // Note that 'responses' was captured from the root of the call, and will be
            // returned to the original caller.
            return getAdapter().sendActivities(this, activityList.toArray(new Activity[activityList.size()]))
                .thenApply(responses -> {
                    if (responses != null && responses.length == activityList.size()) {
                        // stitch up activity ids
                        for (int i = 0; i < responses.length; i++) {
                            ResourceResponse response = responses[i];
                            Activity activity = activityList.get(i);
                            activity.setId(response.getId());
                        }
                    }

                    // Are the any non-trace activities to send?
                    // The thinking here is that a Trace event isn't user relevant data
                    // so the "Responded" flag should not be set by Trace messages being
                    // sent out.
                    if (activityList.stream().anyMatch((a) -> a.getType() == TRACE)) {
                        this.setResponded(true);
                    }
                    return responses;
                });
        };

        List<Activity> act_list = new ArrayList<>(activityList);
        return sendActivitiesInternal(act_list, onSendActivities.iterator(), actuallySendStuff);
    }

    /**
     * Replaces an existing activity.
     *
     * @param activity New replacement activity.
     * @return A task that represents the work queued to execute.
     * @throws com.microsoft.bot.connector.rest.ErrorResponseException The HTTP operation failed and the response contained additional information.
     */
    @Override
    public CompletableFuture<ResourceResponse> updateActivity(Activity activity) {
        Supplier<CompletableFuture<ResourceResponse>> ActuallyUpdateStuff = () -> {
            return getAdapter().updateActivity(this, activity);
        };

        return updateActivityInternal(activity, onUpdateActivity.iterator(), ActuallyUpdateStuff);
    }

    /**
     * Deletes an existing activity.
     *
     * @param activityId The ID of the activity to delete.
     * @return A task that represents the work queued to execute.
     * @throws Exception The HTTP operation failed and the response contained additional information.
     */
    public CompletableFuture<Void> deleteActivity(String activityId) {
        if (StringUtils.isWhitespace(activityId) || activityId == null) {
            throw new IllegalArgumentException("activityId");
        }

        ConversationReference cr = getConversationReference(getActivity());
        cr.setActivityId(activityId);

        Supplier<CompletableFuture<Void>> ActuallyDeleteStuff = () ->
            getAdapter().deleteActivity(this, cr);

        return deleteActivityInternal(cr, onDeleteActivity.iterator(), ActuallyDeleteStuff);
    }

    /**
     * Deletes an existing activity.
     *
     * @param conversationReference The conversation containing the activity to delete.
     * @return A task that represents the work queued to execute.
     * @throws com.microsoft.bot.connector.rest.ErrorResponseException The HTTP operation failed and the response contained additional information.
     *                                                                 The conversation reference's {@link ConversationReference#getActivityId}
     *                                                                 indicates the activity in the conversation to delete.
     */
    @Override
    public CompletableFuture<Void> deleteActivity(ConversationReference conversationReference) {
        if (conversationReference == null)
            throw new IllegalArgumentException("conversationReference");

        Supplier<CompletableFuture<Void>> ActuallyDeleteStuff = () ->
            getAdapter().deleteActivity(this, conversationReference);

        return deleteActivityInternal(conversationReference, onDeleteActivity.iterator(), ActuallyDeleteStuff);
    }

    private CompletableFuture<ResourceResponse[]> sendActivitiesInternal(
        List<Activity> activities,
        Iterator<SendActivitiesHandler> sendHandlers,
        Supplier<CompletableFuture<ResourceResponse[]>> callAtBottom) {

        if (activities == null) {
            throw new IllegalArgumentException("activities");
        }
        if (sendHandlers == null) {
            throw new IllegalArgumentException("sendHandlers");
        }

        if (!sendHandlers.hasNext()) { // No middleware to run.
            if (callAtBottom != null) {
                return callAtBottom.get();
            }
            return CompletableFuture.completedFuture(new ResourceResponse[0]);
        }

        // Default to "No more Middleware after this".
        Supplier<CompletableFuture<ResourceResponse[]>> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.
            //Iterable<SendActivitiesHandler> remaining = sendHandlers.Skip(1);
            //Iterator<SendActivitiesHandler> remaining = sendHandlers.iterator();
            if (sendHandlers.hasNext())
                sendHandlers.next();
            return sendActivitiesInternal(activities, sendHandlers, callAtBottom);
        };

        // Grab the current middleware, which is the 1st element in the array, and execute it
        SendActivitiesHandler caller = sendHandlers.next();
        return caller.invoke(this, activities, next);
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
    private CompletableFuture<ResourceResponse> updateActivityInternal(Activity activity,
                                                                       Iterator<UpdateActivityHandler> updateHandlers,
                                                                       Supplier<CompletableFuture<ResourceResponse>> callAtBottom) {
        BotAssert.activityNotNull(activity);
        if (updateHandlers == null)
            throw new IllegalArgumentException("updateHandlers");

        if (false == updateHandlers.hasNext()) { // No middleware to run.
            if (callAtBottom != null) {
                return callAtBottom.get();
            }
            return null;
        }

        // Default to "No more Middleware after this".
        Supplier<CompletableFuture<ResourceResponse>> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.
            if (updateHandlers.hasNext())
                updateHandlers.next();

            return updateActivityInternal(activity, updateHandlers, callAtBottom)
                .thenApply(resourceResponse -> {
                    activity.setId(resourceResponse.getId());
                    return resourceResponse;
                });
        };

        // Grab the current middleware, which is the 1st element in the array, and execute it
        UpdateActivityHandler toCall = updateHandlers.next();
        return toCall.invoke(this, activity, next);
    }

    private CompletableFuture<Void> deleteActivityInternal(ConversationReference cr,
                                                           Iterator<DeleteActivityHandler> deleteHandlers,
                                                           Supplier<CompletableFuture<Void>> callAtBottom) {
        BotAssert.conversationReferenceNotNull(cr);
        if (deleteHandlers == null)
            throw new IllegalArgumentException("deleteHandlers");

        if (!deleteHandlers.hasNext()) { // No middleware to run.
            if (callAtBottom != null) {
                return callAtBottom.get();
            }
            return CompletableFuture.completedFuture(null);
        }

        // Default to "No more Middleware after this".
        Supplier<CompletableFuture<Void>> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.

            //Iterator<UpdateActivityHandler> remaining = (deleteHandlers.hasNext()) ? deleteHandlers.next() : null;
            if (deleteHandlers.hasNext())
                deleteHandlers.next();

            return deleteActivityInternal(cr, deleteHandlers, callAtBottom);
        };

        // Grab the current middleware, which is the 1st element in the array, and execute it.
        DeleteActivityHandler toCall = deleteHandlers.next();
        return toCall.invoke(this, cr, next);
    }

    public void close() throws Exception {
        turnState.close();
    }
}
