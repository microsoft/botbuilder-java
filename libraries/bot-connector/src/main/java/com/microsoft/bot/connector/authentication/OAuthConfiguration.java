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
    private Boolean validateAuthority;

    /**
     * Construct with authority and scope.
     *
     * @param withAuthority The auth authority.
     * @param withScope     The auth scope.
     * @param withValidateAuthority Whether the Authority should be validated.
     */
    public OAuthConfiguration(String withAuthority, String withScope, Boolean withValidateAuthority) {
        this.authority = withAuthority;
        this.scope = withScope;
        this.validateAuthority = withValidateAuthority;
    }

    /**
     * Construct with authority and scope.
     *
     * @param withAuthority The auth authority.
     * @param withScope     The auth scope.
     */
    public OAuthConfiguration(String withAuthority, String withScope) {
        this(withAuthority, withScope, null);
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

    /**
     * Gets a value indicating whether the Authority should be validated.
     * @return Boolean value indicating whether the Authority should be validated.
     */
    public Boolean getValidateAuthority() {
        return validateAuthority;
    }

    /**
     * Sets a value indicating whether the Authority should be validated.
     * @param withValidateAuthority Boolean value indicating whether the Authority should be validated.
     */
    public void setValidateAuthority(Boolean withValidateAuthority) {
        this.validateAuthority = withValidateAuthority;
    }
}
