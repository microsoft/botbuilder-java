// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.

package com.microsoft.bot.builder;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Middleware to automatically call .SaveChanges() at the end of the turn for all BotState class it is managing.
 */
public class AutoSaveStateMiddleware implements Middleware {
    private BotStateSet botStateSet;

    public AutoSaveStateMiddleware(BotState ... botStates) {
        botStateSet = new BotStateSet(Arrays.asList(botStates));
    }

    public AutoSaveStateMiddleware(BotStateSet withBotStateSet) {
        botStateSet = withBotStateSet;
    }

    public BotStateSet getBotStateSet() {
        return botStateSet;
    }

    public void setBotStateSet(BotStateSet withBotStateSet) {
        botStateSet = withBotStateSet;
    }

    public AutoSaveStateMiddleware add(BotState botState) {
        if (botState == null) {
            throw new IllegalArgumentException("botState cannot be null");
        }

        botStateSet.add(botState);
        return this;
    }

    /**
     * Middleware implementation which calls savesChanges automatically at the end of the turn.
     *
     * @param turnContext The context object for this turn.
     * @param next        The delegate to call to continue the bot middleware pipeline.
     * @return A task representing the asynchronous operation.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        return next.next()
            .thenCompose(result -> botStateSet.saveAllChanges(turnContext));
    }
}
