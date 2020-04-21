// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Attachment;

/**
 * Messaging extension attachment.
 */
public class MessagingExtensionAttachment extends Attachment {
    @JsonProperty(value = "preview")
    private Attachment preview;

    /**
     * Gets the preview Attachment.
     * 
     * @return The Attachment.
     */
    public Attachment getPreview() {
        return preview;
    }

    /**
     * Sets the preview attachment.
     * 
     * @param withPreview The Attachment.
     */
    public void setPreview(Attachment withPreview) {
        preview = withPreview;
    }
}
