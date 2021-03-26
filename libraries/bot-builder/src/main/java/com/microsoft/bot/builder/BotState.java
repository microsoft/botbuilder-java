// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.connector.Async;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Defines a state management object and automates the reading and writing of
 * associated state properties to a storage layer.
 *
 * <p>
 * Each state management object defines a scope for a storage layer. State
 * properties are created within a state management scope, and the Bot Framework
 * defines these scopes: {@link ConversationState}, {@link UserState}, and
 * {@link PrivateConversationState}. You can define additional scopes for your
 * bot.
 * </p>
 */
public abstract class BotState implements PropertyManager {
    /**
     * The key for the state cache.
     */
    private String contextServiceKey;

    /**
     * The storage layer this state management object will use.
     */
    private Storage storage;

    private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    /**
     * Initializes a new instance of the BotState class.
     *
     * @param withStorage           The storage provider to use.
     * @param withContextServiceKey The key for the state cache for this BotState.
     * @throws IllegalArgumentException Null Storage or empty service key arguments.
     */
    public BotState(Storage withStorage, String withContextServiceKey) throws IllegalArgumentException {
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
     * Creates a named state property within the scope of a BotState and returns an
     * accessor for the property.
     *
     * @param name name of property.
     * @param <T>  type of property.
     * @return A {@link StatePropertyAccessor} for the property.
     * @throws IllegalArgumentException Empty name
     */
    public <T extends Object> StatePropertyAccessor<T> createProperty(String name) throws IllegalArgumentException {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        return new BotStatePropertyAccessor<>(this, name);
    }

    /**
     * Populates the state cache for this BotState from the storage layer.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> load(TurnContext turnContext) {
        return load(turnContext, false);
    }

    /**
     * Reads in the current state object and caches it in the context object for
     * this turn.
     *
     * @param turnContext The context object for this turn.
     * @param force       true to overwrite any existing state cache; or false to
     *                    load state from storage only if the cache doesn't already
     *                    exist.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> load(TurnContext turnContext, boolean force) {
        return Async.tryCompletable(() -> {
            if (turnContext == null) {
                throw new IllegalArgumentException("turnContext cannot be null");
            }

            CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
            String storageKey = getStorageKey(turnContext);
            if (force || cachedState == null || cachedState.getState() == null) {
                return storage.read(new String[]{storageKey}).thenApply(val -> {
                    turnContext.getTurnState()
                        .replace(
                            contextServiceKey,
                            new CachedBotState((Map<String, Object>) val.get(storageKey))
                        );
                    return null;
                });
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Writes the state cache for this BotState to the storage layer.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> saveChanges(TurnContext turnContext) {
        return saveChanges(turnContext, false);
    }

    /**
     * Writes the state cache for this BotState to the storage layer.
     *
     * @param turnContext The context object for this turn.
     * @param force       true to save the state cache to storage; or false to save
     *                    state to storage only if a property in the cache has
     *                    changed.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> saveChanges(TurnContext turnContext, boolean force) {
        return Async.tryCompletable(() -> {
            if (turnContext == null) {
                throw new IllegalArgumentException("turnContext cannot be null");
            }

            CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
            if (force || cachedState != null && cachedState.isChanged()) {
                String storageKey = getStorageKey(turnContext);
                Map<String, Object> changes = new HashMap<String, Object>();
                changes.put(storageKey, cachedState.state);

                return storage.write(changes).thenApply(val -> {
                    cachedState.setHash(cachedState.computeHash(cachedState.state));
                    return null;
                });
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Clears the state cache for this BotState.
     *
     * <p>
     * This method clears the state cache in the turn context. Call
     * {@link #saveChanges(TurnContext, boolean)} to persist this change in the
     * storage layer.
     * </p>
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> clearState(TurnContext turnContext) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "TurnContext cannot be null."
            ));
        }

        turnContext.getTurnState().replace(contextServiceKey, new CachedBotState());
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
            return Async.completeExceptionally(new IllegalArgumentException(
                "TurnContext cannot be null."
            ));
        }

        String storageKey = getStorageKey(turnContext);
        return storage.delete(new String[] {storageKey}).thenApply(result -> {
            CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
            if (cachedState != null) {
                turnContext.getTurnState().remove(contextServiceKey);
            }

            return null;
        });
    }

    /**
     * Gets a copy of the raw cached data for this BotState from the turn context.
     *
     * @param turnContext The context object for this turn.
     * @return A JSON representation of the cached state.
     */
    public JsonNode get(TurnContext turnContext) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        String stateKey = getClass().getSimpleName();
        CachedBotState cachedState = turnContext.getTurnState().get(stateKey);
        return mapper.valueToTree(cachedState.state);
    }

    /**
     * Gets the cached bot state instance that wraps the raw cached data for this BotState from the turn context.
     *
     * @param turnContext The context object for this turn.
     * @return The cached bot state instance.
     */
    public CachedBotState getCachedState(TurnContext turnContext) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        return turnContext.getTurnState().get(contextServiceKey);
    }

    /**
     * When overridden in a derived class, gets the key to use when reading and
     * writing state to and from storage.
     *
     * @param turnContext The context object for this turn.
     * @return The storage key.
     * @throws IllegalArgumentException TurnContext doesn't contain all the required data.
     */
    public abstract String getStorageKey(TurnContext turnContext) throws IllegalArgumentException;

    /**
     * Gets the value of a property from the state cache for this BotState.
     *
     * @param turnContext  The context object for this turn.
     * @param propertyName The name of the property to get.
     * @param <T>          The property type.
     * @return A task that represents the work queued to execute. If the task is
     *         successful, the result contains the property value.
     */
    protected <T> CompletableFuture<T> getPropertyValue(
        TurnContext turnContext,
        String propertyName
    ) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        if (StringUtils.isEmpty(propertyName)) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "propertyName cannot be empty"
            ));
        }

        return Async.tryCompletable(() -> {
            CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
            return (CompletableFuture<T>) CompletableFuture
                .completedFuture(cachedState.getState().get(propertyName));
        });
    }

    /**
     * Deletes a property from the state cache for this BotState.
     *
     * @param turnContext  The context object for this turn.
     * @param propertyName The name of the property to delete.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> deletePropertyValue(
        TurnContext turnContext,
        String propertyName
    ) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "TurnContext cannot be null."
            ));
        }

        if (StringUtils.isEmpty(propertyName)) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "propertyName cannot be empty"
            ));
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        cachedState.getState().remove(propertyName);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sets the value of a property in the state cache for this BotState.
     *
     * @param turnContext  The context object for this turn.
     * @param propertyName The name of the property to set.
     * @param value        The value to set on the property.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> setPropertyValue(
        TurnContext turnContext,
        String propertyName,
        Object value
    ) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null."
            ));
        }

        if (StringUtils.isEmpty(propertyName)) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "propertyName cannot be empty"
            ));
        }

        CachedBotState cachedState = turnContext.getTurnState().get(contextServiceKey);
        cachedState.getState().put(propertyName, value);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Internal cached bot state.
     */
    public static class CachedBotState {
        /**
         * In memory cache of BotState properties.
         */
        private Map<String, Object> state;

        /**
         * Used to compute the hash of the state.
         */
        private String hash;

        /**
         * Object-JsonNode converter.
         */
        private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        /**
         * Construct with empty state.
         */
        CachedBotState() {
            this(null);
        }

        /**
         * Construct with supplied state.
         *
         * @param withState The initial state.
         */
        CachedBotState(Map<String, Object> withState) {
            state = withState != null ? withState : new ConcurrentHashMap<>();
            hash = computeHash(withState);
        }

        /**
         * @return The Map of key value pairs which are the state.
         */
        public Map<String, Object> getState() {
            return state;
        }

        /**
         * @param withState The key value pairs to set the state with.
         */
        void setState(Map<String, Object> withState) {
            state = withState;
        }

        /**
         * @return The hash value for the state.
         */
        String getHash() {
            return hash;
        }

        /**
         * @param witHashCode Set the hash value.
         */
        void setHash(String witHashCode) {
            hash = witHashCode;
        }

        /**
         *
         * @return Boolean to tell if the state has changed.
         */
        boolean isChanged() {
            return !StringUtils.equals(hash, computeHash(state));
        }

        /**
         * @param obj The object to compute the hash for.
         * @return The computed has for the provided object.
         */
        String computeHash(Object obj) {
            if (obj == null) {
                return "";
            }

            try {
                return mapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
    }

    /**
     * Implements StatePropertyAccessor for an PropertyContainer.
     *
     * <p>
     * Note the semantic of this accessor are intended to be lazy, this means teh
     * Get, Set and Delete methods will first call LoadAsync. This will be a no-op
     * if the data is already loaded. The implication is you can just use this
     * accessor in the application code directly without first calling LoadAsync
     * this approach works with the AutoSaveStateMiddleware which will save as
     * needed at the end of a turn.
     * </p>
     *
     * @param <T> type of value the propertyAccessor accesses.
     */
    private static class BotStatePropertyAccessor<T> implements StatePropertyAccessor<T> {
        /**
         * The name of the property.
         */
        private String name;

        /**
         * The parent BotState.
         */
        private BotState botState;

        /**
         * StatePropertyAccessor constructor.
         *
         * @param withState The parent BotState.
         * @param withName  The property name.
         */
        BotStatePropertyAccessor(BotState withState, String withName) {
            botState = withState;
            name = withName;
        }

        /**
         * Get the property value. The semantics are intended to be lazy, note the use
         * of {@link BotState#load(TurnContext)} at the start.
         *
         * @param turnContext         The context object for this turn.
         * @param defaultValueFactory Defines the default value. Invoked when no value
         *                            been set for the requested state property. If
         *                            defaultValueFactory is defined as null, the
         *                            MissingMemberException will be thrown if the
         *                            underlying property is not set.
         * @return A task that represents the work queued to execute.
         */
        @Override
        public CompletableFuture<T> get(TurnContext turnContext, Supplier<T> defaultValueFactory) {
            return botState.load(turnContext)
                .thenCombine(botState.getPropertyValue(turnContext, name), (loadResult, value) -> {
                    if (value != null) {
                        return (T) value;
                    }

                    if (defaultValueFactory == null) {
                        return null;
                    }

                    value = defaultValueFactory.get();
                    set(turnContext, (T) value).join();
                    return (T) value;
                });
        }

        /**
         * Delete the property. The semantics are intended to be lazy, note the use of
         * {@link BotState#load(TurnContext)} at the start.
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
         * Set the property value. The semantics are intended to be lazy, note the use
         * of {@link BotState#load(TurnContext)} at the start.
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
        public void setName(String withName) {
            name = withName;
        }
    }
}
