// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Attachment;

/**
 * Metadata for a Task Module.
 */
public class TaskModuleTaskInfo {
    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "height")
    private Object height;

    @JsonProperty(value = "width")
    private Object width;

    @JsonProperty(value = "url")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String url;

    @JsonProperty(value = "card")
    private Attachment card;

    @JsonProperty(value = "fallbackUrl")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String fallbackUrl;

    @JsonProperty(value = "completionBotId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String completionBotId;

    /**
     * Gets the text that appears below the app name and to the right of the app
     * icon.
     * 
     * @return The title text.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the text that appears below the app name and to the right of the app
     * icon.
     * 
     * @param withTitle The title text.
     */
    public void setTitle(String withTitle) {
        title = withTitle;
    }

    /**
     * Gets title height. This can be a number, representing the task module's
     * height in pixels, or a string, one of: small, medium, large.
     * 
     * @return The title height.
     */
    public Object getHeight() {
        return height;
    }

    /**
     * Sets title height. This can be a number, representing the task module's
     * height in pixels, or a string, one of: small, medium, large.
     * 
     * @param withHeight The title height.
     */
    public void setHeight(Object withHeight) {
        height = withHeight;
    }

    /**
     * Gets title width. This can be a number, representing the task module's width
     * in pixels, or a string, one of: small, medium, large.
     * 
     * @return The title width.
     */
    public Object getWidth() {
        return width;
    }

    /**
     * Sets title width. This can be a number, representing the task module's width
     * in pixels, or a string, one of: small, medium, large.
     * 
     * @param withWidth The title width.
     */
    public void setWidth(Object withWidth) {
        width = withWidth;
    }

    /**
     * Gets the URL of what is loaded as an iframe inside the task module. One of
     * url or card is required.
     * 
     * @return The module url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of what is loaded as an iframe inside the task module. One of
     * url or card is required.
     * 
     * @param withUrl The module url.
     */
    public void setUrl(String withUrl) {
        url = withUrl;
    }

    /**
     * Gets the Adaptive card to appear in the task module.
     * 
     * @return The module task card.
     */
    public Attachment getCard() {
        return card;
    }

    /**
     * Sets the Adaptive card to appear in the task module.
     * 
     * @param withCard The module task card.
     */
    public void setCard(Attachment withCard) {
        card = withCard;
    }

    /**
     * Gets the URL if a client does not support the task module feature, this URL
     * is opened in a browser tab.
     * 
     * @return The fallback url.
     */
    public String getFallbackUrl() {
        return fallbackUrl;
    }

    /**
     * Sets the URL if a client does not support the task module feature, this URL
     * is opened in a browser tab.
     * 
     * @param withFallbackUrl The fallback url.
     */
    public void setFallbackUrl(String withFallbackUrl) {
        fallbackUrl = withFallbackUrl;
    }

    /**
     * Gets id if a client does not support the task module feature, this URL is
     * opened in a browser tab.
     * 
     * @return The completion id.
     */
    public String getCompletionBotId() {
        return completionBotId;
    }

    /**
     * Sets id if a client does not support the task module feature, this URL is
     * opened in a browser tab.
     * 
     * @param withCompletionBotId The completion id.
     */
    public void setCompletionBotId(String withCompletionBotId) {
        completionBotId = withCompletionBotId;
    }
}
