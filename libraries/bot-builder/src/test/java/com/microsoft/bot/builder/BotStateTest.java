
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.bot.schema.models.ResourceResponse;
import com.microsoft.rest.RestClient;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


//    [TestClass]
//            [TestCategory("State Management")]
public class BotStateTest {
    protected ConnectorClientImpl connector;
    protected ChannelAccount bot;
    protected ChannelAccount user;


    protected void initializeClients(RestClient restClient, String botId, String userId) {

        connector = new ConnectorClientImpl(restClient);
        bot = new ChannelAccount().withId(botId);
        user = new ChannelAccount().withId(userId);

    }


    protected void cleanUpResources() {
    }

    @Test
    public void State_DoNOTRememberContextState() throws ExecutionException, InterruptedException {

        TestAdapter adapter = new TestAdapter();

        new TestFlow(adapter, (context) -> {
            TestPocoState obj = StateTurnContextExtensions.<TestPocoState>GetConversationState(context);
            Assert.assertNull("context.state should not exist", obj); }
        )
                .Send("set value")
                .StartTest();

    }

    //@Test
    public void State_RememberIStoreItemUserState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new UserState<TestState>(new MemoryStorage(), TestState::new));


        Consumer<TurnContext> callback = (context) -> {
                System.out.print(String.format("State_RememberIStoreItemUserState CALLBACK called.."));
                System.out.flush();
                TestState userState = StateTurnContextExtensions.<TestState>GetUserState(context);
                Assert.assertNotNull("user state should exist", userState);
                switch (context.getActivity().text()) {
                    case "set value":
                        userState.withValue("test");
                        try {
                            ((TurnContextImpl)context).SendActivity("value saved");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.fail(String.format("Error sending activity! - set value"));
                        }
                        break;
                    case "get value":
                        try {
                            Assert.assertFalse(StringUtils.isBlank(userState.value()));
                            ((TurnContextImpl)context).SendActivity(userState.value());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.fail(String.format("Error sending activity! - get value"));
                        }
                        break;
                }

        };

        new TestFlow(adapter, callback)
                .Test("set value", "value saved")
                .Test("get value", "test")
        .StartTest();

    }

    @Test
    public void State_RememberPocoUserState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new UserState<TestPocoState>(new MemoryStorage(), TestPocoState::new));
        new TestFlow(adapter,
                (context) ->
                {
                            TestPocoState userState = StateTurnContextExtensions.<TestPocoState>GetUserState(context);

                            Assert.assertNotNull("user state should exist", userState);
                            switch (context.getActivity().text()) {
                                case "set value":
                                    userState.setValue("test");
                                    try {
                                        context.SendActivity("value saved");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Assert.fail(String.format("Error sending activity! - set value"));
                                    }
                                    break;
                                case "get value":
                                    try {
                                        Assert.assertFalse(StringUtils.isBlank(userState.getValue()));
                                        context.SendActivity(userState.getValue());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Assert.fail(String.format("Error sending activity! - get value"));
                                    }
                                    break;
                            }
                })
                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest();
    }

    //@Test
    public void State_RememberIStoreItemConversationState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TestState>(new MemoryStorage(), TestState::new));
        new TestFlow(adapter,
                (context) ->
                {
                        TestState conversationState = StateTurnContextExtensions.<TestState>GetConversationState(context);
                        Assert.assertNotNull("state.conversation should exist", conversationState);
                        switch (context.getActivity().text()) {
                            case "set value":
                                conversationState.withValue("test");
                                try {
                                    context.SendActivity("value saved");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    Assert.assertFalse(StringUtils.isBlank(conversationState.value()));
                                    context.SendActivity(conversationState.value());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                })
                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest();
    }

    //@Test
    public void State_RememberPocoConversationState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TestPocoState>(new MemoryStorage(), TestPocoState::new));
        new TestFlow(adapter,
                (context) ->
                {
                        TestPocoState conversationState = StateTurnContextExtensions.<TestPocoState>GetConversationState(context);
                        Assert.assertNotNull("state.conversation should exist", conversationState);
                        switch (context.getActivity().text()) {
                            case "set value":
                                conversationState.setValue("test");
                                try {
                                    context.SendActivity("value saved");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    Assert.assertFalse(StringUtils.isBlank(conversationState.getValue()));
                                    context.SendActivity(conversationState.getValue());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                })

                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest();
    }

    @Test
    public void State_CustomStateManagerTest() throws ExecutionException, InterruptedException {

        String testGuid = UUID.randomUUID().toString();
        TestAdapter adapter = new TestAdapter()
                .Use(new CustomKeyState(new MemoryStorage()));
        new TestFlow(adapter,
                (context) ->
                {
                        CustomState customState = CustomKeyState.Get(context);

                        switch (context.getActivity().text()) {
                            case "set value":
                                customState.setCustomString(testGuid);
                                try {
                                    context.SendActivity("value saved");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    Assert.assertFalse(StringUtils.isBlank(customState.getCustomString()));
                                    context.SendActivity(customState.getCustomString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                })
                .Test("set value", "value saved")
                .Test("get value", testGuid.toString())
                .StartTest();
    }
    @Test
    public void State_RoundTripTypedObjectwTrace() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TypedObject>(new MemoryStorage(), TypedObject::new));
        new TestFlow(adapter,
                (context) ->
                {
                        System.out.println(String.format(">>Test Callback(tid:%s): STARTING : %s", Thread.currentThread().getId(), context.getActivity().text()));
                        System.out.flush();
                        TypedObject conversation = StateTurnContextExtensions.<TypedObject>GetConversationState(context);
                        Assert.assertNotNull("conversationstate should exist", conversation);
                        System.out.println(String.format(">>Test Callback(tid:%s): Text is : %s", Thread.currentThread().getId(), context.getActivity().text()));
                        System.out.flush();
                        switch (context.getActivity().text()) {
                            case "set value":
                                conversation.withName("test");
                                try {
                                    System.out.println(String.format(">>Test Callback(tid:%s): Send activity : %s", Thread.currentThread().getId(),
                                            "value saved"));
                                    System.out.flush();
                                    ResourceResponse response = context.SendActivity("value saved");
                                    System.out.println(String.format(">>Test Callback(tid:%s): Response Id: %s", Thread.currentThread().getId(),
                                            response.id()));
                                    System.out.flush();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    System.out.println(String.format(">>Test Callback(tid:%s): Send activity : %s", Thread.currentThread().getId(),
                                            "TypedObject"));
                                    System.out.flush();
                                    context.SendActivity("TypedObject");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                })
                .Turn("set value", "value saved", "Description", 50000)
                .Turn("get value", "TypedObject", "Description", 50000)
                .StartTest();

    }


    @Test
    public void State_RoundTripTypedObject() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TypedObject>(new MemoryStorage(), TypedObject::new));

        new TestFlow(adapter,
                (context) ->
                {
                        TypedObject conversation = StateTurnContextExtensions.<TypedObject>GetConversationState(context);
                        Assert.assertNotNull("conversationstate should exist", conversation);
                        switch (context.getActivity().text()) {
                            case "set value":
                                conversation.withName("test");
                                try {
                                    context.SendActivity("value saved");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    context.SendActivity("TypedObject");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                })
                .Test("set value", "value saved")
                .Test("get value", "TypedObject")
                .StartTest();

    }

    @Test
    public void State_UseBotStateDirectly() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter();

        new TestFlow(adapter,
                (context) ->
                {
                        BotState botStateManager = new BotState<CustomState>(new MemoryStorage(), "BotState:com.microsoft.bot.builder.core.extensions.BotState<CustomState>",
                                (ctx) -> String.format("botstate/%s/%s/com.microsoft.bot.builder.core.extensions.BotState<CustomState>",
                                        ctx.getActivity().channelId(), ctx.getActivity().conversation().id()), CustomState::new);

                        // read initial state object
                        CustomState customState = null;
                        try {
                            customState = (CustomState) botStateManager.<CustomState>Read(context).join();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            Assert.fail("Error reading custom state");
                        }

                        // this should be a 'new CustomState' as nothing is currently stored in storage
                        Assert.assertEquals(customState, new CustomState());

                        // amend property and write to storage
                        customState.setCustomString("test");
                        try {
                            botStateManager.Write(context, customState).join();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.fail("Could not write customstate");
                        }

                        // set customState to null before reading from storage
                        customState = null;
                        try {
                            customState = (CustomState) botStateManager.<CustomState>Read(context).join();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            Assert.fail("Could not read customstate back");
                        }

                        // check object read from value has the correct value for CustomString
                        Assert.assertEquals(customState.getCustomString(), "test");
                }
                )
                .StartTest();
    }


}

