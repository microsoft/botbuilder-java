// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface StatePropertyAccessor<T> extends StatePropertyInfo {
    default CompletableFuture<T> get(TurnContext turnContext) {
        return get(turnContext, null);
    }

    CompletableFuture<T> get(TurnContext turnContext, Supplier<T> defaultValueFactory);

    CompletableFuture<Void> delete(TurnContext turnContext);

    CompletableFuture<Void> set(TurnContext turnContext, T value);
}
