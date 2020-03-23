// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardActionBase {
    @JsonProperty(value = "@type")
    private String type;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "@id")
    private String id;

    public String getType() {
        return type;
    }

    public void setType(String withType) {
        type = withType;
    }

    public String getName() {
        return name;
    }

    public void setName(String withName) {
        name = withName;
    }

    public String getId() {
        return id;
    }

    public void setId(String withId) {
        id = withId;
    }
}
