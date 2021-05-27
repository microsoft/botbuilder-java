// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.Recognizer;
import com.microsoft.bot.builder.RecognizerConvert;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Telemetry Recognizer to enforce controls and properties on telemetry logged.
 * Recognizer with Telemetry support.
 *
 */
public abstract class TelemetryRecognizer implements Recognizer {

    private boolean logPersonalInformation;

    private BotTelemetryClient telemetryClient;

    /**
     * Indicates if personal information should be sent as telemetry.
     *
     * @return value boolean value to control personal information logging.
     */
    public boolean isLogPersonalInformation() {
        return logPersonalInformation;
    }

    /**
     * Indicates if personal information should be sent as telemetry.
     *
     * @param logPersonalInformation to set personal information logging preference.
     */
    protected void setLogPersonalInformation(boolean logPersonalInformation) {
        this.logPersonalInformation = logPersonalInformation;
    }

    /**
     * Gets the currently configured Bot Telemetry Client that logs the LuisResult
     * event.
     *
     * @return The Bot Telemetry Client.
     */
    protected BotTelemetryClient getTelemetryClient() {
        return telemetryClient;
    }

    /**
     * Sets the currently configured Bot Telemetry Client that logs the LuisResult
     * event.
     *
     * @param telemetryClient Bot Telemetry Client.
     */
    public void setTelemetryClient(BotTelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
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
    abstract CompletableFuture<RecognizerResult> recognize(TurnContext turnContext,
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics);

    /**
     * Return results of the analysis (Suggested actions and intents).
     *
     * @param turnContext         Context object containing information for a single
     *                            turn of conversation with a user.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @param <T>                 Result type.
     * @param c                   The recognition result type class
     * @return The LUIS results of the analysis of the current message text in the
     *         current turn's context activity.
     */
    abstract <T extends RecognizerConvert> CompletableFuture<T> recognize(TurnContext turnContext,
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics, Class<T> c);

}
