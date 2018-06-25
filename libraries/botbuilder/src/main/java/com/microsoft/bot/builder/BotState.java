// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.NextDelegate;
import com.microsoft.bot.builder.ServiceKeyAlreadyRegisteredException;
import com.microsoft.bot.builder.StateSettings;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.StoreItem;
import com.microsoft.bot.builder.TurnContext;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/// <summary>
/// Abstract Base class which manages details of automatic loading and saving of bot state.
/// </summary>
/// <typeparam name="TState">The type of the bot state object.</typeparam>
//public class BotState<TState> : IMiddleware
//    where TState : class, new()
public class BotState<TState> implements Middleware
{

    private final StateSettings _settings;
    private final Storage _storage;
    private final Function<TurnContext, String> _keyDelegate;
    private final String _propertyName;
    private final Supplier<? extends TState> _ctor;

    /// <summary>
    /// Creates a new <see cref="BotState{TState}"/> middleware object.
    /// </summary>
    /// <param name="name">The name to use to load or save the state object.</param>
    /// <param name="storage">The storage provider to use.</param>
    /// <param name="settings">The state persistance options to use.</param>
    public BotState(Storage storage, String propertyName, Function<TurnContext, String> keyDelegate, Supplier<? extends TState> ctor) {
        this(storage, propertyName, keyDelegate, ctor, null);
    }
    public BotState(Storage storage, String propertyName, Function<TurnContext, String> keyDelegate, Supplier<? extends TState> ctor, StateSettings settings) {
        if (null == storage) {
            throw new IllegalArgumentException("Storage");
        }
        if (null == propertyName) {
            throw new IllegalArgumentException("String propertyName");
        }
        if (null == keyDelegate) {
            throw new IllegalArgumentException("Key Delegate");
        }
        if (null == ctor) {
            throw new IllegalArgumentException("ctor");
        }
        _ctor = ctor;
        _storage = storage;
        _propertyName = propertyName;
        _keyDelegate = keyDelegate;
        if (null == settings)
            _settings = new StateSettings();
        else
            _settings = settings;
    }


    /// <summary>
    /// Processess an incoming activity.
    /// </summary>
    /// <param name="context">The context object for this turn.</param>
    /// <param name="next">The delegate to call to continue the bot middleware pipeline.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>This middleware loads the state object on the leading edge of the middleware pipeline
    /// and persists the state object on the trailing edge.
    /// </remarks>
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception, ServiceKeyAlreadyRegisteredException {
        await(ReadToContextService(context));
        await(next.next());
        await(WriteFromContextService(context));
        return completedFuture(null);
    }

    protected CompletableFuture ReadToContextService(TurnContext context) throws ServiceKeyAlreadyRegisteredException, IllegalArgumentException, JsonProcessingException {
        String key = this._keyDelegate.apply(context);
        Map<String, ?> items = await(_storage.Read(new String[] { key }));
        TState state = StreamSupport.stream(items.entrySet().spliterator(), false)
                .filter(entry -> entry.getKey() == key)
                .map(Map.Entry::getValue)
                .map(entry -> (TState) entry)
                .findFirst()
                .orElse(null);



        //var state = items.Where(entry => entry.Key == key).Select(entry => entry.Value).OfType<TState>().FirstOrDefault();
        if (state == null)
            state = _ctor.get();
        context.getServices().Add(this._propertyName, state);
        return completedFuture(null);
    }

    protected CompletableFuture WriteFromContextService(TurnContext context) throws Exception {
        TState state = context.getServices().Get(this._propertyName);
        return completedFuture(await(Write(context, state)));
    }

    /// <summary>
    /// Reads state from storage.
    /// </summary>
    /// <typeparam name="TState">The type of the bot state object.</typeparam>
    /// <param name="context">The context object for this turn.</param>
    public CompletableFuture<TState> Read(TurnContext context) throws JsonProcessingException {
        String key = this._keyDelegate.apply(context);
        Map<String, ?> items = await( _storage.Read(new String[] { key }));
        TState state = StreamSupport.stream(items.entrySet().spliterator(), false)
                .filter(item -> item.getKey() == key)
                .map(Map.Entry::getValue)
                .map(item -> (TState) item)
                .findFirst()
                .orElse(null);
        //var state = items.Where(entry => entry.Key == key).Select(entry => entry.Value).OfType<TState>().FirstOrDefault();
        if (state == null)
            state = _ctor.get();
        return completedFuture(state);
    }

    /// <summary>
    /// Writes state to storage.
    /// </summary>
    /// <param name="context">The context object for this turn.</param>
    /// <param name="state">The state object.</param>
    public CompletableFuture Write(TurnContext context, TState state) throws Exception {
        HashMap<String, TState> changes = new HashMap<String, TState>();
        //List<Map.Entry<String, ?>> changes = new ArrayList<Map.Entry<String, ?>>();

        if (state == null)
            state = _ctor.get();
        String key = _keyDelegate.apply(context);
        changes.put(key, state);

        if (this._settings.getLastWriterWins()) {
            for (Map.Entry<String, ?> item  : changes.entrySet()) {
                if (item.getValue() instanceof StoreItem) {
                    StoreItem valueStoreItem = (StoreItem) item.getValue();
                    valueStoreItem.seteTag("*");
                }
            }
        }
        return completedFuture(await(_storage.Write(changes)));
    }
}



