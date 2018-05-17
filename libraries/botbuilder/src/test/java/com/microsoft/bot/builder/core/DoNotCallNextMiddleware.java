package com.microsoft.bot.builder.core;
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
