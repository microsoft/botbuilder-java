// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.JWT;
import com.microsoft.bot.connector.Async;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Validates JWT tokens sent to and from a Skill.
 */
@SuppressWarnings("PMD")
public final class SkillValidation {

    private SkillValidation() {

    }

    /// <summary>
    /// TO SKILL FROM BOT and TO BOT FROM SKILL: Token validation parameters when
    /// connecting a bot to a skill.
    /// </summary>
    private static final TokenValidationParameters TOKENVALIDATIONPARAMETERS = new TokenValidationParameters(true,
            Stream.of(
                    // Auth v3.1, 1.0 token
                    "https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/",
                    // Auth v3.1, 2.0 token
                    "https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0",
                    // Auth v3.2, 1.0 token
                    "https://sts.windows.net/f8cdef31-a31e-4b4a-93e4-5f571e91255a/",
                    // Auth v3.2, 2.0 token
                    "https://login.microsoftonline.com/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0",
                    // Auth for US Gov, 1.0 token
                    "https://sts.windows.net/cab8a31a-1906-4287-a0d8-4eef66b95f6e/",
                    // Auth for US Gov, 2.0 token
                    "https://login.microsoftonline.us/cab8a31a-1906-4287-a0d8-4eef66b95f6e/v2.0",
                    // Auth for US Gov, 1.0 token
                    "https://login.microsoftonline.us/f8cdef31-a31e-4b4a-93e4-5f571e91255a/",
                    // Auth for US Gov, 2.0 token
                    "https://login.microsoftonline.us/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0")
                    .collect(Collectors.toList()),
            false, // Audience validation takes place manually in code.
            true, Duration.ofMinutes(5), true);

    /**
     * Determines if a given Auth header is from from a skill to bot or bot to skill
     * request.
     *
     * @param authHeader Bearer Token, in the "Bearer [Long String]" Format.
     * @return True, if the token was issued for a skill to bot communication.
     *         Otherwise, false.
     */
    public static boolean isSkillToken(String authHeader) {
        if (!JwtTokenValidation.isValidTokenFormat(authHeader)) {
            return false;
        }

        // We know is a valid token, split it and work with it:
        // [0] = "Bearer"
        // [1] = "[Big Long String]"
        String bearerToken = authHeader.split(" ")[1];

        // Parse token
        ClaimsIdentity identity = new ClaimsIdentity(JWT.decode(bearerToken));

        return isSkillClaim(identity.claims());
    }

    /**
     * Checks if the given list of claims represents a skill.
     *
     * A skill claim should contain: An {@link AuthenticationConstants.VERSION_CLAIM}
     * claim. An {@link AuthenticationConstants.AUTIENCE_CLAIM} claim. An
     * {@link AuthenticationConstants.APPID_CLAIM} claim (v1) or an a
     * {@link AuthenticationConstants.AUTHORIZED_PARTY} claim (v2). And the appId
     * claim should be different than the audience claim. When a channel (webchat,
     * teams, etc.) invokes a bot, the {@link AuthenticationConstants.AUTIENCE_CLAIM}
     * is set to {@link AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER} but
     * when a bot calls another bot, the audience claim is set to the appId of the
     * bot being invoked. The protocol supports v1 and v2 tokens: For v1 tokens, the
     * {@link AuthenticationConstants.APPID_CLAIM} is present and set to the app Id
     * of the calling bot. For v2 tokens, the
     * {@link AuthenticationConstants.AUTHORIZED_PARTY} is present and set to the app
     * Id of the calling bot.
     *
     * @param claims A list of claims.
     *
     * @return True if the list of claims is a skill claim, false if is not.
     */
    public static Boolean isSkillClaim(Map<String, String> claims) {

        for (Map.Entry<String, String> entry : claims.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(AuthenticationConstants.ANONYMOUS_SKILL_APPID)
                    && entry.getKey().equals(AuthenticationConstants.APPID_CLAIM)) {
                return true;
            }
        }

        Optional<Map.Entry<String, String>> version = claims.entrySet().stream()
                .filter((x) -> x.getKey().equals(AuthenticationConstants.VERSION_CLAIM)).findFirst();
        if (!version.isPresent()) {
            // Must have a version claim.
            return false;
        }

        Optional<Map.Entry<String, String>> audience = claims.entrySet().stream()
                .filter((x) -> x.getKey().equals(AuthenticationConstants.AUDIENCE_CLAIM)).findFirst();

        if (!audience.isPresent()
                || AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER.equals(audience.get().getValue())) {
            // The audience is https://api.botframework.com and not an appId.
            return false;
        }

        String appId = JwtTokenValidation.getAppIdFromClaims(claims);
        if (StringUtils.isBlank(appId)) {
            return false;
        }

        // Skill claims must contain and app ID and the AppID must be different than the
        // audience.
        return !StringUtils.equals(appId, audience.get().getValue());
    }

    /**
     * Validates that the incoming Auth Header is a token sent from a bot to a skill
     * or from a skill to a bot.
     *
     * @param authHeader      The raw HTTP header in the format: "Bearer
     *                        [longString]".
     * @param credentials     The user defined set of valid credentials, such as the
     *                        AppId.
     * @param channelProvider The channelService value that distinguishes public
     *                        Azure from US Government Azure.
     * @param channelId       The ID of the channel to validate.
     * @param authConfig      The authentication configuration.
     *
     * @return A {@link ClaimsIdentity} instance if the validation is successful.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateChannelToken(String authHeader,
            CredentialProvider credentials, ChannelProvider channelProvider, String channelId,
            AuthenticationConfiguration authConfig) {
        if (authConfig == null) {
            return Async.completeExceptionally(new IllegalArgumentException("authConfig cannot be null."));
        }

        String openIdMetadataUrl = channelProvider != null && channelProvider.isGovernment()
                ? GovernmentAuthenticationConstants.TO_BOT_FROM_EMULATOR_OPENID_METADATA_URL
                : AuthenticationConstants.TO_BOT_FROM_EMULATOR_OPENID_METADATA_URL;

        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(TOKENVALIDATIONPARAMETERS, openIdMetadataUrl,
                AuthenticationConstants.ALLOWED_SIGNING_ALGORITHMS);

        return tokenExtractor.getIdentity(authHeader, channelId, authConfig.requiredEndorsements())
        .thenCompose(identity -> {
            return validateIdentity(identity, credentials).thenCompose(result -> {
                return CompletableFuture.completedFuture(identity);
            });
        });
    }

    /**
     * Helper to validate a skills ClaimsIdentity.
     *
     * @param identity    The ClaimsIdentity to validate.
     * @param credentials The CredentialProvider.
     * @return Nothing if success, otherwise a CompletionException
     */
    public static CompletableFuture<Void> validateIdentity(ClaimsIdentity identity, CredentialProvider credentials) {
        if (identity == null) {
            // No valid identity. Not Authorized.
            return Async.completeExceptionally(new AuthenticationException("Invalid Identity"));
        }

        if (!identity.isAuthenticated()) {
            // The token is in some way invalid. Not Authorized.
            return Async.completeExceptionally(new AuthenticationException("Token Not Authenticated"));
        }

        Optional<Map.Entry<String, String>> versionClaim = identity.claims().entrySet().stream()
                .filter(item -> StringUtils.equals(AuthenticationConstants.VERSION_CLAIM, item.getKey())).findFirst();
        if (!versionClaim.isPresent()) {
            // No version claim
            return Async.completeExceptionally(new AuthenticationException(
                    AuthenticationConstants.VERSION_CLAIM + " claim is required on skill Tokens."));
        }

        // Look for the "aud" claim, but only if issued from the Bot Framework
        Optional<Map.Entry<String, String>> audienceClaim = identity.claims().entrySet().stream()
                .filter(item -> StringUtils.equals(AuthenticationConstants.AUDIENCE_CLAIM, item.getKey())).findFirst();
        if (!audienceClaim.isPresent() || StringUtils.isEmpty(audienceClaim.get().getValue())) {
            // Claim is not present or doesn't have a value. Not Authorized.
            return Async.completeExceptionally(new AuthenticationException(
                    AuthenticationConstants.AUDIENCE_CLAIM + " claim is required on skill Tokens."));
        }

        String appId = JwtTokenValidation.getAppIdFromClaims(identity.claims());
        if (StringUtils.isEmpty(appId)) {
            return Async.completeExceptionally(new AuthenticationException("Invalid appId."));
        }

        return credentials.isValidAppId(audienceClaim.get().getValue()).thenApply(isValid -> {
            if (!isValid) {
                throw new AuthenticationException("Invalid audience.");
            }
            return null;
        });
    }

    /**
     * Creates a ClaimsIdentity for an anonymous (unauthenticated) skill.
     *
     * @return A ClaimsIdentity instance with authentication type set to
     *         AuthenticationConstants.AnonymousAuthType and a reserved
     *         AuthenticationConstants.AnonymousSkillAppId claim.
     */
    public static ClaimsIdentity createAnonymousSkillClaim() {
        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.APPID_CLAIM, AuthenticationConstants.ANONYMOUS_SKILL_APPID);
        return new ClaimsIdentity(AuthenticationConstants.ANONYMOUS_AUTH_TYPE,
                AuthenticationConstants.ANONYMOUS_AUTH_TYPE, claims);
    }
}
