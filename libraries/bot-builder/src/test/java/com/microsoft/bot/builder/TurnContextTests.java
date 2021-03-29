// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Attachments;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.restclient.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class TurnContextTests {
    @Test(expected = IllegalArgumentException.class)
    public void ConstructorNullAdapter() {
        new TurnContextImpl(null, new Activity(ActivityTypes.MESSAGE));
        Assert.fail("Should Fail due to null Adapter");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ConstructorNullActivity() {
        new TurnContextImpl(new TestAdapter(), null);
        Assert.fail("Should Fail due to null Activity");
    }

    @Test
    public void Constructor() {
        new TurnContextImpl(new TestAdapter(), new Activity(ActivityTypes.MESSAGE));
    }

    @Test
    public void CacheValueUsingSetAndGet() {
        TestAdapter adapter = new TestAdapter();
        new TestFlow(adapter, (turnContext -> {
            switch (turnContext.getActivity().getText()) {
                case "count":
                    return turnContext.sendActivity(
                        turnContext.getActivity().createReply("one")
                    ).thenCompose(
                        resourceResponse -> turnContext.sendActivity(
                            turnContext.getActivity().createReply("two")
                        )
                    ).thenCompose(
                        resourceResponse -> turnContext.sendActivity(
                            turnContext.getActivity().createReply("two")
                        )
                    ).thenApply(resourceResponse -> null);

                case "ignore":
                    break;

                case "TestResponded":
                    if (turnContext.getResponded()) {
                        return Async.completeExceptionally(new RuntimeException("Responded is true"));
                    }

                    return turnContext.sendActivity(
                        turnContext.getActivity().createReply("one")
                    ).thenApply(resourceResponse -> {
                        if (!turnContext.getResponded()) {
                            throw new RuntimeException("Responded is false");
                        }
                        return null;
                    });

                default:
                    return turnContext.sendActivity(
                        turnContext.getActivity().createReply(
                            "echo:" + turnContext.getActivity().getText()
                        )
                    ).thenApply(resourceResponse -> null);
            }

            return CompletableFuture.completedFuture(null);
        })).send("TestResponded").startTest().join();
    }

    @Test(expected = IllegalArgumentException.class)
    public void GetThrowsOnNullKey() {
        TurnContext c = new TurnContextImpl(
            new SimpleAdapter(),
            new Activity(ActivityTypes.MESSAGE)
        );
        Object o = c.getTurnState().get((String) null);
    }

    @Test
    public void GetReturnsNullOnEmptyKey() {
        TurnContext c = new TurnContextImpl(
            new SimpleAdapter(),
            new Activity(ActivityTypes.MESSAGE)
        );
        Object service = c.getTurnState().get("");
        Assert.assertNull("Should not have found a service under an empty key", service);
    }

    @Test
    public void GetReturnsNullWithUnknownKey() {
        TurnContext c = new TurnContextImpl(
            new SimpleAdapter(),
            new Activity(ActivityTypes.MESSAGE)
        );
        Object service = c.getTurnState().get("test");
        Assert.assertNull("Should not have found a service with unknown key", service);
    }

    @Test
    public void CacheValueUsingGetAndSet() {
        TurnContext c = new TurnContextImpl(
            new SimpleAdapter(),
            new Activity(ActivityTypes.MESSAGE)
        );

        c.getTurnState().add("bar", "foo");
        String result = c.getTurnState().get("bar");

        Assert.assertEquals("foo", result);
    }

    @Test
    public void CacheValueUsingGetAndSetGenericWithTypeAsKeyName() {
        TurnContext c = new TurnContextImpl(
            new SimpleAdapter(),
            new Activity(ActivityTypes.MESSAGE)
        );

        c.getTurnState().add("foo");
        String result = c.getTurnState().get(String.class);

        Assert.assertEquals("foo", result);
    }

    @Test
    public void RequestIsSet() {
        TurnContext c = new TurnContextImpl(new SimpleAdapter(), TestMessage.Message());
        Assert.assertEquals("1234", c.getActivity().getId());
    }

    @Test
    public void SendAndSetResponded() {
        SimpleAdapter a = new SimpleAdapter();
        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));
        Assert.assertFalse(c.getResponded());
        ResourceResponse response = c.sendActivity(TestMessage.Message("testtest")).join();

        Assert.assertTrue(c.getResponded());
        Assert.assertEquals("testtest", response.getId());
    }

    @Test
    public void SendBatchOfActivities() {
        SimpleAdapter a = new SimpleAdapter();
        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));
        Assert.assertFalse(c.getResponded());

        Activity message1 = TestMessage.Message("message1");
        Activity message2 = TestMessage.Message("message2");

        ResourceResponse[] response = c.sendActivities(Arrays.asList(message1, message2)).join();

        Assert.assertTrue(c.getResponded());
        Assert.assertEquals(2, response.length);
        Assert.assertEquals("message1", response[0].getId());
        Assert.assertEquals("message2", response[1].getId());
    }

    @Test
    public void SendAndSetRespondedUsingIMessageActivity() {
        SimpleAdapter a = new SimpleAdapter();
        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));
        Assert.assertFalse(c.getResponded());

        Activity msg = TestMessage.Message();
        c.sendActivity(msg).join();
        Assert.assertTrue(c.getResponded());
    }

    @Test
    public void TraceActivitiesDoNoSetResponded() {
        SimpleAdapter a = new SimpleAdapter();
        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));
        Assert.assertFalse(c.getResponded());

        // Send a Trace Activity, and make sure responded is NOT set.
        Activity trace = Activity.createTraceActivity("trace");
        c.sendActivity(trace).join();
        Assert.assertFalse(c.getResponded());

        // Just to sanity check everything, send a Message and verify the
        // responded flag IS set.
        Activity msg = TestMessage.Message();
        c.sendActivity(msg).join();
        Assert.assertTrue(c.getResponded());
    }

    @Test
    public void SendOneActivityToAdapter() {
        boolean[] foundActivity = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter((activities) -> {
            Assert.assertTrue("Incorrect Count", activities.size() == 1);
            Assert.assertEquals("1234", activities.get(0).getId());
            foundActivity[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));
        c.sendActivity(TestMessage.Message()).join();
        Assert.assertTrue(foundActivity[0]);
    }

    @Test
    public void CallOnSendBeforeDelivery() {
        SimpleAdapter a = new SimpleAdapter();
        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        int[] count = new int[] { 0 };
        c.onSendActivities(((context, activities, next) -> {
            Assert.assertNotNull(activities);
            count[0] = activities.size();
            return next.get();
        }));

        c.sendActivity(TestMessage.Message()).join();

        Assert.assertEquals(1, count[0]);
    }

    @Test
    public void AllowInterceptionOfDeliveryOnSend() {
        boolean[] responsesSent = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter((activities) -> {
            responsesSent[0] = true;
            Assert.fail("Should not be called. Interceptor did not work");
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        int[] count = new int[] { 0 };
        c.onSendActivities(((context, activities, next) -> {
            Assert.assertNotNull(activities);
            count[0] = activities.size();

            // Do not call next.
            return CompletableFuture.completedFuture(null);
        }));

        c.sendActivity(TestMessage.Message()).join();
        Assert.assertEquals(1, count[0]);
        Assert.assertFalse("Responses made it to the adapter.", responsesSent[0]);
    }

    @Test
    public void InterceptAndMutateOnSend() {
        boolean[] foundIt = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter((activities) -> {
            Assert.assertNotNull(activities);
            Assert.assertTrue(activities.size() == 1);
            Assert.assertEquals("changed", activities.get(0).getId());
            foundIt[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        c.onSendActivities(((context, activities, next) -> {
            Assert.assertNotNull(activities);
            Assert.assertTrue(activities.size() == 1);
            Assert.assertEquals("1234", activities.get(0).getId());
            activities.get(0).setId("changed");
            return next.get();
        }));

        c.sendActivity(TestMessage.Message()).join();
        Assert.assertTrue(foundIt[0]);
    }

    @Test
    public void UpdateOneActivityToAdapter() {
        boolean[] foundActivity = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, (activity) -> {
            Assert.assertNotNull(activity);
            Assert.assertEquals("test", activity.getId());
            foundActivity[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        ResourceResponse updateResult = c.updateActivity(TestMessage.Message("test")).join();
        Assert.assertTrue(foundActivity[0]);
        Assert.assertEquals("test", updateResult.getId());
    }

    @Test
    public void UpdateActivityWithMessageFactory() {
        final String ACTIVITY_ID = "activity ID";
        final String CONVERSATION_ID = "conversation ID";

        boolean[] foundActivity = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, (activity) -> {
            Assert.assertNotNull(activity);
            Assert.assertEquals(ACTIVITY_ID, activity.getId());
            Assert.assertEquals(CONVERSATION_ID, activity.getConversation().getId());
            foundActivity[0] = true;
        });

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setConversation(new ConversationAccount(CONVERSATION_ID));
        TurnContext c = new TurnContextImpl(a, activity);

        Activity message = MessageFactory.text("test text");
        message.setId(ACTIVITY_ID);

        ResourceResponse updateResult = c.updateActivity(message).join();

        Assert.assertTrue(foundActivity[0]);
        Assert.assertEquals(ACTIVITY_ID, updateResult.getId());
    }

    @Test
    public void CallOnUpdateBeforeDelivery() {
        boolean[] activityDelivered = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, (activity) -> {
            Assert.assertNotNull(activity);
            Assert.assertEquals("1234", activity.getId());
            activityDelivered[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        boolean[] wasCalled = new boolean[] { false };
        c.onUpdateActivity(((context, activity, next) -> {
            Assert.assertNotNull(activity);
            Assert.assertFalse(activityDelivered[0]);
            wasCalled[0] = true;
            return next.get();
        }));

        c.updateActivity(TestMessage.Message()).join();

        Assert.assertTrue(wasCalled[0]);
        Assert.assertTrue(activityDelivered[0]);
    }

    @Test
    public void InterceptOnUpdate() {
        boolean[] activityDelivered = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, (activity) -> {
            activityDelivered[0] = true;
            Assert.fail("Should not be called.");
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        boolean[] wasCalled = new boolean[] { false };
        c.onUpdateActivity(((context, activity, next) -> {
            Assert.assertNotNull(activity);
            wasCalled[0] = true;

            // Do Not Call Next
            return CompletableFuture.completedFuture(null);
        }));

        c.updateActivity(TestMessage.Message()).join();

        Assert.assertTrue(wasCalled[0]);
        Assert.assertFalse(activityDelivered[0]);
    }

    @Test
    public void InterceptAndMutateOnUpdate() {
        boolean[] activityDelivered = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, (activity) -> {
            Assert.assertEquals("mutated", activity.getId());
            activityDelivered[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        c.onUpdateActivity(((context, activity, next) -> {
            Assert.assertNotNull(activity);
            Assert.assertEquals("1234", activity.getId());
            activity.setId("mutated");
            return next.get();
        }));

        c.updateActivity(TestMessage.Message()).join();

        Assert.assertTrue(activityDelivered[0]);
    }

    @Test
    public void DeleteOneActivityToAdapter() {
        boolean[] activityDeleted = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, null, (reference) -> {
            Assert.assertEquals("12345", reference.getActivityId());
            activityDeleted[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, TestMessage.Message());

        c.deleteActivity("12345").join();
        Assert.assertTrue(activityDeleted[0]);
    }

    @Test
    public void DeleteConversationReferenceToAdapter() {
        boolean[] activityDeleted = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, null, (reference) -> {
            Assert.assertEquals("12345", reference.getActivityId());
            activityDeleted[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, TestMessage.Message());

        ConversationReference reference = new ConversationReference();
        reference.setActivityId("12345");

        c.deleteActivity(reference).join();
        Assert.assertTrue(activityDeleted[0]);
    }

    @Test
    public void InterceptOnDelete() {
        boolean[] activityDeleted = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, null, (reference) -> {
            activityDeleted[0] = true;
            Assert.fail("Should not be called.");
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        boolean[] wasCalled = new boolean[] { false };
        c.onDeleteActivity(((context, activity, next) -> {
            Assert.assertNotNull(activity);
            wasCalled[0] = true;

            // Do Not Call Next
            return CompletableFuture.completedFuture(null);
        }));

        c.deleteActivity("1234").join();

        Assert.assertTrue(wasCalled[0]);
        Assert.assertFalse(activityDeleted[0]);
    }

    @Test
    public void DeleteWithNoOnDeleteHandlers() {
        boolean[] activityDeleted = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, null, (activity) -> {
            activityDeleted[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        c.deleteActivity("1234").join();

        Assert.assertTrue(activityDeleted[0]);
    }

    @Test
    public void InterceptAndMutateOnDelete() {
        boolean[] activityDeleted = new boolean[] { false };

        SimpleAdapter a = new SimpleAdapter(null, null, (reference) -> {
            Assert.assertEquals("mutated", reference.getActivityId());
            activityDeleted[0] = true;
        });

        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        c.onDeleteActivity(((context, reference, next) -> {
            Assert.assertNotNull(reference);
            Assert.assertEquals("1234", reference.getActivityId());
            reference.setActivityId("mutated");
            return next.get();
        }));

        c.deleteActivity("1234").join();

        Assert.assertTrue(activityDeleted[0]);
    }

    @Test
    public void ThrowExceptionInOnSend() {
        SimpleAdapter a = new SimpleAdapter();
        TurnContext c = new TurnContextImpl(a, new Activity(ActivityTypes.MESSAGE));

        c.onSendActivities(((context, activities, next) -> {
            CompletableFuture<ResourceResponse[]> result = new CompletableFuture();
            result.completeExceptionally(new RuntimeException("test"));
            return result;
        }));

        try {
            c.sendActivity(TestMessage.Message()).join();
            Assert.fail("ThrowExceptionInOnSend have thrown");
        } catch (CompletionException e) {
            Assert.assertEquals("test", e.getCause().getMessage());
        }
    }

    @Test
    public void TurnContextStateNoDispose() {
        ConnectorClient connector = new ConnectorClientThrowExceptionOnDispose();
        Assert.assertTrue(connector instanceof AutoCloseable);

        TurnContextStateCollection stateCollection = new TurnContextStateCollection();
        stateCollection.add("connector", connector);

        try {
            stateCollection.close();
        } catch (Throwable t) {
            Assert.fail("Should not have thrown");
        }
    }

    @Test
    public void TurnContextStateDisposeNonConnectorClient() {
        TrackDisposed disposableObject1 = new TrackDisposed();
        TrackDisposed disposableObject2 = new TrackDisposed();
        TrackDisposed disposableObject3 = new TrackDisposed();
        Assert.assertFalse(disposableObject1.disposed);
        Assert.assertFalse(disposableObject2.disposed);
        Assert.assertFalse(disposableObject3.disposed);

        ConnectorClient connector = new ConnectorClientThrowExceptionOnDispose();

        TurnContextStateCollection stateCollection = new TurnContextStateCollection();
        stateCollection.add("disposable1", disposableObject1);
        stateCollection.add("disposable2", disposableObject2);
        stateCollection.add("disposable3", disposableObject3);
        stateCollection.add("connector", connector);

        try {
            stateCollection.close();
        } catch (Throwable t) {
            Assert.fail("Should not have thrown");
        }

        Assert.assertTrue(disposableObject1.disposed);
        Assert.assertTrue(disposableObject2.disposed);
        Assert.assertTrue(disposableObject3.disposed);
    }

    private static class TrackDisposed implements AutoCloseable {
        public boolean disposed = false;

        @Override
        public void close() throws Exception {
            disposed = true;
        }
    }

    private static class ConnectorClientThrowExceptionOnDispose implements ConnectorClient {

        @Override
        public RestClient getRestClient() {
            return null;
        }

        @Override
        public String getUserAgent() {
            return null;
        }

        @Override
        public String getAcceptLanguage() {
            return null;
        }

        @Override
        public void setAcceptLanguage(String acceptLanguage) {

        }

        @Override
        public int getLongRunningOperationRetryTimeout() {
            return 0;
        }

        @Override
        public void setLongRunningOperationRetryTimeout(int timeout) {

        }

        @Override
        public boolean getGenerateClientRequestId() {
            return false;
        }

        @Override
        public void setGenerateClientRequestId(boolean generateClientRequestId) {

        }

        @Override
        public String baseUrl() {
            return null;
        }

        @Override
        public ServiceClientCredentials credentials() {
            return null;
        }

        @Override
        public Attachments getAttachments() {
            return null;
        }

        @Override
        public Conversations getConversations() {
            return null;
        }

        @Override
        public void close() throws Exception {
            throw new RuntimeException("Should not close");
        }
    }
}
