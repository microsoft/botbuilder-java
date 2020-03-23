// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadConversation {
    @JsonProperty(value = "conversationIdentityType")
    private String conversationIdentityType;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "displayName")
    private String displayName;

    public String getConversationIdentityType() {
        return conversationIdentityType;
    }

    public void setConversationIdentityType(String withConversationIdentityType) {
        conversationIdentityType = withConversationIdentityType;
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
