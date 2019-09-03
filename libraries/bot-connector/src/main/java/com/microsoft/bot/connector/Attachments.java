/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector;

import com.microsoft.bot.schema.models.AttachmentInfo;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;

import java.io.InputStream;

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
     * @param attachmentId    attachment id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    ServiceFuture<AttachmentInfo> getAttachmentInfoAsync(String attachmentId, final ServiceCallback<AttachmentInfo> serviceCallback);

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @return the observable to the AttachmentInfo object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    Observable<AttachmentInfo> getAttachmentInfoAsync(String attachmentId);

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @return the observable to the AttachmentInfo object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    Observable<ServiceResponse<AttachmentInfo>> getAttachmentInfoWithServiceResponseAsync(String attachmentId);

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
     * @param attachmentId    attachment id
     * @param viewId          View id from attachmentInfo
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    ServiceFuture<InputStream> getAttachmentAsync(String attachmentId, String viewId, final ServiceCallback<InputStream> serviceCallback);

    /**
     * GetAttachment.
     * Get the named view as binary content.
     *
     * @param attachmentId attachment id
     * @param viewId       View id from attachmentInfo
     * @return the observable to the InputStream object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    Observable<InputStream> getAttachmentAsync(String attachmentId, String viewId);

    /**
     * GetAttachment.
     * Get the named view as binary content.
     *
     * @param attachmentId attachment id
     * @param viewId       View id from attachmentInfo
     * @return the observable to the InputStream object
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    Observable<ServiceResponse<InputStream>> getAttachmentWithServiceResponseAsync(String attachmentId, String viewId);

}
