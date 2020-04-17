// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365O365 connector card multiple choice input item.
 */
public class O365ConnectorCardMultichoiceInputChoice {
    @JsonProperty(value = "display")
    private String display;

    @JsonProperty(value = "value")
    private String value;

    /**
     * Gets the text rendered on ActionCard.
     * 
     * @return The ActionCard text.
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Sets the text rendered on ActionCard.
     * 
     * @param withDisplay The ActionCard text.
     */
    public void setDisplay(String withDisplay) {
        display = withDisplay;
    }

    /**
     * Gets the value received as results.
     * 
     * @return The result value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value received as results.
     * 
     * @param withValue The result value.
     */
    public void setValue(String withValue) {
        value = withValue;
    }
}
