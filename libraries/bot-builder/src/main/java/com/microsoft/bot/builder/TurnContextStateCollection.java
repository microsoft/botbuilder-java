// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.ConnectorClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a set of collection of services associated with the
 * {@link TurnContext}.
 */
public class TurnContextStateCollection implements AutoCloseable {
    /**
     * Map of objects managed by this class.
     */
    private Map<String, Object> state = new HashMap<>();

    /**
     * Get a value.
     *
     * @param key The key.
     * @param <T> The type of the value.
     * @return The value.
     * @throws IllegalArgumentException Null key.
     */
    public <T> T get(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        Object service = state.get(key);
        try {
            return (T) service;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Returns the Services stored in the TurnContextStateCollection.
     * @return the Map of String, Object pairs that contains the names and services for this collection.
     */
    public Map<String, Object> getTurnStateServices() {
        return state;
    }


    /**
     * Get a service by type using its full type name as the key.
     *
     * @param type The type of service to be retrieved. This will use the value
     *             returned by Class.getName as the key.
     * @param <T>  The type of the value.
     * @return The service stored under the specified key.
     */
    public <T> T get(Class<T> type) {
        return get(type.getName());
    }

    /**
     * Adds a value to the turn's context.
     *
     * @param key   The name of the value.
     * @param value The value to add.
     * @param <T>   The type of the value.
     * @throws IllegalArgumentException For null key or value.
     */
    public <T> void add(String key, T value) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        if (state.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Key %s already exists", key));
        }

        state.put(key, value);
    }

    /**
     * Add a service using its type name ({@link Class#getName()} as the key.
     *
     * @param value The service to add.
     * @param <T>   The type of the value.
     * @throws IllegalArgumentException For null value.
     */
    public <T> void add(T value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        add(value.getClass().getName(), value);
    }

    /**
     * Removes a value.
     *
     * @param key The name of the value to remove.
     */
    public void remove(String key) {
        state.remove(key);
    }

    /**
     * Replaces a value.
     *
     * @param key   The name of the value to replace.
     * @param value The new value.
     */
    public void replace(String key, Object value) {
        state.remove(key);
        add(key, value);
    }

    /**
     * Replaces a value.
     * @param value The service to add.
     * @param <T>   The type of the value.
     */
    public <T> void replace(T value) {
        String key = value.getClass().getName();
        replace(key, value);
    }

    /**
     * Returns <tt>true</tt> if this contains a mapping for the specified
     * key.
     * @param key The name of the value.
     * @return  True if the key exists.
     */
    public boolean containsKey(String key) {
        return state.containsKey(key);
    }

    /**
     * Auto call of {@link #close}.
     */
    @Override
    public void finalize() {
        try {
            close();
        } catch (Exception ignored) {

        }
    }

    /**
     * Close all contained {@link AutoCloseable} values.
     *
     * @throws Exception Exceptions encountered by children during close.
     */
    @Override
    public void close() throws Exception {
        for (Map.Entry entry : state.entrySet()) {
            if (entry.getValue() instanceof AutoCloseable) {
                if (entry.getValue() instanceof ConnectorClient) {
                    continue;
                }
                ((AutoCloseable) entry.getValue()).close();
            }
        }
    }

    /**
     * Copy the values from another TurnContextStateCollection.
     * @param other The collection to copy.
     */
    public void copy(TurnContextStateCollection other) {
        if (other != null) {
            for (String key : other.state.keySet()) {
                state.put(key, other.state.get(key));
            }
        }
    }
}
