// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the attachment in a message.
 */
public class MessageActionsPayloadAttachment {
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "contentType")
    private String contentType;

    @JsonProperty(value = "contentUrl")
    private String contentUrl;

    @JsonProperty(value = "content")
    private Object content;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "thumbnailUrl")
    private String thumbnailUrl;

    /**
     * Gets the id of the attachment.
     * 
     * @return The attachment id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the attachment.
     * 
     * @param withId The attachment id.
     */
    public void setId(String withId) {
        id = id;
    }

    /**
     * Gets the type of the attachment.
     * 
     * @return The content type of the attachment.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the type of the attachment.
     * 
     * @param withContentType The content type of the attachment.
     */
    public void setContentType(String withContentType) {
        contentType = withContentType;
    }

    /**
     * Gets the url of the attachment, in case of a external link.
     * 
     * @return The URL of the attachment.
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * Sets the url of the attachment, in case of a external link.
     * 
     * @param withContentUrl The URL of the attachment.
     */
    public void setContentUrl(String withContentUrl) {
        contentUrl = withContentUrl;
    }

    /**
     * Gets the content of the attachment, in case of a code.
     * 
     * @return The attachment content.
     */
    public Object getContent() {
        return content;
    }

    /**
     * Sets the content of the attachment, in case of a code.
     * 
     * @param withContent The attachment content.
     */
    public void setContent(Object withContent) {
        content = withContent;
    }

    /**
     * Gets the plaintext display name of the attachment.
     * 
     * @return The attachment plaintext name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the plaintext display name of the attachment.
     * 
     * @param withName The attachment plaintext name.
     */
    public void setName(String withName) {
        name = withName;
    }

    /**
     * Gets the url of a thumbnail image that might be embedded in the attachment,
     * in case of a card.
     * 
     * @return The thumbnail URL.
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Sets the url of a thumbnail image that might be embedded in the attachment,
     * in case of a card.
     * 
     * @param withThumbnailUrl The thumbnail URL.
     */
    public void setThumbnailUrl(String withThumbnailUrl) {
        thumbnailUrl = withThumbnailUrl;
    }
}
