// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.adapters;

import com.microsoft.bot.builder.*;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TestAdapter extends BotAdapter {
    private final Queue<Activity> botReplies = new LinkedList<>();
    private int nextId = 0;
    private ConversationReference conversationReference;

    public TestAdapter() {
        this(null);
    }


    public TestAdapter(ConversationReference reference) {
        if (reference != null) {
            setConversationReference(reference);
        } else {
            setConversationReference(new ConversationReference() {{
                setChannelId(Channels.TEST);
                setServiceUrl("https://test.com");
                setUser(new ChannelAccount() {{
                    setId("user1");
                    setName("User1");
                }});
                setBot(new ChannelAccount() {{
                    setId("bot");
                    setName("Bot");
                }});
                setConversation(new ConversationAccount() {{
                    setIsGroup(false);
                    setConversationType("convo1");
                    setId("Conversation1");
                }});
            }});
        }
    }

    public Queue<Activity> activeQueue() {
        return botReplies;
    }

    @Override
    public TestAdapter use(Middleware middleware) {
        super.use(middleware);
        return this;
    }

    public CompletableFuture<Void> processActivity(Activity activity,
                                BotCallbackHandler callback) {
        return CompletableFuture.supplyAsync(() -> {
                synchronized (conversationReference()) {
                    // ready for next reply
                    if (activity.getType() == null)
                        activity.setType(ActivityTypes.MESSAGE);
                    activity.setChannelId(conversationReference().getChannelId());
                    activity.setFrom(conversationReference().getUser());
                    activity.setRecipient(conversationReference().getBot());
                    activity.setConversation(conversationReference().getConversation());
                    activity.setServiceUrl(conversationReference().getServiceUrl());
                    Integer next = nextId++;
                    activity.setId(next.toString());
                }
                // Assume Default DateTime : DateTime(0)
                if (activity.getTimestamp() == null || activity.getTimestamp().toEpochSecond() == 0)
                    activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

                return activity;
            }).thenCompose(activity1 -> {
                TurnContextImpl context = new TurnContextImpl(this, activity1);
                return super.runPipeline(context, callback);
        });
    }

    public ConversationReference conversationReference() {
        return conversationReference;
    }

    public void setConversationReference(ConversationReference conversationReference) {
        this.conversationReference = conversationReference;
    }

    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(TurnContext context, List<Activity> activities) {
        List<ResourceResponse> responses = new LinkedList<ResourceResponse>();

        for (Activity activity : activities) {
            if (StringUtils.isEmpty(activity.getId()))
                activity.setId(UUID.randomUUID().toString());

            if (activity.getTimestamp() == null)
                activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

            responses.add(new ResourceResponse(activity.getId()));

            System.out.println(String.format("TestAdapter:SendActivities(tid:%s):Count:%s", Thread.currentThread().getId(), activities.size()));
            for (Activity act : activities) {
                System.out.printf(":--------\n: To:%s\n", act.getRecipient().getName());
                System.out.printf(": From:%s\n", (act.getFrom() == null) ? "No from set" : act.getFrom().getName());
                System.out.printf(": Text:%s\n:---------", (act.getText() == null) ? "No text set" : act.getText());
            }

            // This is simulating DELAY
            if (activity.getType().toString().equals("delay")) {
                // The BotFrameworkAdapter and Console adapter implement this
                // hack directly in the POST method. Replicating that here
                // to keep the behavior as close as possible to facilitate
                // more realistic tests.
                int delayMs = (int) activity.getValue();
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                }
            } else {
                synchronized (botReplies) {
                    botReplies.add(activity);
                }
            }
        }
        return CompletableFuture.completedFuture(responses.toArray(new ResourceResponse[responses.size()]));
    }


    @Override
    public CompletableFuture<ResourceResponse> updateActivity(TurnContext context, Activity activity) {
        synchronized (botReplies) {
            List<Activity> replies = new ArrayList<>(botReplies);
            for (int i = 0; i < botReplies.size(); i++) {
                if (replies.get(i).getId().equals(activity.getId())) {
                    replies.set(i, activity);
                    botReplies.clear();

                    for (Activity item : replies) {
                        botReplies.add(item);
                    }
                    return CompletableFuture.completedFuture(new ResourceResponse(activity.getId()));
                }
            }
        }
        return CompletableFuture.completedFuture(new ResourceResponse());
    }

    @Override
    public CompletableFuture<Void> deleteActivity(TurnContext context, ConversationReference reference) {
        synchronized (botReplies) {
            ArrayList<Activity> replies = new ArrayList<>(botReplies);
            for (int i = 0; i < botReplies.size(); i++) {
                if (replies.get(i).getId().equals(reference.getActivityId())) {
                    replies.remove(i);
                    botReplies.clear();
                    for (Activity item : replies) {
                        botReplies.add(item);
                    }
                    break;
                }
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called by TestFlow to check next reply
     *
     * @return
     */
    public Activity getNextReply() {
        synchronized (botReplies) {
            if (botReplies.size() > 0) {
                return botReplies.remove();
            }
        }
        return null;
    }

    /**
     * Called by TestFlow to get appropriate activity for conversationReference of testbot
     *
     * @return
     */
    public Activity makeActivity() {
        return makeActivity(null);
    }

    public Activity makeActivity(String withText) {
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
    public CompletableFuture<Void> sendTextToBot(String userSays, BotCallbackHandler callback) {
        return processActivity(this.makeActivity(userSays), callback);
    }
}


