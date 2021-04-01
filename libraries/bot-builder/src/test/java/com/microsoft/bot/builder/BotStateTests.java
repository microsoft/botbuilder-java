// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationAccount;
import java.util.concurrent.CompletionException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BotStateTests {

    @Test(expected = IllegalArgumentException.class)
    public void State_EmptyName() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));

        StatePropertyAccessor<String> propertyA = userState.createProperty("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void State_NullName() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));

        StatePropertyAccessor<String> propertyA = userState.createProperty(null);
    }

    @Test
    public void MakeSureStorageNotCalledNoChangesAsync() {
        int[] storeCount = { 0 };
        int[] readCount = { 0 };

        Storage mock = new Storage() {
            Map<String, Object> dictionary = new HashMap<>();

            @Override
            public CompletableFuture<Map<String, Object>> read(String[] keys) {
                readCount[0]++;
                return CompletableFuture.completedFuture(dictionary);
            }

            @Override
            public CompletableFuture<Void> write(Map<String, Object> changes) {
                storeCount[0]++;
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletableFuture<Void> delete(String[] keys) {
                return CompletableFuture.completedFuture(null);
            }
        };

        UserState userState = new UserState(mock);
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("propertyA");
        Assert.assertEquals(storeCount[0], 0);

        userState.saveChanges(context).join();
        propertyA.set(context, "hello");
        Assert.assertEquals(1, readCount[0]);
        Assert.assertEquals(0, storeCount[0]);

        propertyA.set(context, "there").join();
        Assert.assertEquals(0, storeCount[0]); // Set on property should not bump

        userState.saveChanges(context).join();
        Assert.assertEquals(1, storeCount[0]); // Explicit save should bump

        String valueA = propertyA.get(context, null).join();
        Assert.assertEquals("there", valueA);
        Assert.assertEquals(1, storeCount[0]); // Gets should not bump

        userState.saveChanges(context).join();
        Assert.assertEquals(1, storeCount[0]); // Gets should not bump

        propertyA.delete(context).join();
        Assert.assertEquals(1, storeCount[0]); // Delete alone no bump

        userState.saveChanges(context).join();
        Assert.assertEquals(2, storeCount[0]); // Save when dirty should bump
        Assert.assertEquals(1, readCount[0]);

        userState.saveChanges(context).join();
        Assert.assertEquals(2, storeCount[0]); // Save not dirty should not bump
        Assert.assertEquals(1, readCount[0]);
    }

    @Test
    public void State_SetNoLoad() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("propertyA");
        propertyA.set(context, "hello").join();
    }

    @Test
    public void State_MultipleLoads() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("propertyA");
        userState.load(context).join();
        userState.load(context).join();
    }

    @Test
    public void State_GetNoLoadWithDefault() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("propertyA");
        String valueA = propertyA.get(context, () -> "Default!").join();
        Assert.assertEquals("Default!", valueA);
    }

    @Test
    public void State_GetNoLoadNoDefault() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("propertyA");
        String valueA = propertyA.get(context, null).join();

        Assert.assertNull(valueA);
    }

    @Test
    public void State_POCO_NoDefault() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<TestPocoState> testProperty = userState.createProperty("test");
        TestPocoState value = testProperty.get(context, null).join();

        Assert.assertNull(value);
    }

    @Test
    public void State_bool_NoDefault() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<Boolean> testProperty = userState.createProperty("test");
        Boolean value = testProperty.get(context, null).join();

        Assert.assertNull(value);
    }

    @Test
    public void State_int_NoDefault() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<Integer> testProperty = userState.createProperty("test");
        Integer value = testProperty.get(context).join();

        Assert.assertNull(value);
    }

    @Test
    public void State_SetAfterSave() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("property-a");
        StatePropertyAccessor<String> propertyB = userState.createProperty("property-b");

        userState.load(context).join();
        propertyA.set(context, "hello").join();
        propertyB.set(context, "world").join();
        userState.saveChanges(context).join();

        propertyA.set(context, "hello2").join();
    }

    @Test
    public void State_MultipleSave() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("property-a");
        StatePropertyAccessor<String> propertyB = userState.createProperty("property-b");

        userState.load(context).join();
        propertyA.set(context, "hello").join();
        propertyB.set(context, "world").join();
        userState.saveChanges(context).join();

        propertyA.set(context, "hello2").join();
        userState.saveChanges(context).join();

        String valueA = propertyA.get(context).join();
        Assert.assertEquals("hello2", valueA);
    }

    @Test
    public void LoadSetSave() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("property-a");
        StatePropertyAccessor<String> propertyB = userState.createProperty("property-b");

        userState.load(context).join();
        propertyA.set(context, "hello").join();
        propertyB.set(context, "world").join();
        userState.saveChanges(context).join();

        JsonNode obj = dictionary.get("EmptyContext/users/empty@empty.context.org");
        Assert.assertEquals("hello", obj.get("property-a").textValue());
        Assert.assertEquals("world", obj.get("property-b").textValue());
    }

    @Test
    public void LoadSetSaveTwice() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();

        StatePropertyAccessor<String> propertyA = userState.createProperty("property-a");
        StatePropertyAccessor<String> propertyB = userState.createProperty("property-b");
        StatePropertyAccessor<String> propertyC = userState.createProperty("property-c");

        userState.load(context).join();
        propertyA.set(context, "hello").join();
        propertyB.set(context, "world").join();
        propertyC.set(context, "test").join();
        userState.saveChanges(context).join();

        JsonNode obj = dictionary.get("EmptyContext/users/empty@empty.context.org");
        Assert.assertEquals("hello", obj.get("property-a").textValue());
        Assert.assertEquals("world", obj.get("property-b").textValue());

        // Act 2
        UserState userState2 = new UserState(new MemoryStorage(dictionary));

        StatePropertyAccessor<String> propertyA2 = userState.createProperty("property-a");
        StatePropertyAccessor<String> propertyB2 = userState.createProperty("property-b");

        userState.load(context).join();
        propertyA.set(context, "hello-2").join();
        propertyB.set(context, "world-2").join();
        userState2.saveChanges(context).join();

        // Assert 2
        JsonNode obj2 = dictionary.get("EmptyContext/users/empty@empty.context.org");
        Assert.assertEquals("hello-2", obj2.get("property-a").textValue());
        Assert.assertEquals("world-2", obj2.get("property-b").textValue());
        Assert.assertEquals("test", obj2.get("property-c").textValue());
    }

    @Test
    public void LoadSaveDelete() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        TurnContext context = TestUtilities.createEmptyContext();

        // Act
        UserState userState = new UserState(new MemoryStorage(dictionary));

        StatePropertyAccessor<String> propertyA = userState.createProperty("property-a");
        StatePropertyAccessor<String> propertyB = userState.createProperty("property-b");

        userState.load(context).join();
        propertyA.set(context, "hello").join();
        propertyB.set(context, "world").join();
        userState.saveChanges(context).join();

        // Assert
        JsonNode obj = dictionary.get("EmptyContext/users/empty@empty.context.org");
        Assert.assertEquals("hello", obj.get("property-a").textValue());
        Assert.assertEquals("world", obj.get("property-b").textValue());

        // Act 2
        UserState userState2 = new UserState(new MemoryStorage(dictionary));

        StatePropertyAccessor<String> propertyA2 = userState.createProperty("property-a");
        StatePropertyAccessor<String> propertyB2 = userState.createProperty("property-b");

        userState2.load(context).join();
        propertyA.set(context, "hello-2").join();
        propertyB.delete(context).join();
        userState2.saveChanges(context).join();

        // Assert 2
        JsonNode obj2 = dictionary.get("EmptyContext/users/empty@empty.context.org");
        Assert.assertEquals("hello-2", obj2.get("property-a").textValue());
        Assert.assertNull(obj2.get("property-b"));
    }

    @Test
    public void State_DoNOTRememberContextState() {
        TestAdapter adapter = new TestAdapter();

        new TestFlow(adapter, (turnContext -> {
            UserState obj = turnContext.getTurnState().get("UserState");
            Assert.assertNull(obj);
            return CompletableFuture.completedFuture(null);
        })).send("set value").startTest().join();
    }

    @Test
    public void State_RememberIStoreItemUserState() {
        UserState userState = new UserState(new MemoryStorage());
        StatePropertyAccessor<TestPocoState> testProperty = userState.createProperty("test");
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(userState));

        BotCallbackHandler callback = (context) -> {
            TestPocoState state = testProperty.get(context, TestPocoState::new).join();
            Assert.assertNotNull("user state should exist", state);

            switch (context.getActivity().getText()) {
                case "set value":
                    state.setValue("test");
                    context.sendActivity("value saved").join();
                    break;

                case "get value":
                    context.sendActivity(state.getValue()).join();
                    break;
            }

            return CompletableFuture.completedFuture(null);
        };

        new TestFlow(adapter, callback).test("set value", "value saved").test(
            "get value",
            "test"
        ).startTest().join();
    }

    @Test
    public void State_RememberPocoUserState() {
        UserState userState = new UserState(new MemoryStorage());
        StatePropertyAccessor<TestPocoState> testPocoProperty = userState.createProperty(
            "testPoco"
        );
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(userState));

        new TestFlow(adapter, (turnContext -> {
            TestPocoState testPocoState = testPocoProperty.get(
                turnContext,
                TestPocoState::new
            ).join();
            Assert.assertNotNull("user state should exist", testPocoState);

            switch (turnContext.getActivity().getText()) {
                case "set value":
                    testPocoState.setValue("test");
                    turnContext.sendActivity("value saved").join();
                    break;

                case "get value":
                    turnContext.sendActivity(testPocoState.getValue()).join();
                    break;
            }

            return CompletableFuture.completedFuture(null);
        })).test("set value", "value saved").test("get value", "test").startTest().join();
    }

    @Test
    public void State_RememberIStoreItemConversationState() {
        ConversationState conversationState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<TestState> testProperty = conversationState.createProperty("test");
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(conversationState));

        new TestFlow(adapter, (turnContext -> {
            TestState testState = testProperty.get(turnContext, TestState::new).join();
            Assert.assertNotNull("user state.conversation should exist", conversationState);

            switch (turnContext.getActivity().getText()) {
                case "set value":
                    testState.setValue("test");
                    turnContext.sendActivity("value saved").join();
                    break;

                case "get value":
                    turnContext.sendActivity(testState.getValue()).join();
                    break;
            }

            return CompletableFuture.completedFuture(null);
        })).test("set value", "value saved").test("get value", "test").startTest().join();
    }

    @Test
    public void State_RememberPocoConversationState() {
        ConversationState conversationState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<TestPocoState> testProperty = conversationState.createProperty(
            "testPoco"
        );
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(conversationState));

        new TestFlow(adapter, (turnContext -> {
            TestPocoState testState = testProperty.get(turnContext, TestPocoState::new).join();
            Assert.assertNotNull("user state.conversation should exist", testState);

            switch (turnContext.getActivity().getText()) {
                case "set value":
                    testState.setValue("test");
                    turnContext.sendActivity("value saved").join();
                    break;

                case "get value":
                    turnContext.sendActivity(testState.getValue()).join();
                    break;
            }

            return CompletableFuture.completedFuture(null);
        })).test("set value", "value saved").test("get value", "test").startTest().join();
    }

    @Test
    public void State_RememberPocoPrivateConversationState() {
        PrivateConversationState privateConversationState = new PrivateConversationState(
            new MemoryStorage()
        );
        StatePropertyAccessor<TestPocoState> testProperty = privateConversationState.createProperty(
            "testPoco"
        );
        TestAdapter adapter = new TestAdapter().use(
            new AutoSaveStateMiddleware(privateConversationState)
        );

        new TestFlow(adapter, (turnContext -> {
            TestPocoState testState = testProperty.get(turnContext, TestPocoState::new).join();
            Assert.assertNotNull("user state.conversation should exist", testState);

            switch (turnContext.getActivity().getText()) {
                case "set value":
                    testState.setValue("test");
                    turnContext.sendActivity("value saved").join();
                    break;

                case "get value":
                    turnContext.sendActivity(testState.getValue()).join();
                    break;
            }

            return CompletableFuture.completedFuture(null);
        })).test("set value", "value saved").test("get value", "test").startTest().join();
    }

    @Test
    public void State_CustomStateManagerTest() {
        String testGuid = UUID.randomUUID().toString();
        CustomKeyState customState = new CustomKeyState(new MemoryStorage());

        StatePropertyAccessor<TestPocoState> testProperty = customState.createProperty("test");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(customState));

        new TestFlow(adapter, (turnContext -> {
            TestPocoState testState = testProperty.get(turnContext, TestPocoState::new).join();
            Assert.assertNotNull("user state.conversation should exist", testState);

            switch (turnContext.getActivity().getText()) {
                case "set value":
                    testState.setValue(testGuid);
                    turnContext.sendActivity("value saved").join();
                    break;

                case "get value":
                    turnContext.sendActivity(testState.getValue()).join();
                    break;
            }

            return CompletableFuture.completedFuture(null);
        })).test("set value", "value saved").test("get value", testGuid).startTest().join();
    }

    @Test
    public void State_RoundTripTypedObject() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<TypedObject> testProperty = convoState.createProperty("typed");
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        new TestFlow(adapter, (turnContext -> {
            TypedObject testState = testProperty.get(turnContext, TypedObject::new).join();
            Assert.assertNotNull("conversationstate should exist");

            switch (turnContext.getActivity().getText()) {
                case "set value":
                    testState.setName("test");
                    turnContext.sendActivity("value saved").join();
                    break;

                case "get value":
                    turnContext.sendActivity(testState.getClass().getSimpleName()).join();
                    break;
            }

            return CompletableFuture.completedFuture(null);
        })).test("set value", "value saved").test("get value", "TypedObject").startTest().join();
    }

    @Test
    public void State_UseBotStateDirectly() {
        TestAdapter adapter = new TestAdapter();

        new TestFlow(adapter, turnContext -> {
            TestBotState botStateManager = new TestBotState(new MemoryStorage());
            StatePropertyAccessor<CustomState> testProperty = botStateManager.createProperty(
                "test"
            );

            // read initial state object
            botStateManager.load(turnContext).join();

            CustomState customState = testProperty.get(turnContext, CustomState::new).join();

            // this should be a 'new CustomState' as nothing is currently stored in storage
            Assert.assertNotNull(customState);
            Assert.assertTrue(customState.getCustomString() == null);

            customState.setCustomString("test");
            botStateManager.saveChanges(turnContext).join();

            customState.setCustomString("asdfsadf");

            // force read into context again (without save)
            botStateManager.load(turnContext, true).join();

            customState = testProperty.get(turnContext, CustomState::new).join();

            // check object read from value has the correct value for CustomString
            Assert.assertEquals("test", customState.getCustomString());

            return CompletableFuture.completedFuture(null);
        }).send(Activity.createConversationUpdateActivity()).startTest().join();
    }

    @Test(expected = CompletionException.class)
    public void UserState_NullChannelIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setChannelId(null);
        StatePropertyAccessor<TestPocoState> testProperty = userState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void UserState_EmptyChannelIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setChannelId("");
        StatePropertyAccessor<TestPocoState> testProperty = userState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void UserState_NullFromThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setFrom(null);
        StatePropertyAccessor<TestPocoState> testProperty = userState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void UserState_NullFromIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getFrom().setId(null);
        StatePropertyAccessor<TestPocoState> testProperty = userState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void UserState_EmptyFromIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        UserState userState = new UserState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getFrom().setId("");
        StatePropertyAccessor<TestPocoState> testProperty = userState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void ConversationState_NullConversationThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        ConversationState conversationState = new ConversationState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setConversation(null);
        StatePropertyAccessor<TestPocoState> testProperty = conversationState.createProperty(
            "test"
        );
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void ConversationState_NullConversationIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        ConversationState conversationState = new ConversationState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getConversation().setId(null);
        StatePropertyAccessor<TestPocoState> testProperty = conversationState.createProperty(
            "test"
        );
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void ConversationState_EmptyConversationIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        ConversationState conversationState = new ConversationState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getConversation().setId("");
        StatePropertyAccessor<TestPocoState> testProperty = conversationState.createProperty(
            "test"
        );
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void ConversationState_NullChannelIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        ConversationState conversationState = new ConversationState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setChannelId(null);
        StatePropertyAccessor<TestPocoState> testProperty = conversationState.createProperty(
            "test"
        );
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void ConversationState_EmptyChannelIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        ConversationState conversationState = new ConversationState(new MemoryStorage(dictionary));
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setChannelId("");
        StatePropertyAccessor<TestPocoState> testProperty = conversationState.createProperty(
            "test"
        );
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_NullChannelIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setChannelId(null);
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_EmptyChannelIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setChannelId("");
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_NullFromThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setFrom(null);
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_NullFromIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getFrom().setId(null);
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_EmptyFromIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getFrom().setId("");
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_NullConversationThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().setConversation(null);
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_NullConversationIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getConversation().setId(null);
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test(expected = CompletionException.class)
    public void PrivateConversationState_EmptyConversationIdThrows() {
        Map<String, JsonNode> dictionary = new HashMap<>();
        PrivateConversationState botState = new PrivateConversationState(
            new MemoryStorage(dictionary)
        );
        TurnContext context = TestUtilities.createEmptyContext();
        context.getActivity().getConversation().setId("");
        StatePropertyAccessor<TestPocoState> testProperty = botState.createProperty("test");
        TestPocoState value = testProperty.get(context).join();
    }

    @Test
    public void ClearAndSave() {
        TurnContext turnContext = TestUtilities.createEmptyContext();
        turnContext.getActivity().setConversation(new ConversationAccount("1234"));

        Storage storage = new MemoryStorage(new HashMap<>());

        // Turn 0
        ConversationState botState0 = new ConversationState(storage);
        StatePropertyAccessor<TestPocoState> accessor0 = botState0.createProperty("test-name");
        TestPocoState value0 = accessor0.get(
            turnContext,
            () -> new TestPocoState("test-value")
        ).join();
        value0.setValue("test-value");
        botState0.saveChanges(turnContext).join();

        // Turn 1
        ConversationState botState1 = new ConversationState(storage);
        StatePropertyAccessor<TestPocoState> accessor1 = botState1.createProperty("test-name");
        TestPocoState value1 = accessor1.get(
            turnContext,
            () -> new TestPocoState("default-value")
        ).join();
        botState1.saveChanges(turnContext).join();

        Assert.assertEquals("test-value", value1.getValue());

        // Turn 2
        ConversationState botState3 = new ConversationState(storage);
        botState3.clearState(turnContext).join();
        botState3.saveChanges(turnContext).join();

        // Turn 3
        ConversationState botState4 = new ConversationState(storage);
        StatePropertyAccessor<TestPocoState> accessor3 = botState4.createProperty("test-name");
        TestPocoState value4 = accessor1.get(
            turnContext,
            () -> new TestPocoState("default-value")
        ).join();

        Assert.assertEquals("default-value", value4.getValue());
    }

    @Test
    public void BotStateDelete() {
        TurnContext turnContext = TestUtilities.createEmptyContext();
        turnContext.getActivity().setConversation(new ConversationAccount("1234"));

        Storage storage = new MemoryStorage(new HashMap<>());

        // Turn 0
        ConversationState botState0 = new ConversationState(storage);
        StatePropertyAccessor<TestPocoState> accessor0 = botState0.createProperty("test-name");
        TestPocoState value0 = accessor0.get(
            turnContext,
            () -> new TestPocoState("test-value")
        ).join();
        value0.setValue("test-value");
        botState0.saveChanges(turnContext).join();

        // Turn 1
        ConversationState botState1 = new ConversationState(storage);
        StatePropertyAccessor<TestPocoState> accessor1 = botState1.createProperty("test-name");
        TestPocoState value1 = accessor1.get(
            turnContext,
            () -> new TestPocoState("default-value")
        ).join();
        botState1.saveChanges(turnContext).join();

        Assert.assertEquals("test-value", value1.getValue());

        // Turn 2
        ConversationState botState2 = new ConversationState(storage);
        botState2.delete(turnContext).join();

        // Turn 3
        ConversationState botState3 = new ConversationState(storage);
        StatePropertyAccessor<TestPocoState> accessor3 = botState3.createProperty("test-name");
        TestPocoState value3 = accessor1.get(
            turnContext,
            () -> new TestPocoState("default-value")
        ).join();
        botState1.saveChanges(turnContext).join();

        Assert.assertEquals("default-value", value3.getValue());
    }

    private static class TestPocoState {
        private String value;

        public TestPocoState() {

        }

        public TestPocoState(String withValue) {
            value = withValue;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class CustomState implements StoreItem {
        private String _customString;
        private String _eTag;

        public String getCustomString() {
            return _customString;
        }

        public void setCustomString(String customString) {
            this._customString = customString;
        }

        public String getETag() {
            return _eTag;
        }

        public void setETag(String eTag) {
            this._eTag = eTag;
        }
    }

    private static class TypedObject {
        @JsonProperty
        private String name;

        public String name() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class TestState implements StoreItem {
        private String etag;
        private String value;

        @Override
        public String getETag() {
            return this.etag;
        }

        @Override
        public void setETag(String etag) {
            this.etag = etag;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class TestBotState extends BotState {
        public TestBotState(Storage withStorage) {
            super(withStorage, TestBotState.class.getSimpleName());
        }

        // "botstate/{turnContext.Activity.ChannelId}/{turnContext.Activity.Conversation.Id}/{typeof(BotState).Namespace}.{typeof(BotState).Name}";
        @Override
        public String getStorageKey(TurnContext turnContext) {
            return "botstate/" + turnContext.getActivity().getConversation().getId() + "/"
                + BotState.class.getName();
        }
    }

    private static class CustomKeyState extends BotState {
        public static final String PROPERTY_NAME = "CustomKeyState";

        public CustomKeyState(Storage storage) {
            super(storage, PROPERTY_NAME);
        }

        @Override
        public String getStorageKey(TurnContext turnContext) {
            return "CustomKey";
        }
    }
}
