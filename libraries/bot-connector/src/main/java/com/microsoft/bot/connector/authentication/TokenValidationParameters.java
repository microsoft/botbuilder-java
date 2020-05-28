// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.time.Duration;
import java.util.List;

/**
 * Contains a set of parameters that are used when validating a token.
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public class TokenValidationParameters {
    /**
     * Control if the issuer will be validated during token validation.
     */
    public boolean validateIssuer;

    /**
     * Contains valid issuers that will be used to check against the token's issuer.
     */
    public List<String> validIssuers;

    /**
     * Control if the audience will be validated during token validation.
     */
    public boolean validateAudience;

    /**
     * Control if the lifetime will be validated during token validation.
     */
    public boolean validateLifetime;

    /**
     * Clock skew to apply when validating a time.
     */
    public Duration clockSkew;

    /**
     * Value indicating whether a token can be considered valid if not signed.
     */
    public boolean requireSignedTokens;

    /**
     * Optional (and not recommended) Function to return OpenIdMetaData resolver
     * for a given url.
     */
    public OpenIdMetadataResolver issuerSigningKeyResolver;

    /**
     * True to validate the signing cert.
     */
    public boolean validateIssuerSigningKey = true;

    /**
     * Default parameters.
     */
    public TokenValidationParameters() {
    }

    /**
     * Copy constructor.
     * 
     * @param other The TokenValidationParameters to copy.
     */
    public TokenValidationParameters(TokenValidationParameters other) {
        this(
            other.validateIssuer,
            other.validIssuers,
            other.validateAudience,
            other.validateLifetime,
            other.clockSkew,
            other.requireSignedTokens
        );
        this.issuerSigningKeyResolver = other.issuerSigningKeyResolver;
        this.validateIssuerSigningKey = other.validateIssuerSigningKey;
    }

    /**
     *
     * @param validateIssuer      Control if the issuer will be validated during
     *                            token validation.
     * @param validIssuers        Contains valid issuers that will be used to check
     *                            against the token's issuer.
     * @param validateAudience    Control if the audience will be validated during
     *                            token validation.
     * @param validateLifetime    Control if the lifetime will be validated during
     *                            token validation.
     * @param clockSkew           Clock skew to apply when validating a time.
     * @param requireSignedTokens Value indicating whether a token can be considered
     *                            valid if not signed.
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public TokenValidationParameters(
        boolean validateIssuer,
        List<String> validIssuers,
        boolean validateAudience,
        boolean validateLifetime,
        Duration clockSkew,
        boolean requireSignedTokens
    ) {
        this.validateIssuer = validateIssuer;
        this.validIssuers = validIssuers;
        this.validateAudience = validateAudience;
        this.validateLifetime = validateLifetime;
        this.clockSkew = clockSkew;
        this.requireSignedTokens = requireSignedTokens;
    }
}
