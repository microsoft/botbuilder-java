// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.dialogs;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.schema.Activity;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(MockitoJUnitRunner.class)
public class RecognizerTest {

    @Mock
    BotTelemetryClient telemetryClient;

    @Test
    public void LogsTelemetry() {

        MyRecognizerSubclass recognizer = new MyRecognizerSubclass();
        recognizer.setTelemetryClient(telemetryClient);
        TestAdapter adapter = new TestAdapter(
            TestAdapter.createConversationReference("RecognizerLogsTelemetry", "testUser", "testBot"));
        Activity activity = MessageFactory.text("hi");
        TurnContextImpl context = new TurnContextImpl(adapter, activity);
        DialogContext dc = new DialogContext(new DialogSet(), context, new DialogState());

        RecognizerResult result = recognizer.recognize(dc, activity).join();

        Map<String, String> expectedProperties = new HashMap<String, String>();
        expectedProperties.put("TopIntent", "myTestIntent");
        expectedProperties.put("Intents", "{\"myTestIntent\":{\"score\":1.0}}");
        expectedProperties.put("Text", "hi");
        expectedProperties.put("AlteredText", null);
        expectedProperties.put("AdditionalProperties", null);
        expectedProperties.put("TopIntentScore", "1.0");
        expectedProperties.put("Entities", null);


        verify(telemetryClient, atLeastOnce()).trackEvent("MyRecognizerSubclassResult", expectedProperties, null);
    }

    /**
     * Subclass to test
     * {@link Recognizer#fillRecognizerResultTelemetryProperties(RecognizerResult,
     * Dictionary{String,String}, DialogContext)} functionality.
     */
    private class MyRecognizerSubclass extends Recognizer {

         @Override
        public CompletableFuture<RecognizerResult> recognize(DialogContext dialogContext,
                                                             Activity activity,
                                                             Map<String, String> telemetryProperties,
                                                             Map<String, Double> telemetryMetrics
        ) {

            String text = activity.getText() != null ? activity.getText() : "";

            RecognizerResult recognizerResult =  new RecognizerResult();
            recognizerResult.setText(text);
            recognizerResult.setAlteredText(null);
            Map<String, IntentScore> intentMap = new HashMap<String, IntentScore>();
            IntentScore intentScore = new IntentScore();
            intentScore.setScore(1.0);
            intentMap.put("myTestIntent", intentScore);
            recognizerResult.setIntents(intentMap);

            trackRecognizerResult(dialogContext, "MyRecognizerSubclassResult",
                fillRecognizerResultTelemetryProperties(recognizerResult,
                                                        telemetryProperties,
                                                        dialogContext),
                                                        telemetryMetrics);

            return CompletableFuture.completedFuture(recognizerResult);
        }
    }
}
