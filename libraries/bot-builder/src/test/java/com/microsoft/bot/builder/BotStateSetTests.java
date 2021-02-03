// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.junit.Assert;
import org.junit.Test;

public class BotStateSetTests {
    @Test
    public void BotStateSet_Properties() {
        Storage storage = new MemoryStorage();

        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);
        BotStateSet stateSet = new BotStateSet(userState, conversationState);

        Assert.assertEquals(2, stateSet.getBotStates().size());
        Assert.assertTrue(stateSet.getBotStates().get(0) instanceof UserState);
        Assert.assertTrue(stateSet.getBotStates().get(1) instanceof ConversationState);
    }

    @Test
    public void BotStateSet_LoadAsync() {
        Storage storage = new MemoryStorage();

        TurnContext turnContext = TestUtilities.createEmptyContext();

        {
            UserState userState = new UserState(storage);
            StatePropertyAccessor<Integer> userProperty = userState.createProperty("userCount");

            ConversationState convState = new ConversationState(storage);
            StatePropertyAccessor<Integer> convProperty = convState.createProperty("convCount");

            BotStateSet stateSet = new BotStateSet(userState, convState);

            Assert.assertEquals(2, stateSet.getBotStates().size());

            Integer userCount = userProperty.get(turnContext, () -> 0).join();
            Assert.assertEquals(0, userCount.intValue());
            Integer convCount = convProperty.get(turnContext, () -> 0).join();
            Assert.assertEquals(0, convCount.intValue());

            userProperty.set(turnContext, 10).join();
            convProperty.set(turnContext, 20).join();

            stateSet.saveAllChanges(turnContext).join();
        }

        {
            UserState userState = new UserState(storage);
            StatePropertyAccessor<Integer> userProperty = userState.createProperty("userCount");

            ConversationState convState = new ConversationState(storage);
            StatePropertyAccessor<Integer> convProperty = convState.createProperty("convCount");

            BotStateSet stateSet = new BotStateSet(userState, convState);

            stateSet.loadAll(turnContext).join();

            Integer userCount = userProperty.get(turnContext, () -> 0).join();
            Assert.assertEquals(10, userCount.intValue());
            Integer convCount = convProperty.get(turnContext, () -> 0).join();
            Assert.assertEquals(20, convCount.intValue());
        }
    }

    @Test
    public void BotStateSet_SaveAsync() {
        Storage storage = new MemoryStorage();

        UserState userState = new UserState(storage);
        StatePropertyAccessor<Integer> userProperty = userState.createProperty("userCount");

        ConversationState convState = new ConversationState(storage);
        StatePropertyAccessor<Integer> convProperty = convState.createProperty("convCount");

        BotStateSet stateSet = new BotStateSet(userState, convState);

        Assert.assertEquals(2, stateSet.getBotStates().size());

        TurnContext turnContext = TestUtilities.createEmptyContext();
        stateSet.loadAll(turnContext).join();

        Integer userCount = userProperty.get(turnContext, () -> 0).join();
        Assert.assertEquals(0, userCount.intValue());
        Integer convCount = convProperty.get(turnContext, () -> 0).join();
        Assert.assertEquals(0, convCount.intValue());

        userProperty.set(turnContext, 10).join();
        convProperty.set(turnContext, 20).join();

        stateSet.saveAllChanges(turnContext).join();

        userCount = userProperty.get(turnContext, () -> 0).join();
        Assert.assertEquals(10, userCount.intValue());
        convCount = convProperty.get(turnContext, () -> 0).join();
        Assert.assertEquals(20, convCount.intValue());
    }
}
