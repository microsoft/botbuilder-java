// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BotAdapterTests {
    @Test
    public void AdapterSingleUse() {
        SimpleAdapter a = new SimpleAdapter();
        a.use(new CallCountingMiddleware());
    }

    @Test
    public void AdapterUseChaining() {
        SimpleAdapter a = new SimpleAdapter();
        a.use(new CallCountingMiddleware()).use(new CallCountingMiddleware());
    }

    @Test
    public void PassResourceResponsesThrough() {
        Consumer<List<Activity>> validateResponse = (activities) -> {
            // no need to do anything.
        };

        SimpleAdapter a = new SimpleAdapter(validateResponse);
        TurnContextImpl c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        String activityId = UUID.randomUUID().toString();
        Activity activity = TestMessage.Message();
        activity.setId(activityId);

        ResourceResponse resourceResponse = c.sendActivity(activity).join();
        Assert.assertTrue("Incorrect response Id returned", StringUtils.equals(resourceResponse.getId(), activityId));
    }

    @Test
    public void ContinueConversation_DirectMsgAsync() {
        boolean[] callbackInvoked = new boolean[] { false };

        TestAdapter adapter = new TestAdapter();
        ConversationReference cr = new ConversationReference(){{
            setActivityId("activityId");
            setBot(new ChannelAccount(){{
                setId("channelId");
                setName("testChannelAccount");
                setRole(RoleTypes.BOT);
            }});
            setChannelId("testChannel");
            setServiceUrl("testUrl");
            setConversation(new ConversationAccount() {{
                setConversationType("");
                setId("testConversationId");
                setIsGroup(false);
                setName("testConversationName");
                setRole(RoleTypes.USER);
            }});
            setUser(new ChannelAccount() {{
                setId("channelId");
                setName("testChannelAccount");
                setRole(RoleTypes.BOT);
            }});
        }};

        BotCallbackHandler callback = (turnContext) -> {
            callbackInvoked[0] = true;
            return CompletableFuture.completedFuture(null);
        };

        adapter.continueConversation("MyBot", cr, callback).join();
        Assert.assertTrue(callbackInvoked[0]);
    }
}
