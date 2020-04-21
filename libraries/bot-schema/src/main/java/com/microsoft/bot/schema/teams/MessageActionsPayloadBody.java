// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Plaintext/HTML representation of the content of the message.
 */
public class MessageActionsPayloadBody {
    @JsonProperty(value = "contentType")
    private String contentType;

    @JsonProperty(value = "content")
    private String content;

    /**
     * Gets type of the content. Possible values include: 'html', 'text'
     * 
     * @return The content type of the payload.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets type of the content. Possible values include: 'html',
     * 
     * @param withContentType The content type of the payload.
     */
    public void setContentType(String withContentType) {
        contentType = withContentType;
    }

    /**
     * Gets the content of the body.
     * 
     * @return The payload content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the body.
     * 
     * @param withContent The payload content.
     */
    public void setContent(String withContent) {
        content = withContent;
    }
}
