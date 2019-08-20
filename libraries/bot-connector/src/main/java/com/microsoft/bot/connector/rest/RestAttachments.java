/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import retrofit2.Retrofit;
import com.microsoft.bot.connector.Attachments;
import com.google.common.reflect.TypeToken;
import com.microsoft.bot.schema.models.AttachmentInfo;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.io.InputStream;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

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
        Observable<Response<ResponseBody>> getAttachmentInfo(@Path("attachmentId") String attachmentId, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Attachments getAttachment" })
        @GET("v3/attachments/{attachmentId}/views/{viewId}")
        @Streaming
        Observable<Response<ResponseBody>> getAttachment(@Path("attachmentId") String attachmentId, @Path("viewId") String viewId, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the AttachmentInfo object if successful.
     */
    public AttachmentInfo getAttachmentInfo(String attachmentId) {
        return getAttachmentInfoWithServiceResponseAsync(attachmentId).toBlocking().single().body();
    }

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<AttachmentInfo> getAttachmentInfoAsync(String attachmentId, final ServiceCallback<AttachmentInfo> serviceCallback) {
        return ServiceFuture.fromResponse(getAttachmentInfoWithServiceResponseAsync(attachmentId), serviceCallback);
    }

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AttachmentInfo object
     */
    public Observable<AttachmentInfo> getAttachmentInfoAsync(String attachmentId) {
        return getAttachmentInfoWithServiceResponseAsync(attachmentId).map(new Func1<ServiceResponse<AttachmentInfo>, AttachmentInfo>() {
            @Override
            public AttachmentInfo call(ServiceResponse<AttachmentInfo> response) {
                return response.body();
            }
        });
    }

    /**
     * GetAttachmentInfo.
     * Get AttachmentInfo structure describing the attachment views.
     *
     * @param attachmentId attachment id
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AttachmentInfo object
     */
    public Observable<ServiceResponse<AttachmentInfo>> getAttachmentInfoWithServiceResponseAsync(String attachmentId) {
        if (attachmentId == null) {
            throw new IllegalArgumentException("Parameter attachmentId is required and cannot be null.");
        }
        return service.getAttachmentInfo(attachmentId, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<AttachmentInfo>>>() {
                @Override
                public Observable<ServiceResponse<AttachmentInfo>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<AttachmentInfo> clientResponse = getAttachmentInfoDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<AttachmentInfo> getAttachmentInfoDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
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
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the InputStream object if successful.
     */
    public InputStream getAttachment(String attachmentId, String viewId) {
        return getAttachmentWithServiceResponseAsync(attachmentId, viewId).toBlocking().single().body();
    }

    /**
     * GetAttachment.
     * Get the named view as binary content.
     *
     * @param attachmentId attachment id
     * @param viewId View id from attachmentInfo
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<InputStream> getAttachmentAsync(String attachmentId, String viewId, final ServiceCallback<InputStream> serviceCallback) {
        return ServiceFuture.fromResponse(getAttachmentWithServiceResponseAsync(attachmentId, viewId), serviceCallback);
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
    public Observable<InputStream> getAttachmentAsync(String attachmentId, String viewId) {
        return getAttachmentWithServiceResponseAsync(attachmentId, viewId).map(new Func1<ServiceResponse<InputStream>, InputStream>() {
            @Override
            public InputStream call(ServiceResponse<InputStream> response) {
                return response.body();
            }
        });
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
    public Observable<ServiceResponse<InputStream>> getAttachmentWithServiceResponseAsync(String attachmentId, String viewId) {
        if (attachmentId == null) {
            throw new IllegalArgumentException("Parameter attachmentId is required and cannot be null.");
        }
        if (viewId == null) {
            throw new IllegalArgumentException("Parameter viewId is required and cannot be null.");
        }
        return service.getAttachment(attachmentId, viewId, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<InputStream>>>() {
                @Override
                public Observable<ServiceResponse<InputStream>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<InputStream> clientResponse = getAttachmentDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<InputStream> getAttachmentDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<InputStream, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .register(301, new TypeToken<Void>() { }.getType())
                .register(302, new TypeToken<Void>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

}
