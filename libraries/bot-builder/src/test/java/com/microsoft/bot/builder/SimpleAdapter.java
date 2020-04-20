// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import org.checkerframework.checker.units.qual.C;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SimpleAdapter extends BotAdapter {
    private Consumer<List<Activity>> callOnSend = null;
    private Consumer<Activity> callOnUpdate = null;
    private Consumer<ConversationReference> callOnDelete = null;

    // Callback Function but doesn't need to be. Avoid java legacy type erasure
    public SimpleAdapter(Consumer<List<Activity>> callOnSend) {
        this(callOnSend, null, null);
    }

    public SimpleAdapter(
        Consumer<List<Activity>> callOnSend,
        Consumer<Activity> callOnUpdate
    ) {
        this(callOnSend, callOnUpdate, null);
    }

    public SimpleAdapter(
        Consumer<List<Activity>> callOnSend,
        Consumer<Activity> callOnUpdate,
        Consumer<ConversationReference> callOnDelete
    ) {
        this.callOnSend = callOnSend;
        this.callOnUpdate = callOnUpdate;
        this.callOnDelete = callOnDelete;
    }

    public SimpleAdapter() {

    }

    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(
        TurnContext context,
        List<Activity> activities
    ) {
        Assert.assertNotNull("SimpleAdapter.deleteActivity: missing reference", activities);
        Assert.assertTrue(
            "SimpleAdapter.sendActivities: empty activities array.",
            activities.size() > 0
        );

        if (this.callOnSend != null)
            this.callOnSend.accept(activities);

        List<ResourceResponse> responses = new ArrayList<ResourceResponse>();
        for (Activity activity : activities) {
            responses.add(new ResourceResponse(activity.getId()));
        }
        ResourceResponse[] result = new ResourceResponse[responses.size()];
        return CompletableFuture.completedFuture(responses.toArray(result));
    }

    @Override
    public CompletableFuture<ResourceResponse> updateActivity(
        TurnContext context,
        Activity activity
    ) {
        Assert.assertNotNull("SimpleAdapter.updateActivity: missing activity", activity);
        if (this.callOnUpdate != null)
            this.callOnUpdate.accept(activity);
        return CompletableFuture.completedFuture(new ResourceResponse(activity.getId()));
    }

    @Override
    public CompletableFuture<Void> deleteActivity(
        TurnContext context,
        ConversationReference reference
    ) {
        Assert.assertNotNull("SimpleAdapter.deleteActivity: missing reference", reference);
        if (callOnDelete != null)
            this.callOnDelete.accept(reference);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> processRequest(Activity activity, BotCallbackHandler callback) {
        CompletableFuture<Void> pipelineResult = new CompletableFuture<>();

        try (TurnContextImpl context = new TurnContextImpl(this, activity)) {
            pipelineResult = runPipeline(context, callback);
        } catch (Exception e) {
            pipelineResult.completeExceptionally(e);
        }

        return pipelineResult;
    }
}
