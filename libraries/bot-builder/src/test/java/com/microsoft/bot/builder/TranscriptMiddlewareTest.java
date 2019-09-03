// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;


public class TranscriptMiddlewareTest {

    @Test
    public final void Transcript_SimpleReceive() throws Exception {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).Use(new TranscriptLoggerMiddleware(transcriptStore));
        final String[] conversationId = {null};


        new TestFlow(adapter, (ctxt) ->
        {

                TurnContextImpl context = (TurnContextImpl) ctxt;
                conversationId[0] = context.getActivity().getConversation().getId();
                Activity typingActivity = new Activity(ActivityTypes.TYPING) {{
                    setRelatesTo(context.getActivity().getRelatesTo());
                }};
                try {
                    ResourceResponse response = context.SendActivity(typingActivity);
                    System.out.printf("Here's the response:");
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
                try {
                    context.SendActivity("echo:" + context.getActivity().getText());
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail();
                }

        }).Send("foo")
                .AssertReply((activity) -> {
                    Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
                    return null;
                }).StartTest();
                //.AssertReply("echo:foo").StartTest();


    }

    @Test
    public final void Transcript_MiddlewareTest() throws Exception {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TranscriptLoggerMiddleware logger = new TranscriptLoggerMiddleware(transcriptStore);
        TestAdapter adapter = new TestAdapter();
        Activity activity = Activity.createMessageActivity();
        activity.setFrom(new ChannelAccount("acctid", "MyAccount", RoleTypes.USER));
        TurnContextImpl context = new TurnContextImpl(adapter, activity);
        NextDelegate nd = new NextDelegate() {
            @Override
            public void next() throws Exception {
                System.out.printf("Delegate called!");
                System.out.flush();
                return ;
            }
        };
        Activity typingActivity = new Activity(ActivityTypes.TYPING) {{
            setRelatesTo(context.getActivity().getRelatesTo());
        }};

        try {
            context.SendActivity(typingActivity);
            System.out.printf("HI");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }


        //logger.OnTurn(context, nd).get();
    }

    @Test
    public final void Transcript_LogActivities() throws ExecutionException, InterruptedException {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).Use(new TranscriptLoggerMiddleware(transcriptStore));
        final String[] conversationId = {null};


        String result = new TestFlow(adapter, (context) ->
        {

                //TurnContextImpl context = (TurnContextImpl) ctxt;
                conversationId[0] = context.getActivity().getConversation().getId();
                Activity typingActivity = new Activity(ActivityTypes.TYPING) {{
                    setRelatesTo(context.getActivity().getRelatesTo());
                }};
                try {
                    context.SendActivity((Activity)typingActivity);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
                try {
                    context.SendActivity("echo:" + context.getActivity().getText());
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail();
                }
        }).Send("foo")
                .AssertReply((activity) -> {
                    Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
                    return null;
                })
                .AssertReply("echo:foo")
                .Send("bar")
                .AssertReply((activity) -> {
                    Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
                    return null;
                })
                .AssertReply("echo:bar")
                .StartTest();


        PagedResult pagedResult = transcriptStore.GetTranscriptActivitiesAsync("test", conversationId[0]).join();
        Assert.assertEquals(6, pagedResult.getItems().length);
        Assert.assertEquals( "foo", ((Activity)pagedResult.getItems()[0]).getText());
        Assert.assertNotEquals(((Activity)pagedResult.getItems()[1]), null);
        Assert.assertEquals("echo:foo", ((Activity) pagedResult.getItems()[2]).getText());
        Assert.assertEquals("bar", ((Activity)pagedResult.getItems()[3]).getText());

        Assert.assertTrue(pagedResult.getItems()[4] != null);
        Assert.assertEquals("echo:bar", ((Activity)pagedResult.getItems()[5]).getText());
        for (Object activity : pagedResult.getItems())
        {
            Assert.assertFalse(StringUtils.isBlank(((Activity) activity).getId()));
            Assert.assertTrue(((Activity)activity).getTimestamp().isAfter(Long.MIN_VALUE));
        }
        System.out.printf("Complete");
    }

    @Test
    public void Transcript_LogUpdateActivities() throws InterruptedException, ExecutionException {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).Use(new TranscriptLoggerMiddleware(transcriptStore));
        final String[] conversationId = {null};
        final Activity[] activityToUpdate = {null};
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        new TestFlow(adapter, (context) ->
        {

                conversationId[0] = context.getActivity().getConversation().getId();
                if (context.getActivity().getText().equals("update")) {
                    activityToUpdate[0].setText("new response");
                    try {
                        context.UpdateActivity(activityToUpdate[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Activity activity = ((Activity) context.getActivity()).createReply("response");
                    ResourceResponse response = null;
                    try {
                        response = context.SendActivity(activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail();
                    }
                    activity.setId(response.getId());

                    // clone the activity, so we can use it to do an update
                    activityToUpdate[0] = Activity.clone(activity);
                    //JsonConvert.<Activity>DeserializeObject(JsonConvert.SerializeObject(activity));
                }
        }).Send("foo")
                .Send("update")
                .AssertReply("new response")
                .StartTest();
        Thread.sleep(500);
        PagedResult pagedResult = transcriptStore.GetTranscriptActivitiesAsync("test", conversationId[0]).join();
        Assert.assertEquals(4, pagedResult.getItems().length);
        Assert.assertEquals("foo", ((Activity)pagedResult.getItems()[0]).getText());
        Assert.assertEquals( "response", ((Activity)pagedResult.getItems()[1]).getText());
        // TODO: Fix the following 3 asserts so they work correctly. They succeed in the travis builds and fail in the
        // BotBuilder-Java 4.0 master build.
        //Assert.assertEquals( "new response", ((Activity)pagedResult.getItems()[2]).text());
        //Assert.assertEquals("update", ((Activity)pagedResult.getItems()[3]).text());
        //Assert.assertEquals( ((Activity)pagedResult.getItems()[1]).getId(),  ((Activity) pagedResult.getItems()[2]).getId());
    }

    @Test
    public final void Transcript_LogDeleteActivities() throws InterruptedException, ExecutionException {
        MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
        TestAdapter adapter = (new TestAdapter()).Use(new TranscriptLoggerMiddleware(transcriptStore));
        final String[] conversationId = {null};
        final String[] activityId = {null};
        new TestFlow(adapter, (context) ->
        {

                conversationId[0] = context.getActivity().getConversation().getId();
                if (context.getActivity().getText().equals("deleteIt")) {
                    try {
                        context.DeleteActivity(activityId[0]).join();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail();
                    }
                } else {
                    Activity activity = ((Activity) context.getActivity()).createReply("response");
                    ResourceResponse response = null;
                    try {
                        response = context.SendActivity(activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail();
                    }
                    activityId[0] = response.getId();
                }


        }).Send("foo")
                .AssertReply("response")
                .Send("deleteIt")
                .StartTest();
        Thread.sleep(1500);
        PagedResult pagedResult = transcriptStore.GetTranscriptActivitiesAsync("test", conversationId[0]).join();
        for (Object act : pagedResult.getItems()) {
            System.out.printf("Here is the object: %s : Type: %s\n", act.getClass().getTypeName(), ((Activity)act).getType());
        }

        for (Object activity : pagedResult.getItems() ) {
            System.out.printf("Recipient: %s\nText: %s\n", ((Activity) activity).getRecipient().getName(), ((Activity)activity).getText());
        }
        Assert.assertEquals(4, pagedResult.getItems().length);
        Assert.assertEquals("foo", ((Activity)pagedResult.getItems()[0]).getText());
        Assert.assertEquals("response", ((Activity)pagedResult.getItems()[1]).getText());
        Assert.assertEquals("deleteIt", ((Activity)pagedResult.getItems()[2]).getText());
        Assert.assertEquals(ActivityTypes.MESSAGE_DELETE, ((Activity)pagedResult.getItems()[3]).getType());
        Assert.assertEquals(((Activity)pagedResult.getItems()[1]).getId(), ((Activity) pagedResult.getItems()[3]).getId());
    }
}

