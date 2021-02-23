// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.DeleteActivityHandler;
import com.microsoft.bot.builder.SendActivitiesHandler;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextStateCollection;
import com.microsoft.bot.builder.UpdateActivityHandler;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.bot.schema.ResourceResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MyTurnContext implements TurnContext {

    private BotAdapter adapter;
    private Activity activity;

    public MyTurnContext(BotAdapter withAdapter, Activity withActivity) {
        this.adapter = withAdapter;
        this.activity = withActivity;
    }

    public String getLocale() {
        throw new UnsupportedOperationException();
    }

    public void setLocale(String withLocale) {
        throw new UnsupportedOperationException();
    }

    public BotAdapter getAdapter() {
        return adapter;
    }

    public Activity getActivity() {
        return activity;
    }

    public TurnContextStateCollection getTurnState() {
        throw new UnsupportedOperationException();
    }

    public boolean getResponded() {
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<Void> deleteActivity(String activityId) {
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<Void> deleteActivity(ConversationReference conversationReference) {
        throw new UnsupportedOperationException();
    }

    public TurnContext onDeleteActivity(DeleteActivityHandler handler) {
        throw new UnsupportedOperationException();
    }

    public TurnContext onSendActivities(SendActivitiesHandler handler) {
        throw new UnsupportedOperationException();
    }

    public TurnContext onUpdateActivity(UpdateActivityHandler handler) {
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<ResourceResponse[]> sendActivities(List<Activity> activities) {
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak,
                                                            InputHints inputHint) {
        inputHint = inputHint != null ? inputHint : InputHints.ACCEPTING_INPUT;
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<ResourceResponse> sendActivity(Activity activity) {
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<ResourceResponse> sendActivity(String textToReply) {
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<ResourceResponse> sendActivity(String textReplyToSend, String speak) {
        throw new UnsupportedOperationException();
    }

    public CompletableFuture<ResourceResponse> updateActivity(Activity activity) {
        throw new UnsupportedOperationException();
    }

}
