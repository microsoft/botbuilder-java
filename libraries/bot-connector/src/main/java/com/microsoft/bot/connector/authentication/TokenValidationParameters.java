// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.bot.connector.authentication.AuthenticationConstants.ToBotFromChannelTokenIssuer;

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

    static TokenValidationParameters toBotFromEmulatorTokenValidationParameters() {
        return new TokenValidationParameters() {{
            this.validateIssuer = true;
            this.validIssuers = new ArrayList<String>() {{
                add("https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/");
                add("https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0");
                add("https://sts.windows.net/f8cdef31-a31e-4b4a-93e4-5f571e91255a/");
                add("https://login.microsoftonline.com/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0");
            }};
            this.validateAudience = false;
            this.validateLifetime = true;
            this.clockSkew = Duration.ofMinutes(5);
            this.requireSignedTokens = true;
        }};
    }

    static TokenValidationParameters toBotFromChannelTokenValidationParameters() {
        return new TokenValidationParameters() {{
            this.validateIssuer = true;
            this.validIssuers = new ArrayList<String>() {{
                add(ToBotFromChannelTokenIssuer);
            }};
            this.validateAudience = false;
            this.validateLifetime = true;
            this.clockSkew = Duration.ofMinutes(5);
            this.requireSignedTokens = true;
        }};
    }
}
