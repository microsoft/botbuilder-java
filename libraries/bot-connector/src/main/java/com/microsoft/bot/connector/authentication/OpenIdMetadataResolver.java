// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

/**
 * Gets OpenIdMetadata.
 */
public interface OpenIdMetadataResolver {

    /**
     * Gets OpenIdMetadata for the specified key.
     * @param key The key.
     * @return An OpenIdMetadata object.
     */
    OpenIdMetadata get(String key);
}
