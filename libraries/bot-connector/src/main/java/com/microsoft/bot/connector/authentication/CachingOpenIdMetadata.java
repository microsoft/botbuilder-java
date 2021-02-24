// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a cache of OpenID metadata keys.
 */
class CachingOpenIdMetadata implements OpenIdMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingOpenIdMetadata.class);
    private static final int CACHE_DAYS = 1;
    private static final int CACHE_HOURS = 1;

    private String url;
    private long lastUpdated;
    private ObjectMapper mapper;
    private Map<String, Jwk> keyCache = new HashMap<>();
    private final Object sync = new Object();

    /**
     * Constructs a OpenIdMetaData cache for a url.
     * 
     * @param withUrl The url.
     */
    CachingOpenIdMetadata(String withUrl) {
        url = withUrl;
        mapper = new ObjectMapper().findAndRegisterModules();
    }

    /**
     * Gets a openid key.
     *
     * <p>
     * Note: This could trigger a cache refresh, which will incur network calls.
     * </p>
     *
     * @param keyId The JWT key.
     * @return The cached key.
     */
    @Override
    public OpenIdMetadataKey getKey(String keyId) {
        synchronized (sync) {
            // If keys are more than CACHE_DAYS days old, refresh them
            if (lastUpdated < System.currentTimeMillis() - Duration.ofDays(CACHE_DAYS).toMillis()) {
                refreshCache();
            }

            // Search the cache even if we failed to refresh
            OpenIdMetadataKey key = findKey(keyId);
            if (key == null && lastUpdated < System.currentTimeMillis() - Duration.ofHours(CACHE_HOURS).toMillis()) {
                // Refresh the cache if a key is not found (max once per CACHE_HOURS)
                refreshCache();
                key = findKey(keyId);
            }
            return key;
        }
    }

    private void refreshCache() {
        keyCache.clear();

        try {
            URL openIdUrl = new URL(this.url);
            HashMap<String, Object> openIdConf =
                this.mapper.readValue(openIdUrl, new TypeReference<HashMap<String, Object>>() {
                });
            URL keysUrl = new URL(openIdConf.get("jwks_uri").toString());
            lastUpdated = System.currentTimeMillis();
            UrlJwkProvider provider = new UrlJwkProvider(keysUrl);
            keyCache = provider.getAll().stream().collect(Collectors.toMap(Jwk::getId, jwk -> jwk));
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to load openID config: %s", e.getMessage()));
            lastUpdated = 0;
        } catch (SigningKeyNotFoundException keyexception) {
            LOGGER.error("refreshCache", keyexception);
            lastUpdated = 0;
        }
    }

    @SuppressWarnings("unchecked")
    private OpenIdMetadataKey findKey(String keyId) {
        if (!keyCache.containsKey(keyId)) {
            LOGGER.warn("findKey: keyId " + keyId + " doesn't exist.");
            return null;
        }

        try {
            Jwk jwk = keyCache.get(keyId);
            OpenIdMetadataKey key = new OpenIdMetadataKey();
            key.key = (RSAPublicKey) jwk.getPublicKey();
            key.endorsements = (List<String>) jwk.getAdditionalAttributes().get("endorsements");
            key.certificateChain = jwk.getCertificateChain();
            return key;
        } catch (JwkException e) {
            String errorDescription = String.format("Failed to load keys: %s", e.getMessage());
            LOGGER.warn(errorDescription);
        }
        return null;
    }
}
