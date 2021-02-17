// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogSet;
import com.microsoft.bot.dialogs.DialogState;
import com.microsoft.bot.schema.Activity;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.json.JSONObject;

public class QnAMakerRecognizerTests {
    private final String knowledgeBaseId = "dummy-id";
    private final String endpointKey = "dummy-key";
    private final String hostname = "https://dummy-hostname.azurewebsites.net/qnamaker";
    private final QnAMakerRecognizer recognizer = new QnAMakerRecognizer() {
        {
            setHostName(hostname);
            setKnowledgeBaseId(knowledgeBaseId);
            setEndpointKey(endpointKey);
        }
    };

    @Test
    public void logPiiIsFalseByDefault() {
        QnAMakerRecognizer recognizer = new QnAMakerRecognizer() {
            {
                setHostName(hostname);
                setEndpointKey(endpointKey);
                setKnowledgeBaseId(knowledgeBaseId);
            }
        };
        Boolean logPersonalInfo = recognizer.getLogPersonalInformation();
        // Should be false by default, when not specified by user.
        Assert.assertFalse(logPersonalInfo);
    }

    @Test
    public void noTextNoAnswer() {
        Activity activity = Activity.createMessageActivity();
        TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
        DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
        recognizer.recognize(dc, activity).thenApply(result -> {
            Assert.assertEquals(result.getEntities().get("answer"), null);
            Assert.assertEquals(result.getProperties().get("answers"), null);
            Assert.assertEquals(result.getIntents().get("QnAMatch"), null);
            Assert.assertNotEquals(result.getIntents().get("None"), null);
            return null;
        });
    }

    @Test
    public void noAnswer() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            this.initializeMockServer(mockWebServer, "QnaMaker_ReturnsAnswer.json", "/knowledgebases/");
            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            recognizer.recognize(dc, activity).thenApply(result -> {
                Assert.assertEquals(result.getProperties().get("entities.answer"), null);
                Assert.assertEquals(result.getProperties().get("answers"), null);
                Assert.assertEquals(result.getIntents().get("QnAMatch"), null);
                Assert.assertNotEquals(result.getIntents().get("None"), null);
                return null;
            });
        } finally {
            try {
                mockWebServer.shutdown();
            }
            catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerRecognizerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void returnAnswers() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            this.initializeMockServer(mockWebServer, "QnaMaker_ReturnsAnswer.json", "/knowledgebases/");
            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            recognizer.recognize(dc, activity).thenApply(result -> {
                validateAnswers(result);
                Assert.assertEquals(result.getIntents().get("None"), null);
                Assert.assertNotEquals(result.getIntents().get("QnAMatch"), null);
                return null;
            });
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerRecognizerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void topNAnswers() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            this.initializeMockServer(mockWebServer, "QnaMaker_TopNAnswer.json", "/knowledgebases/");
            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            recognizer.recognize(dc, activity).thenApply(result -> {
                validateAnswers(result);
                Assert.assertEquals(result.getIntents().get("None"), null);
                Assert.assertNotEquals(result.getIntents().get("QnAMatch"), null);
                return null;
            });
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerRecognizerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void returnAnswersWithIntents() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            this.initializeMockServer(mockWebServer, "QnaMaker_ReturnsAnswerWithIntent.json", "/knowledgebases/");
            Activity activity = Activity.createMessageActivity();
            activity.setText("test");
            TurnContext context = new TurnContextImpl(new TestAdapter(), activity);
            DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());
            recognizer.recognize(dc, activity).thenApply(result -> {
                validateAnswers(result);
                Assert.assertEquals(result.getIntents().get("None"), null);
                Assert.assertNotEquals(result.getIntents().get("DeferToRecognizer_xxx"), null);
                return null;
            });
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerRecognizerTests.class).error(e.getMessage());
            }
        }
    }

    private String getFileContent(String fileName) {
        try {
            // Get Oracle file
            return readFileContent("/src/test/java/com/microsoft/bot/ai/qna/testdata/" + fileName);
        } catch (IOException e) {
            LoggerFactory.getLogger(QnAMakerRecognizerTests.class).error(e.getMessage());
            return null;
        }
    }

    private String readFileContent (String pathToFile) throws IOException {
        String path = Paths.get("").toAbsolutePath().toString();
        File file = new File(path + pathToFile);
        return FileUtils.readFileToString(file, "utf-8");
    }

    private JsonNode getResponse(String fileName) {
        String content = this.getFileContent(fileName);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(content);
        } catch (IOException e) {
            LoggerFactory.getLogger(QnAMakerRecognizerTests.class).error(e.getMessage());
            return null;
        }
    }

    private void initializeMockServer(MockWebServer mockWebServer, String fileName, String endpoint) {
        try {
            JsonNode response = getResponse(fileName);
            this.initializeMockServer(mockWebServer, response, endpoint);
        } catch (IOException e) {
            LoggerFactory.getLogger(QnAMakerRecognizerTests.class).error(e.getMessage());
            return;
        }
    }

    private void initializeMockServer(MockWebServer mockWebServer, JsonNode response, String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String mockResponse = mapper.writeValueAsString(response);
        mockWebServer.enqueue(
                new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8").setBody(mockResponse));

        mockWebServer.start();
        mockWebServer.url(url);
    }

    private void validateAnswers(RecognizerResult result) {
        Assert.assertNotEquals(result.getProperties().get("answers"), null);
        Assert.assertEquals(result.getProperties().get("entities.answer").size(), 1);
        Assert.assertEquals(result.getProperties().get("entities.$instance.answer").get("startIndex"), 0);
        Assert.assertTrue(result.getProperties().get("entities.$instance.answer").get("endIndex") != null);
    }
}
