// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.RecognizerConvert;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.schema.Activity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Luis Recognizer class to query the LUIS Service using the configuration set
 * by the LuisRecognizeroptions. A LUIS based implementation of
 * TelemetryRecognizer.
 */
public class LuisRecognizer extends TelemetryRecognizer {
    /**
     * Luis Recognizer options to query the Luis Service.
     */
    private LuisRecognizerOptions luisRecognizerOptions;

    /**
     * Initializes a new instance of the Luis Recognizer.
     *
     * @param recognizerOptions Luis Recognizer options to use when calling the LUIS
     *                          Service.
     * @throws IllegalArgumentException if null is passed as recognizerOptions.
     */
    public LuisRecognizer(LuisRecognizerOptions recognizerOptions) {
        if (recognizerOptions == null) {
            throw new IllegalArgumentException("Recognizer Options cannot be null");
        }

        this.luisRecognizerOptions = recognizerOptions;
        this.setTelemetryClient(recognizerOptions.getTelemetryClient() != null ? recognizerOptions.getTelemetryClient()
                : new NullBotTelemetryClient());
        this.setLogPersonalInformation(recognizerOptions.isLogPersonalInformation());
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     *
     * @param results The Recognizer Result with the list of Intents to filter.
     *                Defaults to a value of "None" and a min score value of `0.0`
     * @return The top scoring intent name.
     */
    public static String topIntent(RecognizerResult results) {
        return topIntent(results, "None");
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     *
     * @param results       The Recognizer Result with the list of Intents to filter
     * @param defaultIntent Intent name to return should a top intent be found.
     *                      Defaults to a value of "None" and a min score value of
     *                      `0.0`
     * @return The top scoring intent name.
     */
    public static String topIntent(RecognizerResult results, String defaultIntent) {
        return topIntent(results, defaultIntent, 0.0);
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     *
     * @param results  The Recognizer Result with the list of Intents to filter.
     * @param minScore Minimum score needed for an intent to be considered as a top
     *                 intent.
     * @return The top scoring intent name.
     */
    public static String topIntent(RecognizerResult results, double minScore) {
        return topIntent(results, "None", minScore);
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     *
     * @param results       The Recognizer Result with the list of Intents to filter
     * @param defaultIntent Intent name to return should a top intent be found.
     *                      Defaults to a value of "None
     * @param minScore      Minimum score needed for an intent to be considered as a
     *                      top intent.
     * @return The top scoring intent name.
     */
    public static String topIntent(RecognizerResult results, String defaultIntent, double minScore) {
        if (results == null) {
            throw new IllegalArgumentException("RecognizerResult");
        }

        defaultIntent = defaultIntent == null || defaultIntent.equals("") ? "None" : defaultIntent;

        String topIntent = null;
        double topScore = -1.0;
        if (!results.getIntents().isEmpty()) {
            for (Map.Entry<String, IntentScore> intent : results.getIntents().entrySet()) {

                double score = intent.getValue().getScore();
                if (score > topScore && score >= minScore) {
                    topIntent = intent.getKey();
                    topScore = score;
                }
            }
        }

        return StringUtils.isNotBlank(topIntent) ? topIntent : defaultIntent;
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param turnContext Context object containing information for a single turn of
     *                    conversation with a user.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    @Override
    public CompletableFuture<RecognizerResult> recognize(TurnContext turnContext) {
        return recognizeInternal(turnContext, null, null, null);
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param dialogContext Context object containing information for a single turn
     *                      of conversation with a user.
     * @param activity      Activity to recognize.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(DialogContext dialogContext, Activity activity) {
        return recognizeInternal(dialogContext, activity, null, null, null);
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param turnContext Context object containing information for a single turn of
     *                    conversation with a user.
     * @param <T>         type of result.
     * @param c           RecognizerConvert implemented class to convert the
     *                    Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(TurnContext turnContext, Class<T> c) {
        return recognizeInternal(turnContext, null, null, null)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param dialogContext Context object containing information for a single turn
     *                      of conversation with a user.
     * @param activity      Activity to recognize.
     * @param <T>           Type of result.
     * @param c             RecognizerConvert implemented class to convert the
     *                      Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(DialogContext dialogContext, Activity activity,
            Class<T> c) {
        return recognizeInternal(dialogContext, activity, null, null, null)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param turnContext         Context object containing information for a single
     *                            turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    @Override
    public CompletableFuture<RecognizerResult> recognize(TurnContext turnContext,
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics) {
        return recognizeInternal(turnContext, null, telemetryProperties, telemetryMetrics);
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param dialogContext       Context object containing information for a single
     *                            turn of conversation with a user.
     * @param activity            Activity to recognize.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(DialogContext dialogContext, Activity activity,
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics) {
        return recognizeInternal(dialogContext, activity, null, telemetryProperties, telemetryMetrics);
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param turnContext         Context object containing information for a single
     *                            turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @param <T>                 Type of result.
     * @param c                   RecognizerConvert implemented class to convert the
     *                            Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(TurnContext turnContext,
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics, Class<T> c) {
        return recognizeInternal(turnContext, null, telemetryProperties, telemetryMetrics)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param dialogContext       Context object containing information for a single
     *                            turn of conversation with a user.
     * @param activity            Activity to recognize.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @param <T>                 Type of result.
     * @param c                   RecognizerConvert implemented class to convert the
     *                            Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(DialogContext dialogContext, Activity activity,
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics, Class<T> c) {
        return recognizeInternal(dialogContext, activity, null, telemetryProperties, telemetryMetrics)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param turnContext       Context object containing information for a single
     *                          turn of conversation with a user.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the
     *                          call. This parameter overrides the default
     *                          LuisRecognizerOptions passed in the constructor.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(TurnContext turnContext,
            LuisRecognizerOptions recognizerOptions) {
        return recognizeInternal(turnContext, recognizerOptions, null, null);
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param dialogContext     Context object containing information for a single
     *                          turn of conversation with a user.
     * @param activity          Activity to recognize.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the
     *                          call. This parameter overrides the default
     *                          LuisRecognizerOptions passed in the constructor.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(DialogContext dialogContext, Activity activity,
            LuisRecognizerOptions recognizerOptions) {
        return recognizeInternal(dialogContext, activity, recognizerOptions, null, null);
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param turnContext       Context object containing information for a single
     *                          turn of conversation with a user.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the
     *                          call. This parameter overrides the default
     *                          LuisRecognizerOptions passed in the constructor.
     * @param <T>               type of result.
     * @param c                 RecognizerConvert implemented class to convert the
     *                          Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(TurnContext turnContext,
            LuisRecognizerOptions recognizerOptions, Class<T> c) {
        return recognizeInternal(turnContext, recognizerOptions, null, null)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param dialogContext     Context object containing information for a single
     *                          turn of conversation with a user.
     * @param activity          Activity to recognize.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the
     *                          call. This parameter overrides the default
     *                          LuisRecognizerOptions passed in the constructor.
     * @param <T>               Type of result.
     * @param c                 RecognizerConvert implemented class to convert the
     *                          Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(DialogContext dialogContext, Activity activity,
            LuisRecognizerOptions recognizerOptions, Class<T> c) {
        return recognizeInternal(dialogContext, activity, recognizerOptions, null, null)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param turnContext         Context object containing information for a single
     *                            turn of conversation with a user.
     * @param recognizerOptions   LuisRecognizerOptions instance to be used by the
     *                            call. This parameter overrides the default
     *                            LuisRecognizerOptions passed in the constructor.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(TurnContext turnContext,
            LuisRecognizerOptions recognizerOptions, Map<String, String> telemetryProperties,
            Map<String, Double> telemetryMetrics) {
        return recognizeInternal(turnContext, recognizerOptions, telemetryProperties, telemetryMetrics);
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param dialogContext       Context object containing information for a single
     *                            turn of conversation with a user.
     * @param activity            Activity to recognize.
     * @param recognizerOptions   A LuisRecognizerOptions instance to be used by the
     *                            call. This parameter overrides the default
     *                            LuisRecognizerOptions passed in the constructor.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(DialogContext dialogContext, Activity activity,
            LuisRecognizerOptions recognizerOptions, Map<String, String> telemetryProperties,
            Map<String, Double> telemetryMetrics) {
        return recognizeInternal(dialogContext, activity, recognizerOptions, telemetryProperties, telemetryMetrics);
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param turnContext         Context object containing information for a single
     *                            turn of conversation with a user.
     * @param recognizerOptions   A LuisRecognizerOptions instance to be used by the
     *                            call. This parameter overrides the default
     *                            LuisRecognizerOptions passed in the constructor.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @param <T>                 Type of result.
     * @param c                   RecognizerConvert implemented class to convert the
     *                            Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(TurnContext turnContext,
            LuisRecognizerOptions recognizerOptions, Map<String, String> telemetryProperties,
            Map<String, Double> telemetryMetrics, Class<T> c) {
        return recognizeInternal(turnContext, recognizerOptions, telemetryProperties, telemetryMetrics)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed
     * recognizer result.
     *
     * @param dialogContext       Context object containing information for a single
     *                            turn of conversation with a user.
     * @param activity            Activity to recognize.
     * @param recognizerOptions   LuisRecognizerOptions instance to be used by the
     *                            call. This parameter overrides the default
     *                            LuisRecognizerOptions passed in the constructor.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @param <T>                 Type of result.
     * @param c                   RecognizerConvert implemented class to convert the
     *                            Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(DialogContext dialogContext, Activity activity,
            LuisRecognizerOptions recognizerOptions, Map<String, String> telemetryProperties,
            Map<String, Double> telemetryMetrics, Class<T> c) {
        return recognizeInternal(dialogContext, activity, recognizerOptions, telemetryProperties, telemetryMetrics)
                .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    /**
     * Invoked prior to a LuisResult being logged.
     *
     * @param recognizerResult    The Luis Results for the call.
     * @param turnContext         Context object containing information for a single
     *                            turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     */
    public void onRecognizerResult(RecognizerResult recognizerResult, TurnContext turnContext,
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics) {
        Map<String, String> properties = fillLuisEventProperties(recognizerResult, turnContext,
                telemetryProperties);
        // Track the event
        this.getTelemetryClient().trackEvent(LuisTelemetryConstants.LUIS_RESULT, properties, telemetryMetrics);
    }

    /**
     * Fills the event properties for LuisResult event for telemetry.
     * These properties are logged when the recognizer is called.
     *
     * @param recognizerResult Last activity sent from user.
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
     * @return
     */
    private Map<String, String> fillLuisEventProperties(RecognizerResult recognizerResult, TurnContext turnContext,
            Map<String, String> telemetryProperties) {

        Map<String, IntentScore> sortedIntents = sortIntents(recognizerResult);
        ArrayList<String> topTwoIntents = new ArrayList<>();
        Iterator<Map.Entry<String, IntentScore>> iterator = sortedIntents.entrySet().iterator();
        int intentCounter = 0;
        while (iterator.hasNext() && intentCounter < 2) {
            intentCounter++;
            Map.Entry<String, IntentScore> intent = iterator.next();
            topTwoIntents.add(intent.getKey());
        }

        // Add the intent score and conversation id properties
        Map<String, String> properties = new HashMap<>();
        properties.put(LuisTelemetryConstants.APPLICATION_ID_PROPERTY,
                luisRecognizerOptions.getApplication().getApplicationId());
        properties.put(LuisTelemetryConstants.INTENT_PROPERTY, topTwoIntents.size() > 0 ? topTwoIntents.get(0) : "");
        properties.put(LuisTelemetryConstants.INTENT_SCORE_PROPERTY,
                topTwoIntents.size() > 0 ? "" + recognizerResult.getIntents().get(topTwoIntents.get(0)).getScore()
                        : "0.00");
        properties.put(LuisTelemetryConstants.INTENT_2_PROPERTY, topTwoIntents.size() > 1 ? topTwoIntents.get(1) : "");
        properties.put(LuisTelemetryConstants.INTENT_SCORE_2_PROPERTY,
                topTwoIntents.size() > 1 ? "" + recognizerResult.getIntents().get(topTwoIntents.get(1)).getScore()
                        : "0.00");
        properties.put(LuisTelemetryConstants.FROM_ID_PROPERTY, turnContext.getActivity().getFrom().getId());

        if (recognizerResult.getProperties().containsKey("sentiment")) {
            JsonNode sentiment = recognizerResult.getProperties().get("sentiment");
            if (sentiment.has("label")) {
                properties.put(LuisTelemetryConstants.SENTIMENT_LABEL_PROPERTY, sentiment.get("label").textValue());
            }

            if (sentiment.has("score")) {
                properties.put(LuisTelemetryConstants.SENTIMENT_SCORE_PROPERTY, sentiment.get("score").textValue());
            }
        }

        properties.put(LuisTelemetryConstants.ENTITIES_PROPERTY, recognizerResult.getEntities().toString());

        // Use the LogPersonalInformation flag to toggle logging PII data, text is a
        // common example
        if (isLogPersonalInformation() && StringUtils.isNotBlank(turnContext.getActivity().getText())) {
            properties.put(LuisTelemetryConstants.QUESTION_PROPERTY, turnContext.getActivity().getText());
        }

        // Additional Properties can override "stock" properties.
        if (telemetryProperties == null) {
            telemetryProperties = new HashMap<>();
        }

        properties.putAll(telemetryProperties);

        return properties;
    }

    private <T extends RecognizerConvert> T convertRecognizerResult(RecognizerResult recognizerResult, Class<T> clazz) {
        T result;
        try {
            result = clazz.newInstance();
            result.convert(recognizerResult);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(
                    String.format("Exception thrown when converting " + "Recognizer Result to strongly typed: %s : %s",
                            clazz.getName(), e.getMessage()));
        }
        return result;
    }

    /**
     * Returns a RecognizerResult object. This method will call the internal
     * recognize implementation of the Luis Recognizer Options.
     */
    private CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext,
            LuisRecognizerOptions options, Map<String, String> telemetryProperties,
            Map<String, Double> telemetryMetrics) {
        LuisRecognizerOptions predictionOptionsToRun = options == null ? luisRecognizerOptions : options;
        return predictionOptionsToRun.recognizeInternal(turnContext).thenApply(recognizerResult -> {
            onRecognizerResult(recognizerResult, turnContext, telemetryProperties, telemetryMetrics);
            return recognizerResult;
        });
    }

    /**
     * Returns a RecognizerResult object. This method will call the internal
     * recognize implementation of the Luis Recognizer Options.
     */
    private CompletableFuture<RecognizerResult> recognizeInternal(DialogContext dialogContext, Activity activity,
            LuisRecognizerOptions options, Map<String, String> telemetryProperties,
            Map<String, Double> telemetryMetrics) {
        LuisRecognizerOptions predictionOptionsToRun = options == null ? luisRecognizerOptions : options;
        return predictionOptionsToRun.recognizeInternal(dialogContext, activity).thenApply(recognizerResult -> {
            onRecognizerResult(recognizerResult, dialogContext.getContext(), telemetryProperties, telemetryMetrics);
            return recognizerResult;
        });
    }

    private Map<String, IntentScore> sortIntents(RecognizerResult recognizerResult) {
        Map<String, IntentScore> sortedIntents = new LinkedHashMap<>();
        recognizerResult.getIntents().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(IntentScore::getScore).reversed()))
                .forEachOrdered(x -> sortedIntents.put(x.getKey(), x.getValue()));
        return sortedIntents;
    }
}
