package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SimpleAdapter extends BotAdapter {
    private Consumer<Activity[]> callOnSend = null;
    private Consumer<Activity> callOnUpdate = null;
    private Consumer<ConversationReference> callOnDelete = null;

    // Callback Function but doesn't need to be.  Avoid java legacy type erasure
    public SimpleAdapter(Consumer<Activity[]> callOnSend) {
        this(callOnSend, null, null);
    }

    public SimpleAdapter(Consumer<Activity[]> callOnSend, Consumer<Activity> callOnUpdate) {
        this(callOnSend, callOnUpdate, null);
    }

    public SimpleAdapter(Consumer<Activity[]> callOnSend, Consumer<Activity> callOnUpdate, Consumer<ConversationReference> callOnDelete) {
        this.callOnSend = callOnSend;
        this.callOnUpdate = callOnUpdate;
        this.callOnDelete = callOnDelete;
    }

    public SimpleAdapter() {

    }


    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(TurnContext context, Activity[] activities) {
        Assert.assertNotNull("SimpleAdapter.deleteActivity: missing reference", activities);
        Assert.assertTrue("SimpleAdapter.sendActivities: empty activities array.", activities.length > 0);

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
    public CompletableFuture<ResourceResponse> updateActivity(TurnContext context, Activity activity) {
        Assert.assertNotNull("SimpleAdapter.updateActivity: missing activity", activity);
        if (this.callOnUpdate != null)
            this.callOnUpdate.accept(activity);
        return CompletableFuture.completedFuture(new ResourceResponse(activity.getId()));
    }

    @Override
    public CompletableFuture<Void> deleteActivity(TurnContext context, ConversationReference reference) {
        Assert.assertNotNull("SimpleAdapter.deleteActivity: missing reference", reference);
        if (callOnDelete != null)
            this.callOnDelete.accept(reference);
        return null;
    }


    public CompletableFuture<Void> processRequest(Activity activity, BotCallbackHandler callback) {
        return runPipeline(new TurnContextImpl(this, activity), callback);
    }
}

