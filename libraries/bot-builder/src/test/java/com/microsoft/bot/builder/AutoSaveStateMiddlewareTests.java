// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class AutoSaveStateMiddlewareTests {
    @Test
    public void AutoSaveStateMiddleware_DualReadWrite() {
        Storage storage = new MemoryStorage();

        // setup userstate
        UserState userState = new UserState(storage);
        StatePropertyAccessor<Integer> userProperty = userState.createProperty("userCount");

        // setup convState
        ConversationState convState = new ConversationState(storage);
        StatePropertyAccessor<Integer> convProperty = userState.createProperty("convCount");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(userState, convState));

        final int USER_INITIAL_COUNT = 100;
        final int CONVERSATION_INITIAL_COUNT = 10;

        BotCallbackHandler botLogic = (turnContext -> {
            Integer userCount = userProperty.get(turnContext, () -> USER_INITIAL_COUNT).join();
            Integer convCount = convProperty.get(turnContext, () -> CONVERSATION_INITIAL_COUNT).join();

            if(turnContext.getActivity().isType(ActivityTypes.MESSAGE)) {
                if (StringUtils.equals(turnContext.getActivity().getText(), "get userCount")) {
                    turnContext.sendActivity(turnContext.getActivity().createReply(userCount.toString()));
                }
                else if (StringUtils.equals(turnContext.getActivity().getText(), "get convCount")) {
                    turnContext.sendActivity(turnContext.getActivity().createReply(convCount.toString()));
                }
            }

            // increment userCount and set property using accessor.  To be saved later by AutoSaveStateMiddleware
            userCount++;
            userProperty.set(turnContext, userCount);

            // increment convCount and set property using accessor.  To be saved later by AutoSaveStateMiddleware
            convCount++;
            convProperty.set(turnContext, convCount);

            return CompletableFuture.completedFuture(null);
        });

        new TestFlow(adapter, botLogic)
            .send("test1")
            .send("get userCount")
            .assertReply(String.format("%d", USER_INITIAL_COUNT + 1))
            .send("get userCount")
            .assertReply(String.format("%d", USER_INITIAL_COUNT + 2))
            .send("get convCount")
            .assertReply(String.format("%d", CONVERSATION_INITIAL_COUNT + 1))
            .startTest();

        adapter = new TestAdapter(new ConversationReference(){{
            setChannelId("test");
            setServiceUrl("https://test.com");
            setUser(new ChannelAccount("user1", "User1"));
            setBot(new ChannelAccount("bot", "Bot"));
            setConversation(new ConversationAccount(false, "convo2", "Conversation2"));
        }}).use(new AutoSaveStateMiddleware(userState, convState));

        new TestFlow(adapter, botLogic)
            .send("get userCount")
            .assertReply(String.format("%d", USER_INITIAL_COUNT + 4))
            .send("get convCount")
            .assertReply(String.format("%d", CONVERSATION_INITIAL_COUNT + 1))
            .startTest();
    }

    @Test
    public void AutoSaveStateMiddleware_Chain() {
        Storage storage = new MemoryStorage();

        // setup userstate
        UserState userState = new UserState(storage);
        StatePropertyAccessor<Integer> userProperty = userState.createProperty("userCount");

        // setup convState
        ConversationState convState = new ConversationState(storage);
        StatePropertyAccessor<Integer> convProperty = userState.createProperty("convCount");

        AutoSaveStateMiddleware bss = new AutoSaveStateMiddleware(){{
           add(userState);
           add(convState);
        }};

        TestAdapter adapter = new TestAdapter().use(bss);

        final int USER_INITIAL_COUNT = 100;
        final int CONVERSATION_INITIAL_COUNT = 10;

        BotCallbackHandler botLogic = (turnContext -> {
            Integer userCount = userProperty.get(turnContext, () -> USER_INITIAL_COUNT).join();
            Integer convCount = convProperty.get(turnContext, () -> CONVERSATION_INITIAL_COUNT).join();

            if(turnContext.getActivity().isType(ActivityTypes.MESSAGE)) {
                if (StringUtils.equals(turnContext.getActivity().getText(), "get userCount")) {
                    turnContext.sendActivity(turnContext.getActivity().createReply(userCount.toString()));
                }
                else if (StringUtils.equals(turnContext.getActivity().getText(), "get convCount")) {
                    turnContext.sendActivity(turnContext.getActivity().createReply(convCount.toString()));
                }
            }

            // increment userCount and set property using accessor.  To be saved later by AutoSaveStateMiddleware
            userCount++;
            userProperty.set(turnContext, userCount);

            // increment convCount and set property using accessor.  To be saved later by AutoSaveStateMiddleware
            convCount++;
            convProperty.set(turnContext, convCount);

            return CompletableFuture.completedFuture(null);
        });

        new TestFlow(adapter, botLogic)
            .send("test1")
            .send("get userCount")
            .assertReply(String.format("%d", USER_INITIAL_COUNT + 1))
            .send("get userCount")
            .assertReply(String.format("%d", USER_INITIAL_COUNT + 2))
            .send("get convCount")
            .assertReply(String.format("%d", CONVERSATION_INITIAL_COUNT + 3))
            .startTest();

        // new adapter on new conversation
        AutoSaveStateMiddleware bss2 = new AutoSaveStateMiddleware(){{
            add(userState);
            add(convState);
        }};

        adapter = new TestAdapter(new ConversationReference(){{
            setChannelId("test");
            setServiceUrl("https://test.com");
            setUser(new ChannelAccount("user1", "User1"));
            setBot(new ChannelAccount("bot", "Bot"));
            setConversation(new ConversationAccount(false, "convo2", "Conversation2"));
        }}).use(bss2);

        new TestFlow(adapter, botLogic)
            .send("get userCount")
            .assertReply(String.format("%d", USER_INITIAL_COUNT + 4))
            .send("get convCount")
            .assertReply(String.format("%d", CONVERSATION_INITIAL_COUNT + 1))
            .startTest();
    }

}
