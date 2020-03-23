// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadBody {
    @JsonProperty(value = "contentType")
    private String contentType;

    @JsonProperty(value = "content")
    private String content;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String withContentType) {
        contentType = withContentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String withContent) {
        content = withContent;
    }
}
