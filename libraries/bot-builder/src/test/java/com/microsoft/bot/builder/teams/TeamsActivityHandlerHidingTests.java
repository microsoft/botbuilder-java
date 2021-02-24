// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.MessageReaction;
import com.microsoft.bot.schema.ResourceResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * These are the same tests as are performed on direct subclasses of
 * ActivityHandler but this time the test bot is derived from
 * TeamsActivityHandler.
 */
public class TeamsActivityHandlerHidingTests {
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
    public void TestMemberAdded1() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("b"));
                    }
                });
                setRecipient(new ChannelAccount("b"));
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMemberAdded2() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("a"));
                        add(new ChannelAccount("b"));
                    }
                });
                setRecipient(new ChannelAccount("b"));
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersAdded", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberAdded3() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("a"));
                        add(new ChannelAccount("b"));
                        add(new ChannelAccount("c"));
                    }
                });
                setRecipient(new ChannelAccount("b"));
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersAdded", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberRemoved1() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersRemoved(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("c"));
                    }
                });
                setRecipient(new ChannelAccount("c"));
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMemberRemoved2() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersRemoved(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("a"));
                        add(new ChannelAccount("c"));
                    }
                });
                setRecipient(new ChannelAccount("c"));
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersRemoved", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberRemoved3() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersRemoved(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("a"));
                        add(new ChannelAccount("b"));
                        add(new ChannelAccount("c"));
                    }
                });
                setRecipient(new ChannelAccount("c"));
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
        Assert.assertEquals("onMembersRemoved", bot.getRecord().get(1));
    }

    @Test
    public void TestMemberAddedJustTheBot() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("b"));
                    }
                });
                setRecipient(new ChannelAccount("b"));
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onConversationUpdateActivity", bot.getRecord().get(0));
    }

    @Test
    public void TestMemberRemovedJustTheBot() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.CONVERSATION_UPDATE);
                setMembersRemoved(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("c"));
                    }
                });
                setRecipient(new ChannelAccount("c"));
            }
        };

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
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.MESSAGE_REACTION);
                setReactionsAdded(new ArrayList<MessageReaction>() {
                    {
                        add(new MessageReaction("sad"));
                    }
                });
                setReactionsRemoved(new ArrayList<MessageReaction>() {
                    {
                        add(new MessageReaction("angry"));
                    }
                });
            }
        };

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
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.EVENT);
                setName("tokens/response");
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onEventActivity", bot.getRecord().get(0));
        Assert.assertEquals("onTokenResponseEvent", bot.getRecord().get(1));
    }

    @Test
    public void TestEventAsync() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.EVENT);
                setName("some.random.event");
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onEventActivity", bot.getRecord().get(0));
        Assert.assertEquals("onEvent", bot.getRecord().get(1));
    }

    @Test
    public void TestEventNullNameAsync() {
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.EVENT);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.getRecord().size());
        Assert.assertEquals("onEventActivity", bot.getRecord().get(0));
        Assert.assertEquals("onEvent", bot.getRecord().get(1));
    }

    @Test
    public void TestUnrecognizedActivityType() {
        Activity activity = new Activity() {
            {
                setType("shall.not.pass");
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(1, bot.getRecord().size());
        Assert.assertEquals("onUnrecognizedActivityType", bot.getRecord().get(0));
    }

    private static class NotImplementedAdapter extends BotAdapter {
        @Override
        public CompletableFuture<ResourceResponse[]> sendActivities(
            TurnContext context,
            List<Activity> activities
        ) {
            throw new RuntimeException();
        }

        @Override
        public CompletableFuture<ResourceResponse> updateActivity(
            TurnContext context,
            Activity activity
        ) {
            throw new RuntimeException();
        }

        @Override
        public CompletableFuture<Void> deleteActivity(
            TurnContext context,
            ConversationReference reference
        ) {
            throw new RuntimeException();
        }
    }

    private static class TestActivityHandler extends TeamsActivityHandler {
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
        protected CompletableFuture onUnrecognizedActivityType(TurnContext turnContext) {
            record.add("onUnrecognizedActivityType");
            return super.onUnrecognizedActivityType(turnContext);
        }

    }
}
