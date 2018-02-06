package com.microsoft.bot.connector.customizations;

import com.microsoft.aad.adal4j.AuthenticationException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ChannelValidation {
    public static final TokenValidationParameters ToBotFromChannelTokenValidationParameters = TokenValidationParameters.toBotFromChannelTokenValidationParameters();
    private static final String ServiceUrlClaim = "serviceurl";

    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader, CredentialProvider credentials) throws ExecutionException, InterruptedException, AuthenticationException {
        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
                ToBotFromChannelTokenValidationParameters,
                AuthenticationConstants.ToBotFromChannelOpenIdMetadataUrl,
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

        // Look for the "aud" claim, but only if issued from the Bot Framework
        if (!identity.getIssuer().equalsIgnoreCase(AuthenticationConstants.BotFrameworkTokenIssuer)) {
            throw new AuthenticationException("Token Not Authenticated");
        }

        // The AppId from the claim in the token must match the AppId specified by the developer. Note that
        // the Bot Framwork uses the Audiance claim ("aud") to pass the AppID.
        String appIdFromClaim = identity.claims().get(AuthenticationConstants.AudienceClaim);
        if (appIdFromClaim == null || appIdFromClaim.isEmpty()) {
            // Claim is present, but doesn't have a value. Not Authorized.
            throw new AuthenticationException("Token Not Authenticated");
        }

        if (!credentials.isValidAppIdAsync(appIdFromClaim).get()) {
            throw new AuthenticationException(String.format("Invalid AppId passed on token: '%s'.", appIdFromClaim));
        }

        return CompletableFuture.completedFuture(identity);
    }

    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader,CredentialProvider credentials, String serviceUrl) throws ExecutionException, InterruptedException, AuthenticationException {
        ClaimsIdentity identity = ChannelValidation.authenticateToken(authHeader, credentials).get();

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
