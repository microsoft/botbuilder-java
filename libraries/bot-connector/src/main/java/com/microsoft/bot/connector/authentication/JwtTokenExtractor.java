// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ClaimsIdentityImpl;
import com.microsoft.bot.connector.authentication.TokenValidationParameters;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JwtTokenExtractor {
    private static final Logger LOGGER = Logger.getLogger(OpenIdMetadata.class.getName());

    private static final ConcurrentMap<String, OpenIdMetadata> openIdMetadataCache = new ConcurrentHashMap<>();

    private TokenValidationParameters tokenValidationParameters;
    private List<String> allowedSigningAlgorithms;
    private OpenIdMetadata openIdMetadata;

    public JwtTokenExtractor(TokenValidationParameters tokenValidationParameters, String metadataUrl, List<String> allowedSigningAlgorithms) {
        this.tokenValidationParameters = new TokenValidationParameters(tokenValidationParameters);
        this.tokenValidationParameters.requireSignedTokens = true;
        this.allowedSigningAlgorithms = allowedSigningAlgorithms;
        this.openIdMetadata = openIdMetadataCache.computeIfAbsent(metadataUrl, key -> new OpenIdMetadata(metadataUrl));
    }

    public CompletableFuture<ClaimsIdentity> getIdentityAsync(String authorizationHeader, String channelId) {
        if (authorizationHeader == null) {
            return CompletableFuture.completedFuture(null);
        }

        String[] parts = authorizationHeader.split(" ");
        if (parts.length == 2) {
            return getIdentityAsync(parts[0], parts[1], channelId);
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<ClaimsIdentity> getIdentityAsync(String schema, String token, String channelId) {
        // No header in correct scheme or no token
        if (!schema.equalsIgnoreCase("bearer") || token == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Issuer isn't allowed? No need to check signature
        if (!this.hasAllowedIssuer(token)) {
            return CompletableFuture.completedFuture(null);
        }

        return this.validateTokenAsync(token, channelId);
    }

    private boolean hasAllowedIssuer(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return this.tokenValidationParameters.validIssuers != null && this.tokenValidationParameters.validIssuers.contains(decodedJWT.getIssuer());
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<ClaimsIdentity> validateTokenAsync(String token, String channelId) {
        DecodedJWT decodedJWT = JWT.decode(token);
        OpenIdMetadataKey key = this.openIdMetadata.getKey(decodedJWT.getKeyId());

        if (key != null) {
            Verification verification = JWT.require(Algorithm.RSA256(key.key, null));
            try {
                verification.build().verify(token);

                // Validate Channel / Token Endorsements. For this, the channelID present on the Activity
                // needs to be matched by an endorsement.
                boolean isEndorsed = EndorsementsValidator.validate(channelId, key.endorsements);
                if (!isEndorsed)
                {
                    throw new AuthenticationException(String.format("Could not validate endorsement for key: %s with endorsements: %s", key.key.toString(), StringUtils.join(key.endorsements)));
                }

                if (!this.allowedSigningAlgorithms.contains(decodedJWT.getAlgorithm())) {
                    throw new AuthenticationException(String.format("Could not validate algorithm for key: %s with algorithms: %s", decodedJWT.getAlgorithm(), StringUtils.join(allowedSigningAlgorithms)));
                }

                Map<String, String> claims = new HashMap<>();
                if (decodedJWT.getClaims() != null) {
                    decodedJWT.getClaims().forEach((k, v) -> claims.put(k, v.asString()));
                }

                return CompletableFuture.completedFuture(new ClaimsIdentityImpl(decodedJWT.getIssuer(), claims));

            } catch (JWTVerificationException ex) {
                String errorDescription = ex.getMessage();
                LOGGER.log(Level.WARNING, errorDescription);
                return CompletableFuture.completedFuture(null);
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
