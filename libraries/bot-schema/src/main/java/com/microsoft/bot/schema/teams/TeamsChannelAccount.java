// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.microsoft.bot.schema.ChannelAccount;

/**
 * Teams channel account detailing user Azure Active Directory details.
 */
public class TeamsChannelAccount extends ChannelAccount {
    @JsonProperty(value = "givenName")
    private String givenName;

    @JsonProperty(value = "surname")
    private String surname;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "userPrincipalName")
    private String userPrincipalName;

    @JsonProperty(value = "userRole")
    private String userRole;

    @JsonProperty(value = "tenantId")
    private String tenantId;

    /**
     * Gets given name part of the user name.
     *
     * @return The users given name.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets given name part of the user name.
     *
     * @param withGivenName The users given name.
     */
    public void setGivenName(String withGivenName) {
        givenName = withGivenName;
    }

    /**
     * Gets surname part of the user name.
     *
     * @return The users surname.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets surname part of the user name.
     *
     * @param withSurname The users surname.
     */
    public void setSurname(String withSurname) {
        surname = withSurname;
    }

    /**
     * Gets email Id of the user.
     *
     * @return The users email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email Id of the user.
     *
     * @param withEmail The users email address.
     */
    public void setEmail(String withEmail) {
        email = withEmail;
    }

    /**
     * Gets unique user principal name.
     *
     * @return The users principal name.
     */
    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    /**
     * Sets unique user principal name.
     *
     * @param withUserPrincipalName The users principal name.
     */
    public void setUserPrincipalName(String withUserPrincipalName) {
        userPrincipalName = withUserPrincipalName;
    }

    /**
     * Gets the AAD object id.
     *
     * @return The AAD object id.
     */
    @JsonGetter(value = "objectId")
    public String getObjectId() {
        return getAadObjectId();
    }

    /**
     * Sets the AAD object id.
     *
     * @param withObjectId The AAD object Id.
     */
    @JsonSetter(value = "objectId")
    public void setObjectId(String withObjectId) {
        setAadObjectId(withObjectId);
    }

    /**
     * Gets user role.
     *
     * @return The user role.
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * Sets user role.
     *
     * @param withUserRole The user role.
     */
    public void setUserRole(String withUserRole) {
        userRole = withUserRole;
    }

    /**
     * Gets the tenant id.
     *
     * @return The tenant id.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Sets tenant id.
     *
     * @param withTenantId The tenant id.
     */
    public void setTenantId(String withTenantId) {
        tenantId = withTenantId;
    }
}
