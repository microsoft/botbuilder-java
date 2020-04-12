// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card date input.
 */
public class O365ConnectorCardDateInput extends O365ConnectorCardInputBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "DateInput";

    @JsonProperty(value = "includeTime")
    private Boolean includeTime;

    /**
     * Gets include time input field. Default value is false (date only).
     * 
     * @return True to include time.
     */
    public Boolean getIncludeTime() {
        return includeTime;
    }

    /**
     * Sets include time input field. Default value is false (date only).
     * 
     * @param withIncludeTime True to include time.
     */
    public void setIncludeTime(Boolean withIncludeTime) {
        includeTime = withIncludeTime;
    }
}
