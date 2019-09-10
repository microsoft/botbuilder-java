// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;

import java.util.concurrent.CompletableFuture;

/**
 * A TurnContext that wraps an untyped inner TurnContext.
 */
public class DelegatingTurnContext implements TurnContext {
    private TurnContext innerTurnContext;

    public DelegatingTurnContext(TurnContext withTurnContext) {
        innerTurnContext = withTurnContext;
    }

    @Override
    public BotAdapter getAdapter() {
        return innerTurnContext.getAdapter();
    }

    @Override
    public TurnContextStateCollection getTurnState() {
        return innerTurnContext.getTurnState();
    }

    @Override
    public Activity getActivity() {
        return innerTurnContext.getActivity();
    }

    @Override
    public boolean getResponded() {
        return innerTurnContext.getResponded();
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend) {
        return innerTurnContext.sendActivity(textReplyToSend);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak) {
        return innerTurnContext.sendActivity(textReplyToSend, speak);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak, String inputHint) {
        return innerTurnContext.sendActivity(textReplyToSend, speak, inputHint);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivity(Activity activity) {
        return innerTurnContext.sendActivity(activity);
    }

    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(Activity[] activities) {
        return innerTurnContext.sendActivities(activities);
    }

    @Override
    public CompletableFuture<ResourceResponse> updateActivity(Activity activity) {
        return innerTurnContext.updateActivity(activity);
    }

    @Override
    public CompletableFuture<Void> deleteActivity(String activityId) {
        return innerTurnContext.deleteActivity(activityId);
    }

    @Override
    public CompletableFuture<Void> deleteActivity(ConversationReference conversationReference) {
        return innerTurnContext.deleteActivity(conversationReference);
    }

    @Override
    public TurnContext onSendActivities(SendActivitiesHandler handler) {
        return innerTurnContext.onSendActivities(handler);
    }

    @Override
    public TurnContext onUpdateActivity(UpdateActivityHandler handler) {
        return innerTurnContext.onUpdateActivity(handler);
    }

    @Override
    public TurnContext onDeleteActivity(DeleteActivityHandler handler) {
        return innerTurnContext.onDeleteActivity(handler);
    }
}
