// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

/**
 * Provides read-only access to configuration properties.
 */
public interface Configuration {
    /**
     * Returns a value for the specified property name.
     * 
     * @param key The property name.
     * @return The property value.
     */
    String getProperty(String key);
}
