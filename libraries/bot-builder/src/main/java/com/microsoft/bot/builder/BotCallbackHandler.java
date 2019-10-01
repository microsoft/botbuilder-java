// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/**
 * The callback delegate for application code.
 */
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
