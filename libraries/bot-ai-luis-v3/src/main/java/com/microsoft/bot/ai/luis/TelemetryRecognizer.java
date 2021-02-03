package com.microsoft.bot.ai.luis;

import com.microsoft.bot.builder.Recognizer;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class TelemetryRecognizer implements Recognizer {

    public boolean isLogPersonalInformation() {
        return logPersonalInformation;
    }

    protected void setLogPersonalInformation(boolean logPersonalInformation) {
        this.logPersonalInformation = logPersonalInformation;
    }

    private boolean logPersonalInformation;

    // Missing Bot Telemetry
    // BotTelemetryClient TelemetryClient  = null;

    abstract CompletableFuture<RecognizerResult>  recognize(TurnContext turnContext, Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics);

}
