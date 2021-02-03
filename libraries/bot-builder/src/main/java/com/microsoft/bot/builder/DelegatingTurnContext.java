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
 * A TurnContext that wraps an untyped inner TurnContext.
 */
public class DelegatingTurnContext implements TurnContext {
    /**
     * The TurnContext being wrapped.
     */
    private TurnContext innerTurnContext;

    /**
     * Initializes a new instance of the DelegatingTurnContext class.
     * 
     * @param withTurnContext The TurnContext to wrap.
     */
    public DelegatingTurnContext(TurnContext withTurnContext) {
        innerTurnContext = withTurnContext;
    }

    /**
     * Gets the locale on this context object.
     * @return The string of locale on this context object.
     */
    @Override
    public String getLocale() {
        return innerTurnContext.getLocale();
    }

    /**
     * Set  the locale on this context object.
     * @param withLocale The string of locale on this context object.
     */
    @Override
    public void setLocale(String withLocale) {
        innerTurnContext.setLocale(withLocale);
    }

    /**
     * Gets the inner context's activity.
     * 
     * @return The inner {@link TurnContext#getAdapter()}.
     */
    @Override
    public BotAdapter getAdapter() {
        return innerTurnContext.getAdapter();
    }

    /**
     * Gets the inner context's activity.
     * 
     * @return The inner {@link TurnContext#getTurnState()}.
     */
    @Override
    public TurnContextStateCollection getTurnState() {
        return innerTurnContext.getTurnState();
    }

    /**
     * Gets the inner context's activity.
     * 
     * @return The inner {@link TurnContext#getActivity()}.
     */
    @Override
    public Activity getActivity() {
        return innerTurnContext.getActivity();
    }

    /**
     * Gets the inner context's responded value.
     * 
     * @return The inner {@link TurnContext#getResponded()}.
     */
    @Override
    public boolean getResponded() {
        return innerTurnContext.getResponded();
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend) {
        return innerTurnContext.sendActivity(textReplyToSend);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak) {
        return innerTurnContext.sendActivity(textReplyToSend, speak);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(
        String textReplyToSend,
        String speak,
        InputHints inputHint
    ) {
        return innerTurnContext.sendActivity(textReplyToSend, speak, inputHint);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<ResourceResponse> sendActivity(Activity activity) {
        return innerTurnContext.sendActivity(activity);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(List<Activity> activities) {
        return innerTurnContext.sendActivities(activities);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<ResourceResponse> updateActivity(Activity activity) {
        return innerTurnContext.updateActivity(activity);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<Void> deleteActivity(String activityId) {
        return innerTurnContext.deleteActivity(activityId);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public CompletableFuture<Void> deleteActivity(ConversationReference conversationReference) {
        return innerTurnContext.deleteActivity(conversationReference);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public TurnContext onSendActivities(SendActivitiesHandler handler) {
        return innerTurnContext.onSendActivities(handler);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public TurnContext onUpdateActivity(UpdateActivityHandler handler) {
        return innerTurnContext.onUpdateActivity(handler);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public TurnContext onDeleteActivity(DeleteActivityHandler handler) {
        return innerTurnContext.onDeleteActivity(handler);
    }
}
