// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadUser {
    @JsonProperty(value = "userIdentityType")
    private String userIdentityType;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "displayName")
    private String displayName;

    public String getUserIdentityType() {
        return userIdentityType;
    }

    public void setUserIdentityType(String withUserIdentityType) {
        userIdentityType = withUserIdentityType;
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
