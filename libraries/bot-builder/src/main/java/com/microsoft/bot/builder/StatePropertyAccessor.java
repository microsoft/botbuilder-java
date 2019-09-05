package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface StatePropertyAccessor<T> extends StatePropertyInfo {
    <T extends Object, S>  CompletableFuture<T> getAsync(TurnContext turnContext, Supplier<S> defaultValueFactory);

    CompletableFuture<Void> deleteAsync(TurnContext turnContext);

    CompletableFuture<Void> setAsync(TurnContext turnContext, T value);
}
