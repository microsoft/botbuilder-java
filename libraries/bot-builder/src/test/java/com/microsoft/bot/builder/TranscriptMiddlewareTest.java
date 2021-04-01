// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

public class TranscriptMiddlewareTest {

    @Test
    public final void Transcript_MiddlewareTest() {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TranscriptLoggerMiddleware logger = new TranscriptLoggerMiddleware(transcriptStore);
        TestAdapter adapter = new TestAdapter();
        Activity activity = Activity.createMessageActivity();
        activity.setFrom(new ChannelAccount("acctid", "MyAccount", RoleTypes.USER));
        TurnContextImpl context = new TurnContextImpl(adapter, activity);
        NextDelegate nd = new NextDelegate() {
            @Override
            public CompletableFuture<Void> next() {
                System.out.printf("Delegate called!");
                System.out.flush();
                return null;
            }
        };
        Activity typingActivity = new Activity(ActivityTypes.TYPING);
        typingActivity.setRelatesTo(context.getActivity().getRelatesTo());

        try {
            context.sendActivity(typingActivity).join();
            System.out.printf("HI");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public final void Transcript_LogActivities() {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).use(
            new TranscriptLoggerMiddleware(transcriptStore)
        );
        final String[] conversationId = { null };

        new TestFlow(adapter, (context) -> {
            delay(500);
            conversationId[0] = context.getActivity().getConversation().getId();
            Activity typingActivity = new Activity(ActivityTypes.TYPING);
            typingActivity.setRelatesTo(context.getActivity().getRelatesTo());

            context.sendActivity(typingActivity).join();

            delay(500);

            context.sendActivity("echo:" + context.getActivity().getText()).join();
            return CompletableFuture.completedFuture(null);
        }
        ).send("foo").delay(50).assertReply(
            (activity) -> Assert.assertEquals(activity.getType(), ActivityTypes.TYPING)
        ).assertReply("echo:foo").send("bar").delay(50).assertReply(
            (activity) -> Assert.assertEquals(activity.getType(), ActivityTypes.TYPING)
        ).assertReply("echo:bar").startTest().join();

        PagedResult pagedResult = transcriptStore.getTranscriptActivities(
            "test",
            conversationId[0]
        ).join();
        Assert.assertEquals(6, pagedResult.getItems().size());
        Assert.assertEquals("foo", ((Activity) pagedResult.getItems().get(0)).getText());
        Assert.assertNotEquals(pagedResult.getItems().get(1), null);
        Assert.assertEquals("echo:foo", ((Activity) pagedResult.getItems().get(2)).getText());
        Assert.assertEquals("bar", ((Activity) pagedResult.getItems().get(3)).getText());

        Assert.assertTrue(pagedResult.getItems().get(4) != null);
        Assert.assertEquals("echo:bar", ((Activity) pagedResult.getItems().get(5)).getText());
        for (Object activity : pagedResult.getItems()) {
            Assert.assertFalse(StringUtils.isBlank(((Activity) activity).getId()));
            Assert.assertTrue(((Activity) activity).getTimestamp().isAfter(OffsetDateTime.MIN));
        }
        System.out.printf("Complete");
    }

    @Test
    public void Transcript_LogUpdateActivities() {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).use(
            new TranscriptLoggerMiddleware(transcriptStore)
        );
        final String[] conversationId = { null };
        final Activity[] activityToUpdate = { null };
        new TestFlow(adapter, (context) -> {
            delay(500);
            conversationId[0] = context.getActivity().getConversation().getId();
            if (context.getActivity().getText().equals("update")) {
                activityToUpdate[0].setText("new response");
                context.updateActivity(activityToUpdate[0]).join();
            } else {
                Activity activity = context.getActivity().createReply("response");
                ResourceResponse response = context.sendActivity(activity).join();
                activity.setId(response.getId());

                // clone the activity, so we can use it to do an update
                activityToUpdate[0] = Activity.clone(activity);
            }

            return CompletableFuture.completedFuture(null);
        }
        ).send("foo").delay(50).send("update").delay(50).assertReply(
            "new response"
        ).startTest().join();

        PagedResult pagedResult = transcriptStore.getTranscriptActivities(
            "test",
            conversationId[0]
        ).join();
        Assert.assertEquals(4, pagedResult.getItems().size());
        Assert.assertEquals("foo", ((Activity) pagedResult.getItems().get(0)).getText());
        Assert.assertEquals("response", ((Activity) pagedResult.getItems().get(1)).getText());
        Assert.assertEquals("new response", ((Activity) pagedResult.getItems().get(2)).getText());
        Assert.assertEquals("update", ((Activity) pagedResult.getItems().get(3)).getText());
        Assert.assertEquals(
            ((Activity) pagedResult.getItems().get(1)).getId(),
            ((Activity) pagedResult.getItems().get(2)).getId()
        );
    }

    @Test
    public final void Transcript_LogDeleteActivities() {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).use(
            new TranscriptLoggerMiddleware(transcriptStore)
        );
        final String[] conversationId = { null };
        final String[] activityId = { null };
        new TestFlow(adapter, (context) -> {
            delay(500);
            conversationId[0] = context.getActivity().getConversation().getId();
            if (context.getActivity().getText().equals("deleteIt")) {
                context.deleteActivity(activityId[0]).join();
            } else {
                Activity activity = context.getActivity().createReply("response");
                ResourceResponse response = context.sendActivity(activity).join();
                activityId[0] = response.getId();
            }

            return CompletableFuture.completedFuture(null);
        }).send("foo").delay(50).assertReply("response").send("deleteIt").startTest().join();

        PagedResult pagedResult = transcriptStore.getTranscriptActivities(
            "test",
            conversationId[0]
        ).join();
        for (Object act : pagedResult.getItems()) {
            System.out.printf(
                "Here is the object: %s : Type: %s\n",
                act.getClass().getTypeName(),
                ((Activity) act).getType()
            );
        }

        for (Object activity : pagedResult.getItems()) {
            System.out.printf(
                "Recipient: %s\nText: %s\n",
                ((Activity) activity).getRecipient().getName(),
                ((Activity) activity).getText()
            );
        }
        Assert.assertEquals(4, pagedResult.getItems().size());
        Assert.assertEquals("foo", ((Activity) pagedResult.getItems().get(0)).getText());
        Assert.assertEquals("response", ((Activity) pagedResult.getItems().get(1)).getText());
        Assert.assertEquals("deleteIt", ((Activity) pagedResult.getItems().get(2)).getText());
        Assert.assertEquals(
            ActivityTypes.MESSAGE_DELETE,
            ((Activity) pagedResult.getItems().get(3)).getType()
        );
        Assert.assertEquals(
            ((Activity) pagedResult.getItems().get(1)).getId(),
            ((Activity) pagedResult.getItems().get(3)).getId()
        );
    }

    @Test
    public void Transcript_TestDateLogUpdateActivities() {
        OffsetDateTime dateTimeStartOffset1 = OffsetDateTime.now();
        OffsetDateTime dateTimeStartOffset2 = OffsetDateTime.now(ZoneId.of("UTC"));

        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).use(
            new TranscriptLoggerMiddleware(transcriptStore)
        );

        final String[] conversationId = { null };
        final Activity[] activityToUpdate = { null };
        new TestFlow(adapter, (context) -> {
            delay(500);
            conversationId[0] = context.getActivity().getConversation().getId();
            if (context.getActivity().getText().equals("update")) {
                activityToUpdate[0].setText("new response");
                context.updateActivity(activityToUpdate[0]).join();
            } else {
                Activity activity = context.getActivity().createReply("response");
                ResourceResponse response = context.sendActivity(activity).join();
                activity.setId(response.getId());

                activityToUpdate[0] = Activity.clone(activity);
            }

            return CompletableFuture.completedFuture(null);
        }
        ).send("foo").delay(50).send("update").delay(50).assertReply(
            "new response"
        ).startTest().join();

        PagedResult pagedResult = transcriptStore.getTranscriptActivities(
            "test",
            conversationId[0],
            null,
            dateTimeStartOffset1
        ).join();
        Assert.assertEquals(4, pagedResult.getItems().size());
        Assert.assertEquals("foo", ((Activity) pagedResult.getItems().get(0)).getText());
        Assert.assertEquals("response", ((Activity) pagedResult.getItems().get(1)).getText());
        Assert.assertEquals("new response", ((Activity) pagedResult.getItems().get(2)).getText());
        Assert.assertEquals("update", ((Activity) pagedResult.getItems().get(3)).getText());
        Assert.assertEquals(
            ((Activity) pagedResult.getItems().get(1)).getId(),
            ((Activity) pagedResult.getItems().get(2)).getId()
        );

        pagedResult = transcriptStore.getTranscriptActivities(
            "test",
            conversationId[0],
            null,
            OffsetDateTime.MIN
        ).join();
        Assert.assertEquals(4, pagedResult.getItems().size());
        Assert.assertEquals("foo", ((Activity) pagedResult.getItems().get(0)).getText());
        Assert.assertEquals("response", ((Activity) pagedResult.getItems().get(1)).getText());
        Assert.assertEquals("new response", ((Activity) pagedResult.getItems().get(2)).getText());
        Assert.assertEquals("update", ((Activity) pagedResult.getItems().get(3)).getText());
        Assert.assertEquals(
            ((Activity) pagedResult.getItems().get(1)).getId(),
            ((Activity) pagedResult.getItems().get(2)).getId()
        );

        pagedResult = transcriptStore.getTranscriptActivities(
            "test",
            conversationId[0],
            null,
            OffsetDateTime.MAX
        ).join();
        Assert.assertEquals(0, pagedResult.getItems().size());
    }

    @Test
    public final void Transcript_RolesAreFilled() {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).use(
            new TranscriptLoggerMiddleware(transcriptStore)
        );
        final String[] conversationId = { null };

        new TestFlow(adapter, (context) -> {
            delay(500);
            // The next assert implicitly tests the immutability of the incoming
            // message. As demonstrated by the asserts after this TestFlow block
            // the role attribute is present on the activity as it is passed to
            // the transcript, but still missing inside the flow
            Assert.assertNotNull(context.getActivity().getFrom().getRole());
            conversationId[0] = context.getActivity().getConversation().getId();
            context.sendActivity("echo:" + context.getActivity().getText()).join();
            return CompletableFuture.completedFuture(null);
        }).send("test").startTest().join();

        PagedResult<Activity> pagedResult = transcriptStore.getTranscriptActivities(
            "test",
            conversationId[0]
        ).join();
        Assert.assertEquals(2, pagedResult.getItems().size());
        Assert.assertNotNull(pagedResult.getItems().get(0).getFrom());
        Assert.assertEquals(RoleTypes.USER, pagedResult.getItems().get(0).getFrom().getRole());
        Assert.assertNotNull(pagedResult.getItems().get(1).getFrom());
        Assert.assertEquals(RoleTypes.BOT, pagedResult.getItems().get(1).getFrom().getRole());

        System.out.printf("Complete");
    }

    /**
     * Time period delay.
     * @param milliseconds Time to delay.
     */
    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
