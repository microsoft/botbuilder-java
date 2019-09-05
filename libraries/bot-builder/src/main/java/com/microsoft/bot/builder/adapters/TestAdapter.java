package com.microsoft.bot.builder.adapters;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestAdapter extends BotAdapter {
    private int nextId = 0;
    private final Queue<Activity> botReplies = new LinkedList<>();
    private ConversationReference conversationReference;

    public TestAdapter() {
        this(null);
    }


    public TestAdapter(ConversationReference reference) {
        if (reference != null) {
            this.setConversationReference(reference);
        } else {
            this.setConversationReference(new ConversationReference() {{
                setChannelId("test");
                setServiceUrl("https://test.com");
            }});

            this.conversationReference().setUser(new ChannelAccount() {{
                setId("user1");
                setName("User1");
            }});
            this.conversationReference().setBot(new ChannelAccount() {{
                setId("bot");
                setName("Bot");
            }});
            this.conversationReference().setConversation(new ConversationAccount() {{
                setIsGroup(Boolean.FALSE);
                setConversationType("convo1");
                setId("Conversation1");
            }});
        }
    }

    public Queue<Activity> activeQueue() {
        return botReplies;
    }

    public TestAdapter Use(Middleware middleware) {
        super.Use(middleware);
        return this;
    }

    public void ProcessActivity(Activity activity,
                                Consumer<TurnContext> callback
    ) throws Exception {
        synchronized (this.conversationReference()) {
            // ready for next reply
            if (activity.getType() == null)
                activity.setType(ActivityTypes.MESSAGE);
            activity.setChannelId(this.conversationReference().getChannelId());
            activity.setFrom(this.conversationReference().getUser());
            activity.setRecipient(this.conversationReference().getBot());
            activity.setConversation(this.conversationReference().getConversation());
            activity.setServiceUrl(this.conversationReference().getServiceUrl());
            Integer next = this.nextId++;
            activity.setId(next.toString());
        }
        // Assume Default DateTime : DateTime(0)
        if (activity.getTimestamp() == null || activity.getTimestamp() == new DateTime(0))
            activity.setTimestamp(DateTime.now());

        try (TurnContextImpl context = new TurnContextImpl(this, activity)) {
            super.RunPipeline(context, callback);
        }
        return;
    }

    public ConversationReference conversationReference() {
        return conversationReference;
    }

    public void setConversationReference(ConversationReference conversationReference) {
        this.conversationReference = conversationReference;
    }

    @Override
    public ResourceResponse[] SendActivities(TurnContext context, Activity[] activities) {
        List<ResourceResponse> responses = new LinkedList<ResourceResponse>();

        for (Activity activity : activities) {
            if (StringUtils.isEmpty(activity.getId()))
                activity.setId(UUID.randomUUID().toString());

            if (activity.getTimestamp() == null)
                activity.setTimestamp(DateTime.now());

            responses.add(new ResourceResponse(activity.getId()));
            // This is simulating DELAY

            System.out.println(String.format("TestAdapter:SendActivities(tid:%s):Count:%s", Thread.currentThread().getId(), activities.length));
            for (Activity act : activities) {
                System.out.printf(":--------\n: To:%s\n", act.getRecipient().getName());
                System.out.printf(": From:%s\n", (act.getFrom() == null) ? "No from set" : act.getFrom().getName());
                System.out.printf(": Text:%s\n:---------", (act.getText() == null) ? "No text set" : act.getText());
            }
            if (activity.getType().toString().equals("delay")) {
                // The BotFrameworkAdapter and Console adapter implement this
                // hack directly in the POST method. Replicating that here
                // to keep the behavior as close as possible to facillitate
                // more realistic tests.
                int delayMs = (int) activity.getValue();
                try { Thread.sleep(delayMs); } catch (InterruptedException e) {}
            } else {
                synchronized (this.botReplies) {
                    this.botReplies.add(activity);
                }
            }
        }
        return responses.toArray(new ResourceResponse[responses.size()]);
    }


    @Override
    public ResourceResponse UpdateActivity(TurnContext context, Activity activity) {
        synchronized (this.botReplies) {
            List<Activity> replies = new ArrayList<>(botReplies);
            for (int i = 0; i < this.botReplies.size(); i++) {
                if (replies.get(i).getId().equals(activity.getId())) {
                    replies.set(i, activity);
                    this.botReplies.clear();

                    for (Activity item : replies) {
                        this.botReplies.add(item);
                    }
                    return new ResourceResponse(activity.getId());
                }
            }
        }
        return new ResourceResponse();
    }

    @Override
    public void DeleteActivity(TurnContext context, ConversationReference reference) {
        synchronized (this.botReplies) {
            ArrayList<Activity> replies = new ArrayList<>(this.botReplies);
            for (int i = 0; i < this.botReplies.size(); i++) {
                if (replies.get(i).getId().equals(reference.getActivityId())) {
                    replies.remove(i);
                    this.botReplies.clear();
                    for (Activity item : replies) {
                        this.botReplies.add(item);
                    }
                    break;
                }
            }
        }
        return;
    }

    /**
     * NOTE: this resets the queue, it doesn't actually maintain multiple converstion queues
     *
     * @param channelId
     * @param callback
     * @return
     */
    //@Override
    public CompletableFuture CreateConversation(String channelId, Function<TurnContext, CompletableFuture> callback) {
        this.activeQueue().clear();
        Activity update = Activity.createConversationUpdateActivity();

        update.setConversation(new ConversationAccount(UUID.randomUUID().toString()));
        TurnContextImpl context = new TurnContextImpl(this, update);
        return callback.apply(context);
    }

    /**
     * Called by TestFlow to check next reply
     *
     * @return
     */
    public Activity GetNextReply() {
        synchronized (this.botReplies) {
            if (this.botReplies.size() > 0) {
                return this.botReplies.remove();
            }
        }
        return null;
    }

    /**
     * Called by TestFlow to get appropriate activity for conversationReference of testbot
     *
     * @return
     */
    public Activity MakeActivity() {
        return MakeActivity(null);
    }

    public Activity MakeActivity(String withText) {
        Integer next = nextId++;
        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setFrom(conversationReference().getUser());
            setRecipient(conversationReference().getBot());
            setConversation(conversationReference().getConversation());
            setServiceUrl(conversationReference().getServiceUrl());
            setId(next.toString());
            setText(withText);
        }};

        return activity;
    }


    /**
     * Called by TestFlow to send text to the bot
     *
     * @param userSays
     * @return
     */
    public void SendTextToBot(String userSays, Consumer<TurnContext> callback) throws Exception {
        this.ProcessActivity(this.MakeActivity(userSays), callback);
    }
}


