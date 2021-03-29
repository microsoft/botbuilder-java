// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Government Channel auth validation.
 */
public final class GovernmentChannelValidation {
    private static String openIdMetaDataUrl =
        GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_OPENID_METADATA_URL;

    /**
     * TO BOT FROM GOVERNMENT CHANNEL.
     * @return Token validation parameters when connecting to a bot.
     */
    public static TokenValidationParameters getTokenValidationParameters() {
        TokenValidationParameters tokenValidationParameters = new TokenValidationParameters();

        ArrayList<String> validIssuers = new ArrayList<String>();
        tokenValidationParameters.validIssuers = validIssuers;

        tokenValidationParameters.validateIssuer = true;
        tokenValidationParameters.validateAudience = false;
        tokenValidationParameters.validateLifetime = true;
        tokenValidationParameters.clockSkew = Duration.ofMinutes(AuthenticationConstants.DEFAULT_CLOCKSKEW_MINUTES);
        tokenValidationParameters.requireSignedTokens = true;

        return tokenValidationParameters;
    }

    private GovernmentChannelValidation() {

    }

    /**
     * Gets the OpenID metadata URL.
     * 
     * @return The url.
     */
    public static String getOpenIdMetaDataUrl() {
        return openIdMetaDataUrl;
    }

    /**
     * Sets the OpenID metadata URL.
     * 
     * @param withOpenIdMetaDataUrl The metadata url.
     */
    public static void setOpenIdMetaDataUrl(String withOpenIdMetaDataUrl) {
        openIdMetaDataUrl = withOpenIdMetaDataUrl;
    }

    /**
     * Validate the incoming Auth Header as a token sent from a Bot Framework
     * Government Channel Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials The user defined set of valid credentials, such as the
     *                    AppId.
     * @param serviceUrl  The service url from the request.
     * @param channelId   The ID of the channel to validate.
     * @return A CompletableFuture representing the asynchronous operation.
     *
     *         On join:
     * @throws AuthenticationException Authentication failed.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(
        String authHeader,
        CredentialProvider credentials,
        String serviceUrl,
        String channelId
    ) {
        return authenticateToken(
            authHeader, credentials, serviceUrl, channelId, new AuthenticationConfiguration()
        );
    }

    /**
     * Validate the incoming Auth Header as a token sent from a Bot Framework
     * Government Channel Service.
     *
     * @param authHeader  The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials The user defined set of valid credentials, such as the
     *                    AppId.
     * @param serviceUrl  The service url from the request.
     * @param channelId   The ID of the channel to validate.
     * @param authConfig  The authentication configuration.
     * @return A CompletableFuture representing the asynchronous operation.
     *
     *         On join:
     * @throws AuthenticationException Authentication failed.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(
        String authHeader,
        CredentialProvider credentials,
        String serviceUrl,
        String channelId,
        AuthenticationConfiguration authConfig
    ) {
        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
            getTokenValidationParameters(),
            getOpenIdMetaDataUrl(),
            AuthenticationConstants.ALLOWED_SIGNING_ALGORITHMS
        );

        return tokenExtractor.getIdentity(authHeader, channelId, authConfig.requiredEndorsements())
            .thenCompose(identity -> validateIdentity(identity, credentials, serviceUrl));
    }

    /**
     * Validate the ClaimsIdentity as sent from a Bot Framework Government Channel
     * Service.
     *
     * @param identity    The claims identity to validate.
     * @param credentials The user defined set of valid credentials, such as the
     *                    AppId.
     * @param serviceUrl  The service url from the request.
     * @return A CompletableFuture representing the asynchronous operation.
     *
     *         On join:
     * @throws AuthenticationException Validation failed.
     */
    public static CompletableFuture<ClaimsIdentity> validateIdentity(
        ClaimsIdentity identity,
        CredentialProvider credentials,
        String serviceUrl
    ) {

        CompletableFuture<ClaimsIdentity> result = new CompletableFuture<>();

        if (identity == null || !identity.isAuthenticated()) {
            result.completeExceptionally(new AuthenticationException("Invalid Identity"));
            return result;
        }

        if (
            !StringUtils.equalsIgnoreCase(
                identity.getIssuer(),
                GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER
            )
        ) {

            result.completeExceptionally(new AuthenticationException("Wrong Issuer"));
            return result;
        }

        // The AppId from the claim in the token must match the AppId specified by the
        // developer. Note that
        // the Bot Framework uses the Audience claim ("aud") to pass the AppID.
        String appIdFromAudienceClaim =
            identity.claims().get(AuthenticationConstants.AUDIENCE_CLAIM);
        if (StringUtils.isEmpty(appIdFromAudienceClaim)) {
            // Claim is present, but doesn't have a value. Not Authorized.
            result.completeExceptionally(new AuthenticationException("No Audience Claim"));
            return result;
        }

        // Now check that the AppID in the claim set matches
        // what we're looking for. Note that in a multi-tenant bot, this value
        // comes from developer code that may be reaching out to a service, hence the
        // Async validation.

        return credentials.isValidAppId(appIdFromAudienceClaim).thenApply(isValid -> {
            if (!isValid) {
                throw new AuthenticationException(
                    String.format("Invalid AppId passed on token: '%s'.", appIdFromAudienceClaim)
                );
            }

            String serviceUrlClaim =
                identity.claims().get(AuthenticationConstants.SERVICE_URL_CLAIM);
            if (StringUtils.isEmpty(serviceUrl)) {
                throw new AuthenticationException(
                    String.format("Invalid serviceurl passed on token: '%s'.", serviceUrlClaim)
                );
            }

            if (!StringUtils.equals(serviceUrl, serviceUrlClaim)) {
                throw new AuthenticationException(
                    String.format("serviceurl doesn't match claim: '%s'.", serviceUrlClaim)
                );
            }

            return identity;
        });
    }
}
