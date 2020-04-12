// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.microsoft.bot.schema.Attachment;

/**
 * Attachment extensions.
 */
public final class AttachmentExtensions {
    private AttachmentExtensions() {
    }

    /**
     * Converts normal attachment into the messaging extension attachment.
     * 
     * @param attachment        The Attachment.
     * @param previewAttachment The preview Attachment.
     * @return Messaging extension attachment.
     */
    public static MessagingExtensionAttachment toMessagingExtensionAttachment(
        Attachment attachment,
        Attachment previewAttachment
    ) {

        MessagingExtensionAttachment messagingAttachment = new MessagingExtensionAttachment();
        messagingAttachment.setContent(attachment.getContent());
        messagingAttachment.setContentType(attachment.getContentType());
        messagingAttachment.setContentUrl(attachment.getContentUrl());
        messagingAttachment.setName(attachment.getName());
        messagingAttachment.setThumbnailUrl(attachment.getThumbnailUrl());
        messagingAttachment.setPreview(
            previewAttachment == null ? Attachment.clone(attachment) : previewAttachment
        );

        return messagingAttachment;
    }
}
