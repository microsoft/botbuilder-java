// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card action base.
 */
public class O365ConnectorCardActionBase {
    @JsonProperty(value = "@type")
    private String type;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "@id")
    private String id;

    /**
     * Gets the type of the action. Possible values include: 'ViewAction',
     * 'OpenUri', 'HttpPOST', 'ActionCard'
     * 
     * @return The action type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the action. Possible values include: 'ViewAction',
     * 'OpenUri', 'HttpPOST', 'ActionCard'
     * 
     * @param withType The action type.
     */
    public void setType(String withType) {
        type = withType;
    }

    /**
     * Gets the name of the action that will be used as button title.
     * 
     * @return The action name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the action that will be used as button title.
     * 
     * @param withName The action name.
     */
    public void setName(String withName) {
        name = withName;
    }

    /**
     * Gets the action id.
     * 
     * @return The action id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the action id.
     * 
     * @param withId The action id.
     */
    public void setId(String withId) {
        id = withId;
    }
}
