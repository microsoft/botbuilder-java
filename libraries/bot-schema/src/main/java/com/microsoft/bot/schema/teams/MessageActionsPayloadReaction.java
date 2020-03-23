// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadReaction {
    @JsonProperty(value = "reactionType")
    private String reactionType;

    @JsonProperty(value = "createdDateTime")
    private String createdDateTime;

    @JsonProperty(value = "user")
    private MessageActionsPayloadFrom user;

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String withReactionType) {
        reactionType = withReactionType;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String withCreatedDateTime) {
        createdDateTime = withCreatedDateTime;
    }

    public MessageActionsPayloadFrom getUser() {
        return user;
    }

    public void setUser(MessageActionsPayloadFrom withUser) {
        user = withUser;
    }
}
