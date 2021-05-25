// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.schema.Activity;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract class to enforce the Strategy pattern consumed by the Luis
 * Recognizer through the options selected.
 *
 */
public abstract class LuisRecognizerOptions {

    /**
     * Initializes an instance of the LuisRecognizerOptions implementation.
     *
     * @param application An instance of LuisApplication".
     */
    protected LuisRecognizerOptions(LuisApplication application) {
        if (application == null) {
            throw new IllegalArgumentException("Luis Application may not be null");
        }
        this.application = application;
    }

    /**
     * Luis Application instance.
     */
    private LuisApplication application;

    /**
     * Bot Telemetry Client instance.
     */
    private BotTelemetryClient telemetryClient = new NullBotTelemetryClient();

    /**
     * Controls if personal information should be sent as telemetry.
     */
    private boolean logPersonalInformation = false;

    /**
     * Controls if full results from the LUIS API should be returned with the
     * recognizer result.
     */
    private boolean includeAPIResults = false;

    /**
     * Gets the Luis Application instance.
     *
     * @return The Luis Application instance used with this Options.
     */
    public LuisApplication getApplication() {
        return application;
    }

    /**
     * Gets the currently configured Bot Telemetry Client that logs the LuisResult
     * event.
     *
     * @return The Bot Telemetry Client.
     */
    public BotTelemetryClient getTelemetryClient() {
        return telemetryClient;
    }

    /**
     * Sets the Bot Telemetry Client to log telemetry with.
     *
     * @param telemetryClient A Bot Telemetry Client instance
     */
    public void setTelemetryClient(BotTelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
    }

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
    public void setLogPersonalInformation(boolean logPersonalInformation) {
        this.logPersonalInformation = logPersonalInformation;
    }

    /**
     * Indicates if full results from the LUIS API should be returned with the
     * recognizer result.
     *
     * @return boolean value showing preference on LUIS API full response added to
     *         recognizer result.
     */
    public boolean isIncludeAPIResults() {
        return includeAPIResults;
    }

    /**
     * Indicates if full results from the LUIS API should be returned with the
     * recognizer result.
     *
     * @param includeAPIResults to set full Luis API response to be added to the
     *                          recognizer result.
     */
    public void setIncludeAPIResults(boolean includeAPIResults) {
        this.includeAPIResults = includeAPIResults;
    }

    /**
     * Implementation of the Luis API http call and result processing. This is
     * intended to follow a Strategy pattern and should only be consumed through the
     * LuisRecognizer class.
     *
     * @param turnContext used to extract the text utterance to be sent to Luis.
     * @return Recognizer Result populated by the Luis response.
     */
    abstract CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext);

    /**
     * Implementation of the Luis API http call and result processing. This is
     * intended to follow a Strategy pattern and should only be consumed through the
     * LuisRecognizer class.
     *
     * @param context  Dialog Context to extract turn context.
     * @param activity to extract the text utterance to be sent to Luis.
     * @return Recognizer Result populated by the Luis response.
     */
    abstract CompletableFuture<RecognizerResult> recognizeInternal(DialogContext context, Activity activity);
}
