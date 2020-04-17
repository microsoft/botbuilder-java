// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.

package com.microsoft.bot.builder;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Middleware to automatically call .SaveChanges() at the end of the turn for
 * all BotState class it is managing.
 */
public class AutoSaveStateMiddleware implements Middleware {
    /**
     * The list of state management objects managed by this object.
     */
    private BotStateSet botStateSet;

    /**
     * Initializes a new instance of the AutoSaveStateMiddleware class.
     *
     * @param botStates Initial list of {@link BotState} objects to manage.
     */
    public AutoSaveStateMiddleware(BotState... botStates) {
        botStateSet = new BotStateSet(Arrays.asList(botStates));
    }

    /**
     * Initializes a new instance of the AutoSaveStateMiddleware class.
     *
     * @param withBotStateSet Initial {@link BotStateSet} object to manage.
     */
    public AutoSaveStateMiddleware(BotStateSet withBotStateSet) {
        botStateSet = withBotStateSet;
    }

    /**
     * Gets the list of state management objects managed by this object.
     *
     * @return The state management objects managed by this object.
     */
    public BotStateSet getBotStateSet() {
        return botStateSet;
    }

    /**
     * Gets the list of state management objects managed by this object.
     *
     * @param withBotStateSet The state management objects managed by this object.
     */
    public void setBotStateSet(BotStateSet withBotStateSet) {
        botStateSet = withBotStateSet;
    }

    /**
     * Add a BotState to the list of sources to load.
     *
     * @param botState botState to manage.
     * @return botstateset for chaining more .use().
     */
    public AutoSaveStateMiddleware add(BotState botState) {
        if (botState == null) {
            throw new IllegalArgumentException("botState cannot be null");
        }

        botStateSet.add(botState);
        return this;
    }

    /**
     * Middleware implementation which calls savesChanges automatically at the end
     * of the turn.
     *
     * @param turnContext The context object for this turn.
     * @param next        The delegate to call to continue the bot middleware
     *                    pipeline.
     * @return A task representing the asynchronous operation.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        return next.next().thenCompose(result -> botStateSet.saveAllChanges(turnContext));
    }
}
