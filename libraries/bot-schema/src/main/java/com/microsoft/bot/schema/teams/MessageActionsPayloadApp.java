// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadApp {
    @JsonProperty(value = "applicationIdentityType")
    private String applicationIdentityType;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "displayName")
    private String displayName;

    public String getApplicationIdentityType() {
        return applicationIdentityType;
    }

    public void setApplicationIdentityType(String withApplicationIdentityType) {
        applicationIdentityType = withApplicationIdentityType;
    }

    public String getId() {
        return id;
    }

    public void setId(String withId) {
        id = withId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String withDisplayName) {
        displayName = withDisplayName;
    }
}
