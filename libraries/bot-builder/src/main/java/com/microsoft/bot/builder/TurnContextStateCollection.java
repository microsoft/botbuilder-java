// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.ConnectorClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a set of collection of services associated with the {@link TurnContext}.
 */
public class TurnContextStateCollection extends HashMap<String, Object> implements AutoCloseable {
    public <T> T get(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        Object service = super.get(key);
        try {
            T result = (T) service;
        } catch (ClassCastException e) {
            return null;
        }

        return (T) service;
    }

    /**
     * Get a service by type using its full type name as the key.
     *
     * @param type The type of service to be retrieved.
     * @return The service stored under the specified key.
     */
    public <T> T get(Class<T> type) throws IllegalArgumentException {
        return get(type.getName());
    }

    public <T> void add(String key, T value) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (value == null) {
            throw new IllegalArgumentException("service");
        }

        if (containsKey(key))
            throw new IllegalArgumentException(String.format("Key %s already exists", key));
        put(key, value);
    }

    /**
     * Add a service using its full type name as the key.
     *
     * @param value The service to add.
     */
    public <T> void add(T value) throws IllegalArgumentException {
        add(value.getClass().getName(), value);
    }

    @Override
    public void finalize() {
        try {
            close();
        } catch (Exception e) {

        }
    }

    @Override
    public void close() throws Exception {
        for (Map.Entry entry : entrySet()) {
            if (entry.getValue() instanceof AutoCloseable) {
                if (entry.getValue() instanceof ConnectorClient) {
                    continue;
                }
                ((AutoCloseable) entry.getValue()).close();
            }
        }
    }
}



