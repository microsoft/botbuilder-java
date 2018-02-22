package com.microsoft.bot.connector.customizations;


import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import sun.security.rsa.RSAPublicKeyImpl;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class OpenIdMetadata {
    private static final Logger LOGGER = Logger.getLogger( OpenIdMetadata.class.getName() );

    private String url;
    private long lastUpdated;
    private JwkProvider cacheKeys;
    private ObjectMapper mapper;

    OpenIdMetadata(String url) {
        this.url = url;
        this.mapper = new ObjectMapper().findAndRegisterModules();
    }

    public OpenIdMetadataKey getKey(String keyId) {
        // If keys are more than 5 days old, refresh them
        long now = System.currentTimeMillis();
        if (lastUpdated < (now - (1000 * 60 * 60 * 24 * 5))) {
            refreshCache();
        }
        // Search the cache even if we failed to refresh
        return findKey(keyId);
    }

    private String refreshCache() {
        try {
            URL openIdUrl = new URL(this.url);
            HashMap<String, String> openIdConf = mapper.readValue(openIdUrl, new TypeReference<HashMap<String, Object>>(){});
            URL keysUrl = new URL(openIdConf.get("jwks_uri"));
            this.lastUpdated = System.currentTimeMillis();
            this.cacheKeys = new UrlJwkProvider(keysUrl);
            return IOUtils.toString(keysUrl);
        } catch (IOException e) {
            String errorDescription = String.format("Failed to load openID config: %s", e.getMessage());
            LOGGER.log(Level.WARNING, errorDescription);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private OpenIdMetadataKey findKey(String keyId) {
        try {
            Jwk jwk = cacheKeys.get(keyId);
            OpenIdMetadataKey key = new OpenIdMetadataKey();
            key.key = new RSAPublicKeyImpl(jwk.getPublicKey().getEncoded());
            key.endorsements = (List<String>) jwk.getAdditionalAttributes().get("endorsements");
            return key;
        } catch (JwkException e) {
            String errorDescription = String.format("Failed to load keys: %s", e.getMessage());
            LOGGER.log(Level.WARNING, errorDescription);
        } catch (InvalidKeyException e) {
            String errorDescription = String.format("Failed to load keys (key not compatible): %s", e.getMessage());
            LOGGER.log(Level.WARNING, errorDescription);
        }
        return null;
    }
}
