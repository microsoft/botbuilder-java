// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.ai.luis.testdata.TestRecognizerResultConvert;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LuisRecognizerTests {

    @Mock
    LuisRecognizerOptionsV3 options;

    @Mock
    BotTelemetryClient telemetryClient;

    @Mock
    TurnContext turnContext;

    @Mock
    DialogContext dialogContext;

    @Mock
    LuisApplication luisApplication;

    private RecognizerResult getMockedResult() {
        RecognizerResult recognizerResult = new RecognizerResult();
        HashMap<String, IntentScore> intents = new HashMap<String, IntentScore>();
        IntentScore testScore = new IntentScore();
        testScore.setScore(0.2);
        IntentScore greetingScore = new IntentScore();
        greetingScore.setScore(0.4);
        intents.put("Test", testScore);
        intents.put("Greeting", greetingScore);
        recognizerResult.setIntents(intents);
        recognizerResult.setEntities(JsonNodeFactory.instance.objectNode());
        recognizerResult.setProperties(
            "sentiment",
            JsonNodeFactory.instance.objectNode()
                .put(
                    "label",
                    "neutral"));
        return recognizerResult;
    };

    @Test
    public void topIntentReturnsTopIntent() {
        String greetingIntent = LuisRecognizer
            .topIntent(getMockedResult());
        assertEquals(greetingIntent, "Greeting");
    }

    @Test
    public void topIntentReturnsDefaultIfMinScoreIsHigher() {
        String defaultIntent = LuisRecognizer
            .topIntent(getMockedResult(), 0.5);
        assertEquals(defaultIntent, "None");
    }

    @Test
    public void topIntentReturnsDefaultIfProvided() {
        String defaultIntent = LuisRecognizer
            .topIntent(getMockedResult(), "Test2", 0.5);
        assertEquals(defaultIntent, "Test2");
    }

    @Test
    public void topIntentThrowsIllegalArgumentIfResultIsNull() {
        Exception exception = Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisRecognizer.topIntent(null);
        });

        String expectedMessage = "RecognizerResult";
        String actualMessage = exception.getMessage();

        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void TopIntentReturnsTopIntentIfScoreEqualsMinScore() {
        String defaultIntent = LuisRecognizer.topIntent(getMockedResult(), 0.4);
        assertEquals(defaultIntent, "Greeting");
    }

    @Test
    public void throwExceptionOnNullOptions() {
        Exception exception = Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisRecognizer lR = new LuisRecognizer(null);
        });

        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains("Recognizer Options cannot be null"));
    }

    @Test
    public void recognizerResult() {
        setMockObjectsForTelemetry();
        LuisRecognizer recognizer = new LuisRecognizer(options);
        RecognizerResult expected = new RecognizerResult();
        expected.setText("Random Message");
        HashMap<String, IntentScore> intents = new HashMap<String, IntentScore>();
        IntentScore testScore = new IntentScore();
        testScore.setScore(0.2);
        IntentScore greetingScore = new IntentScore();
        greetingScore.setScore(0.4);
        intents.put("Test", testScore);
        intents.put("Greeting", greetingScore);
        expected.setIntents(intents);
        expected.setEntities(JsonNodeFactory.instance.objectNode());
        expected.setProperties(
            "sentiment",
            JsonNodeFactory.instance.objectNode()
                .put(
                    "label",
                    "neutral"));
        RecognizerResult actual = null;
        try {
            actual = recognizer.recognize(turnContext).get();
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(actual));
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void recognizerResult_nullTelemetryClient() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Random Message");
        activity.setChannelId("EmptyContext");
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId("Activity-from-ID");
        activity.setFrom(channelAccount);
        when(turnContext.getActivity())
            .thenReturn(activity);

        when(luisApplication.getApplicationId())
            .thenReturn("b31aeaf3-3511-495b-a07f-571fc873214b");

        when(options.getApplication())
            .thenReturn(luisApplication);
        RecognizerResult mockedResult = getMockedResult();
        mockedResult.setText("Random Message");
        doReturn(CompletableFuture.supplyAsync(() -> mockedResult))
            .when(options)
            .recognizeInternal(
                any(TurnContext.class));

        LuisRecognizer recognizer = new LuisRecognizer(options);
        RecognizerResult expected = new RecognizerResult();
        expected.setText("Random Message");
        HashMap<String, IntentScore> intents = new HashMap<String, IntentScore>();
        IntentScore testScore = new IntentScore();
        testScore.setScore(0.2);
        IntentScore greetingScore = new IntentScore();
        greetingScore.setScore(0.4);
        intents.put("Test", testScore);
        intents.put("Greeting", greetingScore);
        expected.setIntents(intents);
        expected.setEntities(JsonNodeFactory.instance.objectNode());
        expected.setProperties(
                "sentiment",
                JsonNodeFactory.instance.objectNode()
                    .put(
                        "label",
                        "neutral"));
        RecognizerResult actual = null;
        try {
            actual = recognizer.recognize(turnContext).get();
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(actual));
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void recognizerResultDialogContext() {
        RecognizerResult expected = new RecognizerResult();
        expected.setText("Random Message");
        HashMap<String, IntentScore> intents = new HashMap<String, IntentScore>();
        IntentScore testScore = new IntentScore();
        testScore.setScore(0.2);
        IntentScore greetingScore = new IntentScore();
        greetingScore.setScore(0.4);
        intents.put("Test", testScore);
        intents.put("Greeting", greetingScore);
        expected.setIntents(intents);
        expected.setEntities(JsonNodeFactory.instance.objectNode());
        expected.setProperties(
                "sentiment",
                JsonNodeFactory.instance.objectNode()
                    .put(
                        "label",
                        "neutral"));
        RecognizerResult actual = null;
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Random Message");
        activity.setChannelId("EmptyContext");
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId("Activity-from-ID");
        activity.setFrom(channelAccount);
        when(turnContext.getActivity())
            .thenReturn(activity);
        when(luisApplication.getApplicationId())
            .thenReturn("b31aeaf3-3511-495b-a07f-571fc873214b");

        when(options.getTelemetryClient()).thenReturn(telemetryClient);

        when(options.getApplication())
            .thenReturn(luisApplication);
        RecognizerResult mockedResult = getMockedResult();
        mockedResult.setText("Random Message");
        when(dialogContext.getContext())
            .thenReturn(turnContext);

        doReturn(CompletableFuture.supplyAsync(() -> mockedResult))
            .when(options)
            .recognizeInternal(
                any(DialogContext.class), any(Activity.class));
        LuisRecognizer recognizer = new LuisRecognizer(options);
        try {
            actual = recognizer.recognize(dialogContext, turnContext.getActivity()).get();
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(actual));
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void recognizerResultConverted() {

        setMockObjectsForTelemetry();
        LuisRecognizer recognizer = new LuisRecognizer(options);
        TestRecognizerResultConvert actual = null;
        try {
            actual = recognizer.recognize(turnContext, TestRecognizerResultConvert.class).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        TestRecognizerResultConvert expected = new TestRecognizerResultConvert();
        expected.recognizerResultText = "Random Message";

        assertEquals(expected.recognizerResultText, actual.recognizerResultText);
    }

    @Test
    public void telemetryPropertiesAreFilledOnRecognizer() {

        setMockObjectsForTelemetry();
        LuisRecognizer recognizer = new LuisRecognizer(options);

        try {
            recognizer.recognize(turnContext).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Map<String, String> expectedProperties = new HashMap<String, String>();
        expectedProperties.put("intentScore", "0.4");
        expectedProperties.put("intent2", "Test");
        expectedProperties.put("entities", "{}");
        expectedProperties.put("intentScore2", "0.2");
        expectedProperties.put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
        expectedProperties.put("intent", "Greeting");
        expectedProperties.put("fromId", "Activity-from-ID");
        expectedProperties.put("sentimentLabel", "neutral");

        verify(telemetryClient, atLeastOnce()).trackEvent("LuisResult", expectedProperties, null);
    }

    @Test
    public void telemetry_PiiLogged() {

        setMockObjectsForTelemetry();
        when(options.isLogPersonalInformation()).thenReturn(true);

        LuisRecognizer recognizer = new LuisRecognizer(options);

        try {
            recognizer.recognize(turnContext).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Map<String, String> expectedProperties = new HashMap<String, String>();
        expectedProperties.put("intentScore", "0.4");
        expectedProperties.put("intent2", "Test");
        expectedProperties.put("entities", "{}");
        expectedProperties.put("intentScore2", "0.2");
        expectedProperties.put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
        expectedProperties.put("intent", "Greeting");
        expectedProperties.put("fromId", "Activity-from-ID");
        expectedProperties.put("sentimentLabel", "neutral");
        expectedProperties.put("question", "Random Message");

        verify(telemetryClient, atLeastOnce()).trackEvent("LuisResult", expectedProperties, null);
    }

    @Test
    public void telemetry_additionalProperties() {
        setMockObjectsForTelemetry();
        when(options.isLogPersonalInformation()).thenReturn(true);

        LuisRecognizer recognizer = new LuisRecognizer(options);
        Map<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("test", "testvalue");
        additionalProperties.put("foo", "foovalue");
        Map<String, Double> telemetryMetrics = new HashMap<String, Double>();
        telemetryMetrics.put("test", 3.1416);
        telemetryMetrics.put("foo", 2.11);
        try {
            recognizer.recognize(turnContext, additionalProperties, telemetryMetrics).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Map<String, String> expectedProperties = new HashMap<String, String>();
        expectedProperties.put("intentScore", "0.4");
        expectedProperties.put("intent2", "Test");
        expectedProperties.put("entities", "{}");
        expectedProperties.put("intentScore2", "0.2");
        expectedProperties.put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
        expectedProperties.put("intent", "Greeting");
        expectedProperties.put("fromId", "Activity-from-ID");
        expectedProperties.put("sentimentLabel", "neutral");
        expectedProperties.put("question", "Random Message");
        expectedProperties.put("test", "testvalue");
        expectedProperties.put("foo", "foovalue");

        verify(telemetryClient, atLeastOnce()).trackEvent("LuisResult", expectedProperties, telemetryMetrics);
    }

    @Test
    public void telemetry_additionalPropertiesOverrideProperty() {
        setMockObjectsForTelemetry();
        when(options.isLogPersonalInformation()).thenReturn(true);

        LuisRecognizer recognizer = new LuisRecognizer(options);
        Map<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("intentScore", "1.15");
        additionalProperties.put("foo", "foovalue");
        Map<String, Double> telemetryMetrics = new HashMap<String, Double>();
        telemetryMetrics.put("test", 3.1416);
        telemetryMetrics.put("foo", 2.11);
        try {
            recognizer.recognize(turnContext, additionalProperties, telemetryMetrics).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Map<String, String> expectedProperties = new HashMap<String, String>();
        expectedProperties.put("intentScore", "1.15");
        expectedProperties.put("intent2", "Test");
        expectedProperties.put("entities", "{}");
        expectedProperties.put("intentScore2", "0.2");
        expectedProperties.put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
        expectedProperties.put("intent", "Greeting");
        expectedProperties.put("fromId", "Activity-from-ID");
        expectedProperties.put("sentimentLabel", "neutral");
        expectedProperties.put("question", "Random Message");
        expectedProperties.put("foo", "foovalue");

        verify(telemetryClient, atLeastOnce()).trackEvent("LuisResult", expectedProperties, telemetryMetrics);
    }

    private void setMockObjectsForTelemetry() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Random Message");
        activity.setType(ActivityTypes.MESSAGE);
        activity.setChannelId("EmptyContext");
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId("Activity-from-ID");
        activity.setFrom(channelAccount);
        when(turnContext.getActivity())
            .thenReturn(activity);

        when(luisApplication.getApplicationId())
            .thenReturn("b31aeaf3-3511-495b-a07f-571fc873214b");

        when(options.getTelemetryClient()).thenReturn(telemetryClient);

        when(options.getApplication())
            .thenReturn(luisApplication);
        RecognizerResult mockedResult = getMockedResult();
        mockedResult.setText("Random Message");
        doReturn(CompletableFuture.supplyAsync(() -> mockedResult))
            .when(options)
            .recognizeInternal(
                any(TurnContext.class));
    }
}
