// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Attachment;

public class TaskModuleTaskInfo {
    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "height")
    private Object height;

    @JsonProperty(value = "width")
    private Object width;

    @JsonProperty(value = "url")
    private String url;

    @JsonProperty(value = "card")
    public Attachment card;

    @JsonProperty(value = "fallbackUrl")
    private String fallbackUrl;

    @JsonProperty(value = "completionBotId")
    private String completionBotId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String withTitle) {
        title = withTitle;
    }

    public Object getHeight() {
        return height;
    }

    public void setHeight(Object withHeight) {
        height = withHeight;
    }

    public Object getWidth() {
        return width;
    }

    public void setWidth(Object withWidth) {
        width = withWidth;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String withUrl) {
        url = withUrl;
    }

    public Attachment getCard() {
        return card;
    }

    public void setCard(Attachment withCard) {
        card = withCard;
    }

    public String getFallbackUrl() {
        return fallbackUrl;
    }

    public void setFallbackUrl(String withFallbackUrl) {
        fallbackUrl = withFallbackUrl;
    }

    public String getCompletionBotId() {
        return completionBotId;
    }

    public void setCompletionBotId(String withCompletionBotId) {
        completionBotId = withCompletionBotId;
    }
}
