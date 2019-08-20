// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashMap;
import java.util.Map;

public class ClaimsIdentity {
    private String issuer;
    private Map<String, String> claims;

    private ClaimsIdentity() {
        this("", new HashMap<>());
    }

    public ClaimsIdentity(String authIssuer) {
        this(authIssuer, new HashMap<>());
    }

    public ClaimsIdentity(String authIssuer, Map<String, String> claims) {
        this.issuer = authIssuer;
        this.claims = claims;
    }

    public ClaimsIdentity(DecodedJWT jwt) {
        claims = new HashMap<>();
        if (jwt.getClaims() != null) {
            jwt.getClaims().forEach((k, v) -> claims.put(k, v.asString()));
        }
        issuer = jwt.getIssuer();
    }

    public boolean isAuthenticated() {
        return this.issuer != null && !this.issuer.isEmpty();
    }

    public Map<String, String> claims() {
        return this.claims;
    }

    public String getIssuer() {
        return issuer;
    }
}
