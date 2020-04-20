// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

/**
 * PropertyManager defines implementation of a source of named properties.
 */
public interface PropertyManager {
    /**
     * Creates a managed state property accessor for a property.
     *
     * @param name The name of the property accessor.
     * @param <T>  The property value type.
     * @return A state property accessor for the property.
     */
    <T> StatePropertyAccessor<T> createProperty(String name);
}
