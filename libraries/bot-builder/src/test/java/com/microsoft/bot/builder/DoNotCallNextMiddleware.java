// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

public class DoNotCallNextMiddleware implements Middleware {
    private final ActionDel _callMe;

    public DoNotCallNextMiddleware(ActionDel callMe) {
        _callMe = callMe;
    }

    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        _callMe.CallMe();
        // DO NOT call NEXT
        return null;
    }
}
