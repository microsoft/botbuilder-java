// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.time.Duration;
import java.util.List;

public class TokenValidationParameters {
    public boolean validateIssuer;
    public List<String> validIssuers;
    public boolean validateAudience;
    public boolean validateLifetime;
    public Duration clockSkew;
    public boolean requireSignedTokens;

    public TokenValidationParameters() {
    }

    public TokenValidationParameters(TokenValidationParameters other) {
        this(other.validateIssuer, other.validIssuers, other.validateAudience, other.validateLifetime, other.clockSkew, other.requireSignedTokens);
    }

    public TokenValidationParameters(boolean validateIssuer, List<String> validIssuers, boolean validateAudience, boolean validateLifetime, Duration clockSkew, boolean requireSignedTokens) {
        this.validateIssuer = validateIssuer;
        this.validIssuers = validIssuers;
        this.validateAudience = validateAudience;
        this.validateLifetime = validateLifetime;
        this.clockSkew = clockSkew;
        this.requireSignedTokens = requireSignedTokens;
    }
}
