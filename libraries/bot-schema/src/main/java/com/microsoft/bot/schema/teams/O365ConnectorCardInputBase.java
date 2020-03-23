// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardInputBase {
    @JsonProperty(value = "@type")
    private String type;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "isRequired")
    private Boolean isRequired;

    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "value")
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String withType) {
        type = withType;
    }

    public String getId() {
        return id;
    }

    public void setId(String withId) {
        id = withId;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public void setRequired(Boolean withRequired) {
        isRequired = withRequired;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String withTitle) {
        title = withTitle;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String withValue) {
        value = withValue;
    }
}
