// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an application entity.
 */
public class MessageActionsPayloadApp {
    @JsonProperty(value = "applicationIdentityType")
    private String applicationIdentityType;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "displayName")
    private String displayName;

    /**
     * Gets the type of application.
     * 
     * @return Possible values include: 'aadApplication', 'bot', 'tenantBot',
     *         'office365Connector', 'webhook'
     */
    public String getApplicationIdentityType() {
        return applicationIdentityType;
    }

    /**
     * Sets the type of application.
     * 
     * @param withApplicationIdentityType Possible values include: 'aadApplication',
     *                                    'bot', 'tenantBot', 'office365Connector',
     *                                    'webhook'
     */
    public void setApplicationIdentityType(String withApplicationIdentityType) {
        applicationIdentityType = withApplicationIdentityType;
    }

    /**
     * Gets the id of the application.
     * 
     * @return The application id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the application.
     * 
     * @param withId The application id.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the plaintext display name of the application.
     * 
     * @return The display name of the application.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the plaintext display name of the application.
     * 
     * @param withDisplayName The display name of the application.
     */
    public void setDisplayName(String withDisplayName) {
        displayName = withDisplayName;
    }
}
