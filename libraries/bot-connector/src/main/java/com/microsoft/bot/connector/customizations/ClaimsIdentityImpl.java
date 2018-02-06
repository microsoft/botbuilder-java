package com.microsoft.bot.connector.customizations;

import java.util.HashMap;
import java.util.Map;

public class ClaimsIdentityImpl implements ClaimsIdentity {
    private String issuer;
    private Map<String, String> claims;

    public ClaimsIdentityImpl() {
        this("", new HashMap<>());
    }

    public ClaimsIdentityImpl(String authType) {
        this(authType, new HashMap<>());
    }

    public ClaimsIdentityImpl(String authType, Map<String, String> claims) {
        this.issuer = authType;
        this.claims = claims;
    }

    @Override
    public boolean isAuthenticated() {
        return this.issuer != null && !this.issuer.isEmpty();
    }

    @Override
    public Map<String, String> claims() {
        return this.claims;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }
}
