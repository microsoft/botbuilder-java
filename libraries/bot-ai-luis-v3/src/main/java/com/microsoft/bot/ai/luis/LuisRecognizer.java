// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.RecognizerConvert;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LuisRecognizer extends TelemetryRecognizer {
    /**
     * Luis Recognizer options to query the Luis Service.
     */
    private  LuisRecognizerOptions luisRecognizerOptions;

    public LuisRecognizer (LuisRecognizerOptions recognizerOptions) {
        this.luisRecognizerOptions = recognizerOptions;
        this.setLogPersonalInformation(recognizerOptions.isLogPersonalInformation());
        this.setTelemetryClient(recognizerOptions.getTelemetryClient());
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     * @param results the Recognizer Result with the list of Intents to filter.
     *                Defaults to a value of "None" and a min score value of `0.0`
     * @return The top scoring intent name.
     */
    public static String topIntent(
        RecognizerResult results) {
        return topIntent(results, "None");
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     * @param results the Recognizer Result with the list of Intents to filter
     * @param defaultIntent Intent name to return should a top intent be found.
     *                      Defaults to a value of "None" and a min score value of `0.0`
     * @return The top scoring intent name.
     */
    public static String topIntent(
        RecognizerResult results,
        String defaultIntent) {
        return topIntent(results, defaultIntent, 0.0);
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     * @param results the Recognizer Result with the list of Intents to filter.
     * @param minScore  Minimum score needed for an intent to be considered as a top intent.
     * @return The top scoring intent name.
     */
    public static String topIntent(
        RecognizerResult results,
        double minScore) {
        return topIntent(results, "None", minScore);
    }

    /**
     * Returns the name of the top scoring intent from a set of LUIS results.
     * @param results the Recognizer Result with the list of Intents to filter
     * @param defaultIntent Intent name to return should a top intent be found. Defaults to a value of "None
     * @param minScore  Minimum score needed for an intent to be considered as a top intent.
     * @return The top scoring intent name.
     */
    public static String topIntent(
        RecognizerResult results,
        String defaultIntent,
        double minScore) {
        if (results == null) {
            throw new IllegalArgumentException("RecognizerResult");
        }

        defaultIntent = (defaultIntent == null || defaultIntent.equals("")) ? "None" : defaultIntent;

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

        return topIntent != null && !topIntent.equals("") ? topIntent : defaultIntent;
    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    @Override
    public CompletableFuture<RecognizerResult> recognize(
        TurnContext turnContext) {
        return recognizeInternal(
            turnContext,
            null,
            null,
            null);
    }
    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Return results of the analysis (Suggested actions and intents).
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public CompletableFuture<RecognizerResult> recognize(
//        DialogContext dialogContext,
//        Activity activity) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            null,
//            null,
//            null);
//    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
        TurnContext turnContext,
        Class<T> c) {
        return recognizeInternal(
            turnContext,
            null,
            null,
            null)
            .thenApply( recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
//        DialogContext dialogContext,
//        Activity activity,
//        Class<T> c) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            null,
//            null,
//            null)
//            .thenApply( recognizerResult -> convertRecognizerResult(recognizerResult, c));
//    }


    /**
     * Return results of the analysis (Suggested actions and intents).
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    @Override
    public CompletableFuture<RecognizerResult> recognize(
        TurnContext turnContext,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics) {
        return recognizeInternal(
            turnContext,
            null,
            telemetryProperties,
            telemetryMetrics);
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Return results of the analysis (Suggested actions and intents).
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
//     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public CompletableFuture<RecognizerResult> recognize(
//        DialogContext dialogContext,
//        Activity activity,
//        Map<String, String> telemetryProperties,
//        Map<String, Double> telemetryMetrics) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            null,
//            telemetryProperties,
//            telemetryMetrics);
//    }



    /**
     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
        TurnContext turnContext,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics,
        Class<T> c) {
        return recognizeInternal(
            turnContext,
            null,
            telemetryProperties,
            telemetryMetrics)
            .thenApply( recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
//     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
//     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
//        DialogContext dialogContext,
//        Activity activity,
//        Map<String, String> telemetryProperties,
//        Map<String, Double> telemetryMetrics,
//        Class<T> c) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            null,
//            telemetryProperties,
//            telemetryMetrics)
//            .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
//    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
     *                          default LuisRecognizerOptions passed in the constructor.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(
        TurnContext turnContext,
        LuisRecognizerOptions recognizerOptions) {
        return recognizeInternal(
            turnContext,
            recognizerOptions,
            null,
            null);
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Return results of the analysis (Suggested actions and intents).
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
//     *                          default LuisRecognizerOptions passed in the constructor.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public CompletableFuture<RecognizerResult> recognize(
//        DialogContext dialogContext,
//        Activity activity,
//        LuisRecognizerOptions recognizerOptions) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            recognizerOptions,
//            null,
//            null);
//    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
     *                          default LuisRecognizerOptions passed in the constructor.
     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
        TurnContext turnContext,
        LuisRecognizerOptions recognizerOptions,
        Class<T> c) {
        return recognizeInternal(
            turnContext,
            recognizerOptions,
            null,
            null)
            .thenApply( recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
//     *                          default LuisRecognizerOptions passed in the constructor.
//     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
//        DialogContext dialogContext,
//        Activity activity,
//        LuisRecognizerOptions recognizerOptions,
//        Class<T> c) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            recognizerOptions,
//            null,
//            null)
//            .thenApply( recognizerResult -> convertRecognizerResult(recognizerResult, c));
//    }

    /**
     * Return results of the analysis (Suggested actions and intents).
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
     *                          default LuisRecognizerOptions passed in the constructor.
     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    public CompletableFuture<RecognizerResult> recognize(
        TurnContext turnContext,
        LuisRecognizerOptions recognizerOptions,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics) {
        return recognizeInternal(
            turnContext,
            recognizerOptions,
            telemetryProperties,
            telemetryMetrics);
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Return results of the analysis (Suggested actions and intents).
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
//     *                          default LuisRecognizerOptions passed in the constructor.
//     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
//     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public CompletableFuture<RecognizerResult> recognize(
//        DialogContext dialogContext,
//        Activity activity,
//        LuisRecognizerOptions recognizerOptions,
//        Map<String, String> telemetryProperties,
//        Map<String, Double> telemetryMetrics) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            recognizerOptions,
//            telemetryProperties,
//            telemetryMetrics);
//    }

    /**
     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
     *                          default LuisRecognizerOptions passed in the constructor.
     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
     */
    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
        TurnContext turnContext,
        LuisRecognizerOptions recognizerOptions,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics,
        Class<T> c) {
        return recognizeInternal(
            turnContext,
            recognizerOptions,
            telemetryProperties,
            telemetryMetrics)
            .thenApply( recognizerResult -> convertRecognizerResult(recognizerResult, c));
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Runs an utterance through a recognizer and returns a strongly-typed recognizer result
//     * @param dialogContext Context object containing information for a single turn of conversation with a user.
//     * @param activity Activity to recognize.
//     * @param recognizerOptions A LuisRecognizerOptions instance to be used by the call. This parameter overrides the
//     *                          default LuisRecognizerOptions passed in the constructor.
//     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
//     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
//     * @param c RecognizerConvert implemented class to convert the Recognizer Result into.
//     * @return The LUIS results of the analysis of the current message text in the current turn's context activity.
//     */
//    public <T extends RecognizerConvert> CompletableFuture<T> recognize(
//        DialogContext dialogContext,
//        Activity activity,
//        LuisRecognizerOptions recognizerOptions,
//        Map<String, String> telemetryProperties,
//        Map<String, Double> telemetryMetrics,
//        Class<T> c) {
//        return recognizeInternal(
//            dialogContext,
//            activity,
//            recognizerOptions,
//            telemetryProperties,
//            telemetryMetrics)
//            .thenApply(recognizerResult -> convertRecognizerResult(recognizerResult, c));
//    }

    /**
     * Invoked prior to a LuisResult being logged.
     * @param recognizerResult The Luis Results for the call.
     * @param turnContext Context object containing information for a single turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry with the LuisResult event.
     * @param telemetryMetrics Additional metrics to be logged to telemetry with the LuisResult event.
     */
    public void onRecognizerResult(
        RecognizerResult recognizerResult,
        TurnContext turnContext,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics) {
        Map<String, String> properties =  fillLuisEventPropertiesAsync(
            recognizerResult,
            turnContext,
            telemetryProperties);
        // Track the event
        this.getTelemetryClient().trackEvent(
            LuisTelemetryConstants.LuisResult,
            properties,
            telemetryMetrics);
    }

    protected Map<String, String> fillLuisEventPropertiesAsync(
        RecognizerResult recognizerResult,
        TurnContext turnContext,
        Map<String, String> telemetryProperties) {

        Map<String, IntentScore> sortedIntents = sortIntents(recognizerResult);
        ArrayList<String> topTwoIntents = new ArrayList<>();
        Iterator<Map.Entry<String, IntentScore>> iterator = sortedIntents.entrySet().iterator();
        int intentCounter = 0;
        while(iterator.hasNext()
            && intentCounter < 2){
            intentCounter++;
            Map.Entry<String, IntentScore> intent = iterator.next();
            topTwoIntents.add(intent.getKey());
        }

        // Add the intent score and conversation id properties
        Map<String, String> properties = new HashMap<>();
        properties.put(
            LuisTelemetryConstants.ApplicationIdProperty,
            luisRecognizerOptions.getApplication().getApplicationId());
        properties.put(
            LuisTelemetryConstants.IntentProperty,
            topTwoIntents.size() > 0 ? topTwoIntents.get(0) : "");
        properties.put(
            LuisTelemetryConstants.IntentScoreProperty,
            topTwoIntents.size() > 0 ? "" + recognizerResult.getIntents().get(topTwoIntents.get(0)).getScore() : "0.00" );
        properties.put(
            LuisTelemetryConstants.Intent2Property,
            topTwoIntents.size() > 1 ? topTwoIntents.get(1) : "");
        properties.put(
            LuisTelemetryConstants.IntentScore2Property,
            topTwoIntents.size() > 1 ? "" + recognizerResult.getIntents().get(topTwoIntents.get(1)).getScore() : "0.00");
        properties.put(
            LuisTelemetryConstants.FromIdProperty, turnContext.getActivity().getFrom().getId());

        if (recognizerResult.getProperties().containsKey("sentiment")) {
            JsonNode sentiment = recognizerResult.getProperties().get("sentiment");
            if (sentiment.has("label")) {
                properties.put(
                    LuisTelemetryConstants.SentimentLabelProperty,
                    sentiment.get("label").textValue());
            }

            if (sentiment.has("score")) {
                properties.put(
                    LuisTelemetryConstants.SentimentScoreProperty,
                    sentiment.get("score").textValue());
            }
        }

        properties.put(
            LuisTelemetryConstants.EntitiesProperty,
            recognizerResult.getEntities().toString());

        // Use the LogPersonalInformation flag to toggle logging PII data, text is a common example
        if (isLogPersonalInformation()
            && turnContext.getActivity().getText() != null
            && !turnContext.getActivity().getText().equals("")) {
            properties.put(
                LuisTelemetryConstants.QuestionProperty,
                turnContext.getActivity().getText());
        }

        // Additional Properties can override "stock" properties.
        if (telemetryProperties == null) {
            telemetryProperties = new HashMap<>();
        }

        properties.putAll(telemetryProperties);

        return properties;
    }

    private <T extends RecognizerConvert> T convertRecognizerResult(RecognizerResult recognizerResult, Class<T> clazz) {
        T result = null;
        try {
            result = clazz.newInstance();
            result.convert(recognizerResult);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns a RecognizerResult object. This method will call the internal recognize implementation of the
     * Luis Recognizer Options.
     */
    private CompletableFuture<RecognizerResult> recognizeInternal(
        TurnContext turnContext,
        LuisRecognizerOptions options,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics) {
        LuisRecognizerOptions predictionOptionsToRun = options == null ? luisRecognizerOptions : options;
        return predictionOptionsToRun.recognizeInternal(turnContext)
            .thenApply(recognizerResult -> {
                onRecognizerResult(
                    recognizerResult,
                    turnContext,
                    telemetryProperties,
                    telemetryMetrics);
                return recognizerResult;
            });
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    /**
//     * Returns a RecognizerResult object. This method will call the internal recognize implementation of the
//     * Luis Recognizer Options.
//     */
//    private CompletableFuture<RecognizerResult> recognizeInternal(
//        DialogContext dialogContext,
//        Activity activity,
//        LuisRecognizerOptions options,
//        Map<String, String> telemetryProperties,
//        Map<String, Double> telemetryMetrics) {
//        LuisRecognizerOptions predictionOptionsToRun = options == null ? luisRecognizerOptions : options;
//        return predictionOptionsToRun.recognizeInternal(
//            dialogContext,
//            activity)
//            .thenApply(recognizerResult -> {
//                onRecognizerResult(
//                    recognizerResult,
//                    dialogContext.getContext(),
//                    telemetryProperties,
//                    telemetryMetrics);
//                return recognizerResult;
//            });
//    }

    private Map<String, IntentScore> sortIntents(RecognizerResult recognizerResult){
        Map<String, IntentScore> sortedIntents = new LinkedHashMap<>();
        recognizerResult.getIntents().entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(IntentScore::getScore).reversed()))
            .forEachOrdered(x -> sortedIntents.put(x.getKey(), x.getValue()));
        return sortedIntents;
    }
}
