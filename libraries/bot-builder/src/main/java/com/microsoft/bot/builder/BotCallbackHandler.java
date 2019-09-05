package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface BotCallbackHandler {
    /**
     * The callback delegate for application code.
     *
     * @param turnContext The turn context.
     * @return A Task representing the asynchronous operation.
     */
    CompletableFuture<Void> invoke(TurnContext turnContext);
}
