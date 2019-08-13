package com.microsoft.bot.builder;

public class CallCountingMiddleware implements Middleware {
    private int calls = 0;

    public int calls() {
        return this.calls;
    }

    public CallCountingMiddleware withCalls(int calls) {
        this.calls = calls;
        return this;
    }


    @Override
    public void OnTurn(TurnContext context, NextDelegate next) throws Exception {
        this.calls++;
        try {
            next.next();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("CallCountingMiddleWare: %s", e.toString()));
        }


    }
}
