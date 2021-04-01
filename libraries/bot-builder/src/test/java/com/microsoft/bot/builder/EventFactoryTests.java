// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.HandoffEventNames;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.Transcript;

import org.junit.Assert;
import org.junit.Test;

public class EventFactoryTests {

    @Test
    public void HandoffInitiationNullTurnContext() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> EventFactory.createHandoffInitiation(null, "some text"));
    }

    @Test
    public void HandoffStatusNullConversation() {
        Assert.assertThrows(IllegalArgumentException.class, () -> EventFactory.createHandoffStatus(null, "accepted"));
    }

    @Test
    public void HandoffStatusNullStatus() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> EventFactory.createHandoffStatus(new ConversationAccount(), null));
    }

    @Test
    public void TestCreateHandoffInitiation() {
        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("TestCreateHandoffInitiation", "User1", "Bot"));
        String fromD = "test";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("");
        activity.setConversation(new ConversationAccount());
        activity.setRecipient(new ChannelAccount());
        activity.setFrom(new ChannelAccount(fromD));
        activity.setChannelId("testchannel");
        activity.setServiceUrl("http://myservice");
        TurnContext context = new TurnContextImpl(adapter, activity);
        List<Activity> activities = new ArrayList<Activity>();
        activities.add(MessageFactory.text("hello"));
        Transcript transcript = new Transcript();
        transcript.setActivities(activities);

        Assert.assertNull(transcript.getActivities().get(0).getChannelId());
        Assert.assertNull(transcript.getActivities().get(0).getServiceUrl());
        Assert.assertNull(transcript.getActivities().get(0).getConversation());

        ObjectNode handoffContext = JsonNodeFactory.instance.objectNode();
        handoffContext.set("Skill", JsonNodeFactory.instance.textNode("any"));

        Activity handoffEvent = EventFactory.createHandoffInitiation(context, handoffContext, transcript);
        Assert.assertEquals(handoffEvent.getName(), HandoffEventNames.INITIATEHANDOFF);
        ObjectNode node = (ObjectNode) handoffEvent.getValue();
        String skill = node.get("Skill").asText();
        Assert.assertEquals("any", skill);
        Assert.assertEquals(handoffEvent.getFrom().getId(), fromD);
    }

    @Test
    public void TestCreateHandoffInitiationNoTranscript() {
        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("TestCreateHandoffInitiation", "User1", "Bot"));
        String fromD = "test";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("");
        activity.setConversation(new ConversationAccount());
        activity.setRecipient(new ChannelAccount());
        activity.setFrom(new ChannelAccount(fromD));
        activity.setChannelId("testchannel");
        activity.setServiceUrl("http://myservice");
        TurnContext context = new TurnContextImpl(adapter, activity);
        List<Activity> activities = new ArrayList<Activity>();
        activities.add(MessageFactory.text("hello"));

        ObjectNode handoffContext = JsonNodeFactory.instance.objectNode();
        handoffContext.set("Skill", JsonNodeFactory.instance.textNode("any"));

        Activity handoffEvent = EventFactory.createHandoffInitiation(context, handoffContext);
        Assert.assertEquals(handoffEvent.getName(), HandoffEventNames.INITIATEHANDOFF);
        ObjectNode node = (ObjectNode) handoffEvent.getValue();
        String skill = node.get("Skill").asText();
        Assert.assertEquals("any", skill);
        Assert.assertEquals(handoffEvent.getFrom().getId(), fromD);
    }

    @Test
    public void TestCreateHandoffStatus() throws JsonProcessingException {
        String state = "failed";
        String message = "timed out";
        Activity handoffEvent = EventFactory.createHandoffStatus(new ConversationAccount(), state, message);
        Assert.assertEquals(handoffEvent.getName(), HandoffEventNames.HANDOFFSTATUS);

        ObjectNode node = (ObjectNode) handoffEvent.getValue();

        String stateFormEvent = node.get("state").asText();
        Assert.assertEquals(stateFormEvent, state);

        String messageFormEvent = node.get("message").asText();
        Assert.assertEquals(messageFormEvent, message);

        String status = Serialization.toString(node);
        Assert.assertEquals(status, String.format("{\"state\":\"%s\",\"message\":\"%s\"}", state, message));
        Assert.assertNotNull(handoffEvent.getAttachments());
        Assert.assertNotNull(handoffEvent.getId());
    }

    @Test
    public void TestCreateHandoffStatusNoMessage() {
        String state = "failed";
        Activity handoffEvent = EventFactory.createHandoffStatus(new ConversationAccount(), state);

        ObjectNode node = (ObjectNode) handoffEvent.getValue();

        String stateFormEvent = node.get("state").asText();
        Assert.assertEquals(stateFormEvent, state);

        JsonNode messageFormEvent = node.get("message");
        Assert.assertNull(messageFormEvent);
    }
}
