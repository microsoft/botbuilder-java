package com.microsoft.bot.ai.luis;

import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import okhttp3.OkHttpClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LuisRecognizer extends TelemetryRecognizer {

    public final String LUIS_TRACE_TYPE = "https://www.luis.ai/schemas/trace";

    public final String LUIS_TRACE_LABEL = "Luis Trace";

    private  LuisRecognizerOptions luisRecognizerOptions;

    private OkHttpClient httpClient;

    public LuisRecognizer (LuisRecognizerOptions recognizerOptions) {
        this(recognizerOptions, new OkHttpClient());
    }

    public LuisRecognizer (LuisRecognizerOptions recognizerOptions, OkHttpClient clientHandler) {
        this.httpClient = clientHandler;
        this.luisRecognizerOptions = recognizerOptions;
        //Set Telemetry Client
        this.setLogPersonalInformation(recognizerOptions.logPersonalInformation);
    }

    @Override
    CompletableFuture<RecognizerResult> recognize(TurnContext turnContext, Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics) {
        return null;
    }

    @Override
    public CompletableFuture<RecognizerResult> recognize(TurnContext turnContext) {
        return null;
    }

    private CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext) {
        return luisRecognizerOptions.recognizeInternal(turnContext, this.httpClient);
    }
}
