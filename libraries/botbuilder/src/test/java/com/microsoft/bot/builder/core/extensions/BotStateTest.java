
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.core.extensions;


import com.ea.async.Async;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.bot.builder.core.TurnContext;
import com.microsoft.bot.builder.core.TurnContextImpl;
import com.microsoft.bot.builder.core.adapters.TestAdapter;
import com.microsoft.bot.builder.core.adapters.TestFlow;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.bot.schema.models.MessageActivity;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

//    [TestClass]
//            [TestCategory("State Management")]
public class BotStateTest {
    protected ConnectorClientImpl connector;
    protected ChannelAccount bot;
    protected ChannelAccount user;


    protected void initializeClients(RestClient restClient, String botId, String userId) {
        // Initialize async/await(support
        Async.init();

        connector = new ConnectorClientImpl(restClient);
        bot = new ChannelAccount().withId(botId);
        user = new ChannelAccount().withId(userId);

    }


    protected void cleanUpResources() {
    }

    @Test
    public void State_DoNOTRememberContextState() throws ExecutionException, InterruptedException {

        TestAdapter adapter = new TestAdapter();

        await(new TestFlow(adapter, (context) -> {
            TestPocoState obj = StateTurnContextExtensions.<TestPocoState>GetConversationState(context);
            Assert.assertNull("context.state should not exist", obj);
            return completedFuture(null);
        }
        )
                .Send("set value")
                .StartTest());

    }

    @Test
    public void State_RememberIStoreItemUserState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new UserState<TestState>(new MemoryStorage(), TestState::new));


        Function<TurnContext, CompletableFuture> callback = (context) -> {
            CompletableFuture<Void> doit = CompletableFuture.runAsync(() -> {
                System.out.print(String.format("State_RememberIStoreItemUserState CALLBACK called.."));
                System.out.flush();
                TestState userState = StateTurnContextExtensions.<TestState>GetUserState(context);
                Assert.assertNotNull("user state should exist", userState);
                switch (context.getActivity().text()) {
                    case "set value":
                        userState.withValue("test");
                        try {
                            await(context.SendActivity("value saved"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.fail(String.format("Error sending activity! - set value"));
                        }
                        break;
                    case "get value":
                        try {
                            await(context.SendActivity(userState.value()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.fail(String.format("Error sending activity! - get value"));
                        }
                        break;
                }

            });
            return doit;
        };

        TestFlow myTest = new TestFlow(adapter, callback)
                .Test("set value", "value saved")
                .Test("get value", "test");
        await(myTest.StartTest());

    }

    @Test
    public void State_RememberPocoUserState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new UserState<TestPocoState>(new MemoryStorage(), TestPocoState::new));
        await(new TestFlow(adapter,
                (context) ->
                {
                    CompletableFuture<Void> doit = CompletableFuture.runAsync(() -> {
                        {
                            TestPocoState userState = StateTurnContextExtensions.<TestPocoState>GetUserState(context);

                            Assert.assertNotNull("user state should exist", userState);
                            switch (context.getActivity().text()) {
                                case "set value":
                                    userState.setValue("test");
                                    try {
                                        await(context.SendActivity("value saved"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Assert.fail(String.format("Error sending activity! - set value"));
                                    }
                                    break;
                                case "get value":
                                    try {
                                        await(context.SendActivity(userState.getValue()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Assert.fail(String.format("Error sending activity! - get value"));
                                    }
                                    break;
                            }
                        }

                    });
                    return doit;
                })
                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest());
    }

    @Test
    public void State_RememberIStoreItemConversationState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TestState>(new MemoryStorage(), TestState::new));
        await(new TestFlow(adapter,
                (context) ->
                {
                    CompletableFuture<Void> doit = CompletableFuture.runAsync(() -> {
                        TestState conversationState = StateTurnContextExtensions.<TestState>GetConversationState(context);
                        Assert.assertNotNull("state.conversation should exist", conversationState);
                        switch (context.getActivity().text()) {
                            case "set value":
                                conversationState.withValue("test");
                                try {
                                    await(context.SendActivity("value saved"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    await(context.SendActivity(conversationState.value()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                    });
                    return doit;
                })
                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest());
    }

    @Test
    public void State_RememberPocoConversationState() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TestPocoState>(new MemoryStorage(), TestPocoState::new));
        await(new TestFlow(adapter,
                (context) ->
                {
                    CompletableFuture<Void> doit = CompletableFuture.runAsync(() -> {
                        TestPocoState conversationState = StateTurnContextExtensions.<TestPocoState>GetConversationState(context);
                        Assert.assertNotNull("state.conversation should exist", conversationState);
                        switch (context.getActivity().text()) {
                            case "set value":
                                conversationState.setValue("test");
                                try {
                                    await(context.SendActivity("value saved"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    await(context.SendActivity(conversationState.getValue()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                    });
                    return doit;
                })

                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest());
    }

    @Test
    public void State_CustomStateManagerTest() throws ExecutionException, InterruptedException {

        String testGuid = UUID.randomUUID().toString();
        TestAdapter adapter = new TestAdapter()
                .Use(new CustomKeyState(new MemoryStorage()));
        await(new TestFlow(adapter,
                (context) ->
                {
                    CompletableFuture<Void> doit = CompletableFuture.runAsync(() -> {
                        CustomState customState = CustomKeyState.Get(context);

                        switch (context.getActivity().text()) {
                            case "set value":
                                customState.setCustomString(testGuid);
                                try {
                                    await(context.SendActivity("value saved"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    await(context.SendActivity(customState.getCustomString()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                    });
                    return doit;
                })
                .Test("set value", "value saved")
                .Test("get value", testGuid.toString())
                .StartTest());
    }


    @Test
    public void State_RoundTripTypedObject() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TypedObject>(new MemoryStorage(), TypedObject::new));

        await(new TestFlow(adapter,
                (context) ->
                {
                    CompletableFuture<Void> doit = CompletableFuture.runAsync(() -> {
                        TypedObject conversation = StateTurnContextExtensions.<TypedObject>GetConversationState(context);
                        Assert.assertNotNull("conversationstate should exist", conversation);
                        switch (context.getActivity().text()) {
                            case "set value":
                                conversation.setName("test");
                                try {
                                    await(context.SendActivity("value saved"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - set value"));
                                }
                                break;
                            case "get value":
                                try {
                                    await(context.SendActivity("TypedObject"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Assert.fail(String.format("Error sending activity! - get value"));
                                }
                                break;
                        }
                    });
                    return doit;
                })
                .Test("set value", "value saved")
                .Test("get value", "TypedObject")
                .StartTest());

    }

    @Test
    public void State_UseBotStateDirectly() throws ExecutionException, InterruptedException {
        TestAdapter adapter = new TestAdapter();

        await(new TestFlow(adapter,
                (context) ->
                {
                    CompletableFuture<Void> doit = CompletableFuture.runAsync(() -> {
                        BotState botStateManager = new BotState<CustomState>(new MemoryStorage(), "BotState:com.microsoft.bot.builder.core.extensions.BotState<CustomState>",
                                (ctx) -> String.format("botstate/%s/%s/com.microsoft.bot.builder.core.extensions.BotState<CustomState>",
                                        ctx.getActivity().channelId(), ctx.getActivity().conversation().id()), CustomState::new);

                        // read initial state object
                        CustomState customState = null;
                        try {
                            customState = await(botStateManager.<CustomState>Read(context));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            Assert.fail("Error reading custom state");
                        }

                        // this should be a 'new CustomState' as nothing is currently stored in storage
                        Assert.assertEquals(customState, new CustomState());

                        // amend property and write to storage
                        customState.setCustomString("test");
                        try {
                            await(botStateManager.Write(context, customState));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.fail("Could not write customstate");
                        }

                        // set customState to null before reading from storage
                        customState = null;
                        try {
                            customState = await(botStateManager.Read(context));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            Assert.fail("Could not read customstate back");
                        }

                        // check object read from value has the correct value for CustomString
                        Assert.assertEquals(customState.getCustomString(), "test");
                    });
                    return doit;
                }
                )
                .StartTest());
    }


}

