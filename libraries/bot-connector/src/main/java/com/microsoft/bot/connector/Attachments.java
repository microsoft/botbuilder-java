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
 * An instance of this class provides access to all the operations defined
 * in Attachments.
 */
public interface Attachments {
    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @return the AttachmentInfo object if successful.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent
     */
    AttachmentInfo getAttachmentInfo(String attachmentId);

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @return the observable to the AttachmentInfo object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    CompletableFuture<AttachmentInfo> getAttachmentInfo(String attachmentId);

    /**
     * GetAttachment.
     * Get the named view as binary content.
     *
     * @param attachmentId attachment id
     * @param viewId       View id from attachmentInfo
     * @return the InputStream object if successful.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent
     */
    InputStream getAttachment(String attachmentId, String viewId);

    /**
     * GetAttachment.
     * Get the named view as binary content.
     *
     * @param attachmentId attachment id
     * @param viewId       View id from attachmentInfo
     * @return the observable to the InputStream object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    CompletableFuture<InputStream> getAttachment(String attachmentId, String viewId);
}
