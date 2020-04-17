// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a bot that can operate on incoming activities.
 */
public interface Bot {
    /**
     * When implemented in a bot, handles an incoming activity.
     *
     * @param turnContext The context object for this turn. Provides information
     *                    about the incoming activity, and other data needed to
     *                    process the activity.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> onTurn(TurnContext turnContext);
}
