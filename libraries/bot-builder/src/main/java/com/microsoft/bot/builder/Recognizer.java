package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

public interface Recognizer {
    CompletableFuture<RecognizerResult> recognizeAsync(TurnContext turnContext);
}
