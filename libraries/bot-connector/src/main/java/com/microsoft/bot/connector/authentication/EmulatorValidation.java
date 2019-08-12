// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.microsoft.aad.adal4j.AuthenticationException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Validates and Examines JWT tokens from the Bot Framework Emulator
 */
public class EmulatorValidation {
    /**
     * TO BOT FROM EMULATOR: Token validation parameters when connecting to a channel.
     */
    public static final TokenValidationParameters ToBotFromEmulatorTokenValidationParameters = TokenValidationParameters.toBotFromEmulatorTokenValidationParameters();

    /**
     * Determines if a given Auth header is from the Bot Framework Emulator
     * @param authHeader Bearer Token, in the "Bearer [Long String]" Format.
     * @return True, if the token was issued by the Emulator. Otherwise, false.
     */
    public static CompletableFuture<Boolean> isTokenFromEmulator(String authHeader) {
        // The Auth Header generally looks like this:
        // "Bearer eyJ0e[...Big Long String...]XAiO"
        if (authHeader == null || authHeader.isEmpty()) {
            // No token. Can't be an emulator token.
            return CompletableFuture.completedFuture(false);
        }

        String[] parts = authHeader.split(" ");
        if (parts.length != 2) {
            // Emulator tokens MUST have exactly 2 parts. If we don't have 2 parts, it's not an emulator token
            return CompletableFuture.completedFuture(false);
        }

        String schema = parts[0];
        String token = parts[1];

        if (!schema.equalsIgnoreCase("bearer")) {
            // The scheme from the emulator MUST be "Bearer"
            return CompletableFuture.completedFuture(false);
        }

        // Parse the Big Long String into an actual token.
        DecodedJWT decodedJWT = JWT.decode(token);

        // Is there an Issuer?
        if (decodedJWT.getIssuer().isEmpty()) {
            // No Issuer, means it's not from the Emulator.
            return CompletableFuture.completedFuture(false);
        }

        // Is the token issues by a source we consider to be the emulator?
        if (!ToBotFromEmulatorTokenValidationParameters.validIssuers.contains(decodedJWT.getIssuer())) {
            // Not a Valid Issuer. This is NOT a Bot Framework Emulator Token.
            return CompletableFuture.completedFuture(false);
        }

        // The Token is from the Bot Framework Emulator. Success!
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework Emulator.
     * @param authHeader The raw HTTP header in the format: "Bearer [longString]"
     * @param credentials The user defined set of valid credentials, such as the AppId.
     * @return A valid ClaimsIdentity.
     * @throws AuthenticationException A token issued by the Bot Framework will FAIL this check. Only Emulator tokens will pass.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials, String channelId) throws ExecutionException, InterruptedException, AuthenticationException {
        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
                ToBotFromEmulatorTokenValidationParameters,
                AuthenticationConstants.ToBotFromEmulatorOpenIdMetadataUrl,
                AuthenticationConstants.AllowedSigningAlgorithms);

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
        if (!identity.claims().containsKey(AuthenticationConstants.VersionClaim)) {
            throw new AuthenticationException(String.format("'%s' claim is required on Emulator Tokens.", AuthenticationConstants.VersionClaim));
        }

        String tokenVersion = identity.claims().get(AuthenticationConstants.VersionClaim);
        String appId = "";

        // The Emulator, depending on Version, sends the AppId via either the
        // appid claim (Version 1) or the Authorized Party claim (Version 2).
        if (tokenVersion.isEmpty() || tokenVersion.equalsIgnoreCase("1.0")) {
            // either no Version or a version of "1.0" means we should look for
            // the claim in the "appid" claim.
            if (!identity.claims().containsKey(AuthenticationConstants.AppIdClaim)) {
                // No claim around AppID. Not Authorized.
                throw new AuthenticationException(String.format("'%s' claim is required on Emulator Token version '1.0'.", AuthenticationConstants.AppIdClaim));
            }

            appId = identity.claims().get(AuthenticationConstants.AppIdClaim);
        } else if (tokenVersion.equalsIgnoreCase("2.0")) {
            // Emulator, "2.0" puts the AppId in the "azp" claim.
            if (!identity.claims().containsKey(AuthenticationConstants.AuthorizedParty)) {
                // No claim around AppID. Not Authorized.
                throw new AuthenticationException(String.format("'%s' claim is required on Emulator Token version '2.0'.", AuthenticationConstants.AuthorizedParty));
            }

            appId = identity.claims().get(AuthenticationConstants.AuthorizedParty);
        } else {
            // Unknown Version. Not Authorized.
            throw new AuthenticationException(String.format("Unknown Emulator Token version '%s'.", tokenVersion));
        }

        if (!credentials.isValidAppIdAsync(appId).get()) {
            throw new AuthenticationException(String.format("Invalid AppId passed on token: '%s'.", appId));
        }

        return CompletableFuture.completedFuture(identity);
    }
}
