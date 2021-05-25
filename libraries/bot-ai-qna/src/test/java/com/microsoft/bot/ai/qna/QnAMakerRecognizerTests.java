// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogSet;
import com.microsoft.bot.dialogs.DialogState;
import com.microsoft.bot.schema.Activity;

import okhttp3.HttpUrl;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.fail;

public class QnAMakerRecognizerTests {
    private final String knowledgeBaseId = "dummy-id";
    private final String endpointKey = "dummy-key";
    private final String hostname = "http://localhost";
    private final Boolean mockQnAResponse = true;

    @Test
    public void logPiiIsFalseByDefault() {
        QnAMakerRecognizer recognizer = new QnAMakerRecognizer();
        recognizer.setHostName(hostname);
        recognizer.setEndpointKey(endpointKey);
        recognizer.setKnowledgeBaseId(knowledgeBaseId);

        Boolean logPersonalInfo = recognizer.getLogPersonalInformation();
        // Should be false by default, when not specified by user.
        Assert.assertFalse(logPersonalInfo);
    }

    @Test
    public void noTextNoAnswer() {
        Activity activity = Activity.createMessageActivity();
        TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
        DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
        QnAMakerRecognizer recognizer = new QnAMakerRecognizer();
        recognizer.setHostName(hostname);
        recognizer.setKnowledgeBaseId(knowledgeBaseId);
        recognizer.setEndpointKey(endpointKey);

        RecognizerResult result = recognizer.recognize(dc, activity).join();
        Assert.assertEquals(result.getEntities(), null);
        Assert.assertEquals(result.getProperties().get("answers"), null);
        Assert.assertEquals(result.getIntents().get("QnAMatch"), null);
        Assert.assertNotEquals(result.getIntents().get("None"), null);
    }

    @Test
    public void noAnswer() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsNoAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            // Set mock response in MockWebServer
            String url = "/qnamaker/knowledgebases/";
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer,response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerRecognizer recognizer = new QnAMakerRecognizer();
            recognizer.setHostName(finalEndpoint);
            recognizer.setKnowledgeBaseId(knowledgeBaseId);
            recognizer.setEndpointKey(endpointKey);

            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            RecognizerResult result = recognizer.recognize(dc, activity).join();
            Assert.assertEquals(result.getEntities(), null);
            Assert.assertEquals(result.getProperties().get("answers"), null);
            Assert.assertEquals(result.getIntents().get("QnAMatch"), null);
            Assert.assertNotEquals(result.getIntents().get("None"), null);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                // Empty error
            }
        }
    }

    @Test
    public void returnAnswers() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            // Set mock response in MockWebServer
            String url = "/qnamaker/knowledgebases/";
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer,response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerRecognizer recognizer = new QnAMakerRecognizer();
            recognizer.setHostName(finalEndpoint);
            recognizer.setKnowledgeBaseId(knowledgeBaseId);
            recognizer.setEndpointKey(endpointKey);

            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            RecognizerResult result = recognizer.recognize(dc, activity).join();
            validateAnswers(result);
            Assert.assertEquals(result.getIntents().get("None"), null);
            Assert.assertNotEquals(result.getIntents().get("QnAMatch"), null);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                // Empty error
            }
        }
    }

    @Test
    public void topNAnswers() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_TopNAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            // Set mock response in MockWebServer
            String url = "/qnamaker/knowledgebases/";
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer,response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerRecognizer recognizer = new QnAMakerRecognizer();
            recognizer.setHostName(finalEndpoint);
            recognizer.setKnowledgeBaseId(knowledgeBaseId);
            recognizer.setEndpointKey(endpointKey);

            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            RecognizerResult result = recognizer.recognize(dc, activity).join();
            validateAnswers(result);
            Assert.assertEquals(result.getIntents().get("None"), null);
            Assert.assertNotEquals(result.getIntents().get("QnAMatch"), null);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                // Empty error
            }
        }
    }

    @Test
    public void returnAnswersWithIntents() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswerWithIntent.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            // Set mock response in MockWebServer
            String url = "/qnamaker/knowledgebases/";
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer,response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerRecognizer recognizer = new QnAMakerRecognizer();
            recognizer.setHostName(finalEndpoint);
            recognizer.setKnowledgeBaseId(knowledgeBaseId);
            recognizer.setEndpointKey(endpointKey);

            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            RecognizerResult result = recognizer.recognize(dc, activity).join();
            validateAnswers(result);
            Assert.assertEquals(result.getIntents().get("None"), null);
            Assert.assertNotEquals(result.getIntents().get("DeferToRecognizer_xxx"), null);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                // Empty error
            }
        }
    }

    private String readFileContent (String fileName) throws IOException {
        String path = Paths.get("", "src", "test", "java", "com", "microsoft", "bot", "ai", "qna",
            "testdata", fileName).toAbsolutePath().toString();
        File file = new File(path);
        return FileUtils.readFileToString(file, "utf-8");
    }

    private HttpUrl initializeMockServer(MockWebServer mockWebServer, JsonNode response, String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String mockResponse = mapper.writeValueAsString(response);
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(mockResponse));

        mockWebServer.start();
        return mockWebServer.url(url);
    }

    private void validateAnswers(RecognizerResult result) {
        Assert.assertNotEquals(result.getProperties().get("answers"), null);
        Assert.assertEquals(result.getEntities().get("answer").size(), 1);
        Assert.assertEquals(result.getEntities().get("$instance").get("answer").get(0).get("startIndex").asInt(), 0);
        Assert.assertTrue(result.getEntities().get("$instance").get("answer").get(0).get("endIndex") != null);
    }
}
