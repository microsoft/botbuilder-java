// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardMultichoiceInputChoice {
    @JsonProperty(value = "display")
    private String display;

    @JsonProperty(value = "value")
    private String value;

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String withDisplay) {
        display = withDisplay;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String withValue) {
        value = withValue;
    }
}
