// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/**
 * Error handler that can catch exceptions in the middleware or application.
 */
@FunctionalInterface
public interface OnTurnErrorHandler {
    /**
     * Error handler that can catch exceptions in the middleware or application.
     *
     * @param turnContext The context object for this turn.
     * @param exception   The exception thrown.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> invoke(TurnContext turnContext, Throwable exception);
}
