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

    public OAuthConfiguration(String authority, String scope) {
        this.authority = authority;
        this.scope = scope;
    }

    /**
     * Sets oAuth Authority for authentication.
     *
     * @param authority
     * @return This OAuthConfiguration object.
     */
    public OAuthConfiguration withAuthority(String authority) {
        this.authority = authority;
        return this;
    }

    /**
     * Gets oAuth Authority for authentication.
     *
     * @return OAuth Authority for authentication.
     */
    public String authority() {
        return authority;
    }

    /**
     * Sets oAuth scope for authentication.
     *
     * @param scope
     * @return This OAuthConfiguration object.
     */
    public OAuthConfiguration withScope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Gets oAuth scope for authentication.
     *
     * @return OAuth scope for authentication.
     */
    public String scope() {
        return scope;
    }
}
