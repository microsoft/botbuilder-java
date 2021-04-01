// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class TestAdapterTests {
    public CompletableFuture<Void> myBotLogic(TurnContext turnContext) {
        switch (turnContext.getActivity().getText()) {
            case "count":
                turnContext.sendActivity(turnContext.getActivity().createReply("one")).join();
                turnContext.sendActivity(turnContext.getActivity().createReply("two")).join();
                turnContext.sendActivity(turnContext.getActivity().createReply("three")).join();
                break;

            case "ignore":
                break;

            default:
                turnContext.sendActivity(
                    turnContext.getActivity().createReply(
                        "echo:" + turnContext.getActivity().getText()
                    )
                ).join();
                break;
        }

        return CompletableFuture.completedFuture(null);
    }

    @Test
    public void TestAdapter_ExceptionTypesOnTest() {
        String uniqueExceptionId = UUID.randomUUID().toString();
        TestAdapter adapter = new TestAdapter();

        try {

            new TestFlow(adapter, turnContext -> {
                turnContext.sendActivity(turnContext.getActivity().createReply("one")).join();
                return CompletableFuture.completedFuture(null);
            }).test("foo", activity -> {
                throw new RuntimeException(uniqueExceptionId);
            }).startTest().join();

            Assert.fail("An exception should have been thrown");
        } catch (Throwable t) {
            Assert.assertTrue(t.getMessage().contains(uniqueExceptionId));
        }
    }

    @Test
    public void TestAdapter_ExceptionInBotOnReceive() {
        String uniqueExceptionId = UUID.randomUUID().toString();
        TestAdapter adapter = new TestAdapter();

        try {

            new TestFlow(adapter, turnContext -> {
                return Async.completeExceptionally(new RuntimeException(uniqueExceptionId));
            }).test("foo", activity -> {
                Assert.assertNull(activity);
            }).startTest().join();

            Assert.fail("An exception should have been thrown");
        } catch (Throwable t) {
            Assert.assertTrue(t.getMessage().contains(uniqueExceptionId));
        }
    }

    @Test
    public void TestAdapter_ExceptionTypesOnAssertReply() {
        String uniqueExceptionId = UUID.randomUUID().toString();
        TestAdapter adapter = new TestAdapter();

        try {

            new TestFlow(adapter, turnContext -> {
                turnContext.sendActivity(turnContext.getActivity().createReply("one")).join();
                return CompletableFuture.completedFuture(null);
            }).send("foo").assertReply(activity -> {
                throw new RuntimeException(uniqueExceptionId);
            }).startTest().join();

            Assert.fail("An exception should have been thrown");
        } catch (Throwable t) {
            Assert.assertTrue(t.getMessage().contains(uniqueExceptionId));
        }
    }

    @Test
    public void TestAdapter_SaySimple() {
        TestAdapter adapter = new TestAdapter();
        new TestFlow(adapter, this::myBotLogic).test(
            "foo",
            "echo:foo",
            "say with string works"
        ).startTest().join();
    }

    @Test
    public void TestAdapter_Say() {
        TestAdapter adapter = new TestAdapter();
        Activity messageActivity = new Activity(ActivityTypes.MESSAGE);
        messageActivity.setText("echo:foo");
        new TestFlow(adapter, this::myBotLogic).test(
            "foo",
            "echo:foo",
            "say with string works"
        ).test("foo", messageActivity, "say with activity works").test("foo", activity -> {
            Assert.assertEquals("echo:foo", activity.getText());
        }, "say with validator works").startTest().join();
    }

    @Test
    public void TestAdapter_SendReply() {
        TestAdapter adapter = new TestAdapter();
        Activity messageActivity = new Activity(ActivityTypes.MESSAGE);
        messageActivity.setText("echo:foo");
        new TestFlow(adapter, this::myBotLogic).send("foo").assertReply(
            "echo:foo",
            "say with string works"
        ).send("foo").assertReply(messageActivity, "say with activity works").send("foo").assertReply(activity -> {
            Assert.assertEquals("echo:foo", activity.getText());
        }, "say with validator works").startTest().join();
    }

    @Test
    public void TestAdapter_ReplyOneOf() {
        TestAdapter adapter = new TestAdapter();
        new TestFlow(adapter, this::myBotLogic).send("foo").assertReplyOneOf(
            new String[] { "echo:bar", "echo:foo", "echo:blat" },
            "say with string works"
        ).startTest().join();
    }

    @Test
    public void TestAdapter_MultipleReplies() {
        TestAdapter adapter = new TestAdapter();
        new TestFlow(adapter, this::myBotLogic).send("foo").assertReply("echo:foo").send(
            "bar"
        ).assertReply("echo:bar").send("ignore").send("count").assertReply("one").assertReply(
            "two"
        ).assertReply("three").startTest().join();
    }

    @Test
    public void TestAdapter_TestFlow() {
        String uniqueExceptionId = UUID.randomUUID().toString();
        TestAdapter adapter = new TestAdapter();

        TestFlow testFlow = new TestFlow(adapter, turnContext -> {
            CompletableFuture<Void> result = new CompletableFuture<>();
            result.completeExceptionally(new Exception());
            return result;
        }).send("foo");

        testFlow.startTest().exceptionally(exception -> {
            Assert.assertTrue(exception instanceof CompletionException);
            Assert.assertNotNull(exception.getCause());
            return null;
        }).join();
    }

    @Test
    public void TestAdapter_GetUserTokenAsyncReturnsNull() {
        TestAdapter adapter = new TestAdapter();
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId("directline");
        ChannelAccount from = new ChannelAccount();
        from.setId("testuser");
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        TokenResponse token = adapter.getUserToken(turnContext, "myconnection", null).join();
        Assert.assertNull(token);
    }

    @Test
    public void TestAdapter_GetUserTokenAsyncReturnsNullWithCode() {
        TestAdapter adapter = new TestAdapter();
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId("directline");
        ChannelAccount from = new ChannelAccount();
        from.setId("testuser");
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        TokenResponse token = adapter.getUserToken(turnContext, "myconnection", "abc123").join();
        Assert.assertNull(token);
    }

    @Test
    public void TestAdapter_GetUserTokenAsyncReturnsToken() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        String token = "abc123";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        adapter.addUserToken(connectionName, channelId, userId, token, null);

        TokenResponse tokenResponse = adapter.getUserToken(
            turnContext,
            connectionName,
            null
        ).join();
        Assert.assertNotNull(tokenResponse);
        Assert.assertEquals(token, tokenResponse.getToken());
        Assert.assertEquals(connectionName, tokenResponse.getConnectionName());
    }

    @Test
    public void TestAdapter_GetUserTokenAsyncReturnsTokenWithMagicCode() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        String token = "abc123";
        String magicCode = "888999";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        adapter.addUserToken(connectionName, channelId, userId, token, magicCode);

        TokenResponse tokenResponse = adapter.getUserToken(
            turnContext,
            connectionName,
            null
        ).join();
        Assert.assertNull(tokenResponse);

        tokenResponse = adapter.getUserToken(turnContext, connectionName, magicCode).join();
        Assert.assertNotNull(tokenResponse);
        Assert.assertEquals(token, tokenResponse.getToken());
        Assert.assertEquals(connectionName, tokenResponse.getConnectionName());
    }

    @Test
    public void TestAdapter_GetSignInLink() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        String link = adapter.getOAuthSignInLink(turnContext, connectionName, userId, null).join();
        Assert.assertNotNull(link);
        Assert.assertTrue(link.length() > 0);
    }

    @Test
    public void TestAdapter_GetSignInLinkWithNoUserId() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        String link = adapter.getOAuthSignInLink(turnContext, connectionName).join();
        Assert.assertNotNull(link);
        Assert.assertTrue(link.length() > 0);
    }

    @Test
    public void TestAdapter_SignOutNoop() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        adapter.signOutUser(turnContext, null, null).join();
        adapter.signOutUser(turnContext, connectionName, null).join();
        adapter.signOutUser(turnContext, connectionName, userId).join();
        adapter.signOutUser(turnContext, null, userId).join();
    }

    @Test
    public void TestAdapter_SignOut() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        String token = "abc123";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        adapter.addUserToken(connectionName, channelId, userId, token, null);

        TokenResponse tokenResponse = adapter.getUserToken(
            turnContext,
            connectionName,
            null
        ).join();
        Assert.assertNotNull(tokenResponse);
        Assert.assertEquals(token, tokenResponse.getToken());
        Assert.assertEquals(connectionName, tokenResponse.getConnectionName());

        adapter.signOutUser(turnContext, connectionName, userId).join();
        tokenResponse = adapter.getUserToken(turnContext, connectionName, null).join();
        Assert.assertNull(tokenResponse);
    }

    @Test
    public void TestAdapter_SignOutAll() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        String token = "abc123";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        adapter.addUserToken("ABC", channelId, userId, token, null);
        adapter.addUserToken("DEF", channelId, userId, token, null);

        TokenResponse tokenResponse = adapter.getUserToken(turnContext, "ABC", null).join();
        Assert.assertNotNull(tokenResponse);
        Assert.assertEquals(token, tokenResponse.getToken());
        Assert.assertEquals("ABC", tokenResponse.getConnectionName());

        tokenResponse = adapter.getUserToken(turnContext, "DEF", null).join();
        Assert.assertNotNull(tokenResponse);
        Assert.assertEquals(token, tokenResponse.getToken());
        Assert.assertEquals("DEF", tokenResponse.getConnectionName());

        adapter.signOutUser(turnContext, null, userId).join();
        tokenResponse = adapter.getUserToken(turnContext, "ABC", null).join();
        Assert.assertNull(tokenResponse);
        tokenResponse = adapter.getUserToken(turnContext, "DEF", null).join();
        Assert.assertNull(tokenResponse);
    }

    @Test
    public void TestAdapter_GetTokenStatus() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        String token = "abc123";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        adapter.addUserToken("ABC", channelId, userId, token, null);
        adapter.addUserToken("DEF", channelId, userId, token, null);

        List<TokenStatus> status = adapter.getTokenStatus(turnContext, userId, null).join();
        Assert.assertNotNull(status);
        Assert.assertEquals(2, status.size());
    }

    @Test
    public void TestAdapter_GetTokenStatusWithFilter() {
        TestAdapter adapter = new TestAdapter();
        String connectionName = "myConnection";
        String channelId = "directline";
        String userId = "testUser";
        String token = "abc123";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId(channelId);
        ChannelAccount from = new ChannelAccount();
        from.setId(userId);
        activity.setFrom(from);
        TurnContext turnContext = new TurnContextImpl(adapter, activity);

        adapter.addUserToken("ABC", channelId, userId, token, null);
        adapter.addUserToken("DEF", channelId, userId, token, null);

        List<TokenStatus> status = adapter.getTokenStatus(turnContext, userId, "DEF").join();
        Assert.assertNotNull(status);
        Assert.assertEquals(1, status.size());
    }
}
