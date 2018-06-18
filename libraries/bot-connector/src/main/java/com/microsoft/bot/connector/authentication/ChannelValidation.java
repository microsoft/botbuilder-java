// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.JwtTokenExtractor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.microsoft.bot.connector.authentication.AuthenticationConstants.*;

public class ChannelValidation {
    /**
     * TO BOT FROM CHANNEL: Token validation parameters when connecting to a bot
     */
    public static final TokenValidationParameters ToBotFromChannelTokenValidationParameters = TokenValidationParameters.toBotFromChannelTokenValidationParameters();

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework Service.
     * @param authHeader The raw HTTP header in the format: "Bearer [longString]"
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @return A valid ClaimsIdentity.
     * @throws AuthenticationException A token issued by the Bot Framework emulator will FAIL this check.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String channelId) throws ExecutionException, InterruptedException, AuthenticationException {
        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
                ToBotFromChannelTokenValidationParameters,
                ToBotFromChannelOpenIdMetadataUrl,
                AllowedSigningAlgorithms, null);

        ClaimsIdentity identity = tokenExtractor.getIdentityAsync(authHeader, channelId).get();
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
        if (!identity.getIssuer().equalsIgnoreCase(ToBotFromChannelTokenIssuer)) {
            throw new AuthenticationException("Token Not Authenticated");
        }

        // The AppId from the claim in the token must match the AppId specified by the developer. Note that
        // the Bot Framework uses the Audience claim ("aud") to pass the AppID.
        String appIdFromClaim = identity.claims().get(AudienceClaim);
        if (appIdFromClaim == null || appIdFromClaim.isEmpty()) {
            // Claim is present, but doesn't have a value. Not Authorized.
            throw new AuthenticationException("Token Not Authenticated");
        }

        if (!credentials.isValidAppIdAsync(appIdFromClaim).get()) {
            throw new AuthenticationException(String.format("Invalid AppId passed on token: '%s'.", appIdFromClaim));
        }

        return CompletableFuture.completedFuture(identity);
    }

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework Service.
     * @param authHeader The raw HTTP header in the format: "Bearer [longString]"
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @param serviceUrl service url
     * @return A valid ClaimsIdentity.
     * @throws AuthenticationException A token issued by the Bot Framework emulator will FAIL this check.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader,CredentialProvider credentials, String channelId, String serviceUrl) throws ExecutionException, InterruptedException, AuthenticationException {
        ClaimsIdentity identity = ChannelValidation.authenticateToken(authHeader, credentials, channelId).get();

        if (!identity.claims().containsKey(ServiceUrlClaim)) {
            // Claim must be present. Not Authorized.
            throw new AuthenticationException(String.format("'%s' claim is required on Channel Token.", ServiceUrlClaim));
        }

        if (!serviceUrl.equalsIgnoreCase(identity.claims().get(ServiceUrlClaim))) {
            // Claim must match. Not Authorized.
            throw new AuthenticationException(String.format("'%s' claim does not match service url provided (%s).", ServiceUrlClaim, serviceUrl));
        }

        return CompletableFuture.completedFuture(identity);
    }
}
