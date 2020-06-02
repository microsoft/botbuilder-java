// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maintains a cache of OpenIdMetadata objects.
 */
public class CachingOpenIdMetadataResolver implements OpenIdMetadataResolver {
    private static final ConcurrentMap<String, CachingOpenIdMetadata> OPENID_METADATA_CACHE =
        new ConcurrentHashMap<>();

    /**
     * Gets the OpenIdMetadata object for the specified key.
     * @param metadataUrl  The key
     * @return The OpenIdMetadata object.  If the key is not found, an new OpenIdMetadata
     * object is created.
     */
    @Override
    public OpenIdMetadata get(String metadataUrl) {
        return OPENID_METADATA_CACHE
            .computeIfAbsent(metadataUrl, key -> new CachingOpenIdMetadata(metadataUrl));
    }
}
