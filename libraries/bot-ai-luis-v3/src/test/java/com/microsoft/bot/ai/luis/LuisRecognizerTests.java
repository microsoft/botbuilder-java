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

    private RecognizerResult mockedResult = new RecognizerResult(){{
        setIntents(new HashMap<String, IntentScore>(){{
            put("Test",
                new IntentScore(){{
                setScore(0.2);
            }});
            put("Greeting",
                new IntentScore(){{
                setScore(0.4);
            }});
        }});
        setEntities(JsonNodeFactory.instance.objectNode());
        setProperties(
            "sentiment",
            JsonNodeFactory.instance.objectNode()
                .put(
                    "label",
                    "neutral"));
    }};

    @Test
    public void topIntentReturnsTopIntent() {
        String defaultIntent = LuisRecognizer
            .topIntent(mockedResult);
        assertEquals(defaultIntent, "Greeting");
    }

    @Test
    public void topIntentReturnsDefaultIfMinScoreIsHigher() {
        String defaultIntent = LuisRecognizer
            .topIntent(mockedResult, 0.5);
        assertEquals(defaultIntent, "None");
    }

    @Test
    public void topIntentReturnsDefaultIfProvided() {
        String defaultIntent = LuisRecognizer
            .topIntent(mockedResult, "Test2", 0.5);
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
        String defaultIntent = LuisRecognizer.topIntent(mockedResult, 0.4);
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
        RecognizerResult expected  = new RecognizerResult(){{
            setText("Random Message");
            setIntents(new HashMap<String, IntentScore>(){{
                put("Test",
                    new IntentScore(){{
                        setScore(0.2);
                    }});
                put("Greeting",
                    new IntentScore(){{
                        setScore(0.4);
                    }});
            }});
            setEntities(JsonNodeFactory.instance.objectNode());
            setProperties(
                "sentiment",
                JsonNodeFactory.instance.objectNode()
                    .put(
                        "label",
                        "neutral"));
        }};
        RecognizerResult actual = null;
        try {
            actual = recognizer.recognize(turnContext).get();
            ObjectMapper mapper = new ObjectMapper();
            assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(actual));
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void recognizerResult_nullTelemetryClient() {
        when(turnContext.getActivity())
            .thenReturn(new Activity() {{
                setText("Random Message");
                setType(ActivityTypes.MESSAGE);
                setChannelId("EmptyContext");
                setFrom(new ChannelAccount(){{
                    setId("Activity-from-ID");
                }});
            }});

        when(luisApplication.getApplicationId())
            .thenReturn("b31aeaf3-3511-495b-a07f-571fc873214b");

        when(options.getApplication())
            .thenReturn(luisApplication);
        mockedResult.setText("Random Message");
        doReturn(CompletableFuture.supplyAsync(() -> mockedResult))
            .when(options)
            .recognizeInternal(
                any(TurnContext.class));

        LuisRecognizer recognizer = new LuisRecognizer(options);
        RecognizerResult expected  = new RecognizerResult(){{
            setText("Random Message");
            setIntents(new HashMap<String, IntentScore>(){{
                put("Test",
                    new IntentScore(){{
                        setScore(0.2);
                    }});
                put("Greeting",
                    new IntentScore(){{
                        setScore(0.4);
                    }});
            }});
            setEntities(JsonNodeFactory.instance.objectNode());
            setProperties(
                "sentiment",
                JsonNodeFactory.instance.objectNode()
                    .put(
                        "label",
                        "neutral"));
        }};
        RecognizerResult actual = null;
        try {
            actual = recognizer.recognize(turnContext).get();
            ObjectMapper mapper = new ObjectMapper();
            assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(actual));
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void recognizerResultDialogContext() {
        RecognizerResult expected  = new RecognizerResult(){{
            setText("Random Message");
            setIntents(new HashMap<String, IntentScore>(){{
                put("Test",
                    new IntentScore(){{
                        setScore(0.2);
                    }});
                put("Greeting",
                    new IntentScore(){{
                        setScore(0.4);
                    }});
            }});
            setEntities(JsonNodeFactory.instance.objectNode());
            setProperties(
                "sentiment",
                JsonNodeFactory.instance.objectNode()
                    .put(
                        "label",
                        "neutral"));
        }};
        RecognizerResult actual = null;
        when(turnContext.getActivity())
            .thenReturn(new Activity() {{
                setText("Random Message");
                setType(ActivityTypes.MESSAGE);
                setChannelId("EmptyContext");
                setFrom(new ChannelAccount(){{
                    setId("Activity-from-ID");
                }});
            }});

        when(luisApplication.getApplicationId())
            .thenReturn("b31aeaf3-3511-495b-a07f-571fc873214b");

        when(options.getTelemetryClient()).thenReturn(telemetryClient);

        when(options.getApplication())
            .thenReturn(luisApplication);
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
            ObjectMapper mapper = new ObjectMapper();
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

        TestRecognizerResultConvert expected  = new TestRecognizerResultConvert(){{
            recognizerResultText = "Random Message";
        }};

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
        Map<String, String> expectedProperties = new HashMap<String, String> (){{
            put("intentScore", "0.4");
            put("intent2", "Test");
            put("entities", "{}");
            put("intentScore2", "0.2");
            put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
            put("intent", "Greeting");
            put("fromId", "Activity-from-ID");
            put("sentimentLabel", "neutral");
        }};

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
        Map<String, String> expectedProperties = new HashMap<String, String> (){{
            put("intentScore", "0.4");
            put("intent2", "Test");
            put("entities", "{}");
            put("intentScore2", "0.2");
            put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
            put("intent", "Greeting");
            put("fromId", "Activity-from-ID");
            put("sentimentLabel", "neutral");
            put("question", "Random Message");
        }};

        verify(telemetryClient, atLeastOnce()).trackEvent("LuisResult", expectedProperties, null);
    }

    @Test
    public void telemetry_additionalProperties() {
        setMockObjectsForTelemetry();
        when(options.isLogPersonalInformation()).thenReturn(true);

        LuisRecognizer recognizer = new LuisRecognizer(options);
        Map<String, String> additionalProperties = new HashMap<String, String>(){{
            put("test", "testvalue");
            put("foo", "foovalue");
        }};
        Map<String, Double> telemetryMetrics = new HashMap<String, Double>(){{
            put("test", 3.1416);
            put("foo", 2.11);
        }};
        try {
            recognizer.recognize(turnContext, additionalProperties, telemetryMetrics).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Map<String, String> expectedProperties = new HashMap<String, String> (){{
            put("intentScore", "0.4");
            put("intent2", "Test");
            put("entities", "{}");
            put("intentScore2", "0.2");
            put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
            put("intent", "Greeting");
            put("fromId", "Activity-from-ID");
            put("sentimentLabel", "neutral");
            put("question", "Random Message");
            put("test", "testvalue");
            put("foo", "foovalue");
        }};

        verify(telemetryClient, atLeastOnce()).trackEvent("LuisResult", expectedProperties, telemetryMetrics);
    }

    @Test
    public void telemetry_additionalPropertiesOverrideProperty() {
        setMockObjectsForTelemetry();
        when(options.isLogPersonalInformation()).thenReturn(true);

        LuisRecognizer recognizer = new LuisRecognizer(options);
        Map<String, String> additionalProperties = new HashMap<String, String>(){{
            put("intentScore", "1.15");
            put("foo", "foovalue");
        }};
        Map<String, Double> telemetryMetrics = new HashMap<String, Double>(){{
            put("test", 3.1416);
            put("foo", 2.11);
        }};
        try {
            recognizer.recognize(turnContext, additionalProperties, telemetryMetrics).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Map<String, String> expectedProperties = new HashMap<String, String> (){{
            put("intentScore", "1.15");
            put("intent2", "Test");
            put("entities", "{}");
            put("intentScore2", "0.2");
            put("applicationId", "b31aeaf3-3511-495b-a07f-571fc873214b");
            put("intent", "Greeting");
            put("fromId", "Activity-from-ID");
            put("sentimentLabel", "neutral");
            put("question", "Random Message");
            put("foo", "foovalue");
        }};

        verify(telemetryClient, atLeastOnce()).trackEvent("LuisResult", expectedProperties, telemetryMetrics);
    }

    private void setMockObjectsForTelemetry() {
        when(turnContext.getActivity())
            .thenReturn(new Activity() {{
                setText("Random Message");
                setType(ActivityTypes.MESSAGE);
                setChannelId("EmptyContext");
                setFrom(new ChannelAccount(){{
                    setId("Activity-from-ID");
                }});
            }});

        when(luisApplication.getApplicationId())
            .thenReturn("b31aeaf3-3511-495b-a07f-571fc873214b");

        when(options.getTelemetryClient()).thenReturn(telemetryClient);

        when(options.getApplication())
            .thenReturn(luisApplication);
        mockedResult.setText("Random Message");
        doReturn(CompletableFuture.supplyAsync(() -> mockedResult))
            .when(options)
            .recognizeInternal(
                any(TurnContext.class));
    }
}
