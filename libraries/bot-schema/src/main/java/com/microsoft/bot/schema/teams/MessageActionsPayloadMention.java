// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadMention {
    @JsonProperty(value = "id")
    private int id;

    @JsonProperty(value = "mentionText")
    private String mentionText;

    @JsonProperty(value = "mentioned")
    private MessageActionsPayloadFrom mentioned;

    public int getId() {
        return id;
    }

    public void setId(int withId) {
        id = withId;
    }

    public String getMentionText() {
        return mentionText;
    }

    public void setMentionText(String withMentionText) {
        mentionText = withMentionText;
    }

    public MessageActionsPayloadFrom getMentioned() {
        return mentioned;
    }

    public void setMentioned(MessageActionsPayloadFrom withMentioned) {
        mentioned = withMentioned;
    }
}
