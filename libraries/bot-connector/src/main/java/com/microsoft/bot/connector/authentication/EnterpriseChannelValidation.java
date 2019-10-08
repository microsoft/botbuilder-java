// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class EnterpriseChannelValidation {
    private static final TokenValidationParameters TOKENVALIDATIONPARAMETERS = new TokenValidationParameters() {{
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
     * Validate the incoming Auth Header as a token sent from a Bot Framework Channel Service.
     * A token issued by the Bot Framework will FAIL this check. Only Emulator tokens will pass.
     *
     * @param authHeader      The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials     The user defined set of valid credentials, such as the AppId.
     * @param channelProvider The channelService value that distinguishes public Azure from US Government Azure.
     * @param channelId       The ID of the channel to validate.
     * @return A valid ClaimsIdentity.
     *
     * On join:
     * @throws AuthenticationException A token issued by the Bot Framework will FAIL this check. Only Emulator tokens
     * will pass.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader,
                                                                      CredentialProvider credentials,
                                                                      ChannelProvider channelProvider,
                                                                      String serviceUrl,
                                                                      String channelId) {
        return authenticateToken(
            authHeader, credentials, channelProvider, serviceUrl, channelId, new AuthenticationConfiguration());
    }

    /**
     * Validate the incoming Auth Header as a token sent from a Bot Framework Channel Service.
     * A token issued by the Bot Framework will FAIL this check. Only Emulator tokens will pass.
     *
     * @param authHeader      The raw HTTP header in the format: "Bearer [longString]".
     * @param credentials     The user defined set of valid credentials, such as the AppId.
     * @param channelProvider The channelService value that distinguishes public Azure from US Government Azure.
     * @param channelId       The ID of the channel to validate.
     * @param authConfig      The authentication configuration.
     * @return A valid ClaimsIdentity.
     *
     * On join:
     * @throws AuthenticationException A token issued by the Bot Framework will FAIL this check. Only Emulator tokens
     * will pass.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(String authHeader,
                                                                      CredentialProvider credentials,
                                                                      ChannelProvider channelProvider,
                                                                      String serviceUrl,
                                                                      String channelId,
                                                                      AuthenticationConfiguration authConfig) {
        if (authConfig == null) {
            throw new IllegalArgumentException("Missing AuthenticationConfiguration");
        }

        return channelProvider.getChannelService()

            .thenCompose(channelService -> {
                JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
                    TOKENVALIDATIONPARAMETERS,
                    String.format(AuthenticationConstants.TO_BOT_FROM_ENTERPRISE_CHANNEL_OPENID_METADATA_URL_FORMAT,
                        channelService),
                    AuthenticationConstants.AllowedSigningAlgorithms);

                return tokenExtractor.getIdentity(authHeader, channelId, authConfig.requiredEndorsements());
            })

            .thenCompose(identity -> {
                if (identity == null) {
                    // No valid identity. Not Authorized.
                    throw new AuthenticationException("Invalid Identity");
                }

                return validateIdentity(identity, credentials, serviceUrl);
            });
    }

    public static CompletableFuture<ClaimsIdentity> validateIdentity(ClaimsIdentity identity,
                                                                     CredentialProvider credentials,
                                                                     String serviceUrl) {

        CompletableFuture<ClaimsIdentity> result = new CompletableFuture<>();

        // Validate the identity

        if (identity == null || !identity.isAuthenticated()) {
            result.completeExceptionally(new AuthenticationException("Invalid Identity"));
            return result;
        }

        if (!StringUtils.equalsIgnoreCase(identity.getIssuer(), AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER)) {
            result.completeExceptionally(new AuthenticationException("Wrong Issuer"));
            return result;
        }

        // The AppId from the claim in the token must match the AppId specified by the developer. Note that
        // the Bot Framework uses the Audience claim ("aud") to pass the AppID.
        String appIdFromAudienceClaim = identity.claims().get(AuthenticationConstants.AUDIENCE_CLAIM);
        if (StringUtils.isEmpty(appIdFromAudienceClaim)) {
            // Claim is present, but doesn't have a value. Not Authorized.
            result.completeExceptionally(new AuthenticationException("No Audience Claim"));
            return result;
        }

        // Now check that the AppID in the claim set matches
        // what we're looking for. Note that in a multi-tenant bot, this value
        // comes from developer code that may be reaching out to a service, hence the
        // Async validation.

        return credentials.isValidAppId(appIdFromAudienceClaim)
             .thenApply(isValid -> {
                 if (!isValid) {
                     throw new AuthenticationException(
                         String.format("Invalid AppId passed on token: '%s'.", appIdFromAudienceClaim));
                 }

                 String serviceUrlClaim = identity.claims().get(AuthenticationConstants.SERVICE_URL_CLAIM);
                 if (StringUtils.isEmpty(serviceUrl)) {
                     throw new AuthenticationException(
                         String.format("Invalid serviceurl passed on token: '%s'.", serviceUrlClaim));
                 }

                 if (!StringUtils.equals(serviceUrl, serviceUrlClaim)) {
                     throw new AuthenticationException(
                         String.format("serviceurl doesn't match claim: '%s'.", serviceUrlClaim));
                 }

                 return identity;
             });
    }
}
