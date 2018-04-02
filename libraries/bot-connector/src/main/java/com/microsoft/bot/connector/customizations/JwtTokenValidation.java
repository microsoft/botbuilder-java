// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.customizations;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.schema.models.Activity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JwtTokenValidation {

    /**
     * Validates the security tokens required by the Bot Framework Protocol. Throws on any exceptions.
     * @param activity The incoming Activity from the Bot Framework or the Emulator
     * @param authHeader The Bearer token included as part of the request
     * @param credentials The set of valid credentials, such as the Bot Application ID
     * @return Nothing
     * @throws AuthenticationException Throws on auth failed.
     */
    public static CompletableFuture assertValidActivity(Activity activity, String authHeader, CredentialProvider credentials) throws AuthenticationException, InterruptedException, ExecutionException {
        if (authHeader == null || authHeader.isEmpty()) {
            // No auth header was sent. We might be on the anonymous code path.
            boolean isAuthDisable = credentials.isAuthenticationDisabledAsync().get();
            if (isAuthDisable) {
                // We are on the anonymous code path.
                return CompletableFuture.completedFuture(null);
            }
        }

        // Go through the standard authentication path.
        JwtTokenValidation.validateAuthHeader(authHeader, credentials, activity.serviceUrl()).get();

        // On the standard Auth path, we need to trust the URL that was incoming.
        MicrosoftAppCredentials.trustServiceUrl(activity.serviceUrl());
        return CompletableFuture.completedFuture(null);
    }

    public static CompletableFuture<ClaimsIdentity> validateAuthHeader(String authHeader, CredentialProvider credentials) throws ExecutionException, InterruptedException, AuthenticationException {
        if (authHeader == null || authHeader.isEmpty()) {
            boolean isAuthDisable = credentials.isAuthenticationDisabledAsync().get();
            if (isAuthDisable) {
                // In the scenario where Auth is disabled, we still want to have the
                // IsAuthenticated flag set in the ClaimsIdentity. To do this requires
                // adding in an empty claim.
                ClaimsIdentity anonymousAuthenticatedIdentity = new ClaimsIdentityImpl("anonymous");
                return CompletableFuture.completedFuture(anonymousAuthenticatedIdentity);
            }
            // No Auth Header. Auth is required. Request is not authorized.
            throw new AuthenticationException("No Auth Header. Auth is required.");
        }

        boolean usingEmulator = EmulatorValidation.isTokenFromEmulator(authHeader).get();
        if (usingEmulator) {
            return EmulatorValidation.authenticateToken(authHeader, credentials);
        } else {
            return ChannelValidation.authenticateToken(authHeader, credentials);
        }
    }

    public static CompletableFuture<ClaimsIdentity> validateAuthHeader(String authHeader, CredentialProvider credentials, String serviceUrl) throws ExecutionException, InterruptedException, AuthenticationException {
        if (authHeader == null || authHeader.isEmpty()) {
            boolean isAuthDisable = credentials.isAuthenticationDisabledAsync().get();

            if (isAuthDisable) {
                // In the scenario where Auth is disabled, we still want to have the
                // IsAuthenticated flag set in the ClaimsIdentity. To do this requires
                // adding in an empty claim.
                ClaimsIdentity anonymousAuthenticatedIdentity = new ClaimsIdentityImpl("anonymous");
                return CompletableFuture.completedFuture(anonymousAuthenticatedIdentity);
            }

            // No Auth Header. Auth is required. Request is not authorized.
            throw new AuthenticationException("No Auth Header. Auth is required.");
        }

        boolean usingEmulator = EmulatorValidation.isTokenFromEmulator(authHeader).get();
        if (usingEmulator) {
            return EmulatorValidation.authenticateToken(authHeader, credentials);
        } else {
            return ChannelValidation.authenticateToken(authHeader, credentials, serviceUrl);
        }
    }
}
