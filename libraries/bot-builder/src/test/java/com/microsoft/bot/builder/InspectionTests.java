// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.inspection.InspectionMiddleware;
import com.microsoft.bot.builder.inspection.InspectionSession;
import com.microsoft.bot.builder.inspection.InspectionState;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.Entity;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InspectionTests {
    @Test
    public void ScenarioWithInspectionMiddlewarePassthrough() {
        InspectionState inspectionState = new InspectionState(new MemoryStorage());
        InspectionMiddleware inspectionMiddleware = new InspectionMiddleware(inspectionState);

        TestAdapter adapter = new TestAdapter().use(inspectionMiddleware);

        Activity inboundActivity = MessageFactory.text("hello");

        adapter.processActivity(inboundActivity, turnContext -> {
            turnContext.sendActivity(MessageFactory.text("hi")).join();
            return CompletableFuture.completedFuture(null);
        }).join();

        Activity outboundActivity = adapter.activeQueue().poll();
        Assert.assertEquals("hi", outboundActivity.getText());
    }

    @Test
    public void ScenarioWithInspectionMiddlewareOpenAttach() throws IOException {
        // any bot state should be returned as trace messages per turn
        MemoryStorage storage = new MemoryStorage();
        InspectionState inspectionState = new InspectionState(storage);
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);

        TestInspectionMiddleware inspectionMiddleware = new TestInspectionMiddleware(
            inspectionState,
            userState,
            conversationState,
            null
        );

        // (1) send the /INSPECT open command from the emulator to the middleware
        Activity openActivity = MessageFactory.text("/INSPECT open");

        TestAdapter inspectionAdapter = new TestAdapter(Channels.TEST, true);
        inspectionAdapter.processActivity(openActivity, turnContext -> {
            inspectionMiddleware.processCommand(turnContext).join();
            return CompletableFuture.completedFuture(null);
        }).join();

        Activity inspectionOpenResultActivity = inspectionAdapter.activeQueue().poll();

        // (2) send the resulting /INSPECT attach command from the channel to the
        // middleware
        TestAdapter applicationAdapter = new TestAdapter(Channels.TEST);
        applicationAdapter.use(inspectionMiddleware);

        String attachCommand = inspectionOpenResultActivity.getValue().toString();

        applicationAdapter.processActivity(
            MessageFactory.text(attachCommand),
            turnContext -> {
                // nothing happens - just attach the inspector
                return CompletableFuture.completedFuture(null);
            }
        ).join();

        Activity attachResponse = applicationAdapter.activeQueue().poll();

        // (3) send an application messaage from the channel, it should get the reply
        // and then so should the emulator http endpioint
        applicationAdapter.processActivity(MessageFactory.text("hi"), turnContext -> {
            turnContext.sendActivity(
                MessageFactory.text("echo: " + turnContext.getActivity().getText())
            ).join();

            userState.<Scratch>createProperty("x").get(
                turnContext,
                Scratch::new
            ).join().setProperty("hello");
            conversationState.<Scratch>createProperty("y").get(
                turnContext,
                Scratch::new
            ).join().setProperty("world");

            userState.saveChanges(turnContext).join();
            conversationState.saveChanges(turnContext).join();

            return CompletableFuture.completedFuture(null);
        }).join();

        Activity outboundActivity = applicationAdapter.activeQueue().poll();
        Assert.assertEquals("echo: hi", outboundActivity.getText());
        Assert.assertEquals(3, inspectionMiddleware.recordingSession.requests.size());

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        JsonNode inboundTrace = mapper.readTree(
            inspectionMiddleware.recordingSession.requests.get(0)
        );
        Assert.assertEquals("trace", inboundTrace.get("type").textValue());
        Assert.assertEquals("ReceivedActivity", inboundTrace.get("name").textValue());
        Assert.assertEquals("message", inboundTrace.get("value").get("type").textValue());
        Assert.assertEquals("hi", inboundTrace.get("value").get("text").textValue());

        JsonNode outboundTrace = mapper.readTree(
            inspectionMiddleware.recordingSession.requests.get(1)
        );
        Assert.assertEquals("trace", outboundTrace.get("type").textValue());
        Assert.assertEquals("SentActivity", outboundTrace.get("name").textValue());
        Assert.assertEquals("message", outboundTrace.get("value").get("type").textValue());
        Assert.assertEquals("echo: hi", outboundTrace.get("value").get("text").textValue());

        JsonNode stateTrace = mapper.readTree(
            inspectionMiddleware.recordingSession.requests.get(2)
        );
        Assert.assertEquals("trace", stateTrace.get("type").textValue());
        Assert.assertEquals("BotState", stateTrace.get("name").textValue());
        Assert.assertEquals(
            "hello",
            stateTrace.get("value").get("userState").get("x").get("property").textValue()
        );
        Assert.assertEquals(
            "world",
            stateTrace.get("value").get("conversationState").get("y").get("property").textValue()
        );
    }

    @Test
    public void ScenarioWithInspectionMiddlewareOpenAttachWithMention() throws IOException {
        // any bot state should be returned as trace messages per turn
        MemoryStorage storage = new MemoryStorage();
        InspectionState inspectionState = new InspectionState(storage);
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);

        TestInspectionMiddleware inspectionMiddleware = new TestInspectionMiddleware(
            inspectionState,
            userState,
            conversationState,
            null
        );

        // (1) send the /INSPECT open command from the emulator to the middleware
        Activity openActivity = MessageFactory.text("/INSPECT open");

        TestAdapter inspectionAdapter = new TestAdapter(Channels.TEST, true);
        inspectionAdapter.processActivity(openActivity, turnContext -> {
            inspectionMiddleware.processCommand(turnContext).join();
            return CompletableFuture.completedFuture(null);
        }).join();

        Activity inspectionOpenResultActivity = inspectionAdapter.activeQueue().poll();

        // (2) send the resulting /INSPECT attach command from the channel to the
        // middleware
        TestAdapter applicationAdapter = new TestAdapter(Channels.TEST);
        applicationAdapter.use(inspectionMiddleware);

        // some channels - for example Microsoft Teams - adds an @ mention to the text -
        // this should be taken into account when evaluating the INSPECT
        String recipientId = "bot";
        String attachCommand = "<at>" + recipientId + "</at> "
            + inspectionOpenResultActivity.getValue();
        Activity attachActivity = MessageFactory.text(attachCommand);
        Entity entity = new Entity();
        entity.setType("mention");
        entity.getProperties().put("text", JsonNodeFactory.instance.textNode("<at>" + recipientId + "</at>"));
        entity.getProperties().put("mentioned", JsonNodeFactory.instance.objectNode().put("id", "bot"));
        attachActivity.getEntities().add(entity);

        applicationAdapter.processActivity(
            attachActivity,
            turnContext -> {
                // nothing happens - just attach the inspector
                return CompletableFuture.completedFuture(null);
            }
        ).join();

        Activity attachResponse = applicationAdapter.activeQueue().poll();

        // (3) send an application messaage from the channel, it should get the reply
        // and then so should the emulator http endpioint
        applicationAdapter.processActivity(MessageFactory.text("hi"), turnContext -> {
            turnContext.sendActivity(
                MessageFactory.text("echo: " + turnContext.getActivity().getText())
            ).join();

            userState.<Scratch>createProperty("x").get(
                turnContext,
                Scratch::new
            ).join().setProperty("hello");
            conversationState.<Scratch>createProperty("y").get(
                turnContext,
                Scratch::new
            ).join().setProperty("world");

            userState.saveChanges(turnContext).join();
            conversationState.saveChanges(turnContext).join();

            return CompletableFuture.completedFuture(null);
        }).join();

        Activity outboundActivity = applicationAdapter.activeQueue().poll();
        Assert.assertEquals("echo: hi", outboundActivity.getText());
        Assert.assertEquals(3, inspectionMiddleware.recordingSession.requests.size());

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        JsonNode inboundTrace = mapper.readTree(
            inspectionMiddleware.recordingSession.requests.get(0)
        );
        Assert.assertEquals("trace", inboundTrace.get("type").textValue());
        Assert.assertEquals("ReceivedActivity", inboundTrace.get("name").textValue());
        Assert.assertEquals("message", inboundTrace.get("value").get("type").textValue());
        Assert.assertEquals("hi", inboundTrace.get("value").get("text").textValue());

        JsonNode outboundTrace = mapper.readTree(
            inspectionMiddleware.recordingSession.requests.get(1)
        );
        Assert.assertEquals("trace", outboundTrace.get("type").textValue());
        Assert.assertEquals("SentActivity", outboundTrace.get("name").textValue());
        Assert.assertEquals("message", outboundTrace.get("value").get("type").textValue());
        Assert.assertEquals("echo: hi", outboundTrace.get("value").get("text").textValue());

        JsonNode stateTrace = mapper.readTree(
            inspectionMiddleware.recordingSession.requests.get(2)
        );
        Assert.assertEquals("trace", stateTrace.get("type").textValue());
        Assert.assertEquals("BotState", stateTrace.get("name").textValue());
        Assert.assertEquals(
            "hello",
            stateTrace.get("value").get("userState").get("x").get("property").textValue()
        );
        Assert.assertEquals(
            "world",
            stateTrace.get("value").get("conversationState").get("y").get("property").textValue()
        );
    }

    // We can't currently supply a custom httpclient like dotnet. So instead, these
    // test differ from dotnet by
    // supplying a custom InspectionSession that records what is sent through it.
    private static class TestInspectionMiddleware extends InspectionMiddleware {
        public RecordingInspectionSession recordingSession = null;

        public TestInspectionMiddleware(InspectionState withInspectionState) {
            super(withInspectionState);
        }

        public TestInspectionMiddleware(
            InspectionState withInspectionState,
            UserState withUserState,
            ConversationState withConversationState,
            MicrosoftAppCredentials withCredentials
        ) {
            super(withInspectionState, withUserState, withConversationState, withCredentials);
        }

        @Override
        protected InspectionSession createSession(
            ConversationReference reference,
            MicrosoftAppCredentials credentials
        ) {
            if (recordingSession == null) {
                recordingSession = new RecordingInspectionSession(reference, credentials);
            }
            return recordingSession;
        }
    }

    private static class RecordingInspectionSession extends InspectionSession {
        private List<String> requests = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        public RecordingInspectionSession(
            ConversationReference withConversationReference,
            MicrosoftAppCredentials withCredentials
        ) {
            super(withConversationReference, withCredentials);
            mapper.findAndRegisterModules();
        }

        public RecordingInspectionSession(
            ConversationReference withConversationReference,
            MicrosoftAppCredentials withCredentials,
            Logger withLogger
        ) {
            super(withConversationReference, withCredentials, withLogger);
            mapper.findAndRegisterModules();
        }

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public CompletableFuture<Boolean> send(Activity activity) {
            try {
                requests.add(mapper.writeValueAsString(activity));
            } catch (Throwable t) {
                // noop
            }

            return CompletableFuture.completedFuture(true);
        }
    }

    private static class Scratch {
        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        private String property;
    }
}
