// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardDateInput extends O365ConnectorCardInputBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "DateInput";

    @JsonProperty(value = "includeTime")
    private Boolean includeTime;

    public Boolean getIncludeTime() {
        return includeTime;
    }

    public void setIncludeTime(Boolean withIncludeTime) {
        includeTime = withIncludeTime;
    }
}
