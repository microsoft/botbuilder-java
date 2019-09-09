/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import com.microsoft.bot.schema.*;
import retrofit2.Retrofit;
import com.microsoft.bot.connector.Conversations;
import com.google.common.reflect.TypeToken;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Conversations.
 */
public class RestConversations implements Conversations {
    /** The Retrofit service to perform REST calls. */
    private ConversationsService service;
    /** The service client containing this operation class. */
    private RestConnectorClient client;

    /**
     * Initializes an instance of ConversationsImpl.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    RestConversations(Retrofit retrofit, RestConnectorClient client) {
        this.service = retrofit.create(ConversationsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Conversations to be
     * used by Retrofit to perform actually REST calls.
     */
    @SuppressWarnings("checkstyle:linelength")
    interface ConversationsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversations" })
        @GET("v3/conversations")
        CompletableFuture<Response<ResponseBody>> getConversations(@Query("continuationToken") String continuationToken,
                                                            @Header("accept-language") String acceptLanguage,
                                                            @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations createConversation" })
        @POST("v3/conversations")
        CompletableFuture<Response<ResponseBody>> createConversation(@Body ConversationParameters parameters,
                                                              @Header("accept-language") String acceptLanguage,
                                                              @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations sendToConversation" })
        @POST("v3/conversations/{conversationId}/activities")
        CompletableFuture<Response<ResponseBody>> sendToConversation(@Path("conversationId") String conversationId,
                                                              @Body Activity activity,
                                                              @Header("accept-language") String acceptLanguage,
                                                              @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations updateActivity" })
        @PUT("v3/conversations/{conversationId}/activities/{activityId}")
        CompletableFuture<Response<ResponseBody>> updateActivity(@Path("conversationId") String conversationId,
                                                          @Path("activityId") String activityId,
                                                          @Body Activity activity,
                                                          @Header("accept-language") String acceptLanguage,
                                                          @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations replyToActivity" })
        @POST("v3/conversations/{conversationId}/activities/{activityId}")
        CompletableFuture<Response<ResponseBody>> replyToActivity(@Path("conversationId") String conversationId,
                                                           @Path("activityId") String activityId,
                                                           @Body Activity activity,
                                                           @Header("accept-language") String acceptLanguage,
                                                           @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations deleteActivity" })
        @HTTP(path = "v3/conversations/{conversationId}/activities/{activityId}", method = "DELETE", hasBody = true)
        CompletableFuture<Response<ResponseBody>> deleteActivity(@Path("conversationId") String conversationId,
                                                          @Path("activityId") String activityId,
                                                          @Header("accept-language") String acceptLanguage,
                                                          @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversationMembers" })
        @GET("v3/conversations/{conversationId}/members")
        CompletableFuture<Response<ResponseBody>> getConversationMembers(@Path("conversationId") String conversationId,
                                                                  @Header("accept-language") String acceptLanguage,
                                                                  @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations deleteConversationMember" })
        @HTTP(path = "v3/conversations/{conversationId}/members/{memberId}", method = "DELETE", hasBody = true)
        CompletableFuture<Response<ResponseBody>> deleteConversationMember(@Path("conversationId") String conversationId,
                                                                    @Path("memberId") String memberId,
                                                                    @Header("accept-language") String acceptLanguage,
                                                                    @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations getActivityMembers" })
        @GET("v3/conversations/{conversationId}/activities/{activityId}/members")
        CompletableFuture<Response<ResponseBody>> getActivityMembers(@Path("conversationId") String conversationId,
                                                              @Path("activityId") String activityId,
                                                              @Header("accept-language") String acceptLanguage,
                                                              @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations uploadAttachment" })
        @POST("v3/conversations/{conversationId}/attachments")
        CompletableFuture<Response<ResponseBody>> uploadAttachment(@Path("conversationId") String conversationId,
                                                            @Body AttachmentData attachmentUpload,
                                                            @Header("accept-language") String acceptLanguage,
                                                            @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations sendConversationHistory" })
        @POST("v3/conversations/{conversationId}/activities/history")
        CompletableFuture<Response<ResponseBody>> sendConversationHistory(@Path("conversationId") String conversationId,
                                                                   @Body Transcript history,
                                                                   @Header("accept-language") String acceptLanguage,
                                                                   @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversationPagedMembers" })
        @GET("v3/conversations/{conversationId}/pagedmembers")
        CompletableFuture<Response<ResponseBody>> getConversationPagedMembers(@Path("conversationId") String conversationId,
                                                                       @Header("accept-language") String acceptLanguage,
                                                                       @Header("User-Agent") String userAgent);
    }

    /**
     * Implementation of getConversations.
     *
     * @see Conversations#getConversations
     */
    @Override
    public ConversationsResult getConversations() {
        return getConversations().join();
    }

    /**
     * Implementation of getConversationsAsync.
     *
     * @see Conversations#getConversationsAsync
     */
    @Override
    public CompletableFuture<ConversationsResult> getConversations() {
        return getConversations(null);
    }

    /**
     * Implementation of getConversations.
     *
     * @see Conversations#getConversations
     */
    @Override
    public ConversationsResult getConversations(String continuationToken) {
        return getConversations(continuationToken).join();
    }

    /**
     * Implementation of getConversationsAsync.
     *
     * @see Conversations#getConversationsAsync
     */
    @Override
    public CompletableFuture<ConversationsResult> getConversations(String continuationToken) {
        return service.getConversations(continuationToken, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return getConversationsDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getConversationsAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ConversationsResult> getConversationsDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException {

        return this.client.restClient().responseBuilderFactory().<ConversationsResult, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ConversationsResult>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of CreateConversation.
     *
     * @see Conversations#createConversation
     */
    @Override
    public ConversationResourceResponse createConversation(ConversationParameters parameters) {
        return createConversation(parameters).join();
    }

    /**
     * Implementation of createConversationWithServiceResponseAsync.
     *
     * @see Conversations#createConversationAsync
     */
    @Override
    public CompletableFuture<ConversationResourceResponse> createConversation(ConversationParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        Validator.validate(parameters);

        return service.createConversation(parameters, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return createConversationDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("createConversationAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ConversationResourceResponse> createConversationDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<ConversationResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ConversationResourceResponse>() { }.getType())
                .register(201, new TypeToken<ConversationResourceResponse>() { }.getType())
                .register(202, new TypeToken<ConversationResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of sendToConversation.
     *
     * @see Conversations#sendToConversation
     */
    @Override
    public ResourceResponse sendToConversation(String conversationId, Activity activity) {
        return sendToConversation(conversationId, activity).join();
    }

    /**
     * Implementation of sendToConversationAsync.
     *
     * @see Conversations#sendToConversationAsync
     */
    @Override
    public CompletableFuture<ResourceResponse> sendToConversation(String conversationId, Activity activity) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity is required and cannot be null.");
        }
        Validator.validate(activity);

        return service.sendToConversation(conversationId, activity, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return sendToConversationDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("sendToConversationAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ResourceResponse> sendToConversationDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of updateActivity.
     *
     * @see Conversations#updateActivity
     */
    @Override
    public ResourceResponse updateActivity(String conversationId, String activityId, Activity activity) {
        return updateActivity(conversationId, activityId, activity).join();
    }

    /**
     * Implementation of updateActivityAsync.
     *
     * @see Conversations#updateActivityAsync
     */
    @Override
    public CompletableFuture<ResourceResponse> updateActivity(String conversationId, String activityId, Activity activity) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activityId == null) {
            throw new IllegalArgumentException("Parameter activityId is required and cannot be null.");
        }
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity is required and cannot be null.");
        }
        Validator.validate(activity);

        return service.updateActivity(conversationId, activityId, activity, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return updateActivityDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("updateActivityAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ResourceResponse> updateActivityDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of replyToActivity.
     *
     * @see Conversations#replyToActivity
     */
    @Override
    public ResourceResponse replyToActivity(String conversationId, String activityId, Activity activity) {
        return replyToActivity(conversationId, activityId, activity).join();
    }

    /**
     * Implementation of replyToActivityAsync.
     *
     * @see Conversations#replyToActivityAsync
     */
    @Override
    public CompletableFuture<ResourceResponse> replyToActivity(String conversationId,
                                                             String activityId,
                                                             Activity activity) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activityId == null) {
            throw new IllegalArgumentException("Parameter activityId is required and cannot be null.");
        }
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity is required and cannot be null.");
        }
        Validator.validate(activity);

        return service.replyToActivity(conversationId, activityId, activity, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return replyToActivityDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("replyToActivityAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ResourceResponse> replyToActivityDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of deleteActivity.
     *
     * @see Conversations#deleteActivity
     */
    @Override
    public void deleteActivity(String conversationId, String activityId) {
        deleteActivity(conversationId, activityId).join();
    }

    /**
     * Implementation of deleteActivityWithServiceResponseAsync.
     *
     * @see Conversations#deleteActivityAsync
     */
    @Override
    public CompletableFuture<Void> deleteActivity(String conversationId, String activityId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activityId == null) {
            throw new IllegalArgumentException("Parameter activityId is required and cannot be null.");
        }

        return service.deleteActivity(conversationId, activityId, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return deleteActivityDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("deleteActivityAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<Void> deleteActivityDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<Void, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of getConversationMembers.
     *
     * @see Conversations#getConversationMembers
     */
    @Override
    public List<ChannelAccount> getConversationMembers(String conversationId) {
        return getConversationMembers(conversationId).join();
    }

    /**
     * Implementation of getConversationMembersAsync.
     *
     * @see Conversations#getConversationMembersAsync
     */
    @Override
    public CompletableFuture<List<ChannelAccount>> getConversationMembers(String conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        return service.getConversationMembers(conversationId, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return getConversationMembersDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getConversationMembersAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<List<ChannelAccount>> getConversationMembersDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<List<ChannelAccount>, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<ChannelAccount>>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of deleteConversationMember.
     *
     * @see Conversations#deleteConversationMember
     */
    @Override
    public void deleteConversationMember(String conversationId, String memberId) {
        deleteConversationMember(conversationId, memberId).join();
    }

    /**
     * Implementation of deleteConversationMemberWithServiceResponseAsync.
     *
     * @see Conversations#deleteConversationMemberAsync
     */
    @Override
    public CompletableFuture<Void> deleteConversationMember(String conversationId, String memberId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("Parameter memberId is required and cannot be null.");
        }

        return service.deleteConversationMember(conversationId, memberId, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return deleteConversationMemberDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("deleteConversationMemberAsync", responseBodyResponse);
                }
            });
    }


    private ServiceResponse<Void> deleteConversationMemberDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<Void, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of getActivityMembers.
     *
     * @see Conversations#getActivityMembers
     */
    @Override
    public List<ChannelAccount> getActivityMembers(String conversationId, String activityId) {
        return getActivityMembers(conversationId, activityId).join();
    }

    /**
     * Implementation of getActivityMembersAsync.
     *
     * @see Conversations#getActivityMembersAsync
     */
    @Override
    public CompletableFuture<List<ChannelAccount>> getActivityMembers(String conversationId, String activityId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activityId == null) {
            throw new IllegalArgumentException("Parameter activityId is required and cannot be null.");
        }

        return service.getActivityMembers(conversationId, activityId, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return getActivityMembersDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getActivityMembersAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<List<ChannelAccount>> getActivityMembersDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<List<ChannelAccount>, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<ChannelAccount>>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * Implementation of uploadAttachment.
     *
     * @see Conversations#uploadAttachment
     */
    @Override
    public ResourceResponse uploadAttachment(String conversationId, AttachmentData attachmentUpload) {
        return uploadAttachment(conversationId, attachmentUpload).join();
    }

    /**
     * Implementation of uploadAttachmentAsync.
     *
     * @see Conversations#uploadAttachmentAsync
     */
    @Override
    public CompletableFuture<ResourceResponse> uploadAttachment(String conversationId, AttachmentData attachmentUpload) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (attachmentUpload == null) {
            throw new IllegalArgumentException("Parameter attachmentUpload is required and cannot be null.");
        }
        Validator.validate(attachmentUpload);

        return service.uploadAttachment(conversationId, attachmentUpload, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return uploadAttachmentDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("uploadAttachmentAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ResourceResponse> uploadAttachmentDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }


    /**
     * Implementation of sendConversationHistory.
     *
     * @see Conversations#sendConversationHistory
     */
    @Override
    public ResourceResponse sendConversationHistory(String conversationId, Transcript history) {
        return sendConversationHistory(conversationId, history).join();
    }

    /**
     * Implementation of sendConversationHistoryAsync.
     *
     * @see Conversations#sendConversationHistoryAsync
     */
    @Override
    public CompletableFuture<ResourceResponse> sendConversationHistory(String conversationId, Transcript history) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (history == null) {
            throw new IllegalArgumentException("Parameter history is required and cannot be null.");
        }
        Validator.validate(history);

        return service.sendConversationHistory(conversationId, history, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return sendConversationHistoryDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("sendConversationHistoryAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ResourceResponse> sendConversationHistoryDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }


    /**
     * Implementation of getConversationPagedMembers.
     *
     * @see Conversations#getConversationPagedMembers
     */
    @Override
    public PagedMembersResult getConversationPagedMembers(String conversationId){
        return getConversationPagedMembers(conversationId).join();
    }

    /**
     * Implementation of getConversationPagedMembersAsync.
     *
     * @see Conversations#getConversationPagedMembersAsync
     */
    @Override
    public CompletableFuture<PagedMembersResult> getConversationPagedMembers(String conversationId){
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }

        return service.getConversationPagedMembers(conversationId, this.client.getAcceptLanguage(), this.client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return getConversationPagedMembersDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getConversationPagedMembersAsync", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<PagedMembersResult> getConversationPagedMembersDelegate(
        Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient().responseBuilderFactory().<PagedMembersResult, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PagedMembersResult>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }
}
