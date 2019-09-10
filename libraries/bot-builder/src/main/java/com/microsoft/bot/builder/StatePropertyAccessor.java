// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface StatePropertyAccessor<T> extends StatePropertyInfo {
    <T extends Object, S> CompletableFuture<T> get(TurnContext turnContext, Supplier<S> defaultValueFactory);

    CompletableFuture<Void> delete(TurnContext turnContext);

    CompletableFuture<Void> set(TurnContext turnContext, T value);
}
