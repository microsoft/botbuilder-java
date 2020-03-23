// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.microsoft.bot.schema.Attachment;

public class AttachmentExtensions {
    public static MessagingExtensionAttachment toMessagingExtensionAttachment(
        Attachment attachment,
        Attachment previewAttachment) {

        MessagingExtensionAttachment messagingAttachment = new MessagingExtensionAttachment();
        messagingAttachment.setContent(attachment.getContent());
        messagingAttachment.setContentType(attachment.getContentType());
        messagingAttachment.setContentUrl(attachment.getContentUrl());
        messagingAttachment.setName(attachment.getName());
        messagingAttachment.setThumbnailUrl(attachment.getThumbnailUrl());
        messagingAttachment.setPreview(previewAttachment == null ? Attachment.clone(attachment) : previewAttachment);

        return messagingAttachment;
    }
}
