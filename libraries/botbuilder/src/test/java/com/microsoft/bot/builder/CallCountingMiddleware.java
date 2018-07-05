package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;

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
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception {
        return CompletableFuture.runAsync(() -> {
            this.calls++;
            try {
                await(next.next());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("CallCountingMiddleWare: %s", e.toString()));
            }


        });
    }
}
