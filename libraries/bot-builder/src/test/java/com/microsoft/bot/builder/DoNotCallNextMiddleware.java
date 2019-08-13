package com.microsoft.bot.builder;

public class DoNotCallNextMiddleware implements Middleware {
    private final ActionDel _callMe;

    public DoNotCallNextMiddleware(ActionDel callMe) {
        _callMe = callMe;
    }

    public void OnTurn(TurnContext context, NextDelegate next) {
        _callMe.CallMe();
        // DO NOT call NEXT
    }
}
