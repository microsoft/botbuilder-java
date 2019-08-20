// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

/**
 * Provides read-only access to configuration properties.
 */
public interface Configuration {
    String getProperty(String key);
}
