// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.adal4j.AuthenticationException;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ChannelValidation {
    /**
     * TO BOT FROM CHANNEL: Token validation parameters when connecting to a bot
     */
    public static final TokenValidationParameters TOKENVALIDATIONPARAMETERS = new TokenValidationParameters() {{
            this.validateIssuer = true;
            this.validIssuers = new ArrayList<String>() {{
                add(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER);
            }};
            this.validateAudience = false;
            this.validateLifetime = true;
            this.clockSkew = Duration.ofMinutes(5);
            this.requireSignedTokens = true;
        }};

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param channelId   ChannelId for endorsements validation.
     * @return A valid ClaimsIdentity.
     *
     * On join:
     * @throws AuthenticationException A token issued by the Bot Framework emulator will FAIL this check.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String channelId) {
        return authenticateToken(authHeader, credentials, channelId, new AuthenticationConfiguration());
    }

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param channelId   ChannelId for endorsements validation.
     * @param authConfig  The AuthenticationConfiguration.
     * @return A valid ClaimsIdentity.
     *
     * On join:
     * @throws AuthenticationException A token issued by the Bot Framework emulator will FAIL this check.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String channelId, AuthenticationConfiguration authConfig) {
        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
            TOKENVALIDATIONPARAMETERS,
            AuthenticationConstants.TO_BOT_FROM_CHANNEL_OPENID_METADATA_URL,
            AuthenticationConstants.AllowedSigningAlgorithms);

        return tokenExtractor.getIdentityAsync(authHeader, channelId)
            .thenApply(identity -> {
                if (identity == null) {
                    // No valid identity. Not Authorized.
                    throw new AuthenticationException("Invalid Identity");
                }

                if (!identity.isAuthenticated()) {
                    // The token is in some way invalid. Not Authorized.
                    throw new AuthenticationException("Token Not Authenticated");
                }

                // Now check that the AppID in the claims set matches
                // what we're looking for. Note that in a multi-tenant bot, this value
                // comes from developer code that may be reaching out to a service, hence the
                // Async validation.

                // Look for the "aud" claim, but only if issued from the Bot Framework
                if (!identity.getIssuer().equalsIgnoreCase(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER)) {
                    throw new AuthenticationException("Wrong Issuer");
                }

                // The AppId from the claim in the token must match the AppId specified by the developer. Note that
                // the Bot Framework uses the Audience claim ("aud") to pass the AppID.
                String appIdFromAudienceClaim = identity.claims().get(AuthenticationConstants.AUDIENCE_CLAIM);
                if (StringUtils.isEmpty(appIdFromAudienceClaim)) {
                    // Claim is present, but doesn't have a value. Not Authorized.
                    throw new AuthenticationException("No Audience Claim");
                }

                if (!credentials.isValidAppIdAsync(appIdFromAudienceClaim).join()) {
                    throw new AuthenticationException(String.format("Invalid AppId passed on token: '%s'.", appIdFromAudienceClaim));
                }

                return identity;
            });
    }

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]"
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param channelId   ChannelId for endorsements validation.
     * @param serviceUrl  Service url.
     * @return A valid ClaimsIdentity.
     *
     * On join:
     * @throws AuthenticationException A token issued by the Bot Framework emulator will FAIL this check.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String channelId, String serviceUrl) {
        return authenticateToken(authHeader, credentials, channelId, serviceUrl, new AuthenticationConfiguration());
    }

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]"
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param channelId   ChannelId for endorsements validation.
     * @param serviceUrl  Service url.
     * @param authConfig  The authentication configuration.
     * @return A valid ClaimsIdentity.
     *
     * On join:
     * @throws AuthenticationException A token issued by the Bot Framework emulator will FAIL this check.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String channelId, String serviceUrl, AuthenticationConfiguration authConfig) {
        return ChannelValidation.authenticateToken(authHeader, credentials, channelId, authConfig)
            .thenApply(identity -> {
                if (!identity.claims().containsKey(AuthenticationConstants.SERVICE_URL_CLAIM)) {
                    // Claim must be present. Not Authorized.
                    throw new AuthenticationException(String.format("'%s' claim is required on Channel Token.", AuthenticationConstants.SERVICE_URL_CLAIM));
                }

                if (!serviceUrl.equalsIgnoreCase(identity.claims().get(AuthenticationConstants.SERVICE_URL_CLAIM))) {
                    // Claim must match. Not Authorized.
                    throw new AuthenticationException(String.format("'%s' claim does not match service url provided (%s).", AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl));
                }

                return identity;
            });
    }
}
