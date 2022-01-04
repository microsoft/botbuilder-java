// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.ai.qna.dialogs.QnAMakerDialog;
import com.microsoft.bot.ai.qna.models.FeedbackRecord;
import com.microsoft.bot.ai.qna.models.FeedbackRecords;
import com.microsoft.bot.ai.qna.models.Metadata;
import com.microsoft.bot.ai.qna.models.QnAMakerTraceInfo;
import com.microsoft.bot.ai.qna.models.QnARequestContext;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.ai.qna.models.QueryResults;
import com.microsoft.bot.ai.qna.utils.QnATelemetryConstants;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MemoryTranscriptStore;
import com.microsoft.bot.builder.PagedResult;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.TraceTranscriptLogger;
import com.microsoft.bot.builder.TranscriptLoggerMiddleware;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;

import com.microsoft.bot.dialogs.ComponentDialog;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogDependencies;
import com.microsoft.bot.dialogs.DialogManager;
import com.microsoft.bot.dialogs.DialogReason;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class QnAMakerTests {
    private final String knowledgeBaseId = "dummy-id";
    private final String endpointKey = "dummy-key";
    private final String hostname = "http://localhost";
    private final Boolean mockQnAResponse = true;

    @Captor
    ArgumentCaptor<String> eventNameCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> propertiesCaptor;

    @Captor
    ArgumentCaptor<Map<String, Double>> metricsCaptor;

    private String getRequestUrl() {
        return String.format("/qnamaker/knowledgebases/%s/generateanswer", knowledgeBaseId);
    }

    private String getV2LegacyRequestUrl() {
        return String.format("/qnamaker/v2.0/knowledgebases/%s/generateanswer", knowledgeBaseId);
    }

    private String getV3LegacyRequestUrl() {
        return String.format("/qnamaker/v3.0/knowledgebases/%s/generateanswer", knowledgeBaseId);
    }

    private String getTrainRequestUrl() {
        return String.format("/qnamaker/v3.0/knowledgebases/%s/train", knowledgeBaseId);
    }

    @Test
    public void qnaMakerTraceActivity() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);

            // Invoke flow which uses mock
            MemoryTranscriptStore transcriptStore = new MemoryTranscriptStore();
            TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("QnaMaker_TraceActivity", "User1", "Bot"))
                .use(new TranscriptLoggerMiddleware(transcriptStore));
            final String[] conversationId = {null};
            new TestFlow(adapter, turnContext -> {
                // Simulate Qna Lookup
                if(turnContext.getActivity().getText().compareTo("how do I clean the stove?") == 0) {
                    QueryResult[] results = qna.getAnswers(turnContext, null).join();
                    Assert.assertNotNull(results);
                    Assert.assertTrue(results.length == 1);
                    Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack", results[0].getAnswer());
                }
                delay(500);
                conversationId[0] = turnContext.getActivity().getConversation().getId();
                Activity typingActivity = new Activity(ActivityTypes.TYPING);
                typingActivity.setRelatesTo(turnContext.getActivity().getRelatesTo());
                turnContext.sendActivity(typingActivity).join();
                delay(500);
                turnContext.sendActivity(String.format("echo:%s", turnContext.getActivity().getText())).join();
                return CompletableFuture.completedFuture(null);
            })
                .send("how do I clean the stove?")
                    .assertReply(activity -> {
                        Assert.assertTrue(activity.isType(ActivityTypes.TYPING));
                    })
                    .assertReply("echo:how do I clean the stove?")
                .send("bar")
                    .assertReply(activity -> Assert.assertTrue(activity.isType(ActivityTypes.TYPING)))
                    .assertReply("echo:bar")
                .startTest().join();

            // Validate Trace Activity created
            PagedResult<Activity> pagedResult = transcriptStore.getTranscriptActivities("test", conversationId[0]).join();
            Assert.assertEquals(7, pagedResult.getItems().size());
            Assert.assertEquals("how do I clean the stove?", pagedResult.getItems().get(0).getText());
            Assert.assertTrue(pagedResult.getItems().get(1).isType(ActivityTypes.TRACE));
            QnAMakerTraceInfo traceInfo = (QnAMakerTraceInfo) pagedResult.getItems().get(1).getValue();
            Assert.assertNotNull(traceInfo);
            Assert.assertEquals("echo:how do I clean the stove?", pagedResult.getItems().get(3).getText());
            Assert.assertEquals("bar", pagedResult.getItems().get(4).getText());
            Assert.assertEquals("echo:bar", pagedResult.getItems().get(6).getText());
            for (Activity activity : pagedResult.getItems()) {
                Assert.assertFalse(StringUtils.isBlank(activity.getId()));
            }
        } catch(Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTraceActivityEmptyText() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            // Get basic Qna
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);

            // No text
            TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("QnaMaker_TraceActivity_EmptyText", "User1", "Bot"));
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setText(new String());
            activity.setConversation(new ConversationAccount());
            activity.setRecipient(new ChannelAccount());
            activity.setFrom(new ChannelAccount());

            TurnContext context = new TurnContextImpl(adapter, activity);
            Assert.assertThrows(CompletionException.class, () -> qna.getAnswers(context, null).join());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTraceActivityNullText() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            // Get basic Qna
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);

            // No text
            TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("QnaMaker_TraceActivity_NullText", "User1", "Bot"));
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setText(null);
            activity.setConversation(new ConversationAccount());
            activity.setRecipient(new ChannelAccount());
            activity.setFrom(new ChannelAccount());

            TurnContext context = new TurnContextImpl(adapter, activity);
            Assert.assertThrows(CompletionException.class, () -> qna.getAnswers(context, null).join());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTraceActivityNullContext() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            // Get basic Qna
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);

            Assert.assertThrows(CompletionException.class, () -> qna.getAnswers(null, null).join());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTraceActivityBadMessage() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            // Get basic Qna
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);

            // No text
            TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("QnaMaker_TraceActivity_BadMessage", "User1", "Bot"));
            Activity activity = new Activity(ActivityTypes.TRACE);
            activity.setText("My Text");
            activity.setConversation(new ConversationAccount());
            activity.setRecipient(new ChannelAccount());
            activity.setFrom(new ChannelAccount());

            TurnContext context = new TurnContextImpl(adapter, activity);
            Assert.assertThrows(CompletionException.class, () -> qna.getAnswers(context, null).join());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTraceActivityNullActivity() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            // Get basic Qna
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);

            // No text
            TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("QnaMaker_TraceActivity_NullActivity", "User1", "Bot"));
            TurnContext context = new MyTurnContext(adapter, null);
            Assert.assertThrows(CompletionException.class, () -> qna.getAnswers(context, null).join());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerReturnsAnswer() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);
            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack", results[0].getAnswer());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerReturnsAnswerRaw() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);
            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            QueryResults results = qna.getAnswersRaw(getContext("how do I clean the stove?"), options, null, null).join();
            Assert.assertNotNull(results.getAnswers());
            Assert.assertTrue(results.getActiveLearningEnabled());
            Assert.assertTrue(results.getAnswers().length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                results.getAnswers()[0].getAnswer());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerLowScoreVariation() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_TopNAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnaMakerEndpoint = new QnAMakerEndpoint();
            qnaMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnaMakerEndpoint.setEndpointKey(endpointKey);
            qnaMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions qnaMakerOptions = new QnAMakerOptions();
            qnaMakerOptions.setTop(5);
            QnAMaker qna = new QnAMaker(qnaMakerEndpoint, qnaMakerOptions);
            QueryResult[] results = qna.getAnswers(getContext("Q11"), null).join();
            Assert.assertNotNull(results);
            Assert.assertEquals(4, results.length);

            QueryResult[] filteredResults = qna.getLowScoreVariation(results);
            Assert.assertNotNull(filteredResults);
            Assert.assertEquals(3, filteredResults.length);

            String content2 = readFileContent("QnaMaker_TopNAnswer_DisableActiveLearning.json");
            JsonNode response2 = mapper.readTree(content2);
            this.initializeMockServer(mockWebServer, response2, this.getRequestUrl());
            QueryResult[] results2 = qna.getAnswers(getContext("Q11"), null).join();
            Assert.assertNotNull(results2);
            Assert.assertEquals(4, results2.length);

            QueryResult[] filteredResults2 = qna.getLowScoreVariation(results2);
            Assert.assertNotNull(filteredResults2);
            Assert.assertEquals(3, filteredResults2.length);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerCallTrain() {
        MockWebServer mockWebServer = new MockWebServer();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        String url = this.getTrainRequestUrl();
        String endpoint = "";
        try {
            JsonNode response = objectMapper.readTree("{}");
            endpoint = String.format(
                "%s:%s",
                hostname,
                initializeMockServer(
                    mockWebServer,
                    response,
                    url).port());
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnaMakerEndpoint = new QnAMakerEndpoint();
            qnaMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnaMakerEndpoint.setEndpointKey(endpointKey);
            qnaMakerEndpoint.setHost(finalEndpoint);

            QnAMaker qna = new QnAMaker(qnaMakerEndpoint, null);
            FeedbackRecords feedbackRecords = new FeedbackRecords();

            FeedbackRecord feedback1 = new FeedbackRecord();
            feedback1.setQnaId(1);
            feedback1.setUserId("test");
            feedback1.setUserQuestion("How are you?");

            FeedbackRecord feedback2 = new FeedbackRecord();
            feedback2.setQnaId(2);
            feedback2.setUserId("test");
            feedback2.setUserQuestion("What up??");

            feedbackRecords.setRecords(new FeedbackRecord[] { feedback1, feedback2 });
            qna.callTrain(feedbackRecords);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerReturnsAnswerConfiguration() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);
            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                results[0].getAnswer());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerReturnsAnswerWithFiltering() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_UsesStrictFilters_ToReturnAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnaMakerEndpoint = new QnAMakerEndpoint();
            qnaMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnaMakerEndpoint.setEndpointKey(endpointKey);
            qnaMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions qnaMakerOptions = new QnAMakerOptions();
            Metadata metadata = new Metadata();
            metadata.setName("topic");
            metadata.setValue("value");
            Metadata[] filters = new Metadata[] { metadata };
            qnaMakerOptions.setStrictFilters(filters);
            qnaMakerOptions.setTop(1);
            QnAMaker qna = new QnAMaker(qnaMakerEndpoint, qnaMakerOptions);
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), qnaMakerOptions).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                results[0].getAnswer());
            Assert.assertEquals("topic", results[0].getMetadata()[0].getName());
            Assert.assertEquals("value", results[0].getMetadata()[0].getValue());

            JsonNode obj = null;
            try {
                RecordedRequest request = mockWebServer.takeRequest();
                obj = objectMapper.readTree(request.getBody().readUtf8());
            } catch (IOException | InterruptedException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
            // verify we are actually passing on the options
            Assert.assertEquals(1, obj.get("top").asInt());
            Assert.assertEquals("topic", obj.get("strictFilters").get(0).get("name").asText());
            Assert.assertEquals("value", obj.get("strictFilters").get(0).get("value").asText());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerSetScoreThresholdWhenThresholdIsZero() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnaMakerEndpoint = new QnAMakerEndpoint();
            qnaMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnaMakerEndpoint.setEndpointKey(endpointKey);
            qnaMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions qnaMakerOptions = new QnAMakerOptions();
            qnaMakerOptions.setScoreThreshold(0.0f);

            QnAMaker qnaWithZeroValueThreshold = new QnAMaker(qnaMakerEndpoint, qnaMakerOptions);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            QueryResult[] results = qnaWithZeroValueThreshold.getAnswers(getContext("how do I clean the stove?"), options).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTestThreshold() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_TestThreshold.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions qnaMakerOptions = new QnAMakerOptions();
            qnaMakerOptions.setTop(1);
            qnaMakerOptions.setScoreThreshold(0.99F);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, qnaMakerOptions);

            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 0);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTestScoreThresholdTooLargeOutOfRange() {
        QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
        qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
        qnAMakerEndpoint.setEndpointKey(endpointKey);
        qnAMakerEndpoint.setHost(hostname);

        QnAMakerOptions tooLargeThreshold = new QnAMakerOptions();
        tooLargeThreshold.setTop(1);
        tooLargeThreshold.setScoreThreshold(1.1f);

        Assert.assertThrows(IllegalArgumentException.class, () -> new QnAMaker(qnAMakerEndpoint, tooLargeThreshold));
    }

    @Test
    public void qnaMakerTestScoreThresholdTooSmallOutOfRange() {
        QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
        qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
        qnAMakerEndpoint.setEndpointKey(endpointKey);
        qnAMakerEndpoint.setHost(hostname);

        QnAMakerOptions tooSmallThreshold = new QnAMakerOptions();
        tooSmallThreshold.setTop(1);
        tooSmallThreshold.setScoreThreshold(-9000.0f);

        Assert.assertThrows(IllegalArgumentException.class, () -> new QnAMaker(qnAMakerEndpoint, tooSmallThreshold));
    }

    @Test
    public void qnaMakerReturnsAnswerWithContext() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswerWithContext.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnARequestContext context = new QnARequestContext();
            context.setPreviousQnAId(5);
            context.setPreviousUserQuery("how do I clean the stove?");

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);
            options.setContext(context);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options);

            QueryResult[] results = qna.getAnswers(getContext("Where can I buy?"), options).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals(55, (int)results[0].getId());
            Assert.assertEquals(1, (double)results[0].getScore(), 0);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerReturnAnswersWithoutContext() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswerWithoutContext.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(3);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options);

            QueryResult[] results = qna.getAnswers(getContext("Where can I buy?"), options).join();
            Assert.assertNotNull(results);
            Assert.assertEquals(2, results.length);
            Assert.assertNotEquals(1, results[0].getScore().intValue());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerReturnsHighScoreWhenIdPassed() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswerWithContext.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);
            options.setQnAId(55);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options);
            QueryResult[] results = qna.getAnswers(getContext("Where can I buy?"), options).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals(55, (int)results[0].getId());
            Assert.assertEquals(1, (double)results[0].getScore(), 0);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTestTopOutOfRange() {
        QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
        qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
        qnAMakerEndpoint.setEndpointKey(endpointKey);
        qnAMakerEndpoint.setHost(hostname);

        QnAMakerOptions options = new QnAMakerOptions();
        options.setTop(-1);
        options.setScoreThreshold(0.5f);

        Assert.assertThrows(IllegalArgumentException.class, () -> new QnAMaker(qnAMakerEndpoint, options));
    }

    @Test
    public void qnaMakerTestEndpointEmptyKbId() {
        QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
        qnAMakerEndpoint.setKnowledgeBaseId(new String());
        qnAMakerEndpoint.setEndpointKey(endpointKey);
        qnAMakerEndpoint.setHost(hostname);

        Assert.assertThrows(IllegalArgumentException.class, () -> new QnAMaker(qnAMakerEndpoint, null));
    }

    @Test
    public void qnaMakerTestEndpointEmptyEndpointKey() {
        QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
        qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
        qnAMakerEndpoint.setEndpointKey(new String());
        qnAMakerEndpoint.setHost(hostname);

        Assert.assertThrows(IllegalArgumentException.class, () -> new QnAMaker(qnAMakerEndpoint, null));
    }

    @Test
    public void qnaMakerTestEndpointEmptyHost() {
        QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
        qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
        qnAMakerEndpoint.setEndpointKey(endpointKey);
        qnAMakerEndpoint.setHost(new String());

        Assert.assertThrows(IllegalArgumentException.class, () -> new QnAMaker(qnAMakerEndpoint, null));
    }

    @Test
    public void qnaMakerUserAgent() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            QnAMaker qna = this.qnaReturnsAnswer(mockWebServer);

            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null).join();
            RecordedRequest request = mockWebServer.takeRequest();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                results[0].getAnswer());

            // Verify that we added the bot.builder package details.
            Assert.assertTrue(request.getHeader("User-Agent").contains("BotBuilder/4."));
        } catch (Exception ex) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerV2LegacyEndpointShouldThrow() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_LegacyEndpointAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getV2LegacyRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String host = String.format("{%s}/v2.0", endpoint);
            QnAMakerEndpoint v2LegacyEndpoint = new QnAMakerEndpoint();
            v2LegacyEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            v2LegacyEndpoint.setEndpointKey(endpointKey);
            v2LegacyEndpoint.setHost(host);

            Assert.assertThrows(UnsupportedOperationException.class,
                () -> new QnAMaker(v2LegacyEndpoint,null));
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerV3LeagacyEndpointShouldThrow() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_LegacyEndpointAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getV3LegacyRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String host = String.format("{%s}/v3.0", endpoint);
            QnAMakerEndpoint v3LegacyEndpoint = new QnAMakerEndpoint();
            v3LegacyEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            v3LegacyEndpoint.setEndpointKey(endpointKey);
            v3LegacyEndpoint.setHost(host);

            Assert.assertThrows(UnsupportedOperationException.class,
                () -> new QnAMaker(v3LegacyEndpoint,null));
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerReturnsAnswerWithMetadataBoost() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswersWithMetadataBoost.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options);

            QueryResult[] results = qna.getAnswers(getContext("who loves me?"), options).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("Kiki", results[0].getAnswer());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTestThresholdInQueryOption() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer_GivenScoreThresholdQueryOption.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions queryOptionsWithScoreThreshold = new QnAMakerOptions();
            queryOptionsWithScoreThreshold.setScoreThreshold(0.5f);
            queryOptionsWithScoreThreshold.setTop(2);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, queryOptionsWithScoreThreshold);

            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

            QueryResult[] results = qna.getAnswers(getContext("What happens when you hug a porcupine?"), queryOptionsWithScoreThreshold).join();
            RecordedRequest request = mockWebServer.takeRequest();
            JsonNode obj = objectMapper.readTree(request.getBody().readUtf8());

            Assert.assertNotNull(results);

            Assert.assertEquals(2, obj.get("top").asInt());
            Assert.assertEquals(0.5, obj.get("scoreThreshold").asDouble(), 0);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTestUnsuccessfulResponse() {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(502));
        try {
            String url = this.getRequestUrl();
            String finalEndpoint = String.format("%s:%s", hostname, mockWebServer.url(url).port());
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, null);
            Assert.assertThrows(CompletionException.class, () -> qna.getAnswers(getContext("how do I clean the stove?"), null).join());
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerIsTestTrue() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_IsTest_True.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions qnaMakerOptions = new QnAMakerOptions();
            qnaMakerOptions.setTop(1);
            qnaMakerOptions.setIsTest(true);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, qnaMakerOptions);

            QueryResult[] results = qna.getAnswers(getContext("Q11"), qnaMakerOptions).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 0);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerRankerTypeQuestionOnly() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_RankerType_QuestionOnly.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions qnaMakerOptions = new QnAMakerOptions();
            qnaMakerOptions.setTop(1);
            qnaMakerOptions.setRankerType("QuestionOnly");

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, qnaMakerOptions);

            QueryResult[] results = qna.getAnswers(getContext("Q11"), qnaMakerOptions).join();
            Assert.assertNotNull(results);
            Assert.assertEquals(2, results.length);
        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerTestOptionsHydration() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String url = this.getRequestUrl();
            String endpoint = "";
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;

            QnAMakerOptions noFiltersOptions = new QnAMakerOptions();
            noFiltersOptions.setTop(30);

            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            Metadata strictFilterMovie = new Metadata();
            strictFilterMovie.setName("movie");
            strictFilterMovie.setValue("disney");

            Metadata strictFilterHome = new Metadata();
            strictFilterHome.setName("home");
            strictFilterHome.setValue("floating");

            Metadata strictFilterDog = new Metadata();
            strictFilterDog.setName("dog");
            strictFilterDog.setValue("samoyed");

            Metadata[] oneStrictFilters = new Metadata[] {strictFilterMovie};
            Metadata[] twoStrictFilters = new Metadata[] {strictFilterMovie, strictFilterHome};
            Metadata[] allChangedRequestOptionsFilters = new Metadata[] {strictFilterDog};
            QnAMakerOptions oneFilteredOption = new QnAMakerOptions();
            oneFilteredOption.setTop(30);
            oneFilteredOption.setStrictFilters(oneStrictFilters);

            QnAMakerOptions twoStrictFiltersOptions = new QnAMakerOptions();
            twoStrictFiltersOptions.setTop(30);
            twoStrictFiltersOptions.setStrictFilters(twoStrictFilters);

            QnAMakerOptions allChangedRequestOptions = new QnAMakerOptions();
            allChangedRequestOptions.setTop(2000);
            allChangedRequestOptions.setScoreThreshold(0.42f);
            allChangedRequestOptions.setStrictFilters(allChangedRequestOptionsFilters);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, noFiltersOptions);

            TurnContext context = getContext("up");

            // Ensure that options from previous requests do not bleed over to the next,
            // And that the options set in the constructor are not overwritten improperly by options passed into .GetAnswersAsync()
            CapturedRequest[] requestContent = new CapturedRequest[6];
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            RecordedRequest request;

            qna.getAnswers(context, noFiltersOptions).join();
            request = mockWebServer.takeRequest();
            requestContent[0] = objectMapper.readValue(request.getBody().readUtf8(), CapturedRequest.class);

            this.enqueueResponse(mockWebServer, response);

            qna.getAnswers(context, twoStrictFiltersOptions).join();
            request = mockWebServer.takeRequest();
            requestContent[1] = objectMapper.readValue(request.getBody().readUtf8(), CapturedRequest.class);

            this.enqueueResponse(mockWebServer, response);

            qna.getAnswers(context, oneFilteredOption).join();
            request = mockWebServer.takeRequest();
            requestContent[2] = objectMapper.readValue(request.getBody().readUtf8(), CapturedRequest.class);

            this.enqueueResponse(mockWebServer, response);

            qna.getAnswers(context, null).join();
            request = mockWebServer.takeRequest();
            requestContent[3] = objectMapper.readValue(request.getBody().readUtf8(), CapturedRequest.class);

            this.enqueueResponse(mockWebServer, response);

            qna.getAnswers(context, allChangedRequestOptions).join();
            request = mockWebServer.takeRequest();
            requestContent[4] = objectMapper.readValue(request.getBody().readUtf8(), CapturedRequest.class);

            this.enqueueResponse(mockWebServer, response);

            qna.getAnswers(context, null).join();
            request = mockWebServer.takeRequest();
            requestContent[5] = objectMapper.readValue(request.getBody().readUtf8(), CapturedRequest.class);


            Assert.assertTrue(requestContent[0].getStrictFilters().length == 0);
            Assert.assertEquals(2, requestContent[1].getStrictFilters().length);
            Assert.assertTrue(requestContent[2].getStrictFilters().length == 1);
            Assert.assertTrue(requestContent[3].getStrictFilters().length == 0);

            Assert.assertEquals(2000, requestContent[4].getTop().intValue());
            Assert.assertEquals(0.42, Math.round(requestContent[4].getScoreThreshold().doubleValue()), 1);
            Assert.assertTrue(requestContent[4].getStrictFilters().length == 1);

            Assert.assertEquals(30, requestContent[5].getTop().intValue());
            Assert.assertEquals(0.3, Math.round(requestContent[5].getScoreThreshold().doubleValue()),1);
            Assert.assertTrue(requestContent[5].getStrictFilters().length == 0);

        } catch (Exception e) {
            fail();
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMakerTests.class).error(e.getMessage());
            }
        }
    }

    @Test
    public void qnaMakerStrictFiltersCompoundOperationType() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            Metadata strictFilterMovie = new Metadata();
            strictFilterMovie.setName("movie");
            strictFilterMovie.setValue("disney");

            Metadata strictFilterProduction = new Metadata();
            strictFilterProduction.setName("production");
            strictFilterProduction.setValue("Walden");

            Metadata[] strictFilters = new Metadata[] {strictFilterMovie, strictFilterProduction};
            QnAMakerOptions oneFilteredOption = new QnAMakerOptions();
            oneFilteredOption.setTop(30);
            oneFilteredOption.setStrictFilters(strictFilters);
            oneFilteredOption.setStrictFiltersJoinOperator(JoinOperator.OR);

            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, oneFilteredOption);

            TurnContext context = getContext("up");
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

            QueryResult[] noFilterResults1 = qna.getAnswers(context, oneFilteredOption).join();
            RecordedRequest request = mockWebServer.takeRequest();
            JsonNode requestContent = objectMapper.readTree(request.getBody().readUtf8());
            Assert.assertEquals(2, oneFilteredOption.getStrictFilters().length);
            Assert.assertEquals(JoinOperator.OR, oneFilteredOption.getStrictFiltersJoinOperator());
        }catch (Exception e) {
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
    public void telemetryNullTelemetryClient() {
        // Arrange
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;

            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            // Act (Null Telemetry client)
            // This will default to the NullTelemetryClient which no-ops all calls.
            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options, null, true);
            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null).join();
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack", results[0].getAnswer());
            Assert.assertEquals("Editorial", results[0].getSource());
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
    public void telemetryReturnsAnswer() {
        // Arrange
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            BotTelemetryClient telemetryClient = Mockito.mock(BotTelemetryClient.class);

            // Act - See if we get data back in telemetry
            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options, telemetryClient, true);
            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null).join();
            // Assert - Check Telemetry logged
            // verify BotTelemetryClient was invoked 1 times, and capture arguments.
            verify(telemetryClient, times(1)).trackEvent(
                eventNameCaptor.capture(),
                propertiesCaptor.capture(),
                metricsCaptor.capture()
            );
            List<String> eventNames = eventNameCaptor.getAllValues();
            List<Map<String, String>> properties = propertiesCaptor.getAllValues();
            List<Map<String, Double>> metrics = metricsCaptor.getAllValues();            ;
            Assert.assertEquals(eventNames.get(0), QnATelemetryConstants.QNA_MSG_EVENT);
            Assert.assertTrue(properties.get(0).containsKey("knowledgeBaseId"));
            Assert.assertTrue(properties.get(0).containsKey("matchedQuestion"));
            Assert.assertTrue(properties.get(0).containsKey("question"));
            Assert.assertTrue(properties.get(0).containsKey("questionId"));
            Assert.assertTrue(properties.get(0).containsKey("answer"));
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack", properties.get(0).get("answer"));
            Assert.assertTrue(properties.get(0).containsKey("articleFound"));
            Assert.assertTrue(metrics.get(0).size() == 1);

            // Assert - Validate we didn't break QnA functionality.
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack", results[0].getAnswer());
            Assert.assertEquals("Editorial", results[0].getSource());
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
    public void telemetryReturnsAnswerWhenNoAnswerFoundInKB() {
        // Arrange
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer_WhenNoAnswerFoundInKb.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            BotTelemetryClient telemetryClient = Mockito.mock(BotTelemetryClient.class);

            // Act - See if we get data back in telemetry
            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options, telemetryClient, true);
            QueryResult[] results = qna.getAnswers(getContext("what is the answer to my nonsense question?"), null).join();
            // Assert - Check Telemetry logged
            // verify BotTelemetryClient was invoked 1 times, and capture arguments.
            verify(telemetryClient, times(1)).trackEvent(
                eventNameCaptor.capture(),
                propertiesCaptor.capture(),
                metricsCaptor.capture()
            );
            List<String> eventNames = eventNameCaptor.getAllValues();
            List<Map<String, String>> properties = propertiesCaptor.getAllValues();
            List<Map<String, Double>> metrics = metricsCaptor.getAllValues();

            Assert.assertEquals(eventNames.get(0), QnATelemetryConstants.QNA_MSG_EVENT);
            Assert.assertTrue(properties.get(0).containsKey("knowledgeBaseId"));
            Assert.assertTrue(properties.get(0).containsKey("matchedQuestion"));
            Assert.assertEquals("No Qna Question matched", properties.get(0).get("matchedQuestion"));
            Assert.assertTrue(properties.get(0).containsKey("question"));
            Assert.assertTrue(properties.get(0).containsKey("questionId"));
            Assert.assertTrue(properties.get(0).containsKey("answer"));
            Assert.assertEquals("No Qna Answer matched", properties.get(0).get("answer"));
            Assert.assertTrue(properties.get(0).containsKey("articleFound"));
            Assert.assertTrue(metrics.get(0).isEmpty());

            // Assert - Validate we didn't break QnA functionality.
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 0);
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
    public void telemetryPii() {
        // Arrange
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            BotTelemetryClient telemetryClient = Mockito.mock(BotTelemetryClient.class);

            // Act
            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options, telemetryClient, false);
            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null).join();
            // verify BotTelemetryClient was invoked 1 times, and capture arguments.
            verify(telemetryClient, times(1)).trackEvent(
                eventNameCaptor.capture(),
                propertiesCaptor.capture(),
                metricsCaptor.capture()
            );
            List<String> eventNames = eventNameCaptor.getAllValues();
            List<Map<String, String>> properties = propertiesCaptor.getAllValues();
            List<Map<String, Double>> metrics = metricsCaptor.getAllValues();

            Assert.assertEquals(eventNames.get(0), QnATelemetryConstants.QNA_MSG_EVENT);
            Assert.assertTrue(properties.get(0).containsKey("knowledgeBaseId"));
            Assert.assertTrue(properties.get(0).containsKey("matchedQuestion"));
            Assert.assertFalse(properties.get(0).containsKey("question"));
            Assert.assertTrue(properties.get(0).containsKey("questionId"));
            Assert.assertTrue(properties.get(0).containsKey("answer"));
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                properties.get(0).get("answer"));
            Assert.assertTrue(properties.get(0).containsKey("articleFound"));
            Assert.assertTrue(metrics.get(0).size() == 1);
            Assert.assertTrue(metrics.get(0).containsKey("score"));

            // Assert - Validate we didn't break QnA functionality.
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                results[0].getAnswer());
            Assert.assertEquals("Editorial", results[0].getSource());
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
    public void telemetryOverride() {
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            BotTelemetryClient telemetryClient = Mockito.mock(BotTelemetryClient.class);

            // Act - Override the QnaMaker object to log custom stuff and honor parms passed in.
            Map<String, String> telemetryProperties = new HashMap<String, String>();
            telemetryProperties.put("Id", "MyID");

            QnAMaker qna = new OverrideTelemetry(qnAMakerEndpoint, options, telemetryClient, false);
            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null, telemetryProperties, null).join();

            // verify BotTelemetryClient was invoked 2 times, and capture arguments.
            verify(telemetryClient, times(2)).trackEvent(
                eventNameCaptor.capture(),
                propertiesCaptor.capture()
            );
            List<String> eventNames = eventNameCaptor.getAllValues();
            List<Map<String, String>> properties = propertiesCaptor.getAllValues();

            Assert.assertEquals(2, eventNames.size());
            Assert.assertEquals(eventNames.get(0), QnATelemetryConstants.QNA_MSG_EVENT);
            Assert.assertTrue(properties.get(0).size() == 2);
            Assert.assertTrue(properties.get(0).containsKey("MyImportantProperty"));
            Assert.assertEquals("myImportantValue", properties.get(0).get("MyImportantProperty"));
            Assert.assertTrue(properties.get(0).containsKey("Id"));
            Assert.assertEquals("MyID", properties.get(0).get("Id"));

            Assert.assertEquals("MySecondEvent", eventNames.get(1));
            Assert.assertTrue(properties.get(1).containsKey("MyImportantProperty2"));
            Assert.assertEquals("myImportantValue2", properties.get(1).get("MyImportantProperty2"));

            // Validate we didn't break QnA functionality.
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                results[0].getAnswer());
            Assert.assertEquals("Editorial", results[0].getSource());
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
    public void telemetryAdditionalPropsMetrics() {
        //Arrange
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            BotTelemetryClient telemetryClient = Mockito.mock(BotTelemetryClient.class);

            // Act - Pass in properties during QnA invocation
            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options, telemetryClient, false);
            Map<String, String> telemetryProperties = new HashMap<String, String>();
            telemetryProperties.put("MyImportantProperty", "myImportantValue");

            Map<String, Double> telemetryMetrics = new HashMap<String, Double>();
            telemetryMetrics.put("MyImportantMetric", 3.14159);

            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null, telemetryProperties, telemetryMetrics).join();
            // Assert - added properties were added.
            // verify BotTelemetryClient was invoked 1 times, and capture arguments.
            verify(telemetryClient, times(1)).trackEvent(
                eventNameCaptor.capture(),
                propertiesCaptor.capture(),
                metricsCaptor.capture()
            );
            List<String> eventNames = eventNameCaptor.getAllValues();
            List<Map<String, String>> properties = propertiesCaptor.getAllValues();
            List<Map<String, Double>> metrics = metricsCaptor.getAllValues();

            Assert.assertEquals(eventNames.get(0), QnATelemetryConstants.QNA_MSG_EVENT);
            Assert.assertTrue(properties.get(0).containsKey(QnATelemetryConstants.KNOWLEDGE_BASE_ID_PROPERTY));
            Assert.assertFalse(properties.get(0).containsKey(QnATelemetryConstants.QUESTION_PROPERTY));
            Assert.assertTrue(properties.get(0).containsKey(QnATelemetryConstants.MATCHED_QUESTION_PROPERTY));
            Assert.assertTrue(properties.get(0).containsKey(QnATelemetryConstants.QUESTION_ID_PROPERTY));
            Assert.assertTrue(properties.get(0).containsKey(QnATelemetryConstants.ANSWER_PROPERTY));
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                properties.get(0).get("answer"));
            Assert.assertTrue(properties.get(0).containsKey("MyImportantProperty"));
            Assert.assertEquals("myImportantValue", properties.get(0).get("MyImportantProperty"));

            Assert.assertEquals(2, metrics.get(0).size());
            Assert.assertTrue(metrics.get(0).containsKey("score"));
            Assert.assertTrue(metrics.get(0).containsKey("MyImportantMetric"));
            Assert.assertTrue(Double.compare((double)metrics.get(0).get("MyImportantMetric"), 3.14159) == 0);

            // Validate we didn't break QnA functionality.
            Assert.assertNotNull(results);
            Assert.assertTrue(results.length == 1);
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                results[0].getAnswer());
            Assert.assertEquals("Editorial", results[0].getSource());
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
    public void telemetryAdditionalPropsOverride() {
        // Arrange
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            BotTelemetryClient telemetryClient = Mockito.mock(BotTelemetryClient.class);

            // Act - Pass in properties during QnA invocation that override default properties
            //  NOTE: We are invoking this with PII turned OFF, and passing a PII property (originalQuestion).
            QnAMaker qna = new QnAMaker(qnAMakerEndpoint, options, telemetryClient, false);
            Map<String, String> telemetryProperties = new HashMap<String, String>();
            telemetryProperties.put("knowledgeBaseId", "myImportantValue");
            telemetryProperties.put("originalQuestion", "myImportantValue2");

            Map<String, Double> telemetryMetrics = new HashMap<String, Double>();
            telemetryMetrics.put("score", 3.14159);

            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null, telemetryProperties, telemetryMetrics).join();
            // Assert - added properties were added.
            // verify BotTelemetryClient was invoked 1 times, and capture arguments.
            verify(telemetryClient, times(1)).trackEvent(
                eventNameCaptor.capture(),
                propertiesCaptor.capture(),
                metricsCaptor.capture()
            );
            List<String> eventNames = eventNameCaptor.getAllValues();
            List<Map<String, String>> properties = propertiesCaptor.getAllValues();
            List<Map<String, Double>> metrics = metricsCaptor.getAllValues();

            Assert.assertEquals(1, eventNames.size());
            Assert.assertEquals(eventNames.get(0), QnATelemetryConstants.QNA_MSG_EVENT);
            Assert.assertTrue(properties.get(0).containsKey("knowledgeBaseId"));
            Assert.assertEquals("myImportantValue", properties.get(0).get("knowledgeBaseId"));
            Assert.assertTrue(properties.get(0).containsKey("matchedQuestion"));
            Assert.assertEquals("myImportantValue2", properties.get(0).get("originalQuestion"));
            Assert.assertFalse(properties.get(0).containsKey("question"));
            Assert.assertTrue(properties.get(0).containsKey("questionId"));
            Assert.assertTrue(properties.get(0).containsKey("answer"));
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                properties.get(0).get("answer"));
            Assert.assertFalse(properties.get(0).containsKey("MyImportantProperty"));

            Assert.assertEquals(1, metrics.get(0).size());
            Assert.assertTrue(metrics.get(0).containsKey("score"));
            Assert.assertTrue(Double.compare((double)metrics.get(0).get("score"), 3.14159) == 0);
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
    public void telemetryFillPropsOverride() {
        //Arrange
        MockWebServer mockWebServer = new MockWebServer();
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer, response, url).port());
            }
            String finalEndpoint = endpoint;
            QnAMakerEndpoint qnAMakerEndpoint = new QnAMakerEndpoint();
            qnAMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnAMakerEndpoint.setEndpointKey(endpointKey);
            qnAMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions options = new QnAMakerOptions();
            options.setTop(1);

            BotTelemetryClient telemetryClient = Mockito.mock(BotTelemetryClient.class);

            // Act - Pass in properties during QnA invocation that override default properties
            //       In addition Override with derivation.  This presents an interesting question of order of setting properties.
            //       If I want to override "originalQuestion" property:
            //           - Set in "Stock" schema
            //           - Set in derived QnAMaker class
            //           - Set in GetAnswersAsync
            //       Logically, the GetAnswersAync should win.  But ultimately OnQnaResultsAsync decides since it is the last
            //       code to touch the properties before logging (since it actually logs the event).
            QnAMaker qna = new OverrideFillTelemetry(qnAMakerEndpoint, options, telemetryClient, false);
            Map<String, String> telemetryProperties = new HashMap<String, String>();
            telemetryProperties.put("knowledgeBaseId", "myImportantValue");
            telemetryProperties.put("matchedQuestion", "myImportantValue2");

            Map<String, Double> telemetryMetrics = new HashMap<String, Double>();
            telemetryMetrics.put("score", 3.14159);

            QueryResult[] results = qna.getAnswers(getContext("how do I clean the stove?"), null, telemetryProperties, telemetryMetrics).join();
            // Assert - added properties were added.
            // verify BotTelemetryClient was invoked 2 times calling different trackEvents methods, and capture arguments.
            verify(telemetryClient, times(1)).trackEvent(
                eventNameCaptor.capture(),
                propertiesCaptor.capture(),
                metricsCaptor.capture()
            );
            List<String> eventNames = eventNameCaptor.getAllValues();
            List<Map<String, String>> properties = propertiesCaptor.getAllValues();
            List<Map<String, Double>> metrics = metricsCaptor.getAllValues();

            Assert.assertEquals(eventNames.get(0), QnATelemetryConstants.QNA_MSG_EVENT);
            Assert.assertEquals(6, properties.get(0).size());
            Assert.assertTrue(properties.get(0).containsKey("knowledgeBaseId"));
            Assert.assertEquals("myImportantValue", properties.get(0).get("knowledgeBaseId"));
            Assert.assertTrue(properties.get(0).containsKey("matchedQuestion"));
            Assert.assertEquals("myImportantValue2", properties.get(0).get("matchedQuestion"));
            Assert.assertTrue(properties.get(0).containsKey("questionId"));
            Assert.assertTrue(properties.get(0).containsKey("answer"));
            Assert.assertEquals("BaseCamp: You can use a damp rag to clean around the Power Pack",
                properties.get(0).get("answer"));
            Assert.assertTrue(properties.get(0).containsKey("articleFound"));
            Assert.assertTrue(properties.get(0).containsKey("MyImportantProperty"));
            Assert.assertEquals("myImportantValue", properties.get(0).get("MyImportantProperty"));

            Assert.assertEquals(1, metrics.get(0).size());
            Assert.assertTrue(metrics.get(0).containsKey("score"));
            Assert.assertTrue(Double.compare((double)metrics.get(0).get("score"), 3.14159) == 0);
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

    private static TurnContext getContext(String utterance) {
        TestAdapter b = new TestAdapter();
        Activity a = new Activity(ActivityTypes.MESSAGE);
        a.setText(utterance);
        a.setConversation(new ConversationAccount());
        a.setRecipient(new ChannelAccount());
        a.setFrom(new ChannelAccount());

        return new TurnContextImpl(b, a);
    }

    private TestFlow createFlow(Dialog rootDialog, String testName) {
        Storage storage = new MemoryStorage();
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);

        TestAdapter adapter = new TestAdapter(TestAdapter.createConversationReference(testName, "User1", "Bot"));
        adapter
            .useStorage(storage)
            .useBotState(userState, conversationState)
            .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        DialogManager dm = new DialogManager(rootDialog, null);
        return new TestFlow(adapter, (turnContext) -> dm.onTurn(turnContext).thenApply(task -> null));
    }

    public class QnAMakerTestDialog extends ComponentDialog implements DialogDependencies {

        public QnAMakerTestDialog(String knowledgeBaseId, String endpointKey, String hostName, OkHttpClient httpClient) {
            super("QnaMakerTestDialog");
            addDialog(new QnAMakerDialog(knowledgeBaseId, endpointKey, hostName, null,
                null, null, null, null,
                null, null, httpClient));
        }

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext outerDc, Object options) {
            return this.continueDialog(outerDc);
        }

        @Override
        public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
            if (dc.getContext().getActivity().getText() == "moo") {
                return dc.getContext().sendActivity("Yippee ki-yay!").thenApply(task -> END_OF_TURN);
            }

            return dc.beginDialog("qnaDialog").thenApply(task -> task);
        }

        public List<Dialog> getDependencies() {
            return getDialogs().getDialogs().stream().collect(Collectors.toList());
        }

        @Override
        public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
            if(!(boolean)result) {
                dc.getContext().sendActivity("I didn't understand that.");
            }

            return super.resumeDialog(dc, reason, result).thenApply(task -> task);
        }
    }

    private QnAMaker qnaReturnsAnswer(MockWebServer mockWebServer) {
        try {
            String content = readFileContent("QnaMaker_ReturnsAnswer.json");
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode response = mapper.readTree(content);
            String url = this.getRequestUrl();
            String endpoint = "";
            if (this.mockQnAResponse) {
                endpoint = String.format("%s:%s", hostname, initializeMockServer(mockWebServer,response, url).port());
            }
            String finalEndpoint = endpoint;
            // Mock Qna
            QnAMakerEndpoint qnaMakerEndpoint = new QnAMakerEndpoint();
            qnaMakerEndpoint.setKnowledgeBaseId(knowledgeBaseId);
            qnaMakerEndpoint.setEndpointKey(endpointKey);
            qnaMakerEndpoint.setHost(finalEndpoint);

            QnAMakerOptions qnaMakerOptions = new QnAMakerOptions();
            qnaMakerOptions.setTop(1);

            return new QnAMaker(qnaMakerEndpoint, qnaMakerOptions);
        } catch (Exception e) {
            return null;
        }
    }

    private String readFileContent (String fileName) throws IOException {
        String path = Paths.get("", "src", "test", "java", "com", "microsoft", "bot", "ai", "qna",
            "testData", fileName).toAbsolutePath().toString();
        File file = new File(path);
        return FileUtils.readFileToString(file, "utf-8");
    }

    private HttpUrl initializeMockServer(MockWebServer mockWebServer, JsonNode response, String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String mockResponse = mapper.writeValueAsString(response);
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(mockResponse));

        try {
            mockWebServer.start();
        } catch (Exception e) {
            // Empty error
        }
        return mockWebServer.url(url);
    }

    private void enqueueResponse(MockWebServer mockWebServer, JsonNode response) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String mockResponse = mapper.writeValueAsString(response);
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(mockResponse));
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

    public class OverrideTelemetry extends QnAMaker {

        public OverrideTelemetry(QnAMakerEndpoint endpoint, QnAMakerOptions options,
                                 BotTelemetryClient telemetryClient, Boolean logPersonalInformation) {
            super(endpoint, options, telemetryClient, logPersonalInformation);
        }

        @Override
        protected CompletableFuture<Void> onQnaResults(QueryResult[] queryResults, TurnContext turnContext,
                                                       Map<String, String> telemetryProperties,
                                                       Map<String, Double> telemetryMetrics) {
            Map<String, String> properties = telemetryProperties == null ? new HashMap<String, String>() : telemetryProperties;

            // GetAnswerAsync overrides derived class.
            properties.put("MyImportantProperty", "myImportantValue");

            // Log event
            BotTelemetryClient telemetryClient = getTelemetryClient();
            telemetryClient.trackEvent(QnATelemetryConstants.QNA_MSG_EVENT, properties);

            // Create second event.
            Map<String, String> secondEventProperties = new HashMap<String, String>();
            secondEventProperties.put("MyImportantProperty2", "myImportantValue2");
            telemetryClient.trackEvent("MySecondEvent", secondEventProperties);
            return CompletableFuture.completedFuture(null);
        }

    }

    public class OverrideFillTelemetry extends QnAMaker {

        public OverrideFillTelemetry(QnAMakerEndpoint endpoint, QnAMakerOptions options,
                                     BotTelemetryClient telemetryClient, Boolean logPersonalInformation) {
            super(endpoint, options, telemetryClient, logPersonalInformation);
        }

        @Override
        protected CompletableFuture<Void> onQnaResults(QueryResult[] queryResults, TurnContext turnContext,
                                                       Map<String, String> telemetryProperties,
                                                       Map<String, Double> telemetryMetrics) throws IOException {
            return this.fillQnAEvent(queryResults, turnContext, telemetryProperties, telemetryMetrics).thenAccept(eventData -> {
                // Add my property
                eventData.getLeft().put("MyImportantProperty", "myImportantValue");

                BotTelemetryClient telemetryClient = this.getTelemetryClient();

                // Log QnaMessage event
                telemetryClient.trackEvent(QnATelemetryConstants.QNA_MSG_EVENT, eventData.getLeft(), eventData.getRight());

                // Create second event.
                Map<String, String> secondEventProperties = new HashMap<String, String>();
                secondEventProperties.put("MyImportantProperty2", "myImportantValue2");

                telemetryClient.trackEvent("MySecondEvent", secondEventProperties);
            });
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CapturedRequest {
        private String[] questions;
        private Integer top;
        private Metadata[] strictFilters;
        private Metadata[] MetadataBoost;
        private Float scoreThreshold;

        public String[] getQuestions() {
            return questions;
        }

        public void setQuestions(String[] questions) {
            this.questions = questions;
        }

        public Integer getTop() {
            return top;
        }

        public Metadata[] getStrictFilters() {
            return strictFilters;
        }

        public Float getScoreThreshold() {
            return scoreThreshold;
        }
    }
}
