// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Validates and Examines JWT tokens from the Bot Framework Emulator.
 */
public final class EmulatorValidation {
    private EmulatorValidation() {

    }

    /**
     * TO BOT FROM EMULATOR.
     * @return Token validation parameters when connecting to a channel.
     */
    public static TokenValidationParameters getTokenValidationParameters() {
        TokenValidationParameters tokenValidationParameters = new TokenValidationParameters();
        tokenValidationParameters.validateIssuer = true;

        ArrayList<String> validIssuers = new ArrayList<String>();
        // Auth v3.1, 1.0
        validIssuers.add("https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/");
        // Auth v3.1, 2.0
        validIssuers.add("https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0");
        // Auth v3.2, 1.0
        validIssuers.add("https://sts.windows.net/f8cdef31-a31e-4b4a-93e4-5f571e91255a/");
        // Auth v3.2, 2.0
        validIssuers.add("https://login.microsoftonline.com/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0");
        // Auth for US Gov, 1.0
        validIssuers.add("https://sts.windows.net/cab8a31a-1906-4287-a0d8-4eef66b95f6e/");
        // Auth for US Gov, 2.0
        validIssuers.add("https://login.microsoftonline.us/cab8a31a-1906-4287-a0d8-4eef66b95f6e/v2.0");
        tokenValidationParameters.validIssuers = validIssuers;

        tokenValidationParameters.validateAudience = false;
        tokenValidationParameters.validateLifetime = true;
        tokenValidationParameters.clockSkew = Duration.ofMinutes(AuthenticationConstants.DEFAULT_CLOCKSKEW_MINUTES);

        tokenValidationParameters.requireSignedTokens = true;

        return tokenValidationParameters;
    }

    /**
     * Determines if a given Auth header is from the Bot Framework Emulator.
     *
     * @param authHeader Bearer Token, in the "Bearer [Long String]" Format.
     * @return True, if the token was issued by the Emulator. Otherwise, false.
     */
    public static Boolean isTokenFromEmulator(String authHeader) {
        // The Auth Header generally looks like this:
        // "Bearer eyJ0e[...Big Long String...]XAiO"
        if (StringUtils.isEmpty(authHeader)) {
            // No token. Can't be an emulator token.
            return false;
        }

        String[] parts = authHeader.split(" ");
        if (parts.length != 2) {
            // Emulator tokens MUST have exactly 2 parts. If we don't have 2 parts, it's not
            // an emulator token
            return false;
        }

        String schema = parts[0];
        String token = parts[1];

        if (!schema.equalsIgnoreCase("bearer")) {
            // The scheme from the emulator MUST be "Bearer"
            return false;
        }

        // Parse the Big Long String into an actual token.
        try {
            DecodedJWT decodedJWT = JWT.decode(token);

            // Is there an Issuer?
            if (StringUtils.isEmpty(decodedJWT.getIssuer())) {
                // No Issuer, means it's not from the Emulator.
                return false;
            }

            // Is the token issues by a source we consider to be the emulator?
            // Not a Valid Issuer. This is NOT a Bot Framework Emulator Token.
            return getTokenValidationParameters().validIssuers.contains(decodedJWT.getIssuer());
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework
     * Emulator. A token issued by the Bot Framework will FAIL this check. Only
     * Emulator tokens will pass.
     *
     * @param authHeader      The raw HTTP header in the format: "Bearer
     *                        [longString]".
     * @param credentials     The user defined set of valid credentials, such as the
     *                        AppId.
     * @param channelProvider The channelService value that distinguishes public
     *                        Azure from US Government Azure.
     * @param channelId       The ID of the channel to validate.
     * @return A valid ClaimsIdentity.
     *         <p>
     *         On join:
     * @throws AuthenticationException A token issued by the Bot Framework will FAIL
     *                                 this check. Only Emulator tokens will pass.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(
        String authHeader,
        CredentialProvider credentials,
        ChannelProvider channelProvider,
        String channelId
    ) {
        return authenticateToken(
            authHeader, credentials, channelProvider, channelId, new AuthenticationConfiguration()
        );
    }

    /**
     * Validate the incoming Auth Header as a token sent from the Bot Framework
     * Emulator. A token issued by the Bot Framework will FAIL this check. Only
     * Emulator tokens will pass.
     *
     * @param authHeader      The raw HTTP header in the format: "Bearer
     *                        [longString]".
     * @param credentials     The user defined set of valid credentials, such as the
     *                        AppId.
     * @param channelProvider The channelService value that distinguishes public
     *                        Azure from US Government Azure.
     * @param channelId       The ID of the channel to validate.
     * @param authConfig      The authentication configuration.
     * @return A valid ClaimsIdentity.
     *         <p>
     *         On join:
     * @throws AuthenticationException A token issued by the Bot Framework will FAIL
     *                                 this check. Only Emulator tokens will pass.
     */
    public static CompletableFuture<ClaimsIdentity> authenticateToken(
        String authHeader,
        CredentialProvider credentials,
        ChannelProvider channelProvider,
        String channelId,
        AuthenticationConfiguration authConfig
    ) {
        String openIdMetadataUrl = channelProvider != null && channelProvider.isGovernment()
            ? GovernmentAuthenticationConstants.TO_BOT_FROM_EMULATOR_OPENID_METADATA_URL
            : AuthenticationConstants.TO_BOT_FROM_EMULATOR_OPENID_METADATA_URL;

        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
            getTokenValidationParameters(),
            openIdMetadataUrl,
            AuthenticationConstants.ALLOWED_SIGNING_ALGORITHMS
        );

        return tokenExtractor.getIdentity(authHeader, channelId, authConfig.requiredEndorsements())
            .thenCompose(identity -> {
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
                if (!identity.claims().containsKey(AuthenticationConstants.VERSION_CLAIM)) {
                    throw new AuthenticationException(
                        String.format(
                            "'%s' claim is required on Emulator Tokens.",
                            AuthenticationConstants.VERSION_CLAIM
                        )
                    );
                }

                String tokenVersion = identity.claims().get(AuthenticationConstants.VERSION_CLAIM);
                String appId;

                // The Emulator, depending on Version, sends the AppId via either the
                // appid claim (Version 1) or the Authorized Party claim (Version 2).
                if (StringUtils.isEmpty(tokenVersion) || tokenVersion.equalsIgnoreCase("1.0")) {
                    // either no Version or a version of "1.0" means we should look for
                    // the claim in the "appid" claim.
                    if (!identity.claims().containsKey(AuthenticationConstants.APPID_CLAIM)) {
                        // No claim around AppID. Not Authorized.
                        throw new AuthenticationException(
                            String.format(
                                "'%s' claim is required on Emulator Token version '1.0'.",
                                AuthenticationConstants.APPID_CLAIM
                            )
                        );
                    }

                    appId = identity.claims().get(AuthenticationConstants.APPID_CLAIM);
                } else if (tokenVersion.equalsIgnoreCase("2.0")) {
                    // Emulator, "2.0" puts the AppId in the "azp" claim.
                    if (!identity.claims().containsKey(AuthenticationConstants.AUTHORIZED_PARTY)) {
                        // No claim around AppID. Not Authorized.
                        throw new AuthenticationException(
                            String.format(
                                "'%s' claim is required on Emulator Token version '2.0'.",
                                AuthenticationConstants.AUTHORIZED_PARTY
                            )
                        );
                    }

                    appId = identity.claims().get(AuthenticationConstants.AUTHORIZED_PARTY);
                } else {
                    // Unknown Version. Not Authorized.
                    throw new AuthenticationException(
                        String.format("Unknown Emulator Token version '%s'.", tokenVersion)
                    );
                }

                return credentials.isValidAppId(appId).thenApply(isValid -> {
                    if (!isValid) {
                        throw new AuthenticationException(
                            String.format("Invalid AppId passed on token: '%s'.", appId)
                        );
                    }

                    return identity;
                });
            });
    }
}
