// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardTextInput extends O365ConnectorCardInputBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "TextInput";

    @JsonProperty(value = "isMultiline")
    public Boolean isMultiline;

    @JsonProperty(value = "maxLength")
    public double maxLength;

    public Boolean getMultiline() {
        return isMultiline;
    }

    public void setMultiline(Boolean withMultiline) {
        isMultiline = withMultiline;
    }

    public double getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(double withMaxLength) {
        maxLength = withMaxLength;
    }
}
