// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple wrapper around for a JWT claims identity.
 */
public class ClaimsIdentity {
    private String issuer;
    private String type;
    private Map<String, String> claims;

    private ClaimsIdentity() {
        this("", new HashMap<>());
    }

    /**
     * Manually construct with auth issuer.
     * 
     * @param withAuthIssuer The auth issuer.
     */
    public ClaimsIdentity(String withAuthIssuer) {
        this(withAuthIssuer, new HashMap<>());
    }

    /**
     * Manually construct with issuer and claims.
     * 
     * @param withAuthIssuer The auth issuer.
     * @param withClaims     A Map of claims.
     */
    public ClaimsIdentity(String withAuthIssuer, Map<String, String> withClaims) {
        this(withAuthIssuer, null, withClaims);
    }

    /**
     * Manually construct with issuer and claims.
     *
     * @param withAuthIssuer The auth issuer.
     * @param withType The auth type.
     * @param withClaims     A Map of claims.
     */
    public ClaimsIdentity(String withAuthIssuer, String withType, Map<String, String> withClaims) {
        issuer = withAuthIssuer;
        type = withType;
        claims = withClaims;
    }

    /**
     * Extract data from an auth0 JWT.
     * 
     * @param jwt The decoded JWT.
     */
    public ClaimsIdentity(DecodedJWT jwt) {
        claims = new HashMap<>();
        if (jwt.getClaims() != null) {
            jwt.getClaims().forEach((k, v) -> claims.put(k, v.asString()));
        }
        issuer = jwt.getIssuer();
        type = jwt.getType();
    }

    /**
     * Gets whether the claim is authenticated.
     * 
     * @return true if authenticated.
     */
    public boolean isAuthenticated() {
        return this.issuer != null && !this.issuer.isEmpty();
    }

    /**
     * The claims for this identity.
     * 
     * @return A Map of claims.
     */
    public Map<String, String> claims() {
        return this.claims;
    }

    /**
     * The issuer.
     * 
     * @return The issuer.
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * The type.
     *
     * @return The type.
     */
    public String getType() {
        return type;
    }
}
