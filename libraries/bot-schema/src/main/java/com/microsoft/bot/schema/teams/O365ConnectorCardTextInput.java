// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card text input.
 */
public class O365ConnectorCardTextInput extends O365ConnectorCardInputBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "TextInput";

    @JsonProperty(value = "isMultiline")
    private Boolean isMultiline;

    @JsonProperty(value = "maxLength")
    private double maxLength;

    /**
     * Indicates if text input is allowed for multiple lines. Default value is
     * false.
     * 
     * @return True if multiline input is allowed.
     */
    public Boolean getMultiline() {
        return isMultiline;
    }

    /**
     * Sets if text input is allowed for multiple lines.
     * 
     * @param withMultiline True if multiline input is allowed.
     */
    public void setMultiline(Boolean withMultiline) {
        isMultiline = withMultiline;
    }

    /**
     * Gets maximum length of text input. Default value is unlimited.
     * 
     * @return Max line length.
     */
    public double getMaxLength() {
        return maxLength;
    }

    /**
     * Sets maximum length of text input. Default value is unlimited.
     * 
     * @param withMaxLength Max line length.
     */
    public void setMaxLength(double withMaxLength) {
        maxLength = withMaxLength;
    }
}
