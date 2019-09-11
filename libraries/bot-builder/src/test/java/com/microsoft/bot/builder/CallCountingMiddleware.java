// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        this.calls++;
        return next.next();
    }
}
