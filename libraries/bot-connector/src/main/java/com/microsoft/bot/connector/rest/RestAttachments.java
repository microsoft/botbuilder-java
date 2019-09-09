/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import retrofit2.Retrofit;
import com.microsoft.bot.connector.Attachments;
import com.google.common.reflect.TypeToken;
import com.microsoft.bot.schema.AttachmentInfo;
import com.microsoft.rest.ServiceResponse;
import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Attachments.
 */
public class RestAttachments implements Attachments {
    /** The Retrofit service to perform REST calls. */
    private AttachmentsService service;
    /** The service client containing this operation class. */
    private RestConnectorClient client;

    /**
     * Initializes an instance of AttachmentsImpl.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    RestAttachments(Retrofit retrofit, RestConnectorClient client) {
        this.service = retrofit.create(AttachmentsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Attachments to be
     * used by Retrofit to perform actually REST calls.
     */
    interface AttachmentsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Attachments getAttachmentInfo" })
        @GET("v3/attachments/{attachmentId}")
        CompletableFuture<Response<ResponseBody>> getAttachmentInfo(@Path("attachmentId") String attachmentId,
                                                                    @Header("accept-language") String acceptLanguage,
                                                                    @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Attachments getAttachment" })
        @GET("v3/attachments/{attachmentId}/views/{viewId}")
        @Streaming
        CompletableFuture<Response<ResponseBody>> getAttachment(@Path("attachmentId") String attachmentId,
                                                         @Path("viewId") String viewId,
                                                         @Header("accept-language") String acceptLanguage,
                                                         @Header("User-Agent") String userAgent);

    }

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AttachmentInfo object
     */
    public CompletableFuture<AttachmentInfo> getAttachmentInfo(String attachmentId) {
        if (attachmentId == null) {
            throw new IllegalArgumentException("Parameter attachmentId is required and cannot be null.");
        }

        return service.getAttachmentInfo(attachmentId, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return getAttachmentInfoDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getAttachmentInfoAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<AttachmentInfo> getAttachmentInfoDelegate(Response<ResponseBody> response)
        throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<AttachmentInfo, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<AttachmentInfo>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * GetAttachment.
     * Get the named view as binary content.
     *
     * @param attachmentId attachment id
     * @param viewId View id from attachmentInfo
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public CompletableFuture<InputStream> getAttachment(String attachmentId, String viewId) {
        if (attachmentId == null) {
            throw new IllegalArgumentException("Parameter attachmentId is required and cannot be null.");
        }
        if (viewId == null) {
            throw new IllegalArgumentException("Parameter viewId is required and cannot be null.");
        }
        return service.getAttachment(attachmentId, viewId, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return getAttachmentDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getAttachmentAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<InputStream> getAttachmentDelegate(Response<ResponseBody> response)
        throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<InputStream, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .register(301, new TypeToken<Void>() { }.getType())
                .register(302, new TypeToken<Void>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }
}
