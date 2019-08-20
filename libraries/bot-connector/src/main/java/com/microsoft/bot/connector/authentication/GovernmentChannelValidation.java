// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.ExecutorFactory;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * TO BOT FROM GOVERNMENT CHANNEL: Token validation parameters when connecting to a bot.
 */
public class GovernmentChannelValidation {
    private static final TokenValidationParameters GOVERNMENT_VALIDATION_PARAMETERS = new TokenValidationParameters() {{
        this.validateIssuer = true;
        this.validIssuers = new ArrayList<String>() {{
            add(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER);
        }};
        this.validateAudience = false;
        this.validateLifetime = true;
        this.clockSkew = Duration.ofMinutes(5);
        this.requireSignedTokens = true;
    }};

    /**
     * Validate the incoming Auth Header as a token sent from a Bot Framework Government Channel Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param serviceUrl  The service url from the request.
     * @param channelId   The ID of the channel to validate.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String serviceUrl, String channelId) {
        return authenticateToken(authHeader, credentials, serviceUrl, channelId, new AuthenticationConfiguration());
    }

    /**
     * Validate the incoming Auth Header as a token sent from a Bot Framework Government Channel Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param serviceUrl  The service url from the request.
     * @param channelId   The ID of the channel to validate.
     * @param authConfig  The authentication configuration.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String serviceUrl, String channelId, AuthenticationConfiguration authConfig) {
        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
            GOVERNMENT_VALIDATION_PARAMETERS,
            GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_OPENID_METADATA_URL,
            AuthenticationConstants.AllowedSigningAlgorithms);

        return tokenExtractor.getIdentityAsync(authHeader, channelId, authConfig.requiredEndorsements())
            .thenCompose(identity -> {
                return validateIdentity(identity, credentials, serviceUrl);
            });
    }

    /**
     * Validate the ClaimsIdentity as sent from a Bot Framework Government Channel Service.
     *
     * @param identity    The claims identity to validate.
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param serviceUrl  The service url from the request.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public static CompletableFuture<ClaimsIdentity> validateIdentity(ClaimsIdentity identity, CredentialProvider credentials, String serviceUrl) {

        return CompletableFuture.supplyAsync(() -> {
            if (identity == null || !identity.isAuthenticated()) {
                throw new AuthenticationException("Invalid Identity");
            }

            return identity;
        }, ExecutorFactory.getExecutor())

            .thenApply(theIdentity -> {
                // Now check that the AppID in the claim set matches
                // what we're looking for. Note that in a multi-tenant bot, this value
                // comes from developer code that may be reaching out to a service, hence the
                // Async validation.

                // The AppId from the claim in the token must match the AppId specified by the developer. Note that
                // the Bot Framework uses the Audience claim ("aud") to pass the AppID.
                String appIdFromClaim = theIdentity.claims().get(AuthenticationConstants.AUDIENCE_CLAIM);
                if (StringUtils.isEmpty(appIdFromClaim)) {
                    // Claim is present, but doesn't have a value. Not Authorized.
                    throw new AuthenticationException("Token Not Authenticated");
                }

                boolean isValid = credentials.isValidAppIdAsync(appIdFromClaim).join();
                if (!isValid) {
                    throw new AuthenticationException(String.format("Invalid AppId passed on token: '%s'.", appIdFromClaim));
                }

                String serviceUrlClaim = theIdentity.claims().get(AuthenticationConstants.SERVICE_URL_CLAIM);
                if (StringUtils.isEmpty(serviceUrl)) {
                    throw new AuthenticationException(String.format("Invalid serviceurl passed on token: '%s'.", serviceUrlClaim));
                }

                if (!StringUtils.equals(serviceUrl, serviceUrlClaim)) {
                    throw new AuthenticationException(String.format("serviceurl doesn't match claim: '%s'.", serviceUrlClaim));
                }

                return theIdentity;
            });
    }
}
