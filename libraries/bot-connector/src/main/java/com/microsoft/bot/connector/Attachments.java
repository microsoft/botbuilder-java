/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector;

import com.microsoft.bot.schema.AttachmentInfo;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * An instance of this class provides access to all the operations defined in
 * Attachments.
 */
public interface Attachments {
    /**
     * GetAttachmentInfo. Get AttachmentInfo structure describing the attachment
     * views.
     *
     * @param attachmentId attachment id
     * @return the observable to the AttachmentInfo object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    CompletableFuture<AttachmentInfo> getAttachmentInfo(String attachmentId);

    /**
     * GetAttachment. Get the named view as binary content.
     *
     * @param attachmentId attachment id
     * @param viewId       View id from attachmentInfo
     * @return the observable to the InputStream object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    CompletableFuture<InputStream> getAttachment(String attachmentId, String viewId);

    /**
     * Get the URI of an attachment view.
     * @param attachmentId id of the attachment.
     * @param viewId default is "original".
     * @return URI of the attachment.
     */
    String getAttachmentUri(String attachmentId, String viewId);

    /**
     * Get the URI of an attachment view.
     * @param attachmentId id of the attachment.
     * @return URI of the attachment.
     */
    default String getAttachmentUri(String attachmentId) {
        return getAttachmentUri(attachmentId, "original");
    }
}
