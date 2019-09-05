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
    public CompletableFuture<ResourceResponse> sendActivityAsync(String textReplyToSend) {
        return innerTurnContext.sendActivityAsync(textReplyToSend);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivityAsync(String textReplyToSend, String speak) {
        return innerTurnContext.sendActivityAsync(textReplyToSend, speak);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivityAsync(String textReplyToSend, String speak, String inputHint) {
        return innerTurnContext.sendActivityAsync(textReplyToSend, speak, inputHint);
    }

    @Override
    public CompletableFuture<ResourceResponse> sendActivityAsync(Activity activity) {
        return innerTurnContext.sendActivityAsync(activity);
    }

    @Override
    public CompletableFuture<ResourceResponse[]> sendActivitiesAsync(Activity[] activities) {
        return innerTurnContext.sendActivitiesAsync(activities);
    }

    @Override
    public CompletableFuture<ResourceResponse> updateActivityAsync(Activity activity) {
        return innerTurnContext.updateActivityAsync(activity);
    }

    @Override
    public CompletableFuture<Void> deleteActivityAsync(String activityId) {
        return innerTurnContext.deleteActivityAsync(activityId);
    }

    @Override
    public CompletableFuture<Void> deleteActivityAsync(ConversationReference conversationReference) {
        return innerTurnContext.deleteActivityAsync(conversationReference);
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
