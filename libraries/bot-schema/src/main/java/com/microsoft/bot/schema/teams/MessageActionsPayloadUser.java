// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a user entity.
 */
public class MessageActionsPayloadUser {
    @JsonProperty(value = "userIdentityType")
    private String userIdentityType;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "displayName")
    private String displayName;

    /**
     * Gets the identity type of the user. Possible values include: 'aadUser',
     * 'onPremiseAadUser', 'anonymousGuest', 'federatedUser'
     * 
     * @return The user type.
     */
    public String getUserIdentityType() {
        return userIdentityType;
    }

    /**
     * Sets the identity type of the user. Possible values include: 'aadUser',
     * 'onPremiseAadUser', 'anonymousGuest', 'federatedUser'
     * 
     * @param withUserIdentityType The user type.
     */
    public void setUserIdentityType(String withUserIdentityType) {
        userIdentityType = withUserIdentityType;
    }

    /**
     * Gets the id of the user.
     * 
     * @return The user id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the user.
     * 
     * @param withId The user id.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the plaintext display name of the user.
     * 
     * @return The plaintext display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the plaintext display name of the user.
     * 
     * @param withDisplayName The plaintext display name.
     */
    public void setDisplayName(String withDisplayName) {
        displayName = withDisplayName;
    }
}
