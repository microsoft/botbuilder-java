// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import java.util.Properties;

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

    /**
     * Returns the Properties in the Configuration.
     *
     * @return The Properties in the Configuration.
     */
    Properties getProperties();

    /**
     * Returns an Array of Properties that are in the Configuration.
     * @param key The property name.
     * @return The property values.
     */
    String[] getProperties(String key);
}
