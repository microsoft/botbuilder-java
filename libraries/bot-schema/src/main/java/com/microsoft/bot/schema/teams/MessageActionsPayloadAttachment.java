// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadAttachment {
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "contentType")
    private String contentType;

    @JsonProperty(value = "contentUrl")
    private String contentUrl;

    @JsonProperty(value = "content")
    public Object content;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "thumbnailUrl")
    private String thumbnailUrl;

    public String getId() {
        return id;
    }

    public void setId(String withId) {
        id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String withContentType) {
        contentType = withContentType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String withContentUrl) {
        contentUrl = withContentUrl;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object withContent) {
        content = withContent;
    }

    public String getName() {
        return name;
    }

    public void setName(String withName) {
        name = withName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String withThumbnailUrl) {
        thumbnailUrl = withThumbnailUrl;
    }
}
