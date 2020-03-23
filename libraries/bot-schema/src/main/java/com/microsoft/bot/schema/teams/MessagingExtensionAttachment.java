// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Attachment;

public class MessagingExtensionAttachment extends Attachment {
    @JsonProperty(value = "preview")
    public Attachment preview;

    public Attachment getPreview() {
        return preview;
    }

    public void setPreview(Attachment withPreview) {
        preview = withPreview;
    }
}
