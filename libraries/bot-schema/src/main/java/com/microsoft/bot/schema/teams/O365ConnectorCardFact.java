// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card fact.
 */
public class O365ConnectorCardFact {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "value")
    private String value;

    /**
     * Gets the display name of the fact.
     * 
     * @return The display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the fact.
     * 
     * @param withName The display name.
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Gets the display value for the fact.
     * 
     * @return The display value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the display value for the fact.
     * 
     * @param withValue The display value.
     */
    public void setValue(String withValue) {
        this.value = withValue;
    }
}
