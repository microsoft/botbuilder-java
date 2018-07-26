package com.microsoft.bot.builder.adapters;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.*;
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
            this.withConversationReference(reference);
        } else {
            this.withConversationReference(new ConversationReference()
                    .withChannelId("test")
                    .withServiceUrl("https://test.com"));

            this.conversationReference().withUser(new ChannelAccount()
                    .withId("user1")
                    .withName("User1"));
            this.conversationReference().withBot(new ChannelAccount()
                    .withId("bot")
                    .withName("Bot"));
            this.conversationReference().withConversation(new ConversationAccount()
                    .withIsGroup(Boolean.FALSE)
                    .withConversationType("convo1")
                    .withId("Conversation1"));
        }
    }

    public Queue<Activity> activeQueue() {
        return botReplies;
    }

    public TestAdapter Use(Middleware middleware) {
        super.Use(middleware);
        return this;
    }

    public void ProcessActivity(ActivityImpl activity,
                                Consumer<TurnContext> callback
    ) throws Exception {
        synchronized (this.conversationReference()) {
            // ready for next reply
            if (activity.type() == null)
                activity.withType(ActivityTypes.MESSAGE);
            activity.withChannelId(this.conversationReference().channelId());
            activity.withFrom(this.conversationReference().user());
            activity.withRecipient(this.conversationReference().bot());
            activity.withConversation(this.conversationReference().conversation());
            activity.withServiceUrl(this.conversationReference().serviceUrl());
            Integer next = this.nextId++;
            activity.withId(next.toString());
        }
        // Assume Default DateTime : DateTime(0)
        if (activity.timestamp() == null || activity.timestamp() == new DateTime(0))
            activity.withTimestamp(DateTime.now());

        try (TurnContextImpl context = new TurnContextImpl(this, activity)) {
            super.RunPipeline(context, callback);
        }
        return;
    }

    public ConversationReference conversationReference() {
        return conversationReference;
    }

    public void withConversationReference(ConversationReference conversationReference) {
        this.conversationReference = conversationReference;
    }

    @Override
    public ResourceResponse[] SendActivities(TurnContext context, Activity[] activities) throws InterruptedException {
        List<ResourceResponse> responses = new LinkedList<ResourceResponse>();

        for (Activity activity : activities) {
            if (StringUtils.isEmpty(activity.id()))
                activity.withId(UUID.randomUUID().toString());

            if (activity.timestamp() == null)
                activity.withTimestamp(DateTime.now());

            responses.add(new ResourceResponse().withId(activity.id()));
            // This is simulating DELAY

            System.out.println(String.format("TestAdapter:SendActivities(tid:%s):Count:%s", Thread.currentThread().getId(), activities.length));
            for (Activity act : activities) {
                System.out.printf(":--------\n: To:%s\n", act.recipient().name());
                System.out.printf(": From:%s\n", (act.from() == null) ? "No from set" : act.from().name());
                System.out.printf(": Text:%s\n:---------", (act.text() == null) ? "No text set" : act.text());
            }
            if (activity.type().toString().equals("delay")) {
                // The BotFrameworkAdapter and Console adapter implement this
                // hack directly in the POST method. Replicating that here
                // to keep the behavior as close as possible to facillitate
                // more realistic tests.
                int delayMs = (int) activity.value();
                Thread.sleep(delayMs);
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
                if (replies.get(i).id().equals(activity.id())) {
                    replies.set(i, activity);
                    this.botReplies.clear();

                    for (Activity item : replies) {
                        this.botReplies.add(item);
                    }
                    return new ResourceResponse().withId(activity.id());
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
                if (replies.get(i).id().equals(reference.activityId())) {
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
        MessageActivity update = MessageActivity.CreateConversationUpdateActivity();

        update.withConversation(new ConversationAccount().withId(UUID.randomUUID().toString()));
        TurnContextImpl context = new TurnContextImpl(this, (ActivityImpl) update);
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
     * @param text
     * @return
     */
    public Activity MakeActivity() {
        return MakeActivity(null);
    }

    public ActivityImpl MakeActivity(String text) {
        Integer next = nextId++;
        ActivityImpl activity = (ActivityImpl) new ActivityImpl()
                .withType(ActivityTypes.MESSAGE)
                .withFrom(conversationReference().user())
                .withRecipient(conversationReference().bot())
                .withConversation(conversationReference().conversation())
                .withServiceUrl(conversationReference().serviceUrl())
                .withId(next.toString())
                .withText(text);

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


