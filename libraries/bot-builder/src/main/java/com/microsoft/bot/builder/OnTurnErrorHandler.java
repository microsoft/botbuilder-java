package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface OnTurnErrorHandler {
    /**
     * Gets or sets an error handler that can catch exceptions in the middleware or application.
     *
     * @param turnContext The context object for this turn.
     * @param exception The exception thrown.
     * @return A task that represents the work queued to execute.
     */
    Void invoke(TurnContext turnContext, Throwable exception);
}
