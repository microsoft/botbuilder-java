
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.core.extensions;


import com.ea.async.Async;
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
            CompletableFuture<Void> doit =  CompletableFuture.runAsync( () -> {
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
                    CompletableFuture<Void> doit =  CompletableFuture.runAsync( () -> {
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
/*
    @Test
    public void State_RememberIStoreItemConversationState() {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TestState>(new MemoryStorage()));
        await(new TestFlow(adapter,
                async(context) = >
                {
                        var conversationState = context.GetConversationState < TestState > ();
        Assert.assertNotNull(conversationState, "state.conversation should exist");
        switch (context.Activity.AsMessageActivity().Text) {
            case "set value":
                conversationState.Value = "test";
                await(context.SendActivity("value saved");
                break;
            case "get value":
                await(context.SendActivity(conversationState.Value);
                break;
        }
                    }
                )
                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest();
    }

    @Test
    public void State_RememberPocoConversationState() {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TestPocoState>(new MemoryStorage()));
        await(new TestFlow(adapter,
                async(context) = >
                {
                        var conversationState = context.GetConversationState < TestPocoState > ();
        Assert.assertNotNull(conversationState, "state.conversation should exist");
        switch (context.Activity.AsMessageActivity().Text) {
            case "set value":
                conversationState.Value = "test";
                await(context.SendActivity("value saved");
                break;
            case "get value":
                await(context.SendActivity(conversationState.Value);
                break;
        }
                    }
                )
                .Test("set value", "value saved")
                .Test("get value", "test")
                .StartTest();
    }

    @Test
    public void State_CustomStateManagerTest() {

        string testGuid = Guid.NewGuid().ToString();
        TestAdapter adapter = new TestAdapter()
                .Use(new CustomKeyState(new MemoryStorage()));
        await(new TestFlow(adapter, async(context) = >
                {
                        var customState = CustomKeyState.Get(context);
        switch (context.Activity.AsMessageActivity().Text) {
            case "set value":
                customState.CustomString = testGuid;
                await(context.SendActivity("value saved");
                break;
            case "get value":
                await(context.SendActivity(customState.CustomString);
                break;
        }
                    }
                )
                .Test("set value", "value saved")
                .Test("get value", testGuid.ToString())
                .StartTest();
    }

    public class TypedObject {
        public string Name

        {
            get;
            set;
        }
    }

    @Test
    public void State_RoundTripTypedObject() {
        TestAdapter adapter = new TestAdapter()
                .Use(new ConversationState<TypedObject>(new MemoryStorage()));

        await(new TestFlow(adapter,
                async(context) = >
                {
                        var conversation = context.GetConversationState < TypedObject > ();
        Assert.assertNotNull(conversation, "conversationstate should exist");
        switch (context.Activity.AsMessageActivity().Text) {
            case "set value":
                conversation.Name = "test";
                await(context.SendActivity("value saved");
                break;
            case "get value":
                await(context.SendActivity(conversation.GetType().Name);
                break;
        }
                    }
                )
                .Test("set value", "value saved")
                .Test("get value", "TypedObject")
                .StartTest();
    }

    @Test
    public void State_UseBotStateDirectly() {
        var adapter = new TestAdapter();

        await(new TestFlow(adapter,
                async(context) = >
                {
                        var botStateManager = new BotState<CustomState>(new MemoryStorage(),
                        $"BotState:{typeof(BotState<CustomState>).Namespace}.{typeof(BotState<CustomState>).Name}",
                        (ctx) = > $"botstate/{ctx.Activity.ChannelId}/{ctx.Activity.Conversation.Id}/{typeof(BotState<CustomState>).Namespace}.{typeof(BotState<CustomState>).Name}");

        // read initial state object
        var customState = await(botStateManager.Read(context);

        // this should be a 'new CustomState' as nothing is currently stored in storage
        Assert.Equals(customState, new CustomState());

        // amend property and write to storage
        customState.CustomString = "test";
        await(botStateManager.Write(context, customState);

        // set customState to null before reading from storage
        customState = null;
        customState = await(botStateManager.Read(context);

        // check object read from value has the correct value for CustomString
        Assert.Equals(customState.CustomString, "test");
                    }
                )
                .StartTest();
    }

    public class CustomState :IStoreItem

    {
        public string CustomString {
        get;
        set;
    }
        public string eTag {
        get;
        set;
    }
    }

    public class CustomKeyState :BotState<CustomState>

    {
            public CustomKeyState(IStorage storage) :base(storage, PropertyName, (context) = > "CustomKey")
        {
        }

        public const string PropertyName = "Microsoft.Bot.Builder.Tests.CustomKeyState";

        public static CustomState Get (ITurnContext context){
        return context.Services.Get < CustomState > (PropertyName);
    }
    }*/
}
