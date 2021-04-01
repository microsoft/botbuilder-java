// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.DeliveryModes;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.bot.schema.ResourceResponse;
import java.util.Locale;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides context for a turn of a bot. Context provides information needed to
 * process an incoming activity. The context object is created by a
 * {@link BotAdapter} and persists for the length of the turn. {@link Bot}
 * {@link Middleware}
 */
public class TurnContextImpl implements TurnContext, AutoCloseable {
    /**
     * The bot adapter that created this context object.
     */
    private final BotAdapter adapter;

    /**
     * The activity associated with this turn; or null when processing a proactive
     * message.
     */
    private final Activity activity;

    private List<Activity> bufferedReplyActivities = new ArrayList<>();

    /**
     * Response handlers for send activity operations.
     */
    private final List<SendActivitiesHandler> onSendActivities = new ArrayList<>();

    /**
     * Response handlers for update activity operations.
     */
    private final List<UpdateActivityHandler> onUpdateActivity = new ArrayList<>();

    /**
     * Response handlers for delete activity operations.
     */
    private final List<DeleteActivityHandler> onDeleteActivity = new ArrayList<>();

    /**
     * The services registered on this context object.
     */
    private final TurnContextStateCollection turnState;

    /**
     * Indicates whether at least one response was sent for the current turn.
     */
    private Boolean responded = false;

    /**
     * Creates a context object.
     *
     * @param withAdapter  The adapter creating the context.
     * @param withActivity The incoming activity for the turn; or {@code null} for a
     *                     turn for a proactive message.
     * @throws IllegalArgumentException {@code activity} or {@code adapter} is
     *                                  {@code null}. For use by bot adapter
     *                                  implementations only.
     */
    public TurnContextImpl(BotAdapter withAdapter, Activity withActivity) {
        if (withAdapter == null) {
            throw new IllegalArgumentException("adapter");
        }
        adapter = withAdapter;

        if (withActivity == null) {
            throw new IllegalArgumentException("activity");
        }
        activity = withActivity;

        turnState = new TurnContextStateCollection();
    }

    /**
     * Adds a response handler for send activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}. When the
     *                                  context's {@link #sendActivity(Activity)} or
     *                                  {@link #sendActivities(List)} methods are
     *                                  called, the adapter calls the registered
     *                                  handlers in the order in which they were
     *                                  added to the context object.
     */
    @Override
    public TurnContext onSendActivities(SendActivitiesHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler");
        }

        onSendActivities.add(handler);
        return this;
    }

    /**
     * Adds a response handler for update activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}. When the
     *                                  context's {@link #updateActivity(Activity)}
     *                                  is called, the adapter calls the registered
     *                                  handlers in the order in which they were
     *                                  added to the context object.
     */
    @Override
    public TurnContext onUpdateActivity(UpdateActivityHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler");
        }

        onUpdateActivity.add(handler);
        return this;
    }

    /**
     * Adds a response handler for delete activity operations.
     *
     * @param handler The handler to add to the context object.
     * @return The updated context object.
     * @throws IllegalArgumentException {@code handler} is {@code null}. When the
     *                                  context's {@link #deleteActivity(String)} is
     *                                  called, the adapter calls the registered
     *                                  handlers in the order in which they were
     *                                  added to the context object.
     */
    @Override
    public TurnContext onDeleteActivity(DeleteActivityHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler");
        }

        onDeleteActivity.add(handler);
        return this;
    }

    /**
     * Gets the bot adapter that created this context object.
     *
     * @return The BotAdaptor for this turn.
     */
    public BotAdapter getAdapter() {
        return this.adapter;
    }

    /**
     * Gets the services registered on this context object.
     *
     * @return the TurnContextStateCollection for this turn.
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
     */
    @Override
    public boolean getResponded() {
        return responded;
    }

    /**
     * Gets the locale on this context object.
     * @return The string of locale on this context object.
     */
    @Override
    public String getLocale() {
        return getTurnState().get(STATE_TURN_LOCALE);
    }

    /**
     * Set  the locale on this context object.
     * @param withLocale The string of locale on this context object.
     */
    @Override
    public void setLocale(String withLocale) {
        if (StringUtils.isEmpty(withLocale)) {
            getTurnState().remove(STATE_TURN_LOCALE);
        } else if (
            LocaleUtils.isAvailableLocale(new Locale.Builder().setLanguageTag(withLocale).build())
        ) {
            getTurnState().replace(STATE_TURN_LOCALE, withLocale);
        } else {
            getTurnState().replace(STATE_TURN_LOCALE, Locale.ENGLISH.getCountry());
        }
    }

    /**
     * Gets a list of activities to send when `context.Activity.DeliveryMode == 'expectReplies'.
     * @return A list of activities.
     */
    public List<Activity> getBufferedReplyActivities() {
        return bufferedReplyActivities;
    }

    /**
     * Sends a message activity to the sender of the incoming activity.
     *
     * <p>
     * If the activity is successfully sent, the task result contains a
     * {@link ResourceResponse} object containing the ID that the receiving channel
     * assigned to the activity.
     * </p>
     *
     * <p>
     * See the channel's documentation for limits imposed upon the contents of
     * {@code textReplyToSend}.
     * </p>
     *
     * @param textReplyToSend The text of the message to send.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException {@code textReplyToSend} is {@code null} or
     *                                  whitespace.
     */
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend) {
        return sendActivity(textReplyToSend, null, null);
    }

    /**
     * Sends a message activity to the sender of the incoming activity.
     *
     * <p>
     * If the activity is successfully sent, the task result contains a
     * {@link ResourceResponse} object containing the ID that the receiving channel
     * assigned to the activity.
     * </p>
     *
     * <p>
     * See the channel's documentation for limits imposed upon the contents of
     * {@code textReplyToSend}.
     * </p>
     *
     * @param textReplyToSend The text of the message to send.
     * @param speak           To control various characteristics of your bot's
     *                        speech such as voice rate, volume, pronunciation, and
     *                        pitch, specify Speech Synthesis Markup Language (SSML)
     *                        format.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException {@code textReplyToSend} is {@code null} or
     *                                  whitespace.
     */
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak) {
        return sendActivity(textReplyToSend, speak, null);
    }

    /**
     * Sends a message activity to the sender of the incoming activity.
     *
     * <p>
     * If the activity is successfully sent, the task result contains a
     * {@link ResourceResponse} object containing the ID that the receiving channel
     * assigned to the activity.
     * </p>
     *
     * <p>
     * See the channel's documentation for limits imposed upon the contents of
     * {@code textReplyToSend}.
     * </p>
     *
     * @param textReplyToSend The text of the message to send.
     * @param speak           To control various characteristics of your bot's
     *                        speech such as voice rate, volume, pronunciation, and
     *                        pitch, specify Speech Synthesis Markup Language (SSML)
     *                        format.
     * @param inputHint       (Optional) Input hint.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException {@code textReplyToSend} is {@code null} or
     *                                  whitespace.
     */
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(
        String textReplyToSend,
        String speak,
        InputHints inputHint
    ) {
        if (StringUtils.isEmpty(textReplyToSend)) {
            return Async.completeExceptionally(new IllegalArgumentException("textReplyToSend"));
        }

        Activity activityToSend = new Activity(ActivityTypes.MESSAGE);
        activityToSend.setText(textReplyToSend);

        if (StringUtils.isNotEmpty(speak)) {
            activityToSend.setSpeak(speak);
        }

        if (inputHint != null) {
            activityToSend.setInputHint(inputHint);
        }

        return sendActivity(activityToSend);
    }

    /**
     * Sends an activity to the sender of the incoming activity.
     *
     * @param activityToSend The activity to send.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException {@code activity} is {@code null}. If the
     *                                  activity is successfully sent, the task
     *                                  result contains a {@link ResourceResponse}
     *                                  object containing the ID that the receiving
     *                                  channel assigned to the activity.
     */
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(Activity activityToSend) {
        if (activityToSend == null) {
            return Async.completeExceptionally(new IllegalArgumentException("Activity"));
        }

        return sendActivities(Collections.singletonList(activityToSend))
            .thenApply(resourceResponses -> {
                if (resourceResponses == null || resourceResponses.length == 0) {
                    // It's possible an interceptor prevented the activity from having been sent.
                    // Just return an empty response in that case.
                    return new ResourceResponse();
                }
                return resourceResponses[0];
            });
    }

    /**
     * Sends a set of activities to the sender of the incoming activity.
     *
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute. If the activities
     *         are successfully sent, the task result contains an array of
     *         {@link ResourceResponse} objects containing the IDs that the
     *         receiving channel assigned to the activities.
     */
    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(List<Activity> activities) {
        if (activities == null || activities.size() == 0) {
            return Async.completeExceptionally(new IllegalArgumentException("activities"));
        }

        // Bind the relevant Conversation Reference properties, such as URLs and
        // ChannelId's, to the activities we're about to send.
        ConversationReference cr = activity.getConversationReference();

        // Buffer the incoming activities into a List<T> since we allow the set to be
        // manipulated by the callbacks
        // Bind the relevant Conversation Reference properties, such as URLs and
        // ChannelId's, to the activity we're about to send
        List<Activity> bufferedActivities = activities.stream()
            .map(a -> a.applyConversationReference(cr))
            .collect(Collectors.toList());

        if (onSendActivities.size() == 0) {
            return sendActivitiesThroughAdapter(bufferedActivities);
        }

        return sendActivitiesThroughCallbackPipeline(bufferedActivities, 0);
    }

    private CompletableFuture<ResourceResponse[]> sendActivitiesThroughAdapter(
        List<Activity> activities
    ) {
        if (DeliveryModes.fromString(getActivity().getDeliveryMode()) == DeliveryModes.EXPECT_REPLIES) {
            ResourceResponse[] responses = new ResourceResponse[activities.size()];
            boolean sentNonTraceActivity = false;

            for (int index = 0; index < responses.length; index++) {
                Activity sendActivity = activities.get(index);
                bufferedReplyActivities.add(sendActivity);

                // Ensure the TurnState has the InvokeResponseKey, since this activity
                // is not being sent through the adapter, where it would be added to TurnState.
                if (activity.isType(ActivityTypes.INVOKE_RESPONSE)) {
                    getTurnState().add(BotFrameworkAdapter.INVOKE_RESPONSE_KEY, activity);
                }

                responses[index] = new ResourceResponse();
                sentNonTraceActivity |= !sendActivity.isType(ActivityTypes.TRACE);
            }

            if (sentNonTraceActivity) {
                responded = true;
            }

            return CompletableFuture.completedFuture(responses);
        } else {
            return adapter.sendActivities(this, activities).thenApply(responses -> {
                boolean sentNonTraceActivity = false;

                for (int index = 0; index < responses.length; index++) {
                    Activity sendActivity = activities.get(index);
                    sendActivity.setId(responses[index].getId());
                    sentNonTraceActivity |= !sendActivity.isType(ActivityTypes.TRACE);
                }

                if (sentNonTraceActivity) {
                    responded = true;
                }

                return responses;
            });
        }
    }

    private CompletableFuture<ResourceResponse[]> sendActivitiesThroughCallbackPipeline(
        List<Activity> activities,
        int nextCallbackIndex
    ) {
        if (nextCallbackIndex == onSendActivities.size()) {
            return sendActivitiesThroughAdapter(activities);
        }

        return onSendActivities.get(nextCallbackIndex)
            .invoke(
                this, activities,
                () -> sendActivitiesThroughCallbackPipeline(activities, nextCallbackIndex + 1)
            );
    }

    /**
     * Replaces an existing activity.
     *
     * @param withActivity New replacement activity.
     * @return A task that represents the work queued to execute.
     * @throws com.microsoft.bot.connector.rest.ErrorResponseException The HTTP
     *                                                                 operation
     *                                                                 failed and
     *                                                                 the response
     *                                                                 contained
     *                                                                 additional
     *                                                                 information.
     */
    @Override
    public CompletableFuture<ResourceResponse> updateActivity(Activity withActivity) {
        if (withActivity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("Activity"));
        }

        ConversationReference conversationReference = activity.getConversationReference();
        withActivity.applyConversationReference(conversationReference);

        Supplier<CompletableFuture<ResourceResponse>> actuallyUpdateStuff = () -> getAdapter()
            .updateActivity(this, withActivity);

        return updateActivityInternal(
            withActivity, onUpdateActivity.iterator(), actuallyUpdateStuff
        );
    }

    private CompletableFuture<ResourceResponse> updateActivityInternal(
        Activity updateActivity,
        Iterator<UpdateActivityHandler> updateHandlers,
        Supplier<CompletableFuture<ResourceResponse>> callAtBottom
    ) {
        if (updateActivity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("Activity"));
        }
        if (updateHandlers == null) {
            return Async.completeExceptionally(new IllegalArgumentException("updateHandlers"));
        }

        // No middleware to run.
        if (!updateHandlers.hasNext()) {
            if (callAtBottom != null) {
                return callAtBottom.get();
            }
            return CompletableFuture.completedFuture(null);
        }

        // Default to "No more Middleware after this".
        Supplier<CompletableFuture<ResourceResponse>> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.
            if (updateHandlers.hasNext()) {
                updateHandlers.next();
            }

            return updateActivityInternal(updateActivity, updateHandlers, callAtBottom)
                .thenApply(resourceResponse -> {
                    updateActivity.setId(resourceResponse.getId());
                    return resourceResponse;
                });
        };

        // Grab the current middleware, which is the 1st element in the array, and
        // execute it
        UpdateActivityHandler toCall = updateHandlers.next();
        return toCall.invoke(this, updateActivity, next);
    }

    /**
     * Deletes an existing activity.
     *
     * @param activityId The ID of the activity to delete.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> deleteActivity(String activityId) {
        if (StringUtils.isWhitespace(activityId) || StringUtils.isEmpty(activityId)) {
            return Async.completeExceptionally(new IllegalArgumentException("activityId"));
        }

        ConversationReference cr = activity.getConversationReference();
        cr.setActivityId(activityId);

        Supplier<CompletableFuture<Void>> actuallyDeleteStuff = () -> getAdapter()
            .deleteActivity(this, cr);

        return deleteActivityInternal(cr, onDeleteActivity.iterator(), actuallyDeleteStuff);
    }

    /**
     * Deletes an existing activity.
     *
     * The conversation reference's {@link ConversationReference#getActivityId}
     * indicates the activity in the conversation to delete.
     *
     * @param conversationReference The conversation containing the activity to
     *                              delete.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> deleteActivity(ConversationReference conversationReference) {
        if (conversationReference == null) {
            return Async.completeExceptionally(new IllegalArgumentException("conversationReference"));
        }

        Supplier<CompletableFuture<Void>> actuallyDeleteStuff = () -> getAdapter()
            .deleteActivity(this, conversationReference);

        return deleteActivityInternal(
            conversationReference, onDeleteActivity.iterator(), actuallyDeleteStuff
        );
    }

    private CompletableFuture<Void> deleteActivityInternal(
        ConversationReference cr,
        Iterator<DeleteActivityHandler> deleteHandlers,
        Supplier<CompletableFuture<Void>> callAtBottom
    ) {
        if (cr == null) {
            return Async.completeExceptionally(new IllegalArgumentException("ConversationReference"));
        }
        if (deleteHandlers == null) {
            return Async.completeExceptionally(new IllegalArgumentException("deleteHandlers"));
        }

        // No middleware to run.
        if (!deleteHandlers.hasNext()) {
            if (callAtBottom != null) {
                return callAtBottom.get();
            }
            return CompletableFuture.completedFuture(null);
        }

        // Default to "No more Middleware after this".
        Supplier<CompletableFuture<Void>> next = () -> {
            // Remove the first item from the list of middleware to call,
            // so that the next call just has the remaining items to worry about.
            if (deleteHandlers.hasNext()) {
                deleteHandlers.next();
            }

            return deleteActivityInternal(cr, deleteHandlers, callAtBottom);
        };

        // Grab the current middleware, which is the 1st element in the array, and
        // execute it.
        DeleteActivityHandler toCall = deleteHandlers.next();
        return toCall.invoke(this, cr, next);
    }

    /**
     * Auto call of {@link #close}.
     */
    @Override
    public void finalize() {
        try {
            close();
        } catch (Exception ignored) {

        }
    }

    /**
     * AutoClosable#close.
     *
     * @throws Exception If the TurnContextStateCollection.
     */
    @Override
    public void close() throws Exception {
        turnState.close();
    }
}
