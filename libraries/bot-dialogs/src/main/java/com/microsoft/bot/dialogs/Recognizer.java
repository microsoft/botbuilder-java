// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.RecognizerConvert;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Serialization;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Recognizer base class.
 *
 * <p>Recognizers operate in a DialogContext environment to recognize user input into Intents
 * and Entities.</p>
 *
 * <p>
 * This class models 3 virtual methods around
 *   * Pure DialogContext (where the recognition happens against current state dialogcontext
 *   * Activity (where the recognition is from an Activity)
 *   * Text/Locale (where the recognition is from text/locale)
 * </p>
 *
 * <p>
 * The default implementation of DialogContext method is to use Context.Activity and call the
 * activity method.
 * The default implementation of Activity method is to filter to Message activities and pull
 * out text/locale and call the text/locale method.
 * </p>
 */
public class Recognizer {
    /**
     * Intent name that will be produced by this recognizer if the child recognizers do not
     * have consensus for intents.
     */
    public static final String CHOOSE_INTENT = "ChooseIntent";

    /**
     * Standard none intent that means none of the recognizers recognize the intent.  If each
     * recognizer returns no intents or None intents, then this recognizer will return None intent.
     */
    public static final String NONE_INTENT = "None";

    @JsonProperty(value = "id")
    private String id;

    @JsonIgnore
    private BotTelemetryClient telemetryClient = new NullBotTelemetryClient();

    /**
     * Initializes a new Recognizer.
     */
    public Recognizer() {
    }

    /**
     * Runs current DialogContext.TurnContext.Activity through a recognizer and returns a
     * generic recognizer result.
     *
     * @param dialogContext Dialog Context.
     * @param activity activity to recognize.
     * @return Analysis of utterance.
     */
    public CompletableFuture<RecognizerResult> recognize(
        DialogContext dialogContext,
        Activity activity
    ) {
        return recognize(dialogContext, activity, null, null);
    }

    /**
     * Runs current DialogContext.TurnContext.Activity through a recognizer and returns a
     * generic recognizer result.
     *
     * @param dialogContext Dialog Context.
     * @param activity activity to recognize.
     * @param telemetryProperties The properties to be included as part of the event tracking.
     * @param telemetryMetrics The metrics to be included as part of the event tracking.
     * @return Analysis of utterance.
     */
    public CompletableFuture<RecognizerResult> recognize(
        DialogContext dialogContext,
        Activity activity,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics
    ) {
        return Async.completeExceptionally(new NotImplementedException("recognize"));
    }

    /**
     * Runs current DialogContext.TurnContext.Activity through a recognizer and returns a
     * strongly-typed recognizer result using RecognizerConvert.
     *
     * @param dialogContext Dialog Context.
     * @param activity activity to recognize.
     * @param telemetryProperties The properties to be included as part of the event tracking.
     * @param telemetryMetrics The metrics to be included as part of the event tracking.
     * @param c Class of type T.
     * @param <T> The RecognizerConvert.
     * @return Analysis of utterance.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
        DialogContext dialogContext,
        Activity activity,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics,
        Class<T> c
    ) {
        return Async.tryCompletable(() -> {
            T result = c.newInstance();
            return recognize(dialogContext, activity, telemetryProperties, telemetryMetrics)
                .thenApply(recognizerResult -> {
                    result.convert(recognizerResult);
                    return result;
                });
        });
    }

    /**
     * Returns ChooseIntent between multiple recognizer results.
     *
     * @param recognizerResults recognizer Id to recognizer results map.
     * @return recognizerResult which is ChooseIntent.
     */
    protected static RecognizerResult createChooseIntentResult(Map<String, RecognizerResult> recognizerResults) {
        String text = null;
        List<JsonNode> candidates = new ArrayList<>();

        for (Map.Entry<String, RecognizerResult> recognizerResult : recognizerResults.entrySet()) {
            text = recognizerResult.getValue().getText();
            RecognizerResult.NamedIntentScore top = recognizerResult.getValue().getTopScoringIntent();
            if (!StringUtils.equals(top.intent, NONE_INTENT)) {
                ObjectNode candidate = Serialization.createObjectNode();
                candidate.put("id", recognizerResult.getKey());
                candidate.put("intent", top.intent);
                candidate.put("score", top.score);
                candidate.put("result", Serialization.objectToTree(recognizerResult.getValue()));
                candidates.add(candidate);
            }
        }

        RecognizerResult result = new RecognizerResult();
        Map<String, IntentScore> intents = new HashMap<>();

        if (!candidates.isEmpty()) {
            // return ChooseIntent with candidates array
            IntentScore intent = new IntentScore();
            intent.setScore(1.0);
            intents.put(CHOOSE_INTENT, intent);

            result.setText(text);
            result.setIntents(intents);
            result.setProperties("candidates", Serialization.objectToTree(candidates));
        } else {
            // just return a none intent
            IntentScore intent = new IntentScore();
            intent.setScore(1.0);
            intents.put(NONE_INTENT, intent);

            result.setText(text);
            result.setIntents(intents);
        }

        return result;
    }

    /**
     * Gets id of the recognizer.
     * @return id of the recognizer
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id of the recognizer.
     * @param withId id of the recognizer
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the currently configured BotTelemetryClient that logs the RecognizerResult event.
     * @return BotTelemetryClient
     */
    public BotTelemetryClient getTelemetryClient() {
        return telemetryClient;
    }

    /**
     * Sets the currently configured BotTelemetryClient that logs the RecognizerResult event.
     * @param withTelemetryClient BotTelemetryClient
     */
    public void setTelemetryClient(BotTelemetryClient withTelemetryClient) {
        telemetryClient = withTelemetryClient;
    }

    /**
     * Uses the RecognizerResult to create a list of propeties to be included when tracking the
     * result in telemetry.
     *
     * @param recognizerResult Recognizer Result.
     * @param telemetryProperties A list of properties to append or override the properties
     *                            created using the RecognizerResult.
     * @param dialogContext Dialog Context.
     * @return A dictionary that can be included when calling the TrackEvent method on the
     * TelemetryClient.
     */
    protected Map<String, String> fillRecognizerResultTelemetryProperties(
        RecognizerResult recognizerResult,
        Map<String, String> telemetryProperties,
        DialogContext dialogContext
    ) {
        Map<String, String> properties = new HashMap<>();
        properties.put("Text", recognizerResult.getText());
        properties.put("AlteredText", recognizerResult.getAlteredText());
        properties.put("TopIntent", !recognizerResult.getIntents().isEmpty()
            ? recognizerResult.getTopScoringIntent().intent : null);
        properties.put("TopIntentScore", !recognizerResult.getIntents().isEmpty()
            ? Double.toString(recognizerResult.getTopScoringIntent().score) : null);
        properties.put("Intents", !recognizerResult.getIntents().isEmpty()
            ? Serialization.toStringSilent(recognizerResult.getIntents()) : null);
        properties.put("Entities", recognizerResult.getEntities() != null
            ? Serialization.toStringSilent(recognizerResult.getEntities()) : null);
        properties.put("AdditionalProperties", !recognizerResult.getProperties().isEmpty()
            ? Serialization.toStringSilent(recognizerResult.getProperties()) : null);

        // Additional Properties can override "stock" properties.
        if (telemetryProperties != null) {
            properties.putAll(telemetryProperties);
        }

        return properties;
    }

    /**
     * Tracks an event with the event name provided using the TelemetryClient attaching the
     * properties / metrics.
     *
     * @param dialogContext Dialog Context.
     * @param eventName The name of the event to track.
     * @param telemetryProperties The properties to be included as part of the event tracking.
     * @param telemetryMetrics The metrics to be included as part of the event tracking.
     */
    protected void trackRecognizerResult(
        DialogContext dialogContext,
        String eventName,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics
    ) {
        if (telemetryClient instanceof NullBotTelemetryClient) {
            BotTelemetryClient turnStateTelemetryClient = dialogContext.getContext()
                .getTurnState().get(BotTelemetryClient.class);
            telemetryClient = turnStateTelemetryClient != null ? turnStateTelemetryClient : telemetryClient;
        }

        telemetryClient.trackEvent(eventName, telemetryProperties, telemetryMetrics);
    }
}
