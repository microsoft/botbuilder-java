// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.interfaces.Claim;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.ClaimsIdentityImpl;
import com.microsoft.bot.connector.authentication.EmulatorValidation;
import com.microsoft.bot.schema.models.Activity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JwtTokenValidation {

    /**
     * Validates the security tokens required by the Bot Framework Protocol. Throws on any exceptions.
     *
     * @param activity    The incoming Activity from the Bot Framework or the Emulator
     * @param authHeader  The Bearer token included as part of the request
     * @param credentials The set of valid credentials, such as the Bot Application ID
     * @return Nothing
     * @throws AuthenticationException Throws on auth failed.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateRequest(Activity activity, String authHeader, CredentialProvider credentials) throws AuthenticationException, InterruptedException, ExecutionException {
        if (authHeader == null || authHeader.isEmpty()) {
            // No auth header was sent. We might be on the anonymous code path.
            boolean isAuthDisable = credentials.isAuthenticationDisabledAsync().get();
            if (isAuthDisable) {
                // In the scenario where Auth is disabled, we still want to have the
                // IsAuthenticated flag set in the ClaimsIdentity. To do this requires
                // adding in an empty claim.
                return CompletableFuture.completedFuture(new ClaimsIdentityImpl("anonymous"));
            }

            // No Auth Header. Auth is required. Request is not authorized.
            throw new AuthenticationException("No Auth Header. Auth is required.");
        }

        // Go through the standard authentication path.
        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(authHeader, credentials, activity.channelId(), activity.serviceUrl()).get();

        // On the standard Auth path, we need to trust the URL that was incoming.
        MicrosoftAppCredentials.trustServiceUrl(activity.serviceUrl());
        return CompletableFuture.completedFuture(identity);
    }

    // TODO: Recieve httpClient and use ClientID
    public static CompletableFuture<ClaimsIdentity> validateAuthHeader(String authHeader, CredentialProvider credentials, String channelId, String serviceUrl) throws ExecutionException, InterruptedException, AuthenticationException {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new IllegalArgumentException("No authHeader present. Auth is required.");
        }

        boolean usingEmulator = EmulatorValidation.isTokenFromEmulator(authHeader).get();
        if (usingEmulator) {
            return EmulatorValidation.authenticateToken(authHeader, credentials, channelId);
        } else {
            // No empty or null check. Empty can point to issues. Null checks only.
            if (serviceUrl != null) {
                return ChannelValidation.authenticateToken(authHeader, credentials, channelId, serviceUrl);
            } else {
                return ChannelValidation.authenticateToken(authHeader, credentials, channelId);
            }
        }
    }
}
