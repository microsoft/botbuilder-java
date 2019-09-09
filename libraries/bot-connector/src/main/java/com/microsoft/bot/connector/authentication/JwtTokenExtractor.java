// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.ExecutorFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JwtTokenExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdMetadata.class);

    private static final ConcurrentMap<String, OpenIdMetadata> openIdMetadataCache = new ConcurrentHashMap<>();

    private TokenValidationParameters tokenValidationParameters;
    private List<String> allowedSigningAlgorithms;
    private OpenIdMetadata openIdMetadata;

    public JwtTokenExtractor(TokenValidationParameters tokenValidationParameters,
                             String metadataUrl,
                             List<String> allowedSigningAlgorithms) {

        this.tokenValidationParameters = new TokenValidationParameters(tokenValidationParameters);
        this.tokenValidationParameters.requireSignedTokens = true;
        this.allowedSigningAlgorithms = allowedSigningAlgorithms;
        this.openIdMetadata = openIdMetadataCache.computeIfAbsent(metadataUrl, key -> new OpenIdMetadata(metadataUrl));
    }

    public CompletableFuture<ClaimsIdentity> getIdentity(String authorizationHeader, String channelId) {
        return getIdentity(authorizationHeader, channelId, new ArrayList<>());
    }

    public CompletableFuture<ClaimsIdentity> getIdentity(String authorizationHeader,
                                                              String channelId,
                                                              List<String> requiredEndorsements) {
        if (authorizationHeader == null) {
            throw new IllegalArgumentException("authorizationHeader is required");
        }

        String[] parts = authorizationHeader.split(" ");
        if (parts.length == 2) {
            return getIdentity(parts[0], parts[1], channelId, requiredEndorsements);
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<ClaimsIdentity> getIdentity(String schema,
                                                              String token,
                                                              String channelId,
                                                              List<String> requiredEndorsements) {
        // No header in correct scheme or no token
        if (!schema.equalsIgnoreCase("bearer") || token == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Issuer isn't allowed? No need to check signature
        if (!hasAllowedIssuer(token)) {
            return CompletableFuture.completedFuture(null);
        }

        return validateToken(token, channelId, requiredEndorsements);
    }

    private boolean hasAllowedIssuer(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return this.tokenValidationParameters.validIssuers != null
            && this.tokenValidationParameters.validIssuers.contains(decodedJWT.getIssuer());
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<ClaimsIdentity> validateToken(String token,
                                                                 String channelId,
                                                                 List<String> requiredEndorsements) {
        DecodedJWT decodedJWT = JWT.decode(token);
        OpenIdMetadataKey key = this.openIdMetadata.getKey(decodedJWT.getKeyId());

        if (key == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            Verification verification = JWT.require(Algorithm.RSA256(key.key, null));
            try {
                verification.build().verify(token);

                // Note: On the Emulator Code Path, the endorsements collection is null so the validation code
                // below won't run. This is normal.
                if (key.endorsements != null) {
                    // Validate Channel / Token Endorsements. For this, the channelID present on the Activity
                    // needs to be matched by an endorsement.
                    boolean isEndorsed = EndorsementsValidator.validate(channelId, key.endorsements);
                    if (!isEndorsed) {
                        throw new AuthenticationException(
                            String.format("Could not validate endorsement for key: %s with endorsements: %s",
                                key.key.toString(), StringUtils.join(key.endorsements)));
                    }

                    // Verify that additional endorsements are satisfied. If no additional endorsements are expected,
                    // the requirement is satisfied as well
                    boolean additionalEndorsementsSatisfied =
                        requiredEndorsements.stream().
                            allMatch((endorsement) -> EndorsementsValidator.validate(endorsement, key.endorsements));
                    if (!additionalEndorsementsSatisfied) {
                        throw new AuthenticationException(
                            String.format("Could not validate additional endorsement for key: %s with endorsements: %s",
                                key.key.toString(), StringUtils.join(requiredEndorsements)));
                    }
                }

                if (!this.allowedSigningAlgorithms.contains(decodedJWT.getAlgorithm())) {
                    throw new AuthenticationException(
                        String.format("Could not validate algorithm for key: %s with algorithms: %s",
                            decodedJWT.getAlgorithm(), StringUtils.join(allowedSigningAlgorithms)));
                }

                return new ClaimsIdentity(decodedJWT);
            } catch (JWTVerificationException ex) {
                String errorDescription = ex.getMessage();
                LOGGER.warn(errorDescription);
                throw new AuthenticationException(ex);
            }
        }, ExecutorFactory.getExecutor());
    }
}
