// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Reads and writes state for your bot to storage.
 */
public abstract class BotState implements PropertyManager {
    private String contextServiceKey;
    private Storage storage;

    /**
     * Initializes a new instance of the BotState class.
     *
     * @param withStorage           The storage provider to use.
     * @param withContextServiceKey The key for caching on the context services dictionary.
     */
    public BotState(Storage withStorage, String withContextServiceKey) {
        if (withStorage == null) {
            throw new IllegalArgumentException("Storage cannot be null");
        }
        storage = withStorage;

        if (StringUtils.isEmpty(withContextServiceKey)) {
            throw new IllegalArgumentException("context service key cannot be empty");
        }
        contextServiceKey = withContextServiceKey;
    }

    /**
     * Create a property definition and register it with this BotState.
     *
     * @param name name of property.
     * @param <T>  type of property.
     * @return The created state property accessor.
     */
    public <T extends Object> StatePropertyAccessor<T> createProperty(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        return new BotStatePropertyAccessor<>(this, name);
    }

    /**
     * Reads in  the current state object and caches it in the context object for this turn.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> load(TurnContext turnContext) {
        return load(turnContext, false);
    }

    /**
     * Reads in  the current state object and caches it in the context object for this turn.
     *
     * @param turnContext The context object for this turn.
     * @param force       True to bypass the cache.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> load(TurnContext turnContext, boolean force) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        String storageKey = getStorageKey(turnContext);
        if (force || cachedState == null || cachedState.getState() == null) {
            return storage.read(new String[]{storageKey})
                .thenApply(val -> {
                    turnContext.getTurnState().put(contextServiceKey, new CachedBotState(val));
                    return null;
                });
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * If it has changed, writes to storage the state object that is cached in the current
     * context object for this turn.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> saveChanges(TurnContext turnContext) {
        return saveChanges(turnContext, false);
    }

    /**
     * If it has changed, writes to storage the state object that is cached in the current
     * context object for this turn.
     *
     * @param turnContext The context object for this turn.
     * @param force       True to save state to storage whether or not there are changes.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> saveChanges(TurnContext turnContext, boolean force) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        if (force || (cachedState != null && cachedState.isChanged())) {
            String storageKey = getStorageKey(turnContext);
            Map<String, Object> changes = new HashMap<String, Object>() {{
                put(storageKey, cachedState.state);
            }};

            return storage.write(changes)
                .thenApply(val -> {
                    cachedState.setHashCode(cachedState.computeHashCode(cachedState.state));
                    return null;
                });
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Clears any state currently stored in this state scope.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> clearState(TurnContext turnContext) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        turnContext.getTurnState().put(contextServiceKey, new CachedBotState(new HashMap<>()));
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Delete any state currently stored in this state scope.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> delete(TurnContext turnContext) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        if (cachedState != null) {
            turnContext.getTurnState().remove(contextServiceKey);
        }

        String storageKey = getStorageKey(turnContext);
        return storage.delete(new String[]{storageKey});
    }

    /**
     * Returns a copy of the raw cached data from the TurnContext, this can be used for tracing scenarios.
     *
     * @param turnContext The context object for this turn.
     * @return A JSON representation of the cached state.
     */
    public JsonNode get(TurnContext turnContext) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        String stateKey = getClass().getName();
        CachedBotState cachedState = turnContext.getTurnState().get(stateKey);
        return new ObjectMapper().valueToTree(cachedState.state);
    }

    /**
     * When overridden in a derived class, gets the key to use when reading and writing state to and from storage.
     *
     * @param turnContext The context object for this turn.
     * @return The storage key.
     */
    public abstract String getStorageKey(TurnContext turnContext);

    /**
     * Gets a property from the state cache in the turn context.
     *
     * @param turnContext  The context object for this turn.
     * @param propertyName The name of the property to get.
     * @param <T>          The property type.
     * @return A task that represents the work queued to execute.
     */
    protected <T> CompletableFuture<T> getPropertyValue(TurnContext turnContext,
                                                             String propertyName) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("propertyName cannot be empty");
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        return (CompletableFuture<T>) CompletableFuture.completedFuture(cachedState.getState().get(propertyName));
    }

    /**
     * Deletes a property from the state cache in the turn context.
     *
     * @param turnContext  The context object for this turn.
     * @param propertyName The name of the property to delete.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> deletePropertyValue(TurnContext turnContext, String propertyName) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("propertyName cannot be empty");
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        cachedState.getState().remove(propertyName);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Set the value of a property in the state cache in the turn context.
     *
     * @param turnContext  The context object for this turn.
     * @param propertyName The name of the property to set.
     * @param value        The value to set on the property.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> setPropertyValue(TurnContext turnContext,
                                                            String propertyName,
                                                            Object value) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("propertyName cannot be empty");
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        cachedState.getState().put(propertyName, value);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Internal cached bot state.
     */
    private static class CachedBotState {
        private Map<String, Object> state;
        private int hash;

        public CachedBotState(Map<String, Object> withState) {
            state = withState;
            hash = computeHashCode(withState);
        }

        public Map<String, Object> getState() {
            return state;
        }

        public void setState(Map<String, Object> withState) {
            state = withState;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        public void setHashCode(int witHashCode) {
            hash = witHashCode;
        }

        public boolean isChanged() {
            return hash != computeHashCode(state);
        }

        public int computeHashCode(Object obj) {
            //TODO: this may not be the same as in dotnet
            return obj.hashCode();
        }
    }

    /**
     * Implements IPropertyAccessor for an IPropertyContainer.
     * <p>
     * Note the semantic of this accessor are intended to be lazy, this means teh Get, Set and Delete
     * methods will first call LoadAsync. This will be a no-op if the data is already loaded.
     * The implication is you can just use this accessor in the application code directly without first calling LoadAsync
     * this approach works with the AutoSaveStateMiddleware which will save as needed at the end of a turn.
     *
     * @param <T> type of value the propertyAccessor accesses.
     */
    private static class BotStatePropertyAccessor<T> implements StatePropertyAccessor<T> {
        private String name;
        private BotState botState;

        public BotStatePropertyAccessor(BotState withState, String withName) {
            botState = withState;
            name = withName;
        }

        /**
         * Get the property value. The semantics are intended to be lazy, note the use of LoadAsync at the start.
         *
         * @param turnContext         The context object for this turn.
         * @param defaultValueFactory Defines the default value. Invoked when no value been set for the requested
         *                            state property.  If defaultValueFactory is defined as null,
         *                            the MissingMemberException will be thrown if the underlying property is not set.
         * @param <T>                 type of value the propertyAccessor accesses.
         * @return A task that represents the work queued to execute.
         */
        @Override
        public <T, S> CompletableFuture<T> get(TurnContext turnContext, Supplier<S> defaultValueFactory) {
            return botState.load(turnContext)
                .thenCombine(botState.getPropertyValue(turnContext, name), (loadResult, value) -> {
                    if (value == null) {
                        value = defaultValueFactory.get();
                    }

                    return (T) value;
                });
        }

        /**
         * Delete the property. The semantics are intended to be lazy, note the use of LoadAsync at the start.
         *
         * @param turnContext The turn context.
         * @return A task that represents the work queued to execute.
         */
        @Override
        public CompletableFuture<Void> delete(TurnContext turnContext) {
            return botState.load(turnContext)
                .thenCompose(state -> botState.deletePropertyValue(turnContext, name));
        }

        /**
         * Set the property value. The semantics are intended to be lazy, note the use of LoadAsync at the start.
         *
         * @param turnContext The turn context.
         * @param value       The value to set.
         * @return A task that represents the work queued to execute.
         */
        @Override
        public CompletableFuture<Void> set(TurnContext turnContext, T value) {
            return botState.load(turnContext)
                .thenCompose(state -> botState.setPropertyValue(turnContext, name, value));
        }

        /**
         * Gets name of the property.
         *
         * @return Name of the property.
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Sets name of the property.
         *
         * @param withName Name of the property.
         */
        @Override
        public void setName(String withName) {
            name = withName;
        }
    }
}
