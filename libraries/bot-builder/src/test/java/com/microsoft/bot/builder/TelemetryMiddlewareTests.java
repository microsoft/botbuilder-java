// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import com.microsoft.bot.schema.teams.TenantInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TelemetryMiddlewareTests {
    @Captor
    ArgumentCaptor<String> eventNameCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> propertiesCaptor;

    @Test
    public void Telemetry_NullTelemetryClient() {
        TelemetryLoggerMiddleware logger = new TelemetryLoggerMiddleware(null, true);
        Assert.assertNotNull(logger.getTelemetryClient());
    }

    @Test
    public void Telemetry_LogActivities() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new TelemetryLoggerMiddleware(mockTelemetryClient, true)
        );

        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();
            Activity activity = new Activity(ActivityTypes.TYPING);
            activity.setRelatesTo(turnContext.getActivity().getRelatesTo());
            turnContext.sendActivity(activity).join();
            turnContext.sendActivity("echo:" + turnContext.getActivity().getText()).join();
            return CompletableFuture.completedFuture(null);
        })).send("foo").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:foo").send("bar").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:bar").startTest().join();

        // verify BotTelemetryClient was invoked 6 times, and capture arguments.
        verify(mockTelemetryClient, times(6)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(0));
        Assert.assertEquals(7, properties.get(0).size());
        Assert.assertTrue(properties.get(0).containsKey("fromId"));
        Assert.assertTrue(properties.get(0).containsKey("conversationName"));
        Assert.assertTrue(properties.get(0).containsKey("locale"));
        Assert.assertTrue(properties.get(0).containsKey("recipientId"));
        Assert.assertTrue(properties.get(0).containsKey("recipientName"));
        Assert.assertTrue(properties.get(0).containsKey("fromName"));
        Assert.assertTrue(properties.get(0).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(0).get("text"), "foo"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(1));
        Assert.assertEquals(5, properties.get(1).size());
        Assert.assertTrue(properties.get(1).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(1).containsKey("recipientId"));
        Assert.assertTrue(properties.get(1).containsKey("conversationName"));
        Assert.assertTrue(properties.get(1).containsKey("locale"));
        Assert.assertTrue(properties.get(1).containsKey("recipientName"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(2));
        Assert.assertEquals(6, properties.get(2).size());
        Assert.assertTrue(properties.get(2).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(2).containsKey("recipientId"));
        Assert.assertTrue(properties.get(2).containsKey("conversationName"));
        Assert.assertTrue(properties.get(2).containsKey("locale"));
        Assert.assertTrue(properties.get(2).containsKey("recipientName"));
        Assert.assertTrue(properties.get(2).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(2).get("text"), "echo:foo"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(3));
        Assert.assertEquals(7, properties.get(3).size());
        Assert.assertTrue(properties.get(3).containsKey("fromId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationName"));
        Assert.assertTrue(properties.get(3).containsKey("locale"));
        Assert.assertTrue(properties.get(3).containsKey("recipientId"));
        Assert.assertTrue(properties.get(3).containsKey("recipientName"));
        Assert.assertTrue(properties.get(3).containsKey("fromName"));
        Assert.assertTrue(properties.get(3).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(3).get("text"), "bar"));
    }

    @Test
    public void Telemetry_NoPII() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new TelemetryLoggerMiddleware(mockTelemetryClient, false)
        );

        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();
            Activity activity = new Activity(ActivityTypes.TYPING);
            activity.setRelatesTo(turnContext.getActivity().getRelatesTo());
            turnContext.sendActivity(activity).join();
            turnContext.sendActivity("echo:" + turnContext.getActivity().getText()).join();
            return CompletableFuture.completedFuture(null);
        })).send("foo").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:foo").send("bar").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:bar").startTest().join();

        // verify BotTelemetryClient was invoked 6 times, and capture arguments.
        verify(mockTelemetryClient, times(6)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(0));
        Assert.assertEquals(5, properties.get(0).size());
        Assert.assertTrue(properties.get(0).containsKey("fromId"));
        Assert.assertTrue(properties.get(0).containsKey("conversationName"));
        Assert.assertTrue(properties.get(0).containsKey("locale"));
        Assert.assertTrue(properties.get(0).containsKey("recipientId"));
        Assert.assertTrue(properties.get(0).containsKey("recipientName"));
        Assert.assertFalse(properties.get(0).containsKey("fromName"));
        Assert.assertFalse(properties.get(0).containsKey("text"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(1));
        Assert.assertEquals(4, properties.get(1).size());
        Assert.assertTrue(properties.get(1).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(1).containsKey("recipientId"));
        Assert.assertTrue(properties.get(1).containsKey("conversationName"));
        Assert.assertTrue(properties.get(1).containsKey("locale"));
        Assert.assertFalse(properties.get(1).containsKey("recipientName"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(2));
        Assert.assertEquals(4, properties.get(2).size());
        Assert.assertTrue(properties.get(2).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(2).containsKey("recipientId"));
        Assert.assertTrue(properties.get(2).containsKey("conversationName"));
        Assert.assertTrue(properties.get(2).containsKey("locale"));
        Assert.assertFalse(properties.get(2).containsKey("recipientName"));
        Assert.assertFalse(properties.get(2).containsKey("text"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(3));
        Assert.assertEquals(5, properties.get(3).size());
        Assert.assertTrue(properties.get(3).containsKey("fromId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationName"));
        Assert.assertTrue(properties.get(3).containsKey("locale"));
        Assert.assertTrue(properties.get(3).containsKey("recipientId"));
        Assert.assertTrue(properties.get(3).containsKey("recipientName"));
        Assert.assertFalse(properties.get(3).containsKey("fromName"));
        Assert.assertFalse(properties.get(3).containsKey("text"));
    }

    @Test
    public void Transcript_LogUpdateActivities() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new TelemetryLoggerMiddleware(mockTelemetryClient, true)
        );
        Activity[] activityToUpdate = new Activity[] { null };

        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();

            if (StringUtils.equals(turnContext.getActivity().getText(), "update")) {
                activityToUpdate[0].setText("new response");
                turnContext.updateActivity(activityToUpdate[0]).join();
            } else {
                Activity activity = turnContext.getActivity().createReply("response");
                ResourceResponse response = turnContext.sendActivity(activity).join();
                activity.setId(response.getId());
                activityToUpdate[0] = Activity.clone(activity);
            }

            return CompletableFuture.completedFuture(null);
        })).send("foo").send("update").assertReply("new response").startTest().join();

        // verify BotTelemetryClient was invoked 4 times, and capture arguments.
        verify(mockTelemetryClient, times(4)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGUPDATEEVENT, eventNames.get(3));
        Assert.assertEquals(5, properties.get(3).size());
        Assert.assertTrue(properties.get(3).containsKey("recipientId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationName"));
        Assert.assertTrue(properties.get(3).containsKey("locale"));
        Assert.assertTrue(properties.get(3).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(3).get("text"), "new response"));
    }

    @Test
    public void Transcript_LogDeleteActivities() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new TelemetryLoggerMiddleware(mockTelemetryClient, true)
        );

        String[] activityId = new String[] { null };
        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();

            if (StringUtils.equals(turnContext.getActivity().getText(), "deleteIt")) {
                turnContext.deleteActivity(activityId[0]).join();
            } else {
                Activity activity = turnContext.getActivity().createReply("response");
                ResourceResponse response = turnContext.sendActivity(activity).join();
                activityId[0] = response.getId();
            }

            return CompletableFuture.completedFuture(null);
        })).send("foo").assertReply("response").send("deleteIt").startTest().join();

        // verify BotTelemetryClient was invoked 4 times, and capture arguments.
        verify(mockTelemetryClient, times(4)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGDELETEEVENT, eventNames.get(3));
        Assert.assertEquals(3, properties.get(3).size());
        Assert.assertTrue(properties.get(3).containsKey("recipientId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationName"));
    }

    @Test
    public void Telemetry_OverrideReceive() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new OverrideReceiveLogger(mockTelemetryClient, true)
        );

        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();
            Activity activity = new Activity(ActivityTypes.TYPING);
            activity.setRelatesTo(turnContext.getActivity().getRelatesTo());
            turnContext.sendActivity(activity).join();
            turnContext.sendActivity("echo:" + turnContext.getActivity().getText()).join();
            return CompletableFuture.completedFuture(null);
        })).send("foo").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:foo").send("bar").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:bar").startTest().join();

        // verify BotTelemetryClient was invoked 8 times, and capture arguments.
        verify(mockTelemetryClient, times(8)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(0));
        Assert.assertEquals(2, properties.get(0).size());
        Assert.assertTrue(properties.get(0).containsKey("foo"));
        Assert.assertTrue(StringUtils.equals(properties.get(0).get("foo"), "bar"));
        Assert.assertTrue(properties.get(0).containsKey("ImportantProperty"));
        Assert.assertTrue(
            StringUtils.equals(properties.get(0).get("ImportantProperty"), "ImportantValue")
        );

        Assert.assertEquals("MyReceive", eventNames.get(1));
        Assert.assertEquals(7, properties.get(1).size());
        Assert.assertTrue(properties.get(1).containsKey("fromId"));
        Assert.assertTrue(properties.get(1).containsKey("conversationName"));
        Assert.assertTrue(properties.get(1).containsKey("locale"));
        Assert.assertTrue(properties.get(1).containsKey("recipientId"));
        Assert.assertTrue(properties.get(1).containsKey("recipientName"));
        Assert.assertTrue(properties.get(1).containsKey("fromName"));
        Assert.assertTrue(properties.get(1).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(1).get("text"), "foo"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(2));
        Assert.assertEquals(5, properties.get(2).size());
        Assert.assertTrue(properties.get(2).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(2).containsKey("recipientId"));
        Assert.assertTrue(properties.get(2).containsKey("conversationName"));
        Assert.assertTrue(properties.get(2).containsKey("locale"));
        Assert.assertTrue(properties.get(2).containsKey("recipientName"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(3));
        Assert.assertEquals(6, properties.get(3).size());
        Assert.assertTrue(properties.get(3).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(3).containsKey("recipientId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationName"));
        Assert.assertTrue(properties.get(3).containsKey("locale"));
        Assert.assertTrue(properties.get(3).containsKey("recipientName"));
        Assert.assertTrue(properties.get(3).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(3).get("text"), "echo:foo"));
    }

    @Test
    public void Telemetry_OverrideSend() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new OverrideSendLogger(mockTelemetryClient, true)
        );

        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();
            Activity activity = new Activity(ActivityTypes.TYPING);
            activity.setRelatesTo(turnContext.getActivity().getRelatesTo());
            turnContext.sendActivity(activity).join();
            turnContext.sendActivity("echo:" + turnContext.getActivity().getText()).join();
            return CompletableFuture.completedFuture(null);
        })).send("foo").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:foo").send("bar").assertReply(activity -> {
            Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
        }).assertReply("echo:bar").startTest().join();

        // verify BotTelemetryClient was invoked 10 times, and capture arguments.
        verify(mockTelemetryClient, times(10)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(0));
        Assert.assertEquals(7, properties.get(0).size());
        Assert.assertTrue(properties.get(0).containsKey("fromId"));
        Assert.assertTrue(properties.get(0).containsKey("conversationName"));
        Assert.assertTrue(properties.get(0).containsKey("locale"));
        Assert.assertTrue(properties.get(0).containsKey("recipientId"));
        Assert.assertTrue(properties.get(0).containsKey("recipientName"));
        Assert.assertTrue(properties.get(0).containsKey("fromName"));
        Assert.assertTrue(properties.get(0).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(0).get("text"), "foo"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(1));
        Assert.assertEquals(2, properties.get(1).size());
        Assert.assertTrue(properties.get(1).containsKey("foo"));
        Assert.assertTrue(StringUtils.equals(properties.get(1).get("foo"), "bar"));
        Assert.assertTrue(properties.get(1).containsKey("ImportantProperty"));
        Assert.assertTrue(
            StringUtils.equals(properties.get(1).get("ImportantProperty"), "ImportantValue")
        );

        Assert.assertEquals("MySend", eventNames.get(2));
        Assert.assertEquals(5, properties.get(2).size());
        Assert.assertTrue(properties.get(2).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(2).containsKey("recipientId"));
        Assert.assertTrue(properties.get(2).containsKey("conversationName"));
        Assert.assertTrue(properties.get(2).containsKey("locale"));
        Assert.assertTrue(properties.get(2).containsKey("recipientName"));
    }

    @Test
    public void Telemetry_OverrideUpdateDeleteActivities() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new OverrideUpdateDeleteLogger(mockTelemetryClient, true)
        );

        Activity[] activityToUpdate = new Activity[] { null };
        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();

            if (StringUtils.equals(turnContext.getActivity().getText(), "update")) {
                activityToUpdate[0].setText("new response");
                turnContext.updateActivity(activityToUpdate[0]).join();
                turnContext.deleteActivity(turnContext.getActivity().getId()).join();
            } else {
                Activity activity = turnContext.getActivity().createReply("response");
                ResourceResponse response = turnContext.sendActivity(activity).join();
                activity.setId(response.getId());

                activityToUpdate[0] = Activity.clone(activity);
            }

            return CompletableFuture.completedFuture(null);
        })).send("foo").send("update").assertReply("new response").startTest().join();

        // verify BotTelemetryClient was invoked 5 times, and capture arguments.
        verify(mockTelemetryClient, times(5)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGUPDATEEVENT, eventNames.get(3));
        Assert.assertEquals(2, properties.get(3).size());
        Assert.assertTrue(properties.get(3).containsKey("foo"));
        Assert.assertTrue(StringUtils.equals(properties.get(3).get("foo"), "bar"));
        Assert.assertTrue(properties.get(3).containsKey("ImportantProperty"));
        Assert.assertTrue(
            StringUtils.equals(properties.get(3).get("ImportantProperty"), "ImportantValue")
        );

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGDELETEEVENT, eventNames.get(4));
        Assert.assertEquals(2, properties.get(4).size());
        Assert.assertTrue(properties.get(4).containsKey("foo"));
        Assert.assertTrue(StringUtils.equals(properties.get(4).get("foo"), "bar"));
        Assert.assertTrue(properties.get(4).containsKey("ImportantProperty"));
        Assert.assertTrue(
            StringUtils.equals(properties.get(4).get("ImportantProperty"), "ImportantValue")
        );
    }

    @Test
    public void Telemetry_AdditionalProps() {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter().use(
            new OverrideFillLogger(mockTelemetryClient, true)
        );

        Activity[] activityToUpdate = new Activity[] { null };
        String[] conversationId = new String[] { null };
        new TestFlow(adapter, (turnContext -> {
            conversationId[0] = turnContext.getActivity().getConversation().getId();

            if (StringUtils.equals(turnContext.getActivity().getText(), "update")) {
                activityToUpdate[0].setText("new response");
                turnContext.updateActivity(activityToUpdate[0]).join();
                turnContext.deleteActivity(turnContext.getActivity().getId()).join();
            } else {
                Activity activity = turnContext.getActivity().createReply("response");
                ResourceResponse response = turnContext.sendActivity(activity).join();
                activity.setId(response.getId());

                activityToUpdate[0] = Activity.clone(activity);
            }

            return CompletableFuture.completedFuture(null);
        })).send("foo").send("update").assertReply("new response").startTest().join();

        // verify BotTelemetryClient was invoked 5 times, and capture arguments.
        verify(mockTelemetryClient, times(5)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(0));
        Assert.assertEquals(9, properties.get(0).size());
        Assert.assertTrue(properties.get(0).containsKey("fromId"));
        Assert.assertTrue(properties.get(0).containsKey("conversationName"));
        Assert.assertTrue(properties.get(0).containsKey("locale"));
        Assert.assertTrue(properties.get(0).containsKey("recipientId"));
        Assert.assertTrue(properties.get(0).containsKey("recipientName"));
        Assert.assertTrue(properties.get(0).containsKey("fromName"));
        Assert.assertTrue(properties.get(0).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(0).get("text"), "foo"));

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGSENDEVENT, eventNames.get(1));
        Assert.assertEquals(8, properties.get(1).size());
        Assert.assertTrue(properties.get(1).containsKey("foo"));
        Assert.assertTrue(properties.get(1).containsKey("replyActivityId"));
        Assert.assertTrue(properties.get(1).containsKey("recipientId"));
        Assert.assertTrue(properties.get(1).containsKey("conversationName"));
        Assert.assertTrue(properties.get(1).containsKey("locale"));
        Assert.assertTrue(properties.get(1).containsKey("foo"));
        Assert.assertTrue(properties.get(1).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(1).get("text"), "response"));
        Assert.assertTrue(StringUtils.equals(properties.get(1).get("foo"), "bar"));
        Assert.assertTrue(
            StringUtils.equals(properties.get(1).get("ImportantProperty"), "ImportantValue")
        );

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGUPDATEEVENT, eventNames.get(3));
        Assert.assertEquals(7, properties.get(3).size());
        Assert.assertTrue(properties.get(3).containsKey("conversationId"));
        Assert.assertTrue(properties.get(3).containsKey("conversationName"));
        Assert.assertTrue(properties.get(3).containsKey("locale"));
        Assert.assertTrue(properties.get(3).containsKey("foo"));
        Assert.assertTrue(properties.get(3).containsKey("text"));
        Assert.assertTrue(StringUtils.equals(properties.get(3).get("text"), "new response"));
        Assert.assertTrue(StringUtils.equals(properties.get(3).get("foo"), "bar"));
        Assert.assertTrue(
            StringUtils.equals(properties.get(3).get("ImportantProperty"), "ImportantValue")
        );

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGDELETEEVENT, eventNames.get(4));
        Assert.assertEquals(5, properties.get(4).size());
        Assert.assertTrue(properties.get(4).containsKey("recipientId"));
        Assert.assertTrue(properties.get(4).containsKey("conversationName"));
        Assert.assertTrue(properties.get(4).containsKey("conversationId"));
        Assert.assertTrue(properties.get(4).containsKey("foo"));
        Assert.assertTrue(StringUtils.equals(properties.get(4).get("foo"), "bar"));
        Assert.assertTrue(
            StringUtils.equals(properties.get(4).get("ImportantProperty"), "ImportantValue")
        );
    }

    @Test
    public void Telemetry_LogAttachments() throws JsonProcessingException {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter(Channels.MSTEAMS).use(
            new TelemetryLoggerMiddleware(mockTelemetryClient, true)
        );

        TeamInfo teamInfo = new TeamInfo();
        teamInfo.setId("teamId");
        teamInfo.setName("teamName");

        Activity activity = MessageFactory.text("test");
        ChannelAccount from = new ChannelAccount();
        from.setId("userId");
        from.setName("userName");
        from.setAadObjectId("aadId");
        activity.setFrom(from);
        Attachment attachment = new Attachment();
        attachment.setContent("Hello World");
        attachment.setContentType("test/attachment");
        attachment.setName("testname");
        activity.setAttachment(attachment);

        new TestFlow(adapter).send(activity).startTest().join();

        verify(mockTelemetryClient).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(0));
        String loggedAttachment = properties.get(0).get("attachments");
        String originalAttachment = Serialization.toString(activity.getAttachments());
        Assert.assertTrue(StringUtils.equals(loggedAttachment, originalAttachment));
    }


    @Test
    public void Telemetry_LogTeamsProperties() throws JsonProcessingException {
        BotTelemetryClient mockTelemetryClient = mock(BotTelemetryClient.class);
        TestAdapter adapter = new TestAdapter(Channels.MSTEAMS).use(
            new TelemetryLoggerMiddleware(mockTelemetryClient, true)
        );

        TeamInfo teamInfo = new TeamInfo();
        teamInfo.setId("teamId");
        teamInfo.setName("teamName");

        TeamsChannelData channelData = new TeamsChannelData();
        channelData.setTeam(teamInfo);
        TenantInfo tenant = new TenantInfo();
        tenant.setId("tenantId");
        channelData.setTenant(tenant);

        Activity activity = MessageFactory.text("test");
        activity.setChannelData(channelData);
        ChannelAccount from = new ChannelAccount();
        from.setId("userId");
        from.setName("userName");
        from.setAadObjectId("aadId");
        activity.setFrom(from);

        new TestFlow(adapter).send(activity).startTest().join();

        verify(mockTelemetryClient).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        List<Map<String, String>> properties = propertiesCaptor.getAllValues();

        Assert.assertEquals(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, eventNames.get(0));

        Assert.assertTrue(StringUtils.equals(properties.get(0).get("TeamsUserAadObjectId"), "aadId"));
        Assert.assertTrue(StringUtils.equals(properties.get(0).get("TeamsTenantId"), "tenantId"));
        Assert.assertTrue(StringUtils.equals(properties.get(0).get("TeamsTeamInfo"), Serialization.toString(teamInfo)));
    }

    private static class OverrideReceiveLogger extends TelemetryLoggerMiddleware {
        public OverrideReceiveLogger(
            BotTelemetryClient withTelemetryClient,
            boolean withLogPersonalInformation
        ) {
            super(withTelemetryClient, withLogPersonalInformation);
        }

        @Override
        protected CompletableFuture<Void> onReceiveActivity(Activity activity) {
            Map<String, String> customProperties = new HashMap<String, String>();
            customProperties.put("foo", "bar");
            customProperties.put("ImportantProperty", "ImportantValue");

            getTelemetryClient().trackEvent(
                TelemetryLoggerConstants.BOTMSGRECEIVEEVENT,
                customProperties
            );

            return fillReceiveEventProperties(activity, null).thenApply(eventProperties -> {
                getTelemetryClient().trackEvent("MyReceive", eventProperties);
                return null;
            });
        }
    }

    private static class OverrideSendLogger extends TelemetryLoggerMiddleware {
        public OverrideSendLogger(
            BotTelemetryClient withTelemetryClient,
            boolean withLogPersonalInformation
        ) {
            super(withTelemetryClient, withLogPersonalInformation);
        }

        @Override
        protected CompletableFuture<Void> onSendActivity(Activity activity) {
            Map<String, String> customProperties = new HashMap<String, String>();
            customProperties.put("foo", "bar");
            customProperties.put("ImportantProperty", "ImportantValue");

            getTelemetryClient().trackEvent(
                TelemetryLoggerConstants.BOTMSGSENDEVENT,
                customProperties
            );

            return fillSendEventProperties(activity, null).thenApply(eventProperties -> {
                getTelemetryClient().trackEvent("MySend", eventProperties);
                return null;
            });
        }
    }

    private static class OverrideUpdateDeleteLogger extends TelemetryLoggerMiddleware {
        public OverrideUpdateDeleteLogger(
            BotTelemetryClient withTelemetryClient,
            boolean withLogPersonalInformation
        ) {
            super(withTelemetryClient, withLogPersonalInformation);
        }

        @Override
        protected CompletableFuture<Void> onUpdateActivity(Activity activity) {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("foo", "bar");
            properties.put("ImportantProperty", "ImportantValue");

            getTelemetryClient().trackEvent(TelemetryLoggerConstants.BOTMSGUPDATEEVENT, properties);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onDeleteActivity(Activity activity) {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("foo", "bar");
            properties.put("ImportantProperty", "ImportantValue");

            getTelemetryClient().trackEvent(TelemetryLoggerConstants.BOTMSGDELETEEVENT, properties);
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class OverrideFillLogger extends TelemetryLoggerMiddleware {
        public OverrideFillLogger(
            BotTelemetryClient withTelemetryClient,
            boolean withLogPersonalInformation
        ) {
            super(withTelemetryClient, withLogPersonalInformation);
        }

        @Override
        protected CompletableFuture<Void> onReceiveActivity(Activity activity) {
            Map<String, String> customProperties = new HashMap<String, String>();
            customProperties.put("foo", "bar");
            customProperties.put("ImportantProperty", "ImportantValue");

            return fillReceiveEventProperties(activity, customProperties).thenApply(
                allProperties -> {
                    getTelemetryClient().trackEvent(
                        TelemetryLoggerConstants.BOTMSGRECEIVEEVENT,
                        allProperties
                    );
                    return null;
                }
            );
        }

        @Override
        protected CompletableFuture<Void> onSendActivity(Activity activity) {
            Map<String, String> customProperties = new HashMap<String, String>();
            customProperties.put("foo", "bar");
            customProperties.put("ImportantProperty", "ImportantValue");

            return fillSendEventProperties(activity, customProperties).thenApply(allProperties -> {
                getTelemetryClient().trackEvent(
                    TelemetryLoggerConstants.BOTMSGSENDEVENT,
                    allProperties
                );
                return null;
            });
        }

        @Override
        protected CompletableFuture<Void> onUpdateActivity(Activity activity) {
            Map<String, String> customProperties = new HashMap<String, String>();
            customProperties.put("foo", "bar");
            customProperties.put("ImportantProperty", "ImportantValue");

            return fillUpdateEventProperties(activity, customProperties).thenApply(
                allProperties -> {
                    getTelemetryClient().trackEvent(
                        TelemetryLoggerConstants.BOTMSGUPDATEEVENT,
                        allProperties
                    );
                    return null;
                }
            );
        }

        @Override
        protected CompletableFuture<Void> onDeleteActivity(Activity activity) {
            Map<String, String> customProperties = new HashMap<String, String>();
            customProperties.put("foo", "bar");
            customProperties.put("ImportantProperty", "ImportantValue");

            return fillDeleteEventProperties(activity, customProperties).thenApply(
                allProperties -> {
                    getTelemetryClient().trackEvent(
                        TelemetryLoggerConstants.BOTMSGDELETEEVENT,
                        allProperties
                    );
                    return null;
                }
            );
        }
    }
}
