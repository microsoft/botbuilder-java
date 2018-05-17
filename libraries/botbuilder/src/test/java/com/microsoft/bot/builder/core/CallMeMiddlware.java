package com.microsoft.bot.builder.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CallMeMiddlware implements Middleware
{
    private ActionDel _callMe;

    public CallMeMiddlware(ActionDel callme)
    {
        _callMe = callme;
    }

    @Override
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws ExecutionException, InterruptedException {
        _callMe.CallMe();
        return next.next();
    }
}
