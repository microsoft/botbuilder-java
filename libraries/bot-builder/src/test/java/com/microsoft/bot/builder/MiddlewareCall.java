// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MiddlewareCall {
    CompletableFuture<Void> requestHandler(TurnContext tc, NextDelegate nd);
}
