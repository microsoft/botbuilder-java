// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Abstract Base class which manages details of automatic loading and saving of bot state.
 *
 * @param TState The type of the bot state object.
 */
//public class BotState<TState> : Middleware
//    where TState : class, new()
public class BotState<TState> implements Middleware {

    private final StateSettings settings;
    private final Storage storage;
    private final Function<TurnContext, String> keyDelegate;
    private final String propertyName;
    private final Supplier<? extends TState> ctor;

    /**
     * Creates a new {@link BotState{TState}} middleware object.
     *
     * @param name     The name to use to load or save the state object.
     * @param storage  The storage provider to use.
     * @param settings The state persistance options to use.
     */
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
        this.ctor = ctor;
        this.storage = storage;
        this.propertyName = propertyName;
        this.keyDelegate = keyDelegate;
        if (null == settings)
            this.settings = new StateSettings();
        else
            this.settings = settings;
    }


    /**
     * Processess an incoming activity.
     *
     * @param context The context object for this turn.
     * @param next    The delegate to call to continue the bot middleware pipeline.
     * @return A task that represents the work queued to execute.
     * This middleware loads the state object on the leading edge of the middleware pipeline
     * and persists the state object on the trailing edge.
     */
    public void OnTurn(TurnContext context, NextDelegate next) throws Exception {
        ReadToContextService(context);
        next.next();
        WriteFromContextService(context).join();
        return;
    }

    protected void ReadToContextService(TurnContext context) throws IllegalArgumentException, JsonProcessingException {
        String key = this.keyDelegate.apply(context);
        Map<String, ?> items = null;
        try {
            CompletableFuture<Map<String, ?>> result = storage.Read(new String[]{key});
            items = result.get();
            System.out.println(String.format("BotState:OnTurn(tid:%s) ReadToContextService: Found %s items", Thread.currentThread().getId(), items.size()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Error waiting context storage read: %s", e.toString()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        TState state = StreamSupport.stream(items.entrySet().spliterator(), false)
                .filter(entry -> entry.getKey() == key)
                .map(Map.Entry::getValue)
                .map(entry -> (TState) entry)
                .findFirst()
                .orElse(null);


        //var state = items.Where(entry => entry.Key == key).Select(entry => entry.Value).OfType<TState>().FirstOrDefault();
        if (state == null)
            state = ctor.get();
        context.getServices().Add(this.propertyName, state);
    }

    protected CompletableFuture WriteFromContextService(TurnContext context) throws Exception {
        TState state = context.getServices().Get(this.propertyName);
        return Write(context, state);
    }

    /**
     * Reads state from storage.
     *
     * @param TState  The type of the bot state object.
     * @param context The context object for this turn.
     */
    public CompletableFuture<TState> Read(TurnContext context) throws JsonProcessingException {
        String key = this.keyDelegate.apply(context);
        Map<String, ?> items = storage.Read(new String[]{key}).join();
        TState state = StreamSupport.stream(items.entrySet().spliterator(), false)
                .filter(item -> item.getKey() == key)
                .map(Map.Entry::getValue)
                .map(item -> (TState) item)
                .findFirst()
                .orElse(null);
        //var state = items.Where(entry => entry.Key == key).Select(entry => entry.Value).OfType<TState>().FirstOrDefault();
        if (state == null)
            state = ctor.get();
        return completedFuture(state);
    }

    /**
     * Writes state to storage.
     *
     * @param context The context object for this turn.
     * @param state   The state object.
     */
    public CompletableFuture Write(TurnContext context, TState state) throws Exception {
        HashMap<String, TState> changes = new HashMap<String, TState>();
        //List<Map.Entry<String, ?>> changes = new ArrayList<Map.Entry<String, ?>>();
        if (state == null)
            state = ctor.get();
        String key = keyDelegate.apply(context);
        changes.put(key, state);

        if (this.settings.getLastWriterWins()) {
            for (Map.Entry<String, ?> item : changes.entrySet()) {
                if (item.getValue() instanceof StoreItem) {
                    StoreItem valueStoreItem = (StoreItem) item.getValue();
                    valueStoreItem.seteTag("*");
                }
            }
        }
        return completedFuture(storage.Write(changes).join());
    }
}



