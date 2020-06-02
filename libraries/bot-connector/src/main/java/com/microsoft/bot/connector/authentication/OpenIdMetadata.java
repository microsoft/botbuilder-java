// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

/**
 * Fetches Jwk data.
 */
public interface OpenIdMetadata {

    /**
     * Returns the partial Jwk data for a key.
     * @param keyId The key id.
     * @return The Jwk data.
     */
    OpenIdMetadataKey getKey(String keyId);
}
