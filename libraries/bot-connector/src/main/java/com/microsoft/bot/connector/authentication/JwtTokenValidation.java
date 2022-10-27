// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.RoleTypes;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Contains helper methods for authenticating incoming HTTP requests.
 */
public final class JwtTokenValidation {
    private JwtTokenValidation() {

    }

    /**
     * Authenticates the request and add's the activity's
     * {@link Activity#getServiceUrl()} to the set of trusted URLs.
     *
     * @param activity        The incoming Activity from the Bot Framework or the
     *                        Emulator
     * @param authHeader      The Bearer token included as part of the request
     * @param credentials     The bot's credential provider.
     * @param channelProvider The bot's channel service provider.
     * @return A task that represents the work queued to execute.
     * @throws AuthenticationException Throws on auth failed.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateRequest(
        Activity activity,
        String authHeader,
        CredentialProvider credentials,
        ChannelProvider channelProvider
    ) {
        return authenticateRequest(
            activity, authHeader, credentials, channelProvider, new AuthenticationConfiguration()
        );
    }

    /**
     * Authenticates the request and add's the activity's
     * {@link Activity#getServiceUrl()} to the set of trusted URLs.
     *
     * @param activity        The incoming Activity from the Bot Framework or the
     *                        Emulator
     * @param authHeader      The Bearer token included as part of the request
     * @param credentials     The bot's credential provider.
     * @param channelProvider The bot's channel service provider.
     * @param authConfig      The optional authentication configuration.
     * @return A task that represents the work queued to execute.
     * @throws AuthenticationException Throws on auth failed.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateRequest(
        Activity activity,
        String authHeader,
        CredentialProvider credentials,
        ChannelProvider channelProvider,
        AuthenticationConfiguration authConfig
    ) {
        if (authConfig == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException("authConfig cannot be null")
            );
        }

        if (StringUtils.isBlank(authHeader)) {
            // No auth header was sent. We might be on the anonymous code path.
            return credentials.isAuthenticationDisabled().thenApply(isAuthDisable -> {
                if (!isAuthDisable) {
                    // No Auth Header. Auth is required. Request is not authorized.
                    throw new AuthenticationException("No Auth Header. Auth is required.");
                }

                if (activity.getChannelId() != null
                    && activity.getChannelId().equals(Channels.EMULATOR)
                    && activity.getRecipient() != null
                    && activity.getRecipient().getRole().equals(RoleTypes.SKILL)) {
                    return SkillValidation.createAnonymousSkillClaim();
                }

                // In the scenario where Auth is disabled, we still want to have the
                // IsAuthenticated flag set in the ClaimsIdentity. To do this requires
                // adding in an empty claim.
                return new ClaimsIdentity(AuthenticationConstants.ANONYMOUS_AUTH_TYPE);
            });
        }

        // Go through the standard authentication path. This will throw
        // AuthenticationException if
        // it fails.
        return JwtTokenValidation.validateAuthHeader(
            authHeader, credentials, channelProvider, activity.getChannelId(),
            activity.getServiceUrl(), authConfig
        );
    }

    /**
     * Validates the authentication header of an incoming request.
     *
     * @param authHeader      The authentication header to validate.
     * @param credentials     The bot's credential provider.
     * @param channelProvider The bot's channel service provider.
     * @param channelId       The ID of the channel that sent the request.
     * @param serviceUrl      The service URL for the activity.
     * @return A task that represents the work queued to execute.
     *
     *         On Call:
     * @throws IllegalArgumentException Incorrect arguments supplied
     *
     *                                  On join:
     * @throws AuthenticationException  Authentication Error
     */
    public static CompletableFuture<ClaimsIdentity> validateAuthHeader(
        String authHeader,
        CredentialProvider credentials,
        ChannelProvider channelProvider,
        String channelId,
        String serviceUrl
    ) {
        return validateAuthHeader(
            authHeader, credentials, channelProvider, channelId, serviceUrl,
            new AuthenticationConfiguration()
        );
    }

    /**
     * Validates the authentication header of an incoming request.
     *
     * @param authHeader      The authentication header to validate.
     * @param credentials     The bot's credential provider.
     * @param channelProvider The bot's channel service provider.
     * @param channelId       The ID of the channel that sent the request.
     * @param serviceUrl      The service URL for the activity.
     * @param authConfig      The authentication configuration.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException Incorrect arguments supplied
     * @throws AuthenticationException  Authentication Error
     */
    public static CompletableFuture<ClaimsIdentity> validateAuthHeader(
        String authHeader,
        CredentialProvider credentials,
        ChannelProvider channelProvider,
        String channelId,
        String serviceUrl,
        AuthenticationConfiguration authConfig
    ) {
        if (StringUtils.isEmpty(authHeader)) {
            return Async.completeExceptionally(
                new IllegalArgumentException("No authHeader present. Auth is required."));
        }

        return authenticateToken(authHeader, credentials, channelProvider, channelId, serviceUrl, authConfig)
            .thenApply(identity -> {
                    validateClaims(authConfig, identity.claims());
                    return identity;
            }
        );
    }

    private static CompletableFuture<Void> validateClaims(
        AuthenticationConfiguration authConfig,
        Map<String, String> claims
    ) {
        if (authConfig.getClaimsValidator() != null) {
            return authConfig.getClaimsValidator().validateClaims(claims);
        } else if (SkillValidation.isSkillClaim(claims)) {
            return Async.completeExceptionally(
                new RuntimeException("ClaimValidator is required for validation of Skill Host calls")
            );
        }
        return CompletableFuture.completedFuture(null);
    }

    private static CompletableFuture<ClaimsIdentity> authenticateToken(
        String authHeader,
        CredentialProvider credentials,
        ChannelProvider channelProvider,
        String channelId,
        String serviceUrl,
        AuthenticationConfiguration authConfig
    ) {
        if (SkillValidation.isSkillToken(authHeader)) {
            return SkillValidation.authenticateChannelToken(
                authHeader, credentials, channelProvider, channelId, authConfig);
        }
        boolean usingEmulator = EmulatorValidation.isTokenFromEmulator(authHeader);
        if (usingEmulator) {
            return EmulatorValidation
                .authenticateToken(authHeader, credentials, channelProvider, channelId, authConfig);
        } else if (channelProvider == null || channelProvider.isPublicAzure()) {
            // No empty or null check. Empty can point to issues. Null checks only.
            if (serviceUrl != null) {
                return ChannelValidation
                    .authenticateToken(authHeader, credentials, channelId, serviceUrl, authConfig);
            } else {
                return ChannelValidation
                    .authenticateToken(authHeader, credentials, channelId, authConfig);
            }
        } else if (channelProvider.isGovernment()) {
            return GovernmentChannelValidation
                .authenticateToken(authHeader, credentials, serviceUrl, channelId, authConfig);
        } else {
            return EnterpriseChannelValidation.authenticateToken(
                authHeader, credentials, channelProvider, serviceUrl, channelId, authConfig
            );
        }

    }

    /**
     * Gets the AppId from claims.
     *
     * <p>
     * In v1 tokens the AppId is in the the AppIdClaim claim. In v2 tokens the AppId
     * is in the AuthorizedParty claim.
     * </p>
     *
     * @param claims The map of claims.
     * @return The value of the appId claim if found (null if it can't find a
     *         suitable claim).
     * @throws IllegalArgumentException Missing claims
     */
    public static String getAppIdFromClaims(Map<String, String> claims) throws IllegalArgumentException {
        if (claims == null) {
            throw new IllegalArgumentException("claims");
        }

        String appId = null;

        String tokenVersion = claims.get(AuthenticationConstants.VERSION_CLAIM);
        if (StringUtils.isEmpty(tokenVersion) || tokenVersion.equalsIgnoreCase("1.0")) {
            // either no Version or a version of "1.0" means we should look for the claim in
            // the "appid" claim.
            appId = claims.get(AuthenticationConstants.APPID_CLAIM);
        } else {
            // "2.0" puts the AppId in the "azp" claim.
            appId = claims.get(AuthenticationConstants.AUTHORIZED_PARTY);
        }

        return appId;
    }

    /**
     * Internal helper to check if the token has the shape we expect "Bearer [big long string]".
     *
     * @param authHeader A string containing the token header.
     * @return True if the token is valid, false if not.
     */
    public static boolean isValidTokenFormat(String authHeader) {
        if (StringUtils.isEmpty(authHeader)) {
            // No token, not valid.
            return false;
        }

        String[] parts = authHeader.split(" ");
        if (parts.length != 2) {
            // Tokens MUST have exactly 2 parts. If we don't have 2 parts, it's not a valid token
            return false;
        }

        // We now have an array that should be:
        // [0] = "Bearer"
        // [1] = "[Big Long String]"
        String authScheme = parts[0];
        if (!StringUtils.equals(authScheme, "Bearer")) {
            // The scheme MUST be "Bearer"
            return false;
        }

        return true;
    }
}
