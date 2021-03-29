// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ActivityHandlerTests {
    @Test
    public void TestMessageActivity() {
        Activity activity = MessageFactory.text("hello");
        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onMessageActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestOnInstallationUpdate() {
        Activity activity = new Activity(ActivityTypes.INSTALLATION_UPDATE);
        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onInstallationUpdate", bot.getRecord().get(0));
    }

    @Test
    public void TestInstallationUpdateAdd() {
        Activity activity = new Activity(ActivityTypes.INSTALLATION_UPDATE);
        activity.setAction("add");

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onInstallationUpdate", bot.getRecord().get(0));
        Assert.assertEquals("onInstallationUpdateAdd", bot.getRecord().get(1));
    }

    @Test
    public void TestInstallationUpdateAddUpgrade() {
        Activity activity = new Activity(ActivityTypes.INSTALLATION_UPDATE);
        activity.setAction("add-upgrade");

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onInstallationUpdate", bot.getRecord().get(0));
        Assert.assertEquals("onInstallationUpdateAdd", bot.getRecord().get(1));
    }

    @Test
    public void TestInstallationUpdateRemove() {
        Activity activity = new Activity(ActivityTypes.INSTALLATION_UPDATE);
        activity.setAction("remove");

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onInstallationUpdate", bot.getRecord().get(0));
        Assert.assertEquals("onInstallationUpdateRemove", bot.getRecord().get(1));
    }

    @Test
    public void TestInstallationUpdateRemoveUpgrade() {
        Activity activity = new Activity(ActivityTypes.INSTALLATION_UPDATE);
        activity.setAction("remove-upgrade");

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onInstallationUpdate", bot.getRecord().get(0));
        Assert.assertEquals("onInstallationUpdateRemove", bot.getRecord().get(1));
    }

    @Test
    public void TestOnAdaptiveCardInvoke() {

        AdaptiveCardInvokeValue adaptiveCardInvokeValue = new AdaptiveCardInvokeValue();
        AdaptiveCardInvokeAction adaptiveCardInvokeAction = new AdaptiveCardInvokeAction();
        adaptiveCardInvokeAction.setType("Action.Execute");
        adaptiveCardInvokeValue.setAction(adaptiveCardInvokeAction);

        JsonNode node = Serialization.objectToTree(adaptiveCardInvokeValue);
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.INVOKE);
                setName("adaptiveCard/action");
                setValue(node);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new TestInvokeAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onInvokeActivity", bot.getRecord().get(0));
        Assert.assertEquals("onAdaptiveCardInvoke", bot.getRecord().get(1));
    }

    @Test
    public void TestOnTypingActivity() {
        Activity activity = new Activity(ActivityTypes.TYPING);
        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onTypingActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMemberAdded1() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("b"));
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("b"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMemberAdded2() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        activity.setType(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("a"));
        members.add(new ChannelAccount("b"));
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("b"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersAdded", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberAdded3() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("a"));
        members.add(new ChannelAccount("b"));
        members.add(new ChannelAccount("c"));
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("b"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersAdded", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberRemoved1() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("c"));
        activity.setMembersRemoved(members);
        activity.setRecipient(new ChannelAccount("c"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMemberRemoved2() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("a"));
        members.add(new ChannelAccount("c"));
        activity.setMembersRemoved(members);
        activity.setRecipient(new ChannelAccount("c"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersRemoved", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberRemoved3() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("a"));
        members.add(new ChannelAccount("b"));
        members.add(new ChannelAccount("c"));
        activity.setMembersRemoved(members);
        activity.setRecipient(new ChannelAccount("c"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersRemoved", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberAddedJustTheBot() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("b"));
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("b"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMemberRemovedJustTheBot() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("c"));
        activity.setMembersRemoved(members);
        activity.setRecipient(new ChannelAccount("c"));

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMessageReaction() {
        // Note the code supports multiple adds and removes in the same activity though
        // a channel may decide to send separate activities for each. For example, Teams
        // sends separate activities each with a single add and a single remove.

        // Arrange
        Activity activity = new Activity(ActivityTypes.MESSAGE_REACTION);
        ArrayList<MessageReaction> reactionsAdded = new ArrayList<MessageReaction>();
        reactionsAdded.add(new MessageReaction("sad"));
        activity.setReactionsAdded(reactionsAdded);
        ArrayList<MessageReaction> reactionsRemoved = new ArrayList<MessageReaction>();
        reactionsRemoved.add(new MessageReaction("angry"));
        activity.setReactionsRemoved(reactionsRemoved);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(3, bot.getRecord().size());
        Assert.assertEquals("onMessageReactionActivity", bot.getRecord().get(0));
        Assert.assertEquals("onReactionsAdded", bot.getRecord().get(1));
        Assert.assertEquals("onReactionsRemoved", bot.getRecord().get(2));
    }

    @Test
    public void TestTokenResponseEventAsync() {
        Activity activity = new Activity(ActivityTypes.EVENT);
        activity.setName("tokens/response");

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onEventActivity", bot.getRecord().get(0));
        Assert.assertEquals("onTokenResponseEvent", bot.getRecord().get(1));
    }

    @Test
    public void TestEventAsync() {
        Activity activity = new Activity(ActivityTypes.EVENT);
        activity.setName("some.random.event");

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onEventActivity", bot.getRecord().get(0));
        Assert.assertEquals("onEvent", bot.getRecord().get(1));
    }

    @Test
    public void TestEventNullNameAsync() {
        Activity activity = new Activity(ActivityTypes.EVENT);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onEventActivity", bot.getRecord().get(0));
        Assert.assertEquals("onEvent", bot.getRecord().get(1));
    }

    @Test
    public void TestCommandActivityType() {
        Activity activity = new Activity(ActivityTypes.COMMAND);
        activity.setName("application/test");
        CommandValue<Object> commandValue = new CommandValue<Object>();
        commandValue.setCommandId("Test");
        commandValue.setData(new Object());
        activity.setValue(commandValue);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(bot.getRecord().size(), 1);
        Assert.assertEquals("onCommandActivity", bot.record.get(0));
    }

    @Test
    public void TestCommandResultActivityType() {
        Activity activity = new Activity(ActivityTypes.COMMAND_RESULT);
        activity.setName("application/test");
        CommandResultValue<Object> commandValue = new CommandResultValue<Object>();
        commandValue.setCommandId("Test");
        commandValue.setData(new Object());
        activity.setValue(commandValue);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(bot.getRecord().size(), 1);
        Assert.assertEquals("onCommandResultActivity", bot.record.get(0));
    }

    @Test
    public void TestUnrecognizedActivityType() {
        Activity activity = new Activity("shall.not.pass");

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onUnrecognizedActivityType", bot.getRecord().get(0));
    }

    private class TestInvokeAdapter extends NotImplementedAdapter {

        private Activity activity;

        public Activity getActivity() {
            return activity;
        }

        public CompletableFuture<ResourceResponse[]> sendActivities(
            TurnContext context,
            List<Activity> activities
        ) {
            activity = activities.stream()
                                 .filter(x -> x.getType().equals(ActivityTypes.INVOKE_RESPONSE))
                                 .findFirst()
                                 .get();
            return CompletableFuture.completedFuture(new ResourceResponse[0]);
        }
    }

    private static class NotImplementedAdapter extends BotAdapter {
        @Override
        public CompletableFuture<ResourceResponse[]> sendActivities(
            TurnContext context,
            List<Activity> activities
        ) {
            return Async.completeExceptionally(new RuntimeException());
        }

        @Override
        public CompletableFuture<ResourceResponse> updateActivity(
            TurnContext context,
            Activity activity
        ) {
            return Async.completeExceptionally(new RuntimeException());
        }

        @Override
        public CompletableFuture<Void> deleteActivity(
            TurnContext context,
            ConversationReference reference
        ) {
            return Async.completeExceptionally(new RuntimeException());
        }
    }

    private static class TestActivityHandler extends ActivityHandler {
        private List<String> record = new ArrayList<>();

        public List<String> getRecord() {
            return record;
        }

        public void setRecord(List<String> record) {
            this.record = record;
        }

        @Override
        protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
            record.add("onMessageActivity");
            return super.onMessageActivity(turnContext);
        }

        @Override
        protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
            record.add("onConversationUpdateActivity");
            return super.onConversationUpdateActivity(turnContext);
        }

        @Override
        protected CompletableFuture<Void> onMembersAdded(
            List<ChannelAccount> membersAdded,
            TurnContext turnContext
        ) {
            record.add("onMembersAdded");
            return super.onMembersAdded(membersAdded, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onMembersRemoved(
            List<ChannelAccount> membersRemoved,
            TurnContext turnContext
        ) {
            record.add("onMembersRemoved");
            return super.onMembersRemoved(membersRemoved, turnContext);
        }

        @Override
        protected CompletableFuture onMessageReactionActivity(TurnContext turnContext) {
            record.add("onMessageReactionActivity");
            return super.onMessageReactionActivity(turnContext);
        }

        @Override
        protected CompletableFuture onReactionsAdded(
            List<MessageReaction> messageReactions,
            TurnContext turnContext
        ) {
            record.add("onReactionsAdded");
            return super.onReactionsAdded(messageReactions, turnContext);
        }

        @Override
        protected CompletableFuture onReactionsRemoved(
            List<MessageReaction> messageReactions,
            TurnContext turnContext
        ) {
            record.add("onReactionsRemoved");
            return super.onReactionsRemoved(messageReactions, turnContext);
        }

        @Override
        protected CompletableFuture onEventActivity(TurnContext turnContext) {
            record.add("onEventActivity");
            return super.onEventActivity(turnContext);
        }

        @Override
        protected CompletableFuture onTokenResponseEvent(TurnContext turnContext) {
            record.add("onTokenResponseEvent");
            return super.onTokenResponseEvent(turnContext);
        }

        @Override
        protected CompletableFuture onEvent(TurnContext turnContext) {
            record.add("onEvent");
            return super.onEvent(turnContext);
        }

        @Override
        protected CompletableFuture<InvokeResponse> onInvokeActivity(TurnContext turnContext) {
            record.add("onInvokeActivity");
            return super.onInvokeActivity(turnContext);
        }

        @Override
        protected CompletableFuture onInstallationUpdate(TurnContext turnContext) {
            record.add("onInstallationUpdate");
            return super.onInstallationUpdate(turnContext);
        }

        @Override
        protected CompletableFuture<Void> onInstallationUpdateAdd(TurnContext turnContext) {
            record.add("onInstallationUpdateAdd");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onInstallationUpdateRemove(TurnContext turnContext) {
            record.add("onInstallationUpdateRemove");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture onTypingActivity(TurnContext turnContext) {
            record.add("onTypingActivity");
            return super.onTypingActivity(turnContext);
        }

        @Override
        protected CompletableFuture onUnrecognizedActivityType(TurnContext turnContext) {
            record.add("onUnrecognizedActivityType");
            return super.onUnrecognizedActivityType(turnContext);
        }

        @Override
        protected CompletableFuture onCommandActivity(TurnContext turnContext){
            record.add("onCommandActivity");
            return super.onCommandActivity(turnContext);
        }

        @Override
        protected CompletableFuture onCommandResultActivity(TurnContext turnContext) {
            record.add("onCommandResultActivity");
            return super.onCommandResultActivity(turnContext);
        }

        @Override
        protected CompletableFuture<AdaptiveCardInvokeResponse> onAdaptiveCardInvoke(
            TurnContext turnContext, AdaptiveCardInvokeValue invokeValue) {
            record.add("onAdaptiveCardInvoke");
            return CompletableFuture.completedFuture(new AdaptiveCardInvokeResponse());
        }

    }
}
