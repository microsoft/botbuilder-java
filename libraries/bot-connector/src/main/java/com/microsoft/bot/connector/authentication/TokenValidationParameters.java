// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.time.Duration;
import java.util.ArrayList;
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

    static TokenValidationParameters toBotFromEmulatorTokenValidationParameters() {
        return new TokenValidationParameters() {{
            this.validateIssuer = true;
            this.validIssuers = new ArrayList<String>() {{
                add("https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/");               // Auth v3.1, 1.0 token
                add("https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0"); // Auth v3.1, 2.0 token
                add("https://sts.windows.net/f8cdef31-a31e-4b4a-93e4-5f571e91255a/");               // Auth v3.2, 1.0 token
                add("https://login.microsoftonline.com/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0"); // Auth v3.2, 2.0 token
                add("https://sts.windows.net/cab8a31a-1906-4287-a0d8-4eef66b95f6e/");               // Auth for US Gov, 1.0 token
                add("https://login.microsoftonline.us/cab8a31a-1906-4287-a0d8-4eef66b95f6e/v2.0");  // Auth for US Gov, 2.0 token
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
                add(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER);
            }};
            this.validateAudience = false;
            this.validateLifetime = true;
            this.clockSkew = Duration.ofMinutes(5);
            this.requireSignedTokens = true;
        }};
    }
}
