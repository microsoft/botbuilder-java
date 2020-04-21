// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

/**
 * Configuration for OAuth client credential authentication.
 */
public class OAuthConfiguration {
    private String scope;
    private String authority;

    /**
     * Construct with authority and scope.
     * 
     * @param withAuthority The auth authority.
     * @param withScope     The auth scope.
     */
    public OAuthConfiguration(String withAuthority, String withScope) {
        this.authority = withAuthority;
        this.scope = withScope;
    }

    /**
     * Sets oAuth Authority for authentication.
     *
     * @param withAuthority oAuth Authority for authentication.
     */
    public void setAuthority(String withAuthority) {
        authority = withAuthority;
    }

    /**
     * Gets oAuth Authority for authentication.
     *
     * @return OAuth Authority for authentication.
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Sets oAuth scope for authentication.
     *
     * @param withScope oAuth Authority for authentication.
     */
    public void setScope(String withScope) {
        scope = withScope;
    }

    /**
     * Gets oAuth scope for authentication.
     *
     * @return OAuth scope for authentication.
     */
    public String getScope() {
        return scope;
    }
}
