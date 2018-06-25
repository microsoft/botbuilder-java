package com.microsoft.bot.builder;
import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.NextDelegate;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.core.ActionDel;

import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class DoNotCallNextMiddleware implements Middleware
{
    private final ActionDel _callMe;
    public DoNotCallNextMiddleware(ActionDel callMe) {
        _callMe = callMe;
    }
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) {
        _callMe.CallMe();
        // DO NOT call NEXT
        return completedFuture(null);
    }
}
