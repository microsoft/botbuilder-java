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
import java.util.function.Function;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class TestAdapter extends BotAdapter {
    private int _nextId=0;
    private final Queue<Activity> botReplies= new LinkedList<>();
    private ConversationReference _conversationReference;
    public TestAdapter() {
        this(null);
    }


    public TestAdapter(ConversationReference reference) {
        if(reference!=null) {
            this.withConversationReference(reference);
        }
        else {
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
                                                .withIsGroup(false)
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

    public CompletableFuture ProcessActivity(ActivityImpl activity,
                                             Function<TurnContext, CompletableFuture> callback
                                             ) throws Exception {
        synchronized (this.conversationReference()) {
            // ready for next reply
            if(activity.type()==null)
                activity.withType(ActivityTypes.MESSAGE);
            activity.withChannelId(this.conversationReference().channelId());
            activity.withFrom(this.conversationReference().user());
            activity.withRecipient(this.conversationReference().bot());
            activity.withConversation(this.conversationReference().conversation());
            activity.withServiceUrl(this.conversationReference().serviceUrl());
            Integer next = this._nextId++;
            activity.withId(next.toString());
        }
        // Assume Default DateTime : DateTime(0)
        if(activity.timestamp() == null ||activity.timestamp()== new DateTime(0))
            activity.withTimestamp(DateTime.now());

        try (TurnContextImpl context=new TurnContextImpl(this, activity)) {
            await(super.RunPipeline(context, callback));
        }
        return completedFuture(null);
    }

    public ConversationReference conversationReference() {
        return _conversationReference;
    }
    public void withConversationReference(ConversationReference conversationReference) {
        this._conversationReference = conversationReference;
    }

    @Override
    public CompletableFuture<ResourceResponse[]>SendActivities(TurnContext context, Activity[] activities) throws InterruptedException {
        List<ResourceResponse> responses=new LinkedList<ResourceResponse>();

        for(Activity activity : activities) {
            if(StringUtils.isEmpty(activity.id()))
                activity.withId(UUID.randomUUID().toString());

            if(activity.timestamp()==null)
                activity.withTimestamp(DateTime.now());

            responses.add(new ResourceResponse().withId(activity.id()));
            // This is simulating DELAY

            System.out.println(String.format("TestAdapter:SendActivities(tid:%s):Count:%s", Thread.currentThread().getId(), activities.length));
            for(Activity act : activities)
                System.out.println(String.format(":--------\n: To:%s\n: From:%s\n> Text:%s\n:---------", act.recipient().name(), act.from().name(),
                        act.text()));
            if(activity.type().toString().equals("delay"))
            {
                // The BotFrameworkAdapter and Console adapter implement this
                // hack directly in the POST method. Replicating that here
                // to keep the behavior as close as possible to facillitate
                // more realistic tests.
                int delayMs=(int)activity.value();
                Thread.sleep(delayMs);
                //await Task.Delay(delayMs);
            }
            else
            {
                synchronized (this.botReplies)
                {
                    this.botReplies.add(activity);
                }
            }
        }
        return completedFuture(responses.toArray(new ResourceResponse[responses.size()]));
    }


    @Override
    public CompletableFuture<ResourceResponse> UpdateActivity(TurnContext context, Activity activity) {
        synchronized (this.botReplies)
        {
            List<Activity> replies= new ArrayList<>(botReplies);
            for(int i=0;i< this.botReplies.size();i++) {
                if(replies.get(i).id().equals(activity.id())) {
                    replies.set(i, activity);
                    this.botReplies.clear();

                    for(Activity item : replies) {
                        this.botReplies.add(item);
                    }
                    return completedFuture(new ResourceResponse().withId(activity.id()));
                }
            }
        }
        return completedFuture(new ResourceResponse());
    }

    @Override
    public CompletableFuture DeleteActivity(TurnContext context, ConversationReference reference) {
        synchronized (this.botReplies)
        {
            ArrayList<Activity> replies= new ArrayList<>(this.botReplies);
            for(int i=0;i< this.botReplies.size();i++) {
                if(replies.get(i).id().equals(reference.activityId())) {
                    replies.remove(i);
                    this.botReplies.clear();
                    for(Activity item : replies) {
                        this.botReplies.add(item);
                    }
                    break;
                }
            }
        }
        return completedFuture(null);
    }

    /**
     * NOTE: this resets the queue, it doesn't actually maintain multiple converstion queues
     * @param channelId 
     * @param callback 
     * @return 
     */
    //@Override
    public CompletableFuture CreateConversation(String channelId,Function<TurnContext, CompletableFuture> callback) {
        this.activeQueue().clear();
        MessageActivity update=MessageActivity.CreateConversationUpdateActivity();

        update.withConversation(new ConversationAccount().withId(UUID.randomUUID().toString()));
        TurnContextImpl context=new TurnContextImpl(this,(ActivityImpl)update);
        return callback.apply(context);
    }

    /**
     * Called by TestFlow to check next reply
     * @return 
     */
    public Activity GetNextReply() {
        synchronized (this.botReplies) {
            if(this.botReplies.size()>0) {
                return this.botReplies.remove();
            }
        }
        return null;
    }

    /**
     * Called by TestFlow to get appropriate activity for conversationReference of testbot
     * @param text 
     * @return 
     */
    public Activity MakeActivity() {
        return MakeActivity(null);
    }
    public ActivityImpl MakeActivity(String text)
    {
        Integer next = _nextId++;
        ActivityImpl activity= (ActivityImpl) new MessageActivity()
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
     * @param userSays 
     * @return 
     */
    public CompletableFuture SendTextToBot(String userSays,Function<TurnContext, CompletableFuture> callback) throws Exception {
        return this.ProcessActivity(this.MakeActivity(userSays),callback);
    }
}


