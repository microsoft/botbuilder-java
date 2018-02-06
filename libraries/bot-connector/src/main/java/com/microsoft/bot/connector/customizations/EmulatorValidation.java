package com.microsoft.bot.connector.customizations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.microsoft.aad.adal4j.AuthenticationException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EmulatorValidation {
    public static final TokenValidationParameters ToBotFromEmulatorTokenValidationParameters = TokenValidationParameters.toBotFromEmulatorTokenValidationParameters();
    private static final String VersionClaim = "ver";
    private static final String AppIdClaim = "appid";

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

    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials) throws ExecutionException, InterruptedException {
        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
                ToBotFromEmulatorTokenValidationParameters,
                AuthenticationConstants.ToBotFromEmulatorOpenIdMetadataUrl,
                AuthenticationConstants.AllowedSigningAlgorithms, null);

        ClaimsIdentity identity = tokenExtractor.getIdentityAsync(authHeader).get();
        if (identity == null) {
            // No valid identity. Not Authorized.
            throw new AuthenticationException("Invalid Identity");
        }

        if (!identity.isAuthenticated()) {
            // The token is in some way invalid. Not Authorized.
            throw new AuthenticationException("Token Not Authenticated");
        }

        // Now check that the AppID in the claimset matches
        // what we're looking for. Note that in a multi-tenant bot, this value
        // comes from developer code that may be reaching out to a service, hence the
        // Async validation.
        if (!identity.claims().containsKey(VersionClaim)) {
            throw new AuthenticationException(String.format("'%s' claim is required on Emulator Tokens.", VersionClaim));
        }

        String tokenVersion = identity.claims().get(VersionClaim);
        String appId = "";

        // The Emulator, depending on Version, sends the AppId via either the
        // appid claim (Version 1) or the Authorized Party claim (Version 2).
        if (tokenVersion.isEmpty() || tokenVersion.equalsIgnoreCase("1.0")) {
            // either no Version or a version of "1.0" means we should look for
            // the claim in the "appid" claim.
            if (!identity.claims().containsKey(AppIdClaim)) {
                // No claim around AppID. Not Authorized.
                throw new AuthenticationException(String.format("'%s' claim is required on Emulator Token version '1.0'.", AppIdClaim));
            }

            appId = identity.claims().get(AppIdClaim);
        } else if (tokenVersion.equalsIgnoreCase("2.0")) {
            // Emulator, "2.0" puts the AppId in the "azp" claim.
            if (!identity.claims().containsKey(AuthenticationConstants.AuthorizedParty)) {
                // No claim around AppID. Not Authorized.
                throw new AuthenticationException(String.format("'%s' claim is required on Emulator Token version '2.0'.", AuthenticationConstants.AuthorizedParty));
            }

            appId = identity.claims().get(AuthenticationConstants.AuthorizedParty);
        } else if (tokenVersion.equalsIgnoreCase("3.0")) {
            // The v3.0 Token types have been disallowed. Not Authorized.
            throw new AuthenticationException("Emulator token version '3.0' is depricated.");
        } else if (tokenVersion.equalsIgnoreCase("3.1") || tokenVersion.equalsIgnoreCase("3.2")) {
            // The emulator for token versions "3.1" & "3.2" puts the AppId in the "Audiance" claim.
            if (!identity.claims().containsKey(AuthenticationConstants.AudienceClaim)) {
                // No claim around AppID. Not Authorized.
                throw new AuthenticationException(String.format("'%s' claim is required on Emulator Token version '%s'.", AuthenticationConstants.AudienceClaim, tokenVersion));
            }

            appId = identity.claims().get(AuthenticationConstants.AudienceClaim);
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
