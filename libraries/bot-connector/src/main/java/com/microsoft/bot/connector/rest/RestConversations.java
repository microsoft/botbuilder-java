/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.ConversationConstants;
import com.microsoft.bot.restclient.ServiceResponseBuilder;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.AttachmentData;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.ConversationsResult;
import com.microsoft.bot.schema.PagedMembersResult;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Transcript;
import retrofit2.Retrofit;
import com.microsoft.bot.connector.Conversations;
import com.google.common.reflect.TypeToken;
import com.microsoft.bot.restclient.ServiceResponse;
import com.microsoft.bot.restclient.Validator;

import java.io.IOException;
import java.net.HttpURLConnection;
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
 * An instance of this class provides access to all the operations defined in
 * Conversations.
 */
public class RestConversations implements Conversations {
    /**
     * The Retrofit service to perform REST calls.
     */
    private ConversationsService service;
    /**
     * The service client containing this operation class.
     */
    private RestConnectorClient client;

    /**
     * Initializes an instance of ConversationsImpl.
     *
     * @param withRetrofit the Retrofit instance built from a Retrofit Builder.
     * @param withClient   the instance of the service client containing this
     *                     operation class.
     */
    RestConversations(Retrofit withRetrofit, RestConnectorClient withClient) {
        this.service = withRetrofit.create(ConversationsService.class);
        client = withClient;
    }

    /**
     * The interface defining all the services for Conversations to be used by
     * Retrofit to perform actually REST calls.
     */
    @SuppressWarnings({ "checkstyle:linelength", "checkstyle:JavadocMethod" })
    interface ConversationsService {
        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversations" })
        @GET("v3/conversations")
        CompletableFuture<Response<ResponseBody>> getConversations(
            @Query("continuationToken") String continuationToken,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations createConversation" })
        @POST("v3/conversations")
        CompletableFuture<Response<ResponseBody>> createConversation(
            @Body ConversationParameters parameters,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations sendToConversation" })
        @POST("v3/conversations/{conversationId}/activities")
        CompletableFuture<Response<ResponseBody>> sendToConversation(
            @Path("conversationId") String conversationId,
            @Body Activity activity,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations updateActivity" })
        @PUT("v3/conversations/{conversationId}/activities/{activityId}")
        CompletableFuture<Response<ResponseBody>> updateActivity(
            @Path("conversationId") String conversationId,
            @Path("activityId") String activityId,
            @Body Activity activity,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({
            "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations replyToActivity"
        })
        @POST("v3/conversations/{conversationId}/activities/{activityId}")
        CompletableFuture<Response<ResponseBody>> replyToActivity(
            @Path("conversationId") String conversationId,
            @Path("activityId") String activityId,
            @Body Activity activity,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent,
            @Header(ConversationConstants.CONVERSATION_ID_HTTP_HEADERNAME) String conversationIdHeader
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations deleteActivity" })
        @HTTP(path = "v3/conversations/{conversationId}/activities/{activityId}", method = "DELETE", hasBody = true)
        CompletableFuture<Response<ResponseBody>> deleteActivity(
            @Path("conversationId") String conversationId,
            @Path("activityId") String activityId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversationMembers" })
        @GET("v3/conversations/{conversationId}/members")
        CompletableFuture<Response<ResponseBody>> getConversationMembers(
            @Path("conversationId") String conversationId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversationMembers" })
        @GET("v3/conversations/{conversationId}/members/{userId}")
        CompletableFuture<Response<ResponseBody>> getConversationMember(
            @Path("userId") String userId,
            @Path("conversationId") String conversationId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations deleteConversationMember" })
        @HTTP(path = "v3/conversations/{conversationId}/members/{memberId}", method = "DELETE", hasBody = true)
        CompletableFuture<Response<ResponseBody>> deleteConversationMember(
            @Path("conversationId") String conversationId,
            @Path("memberId") String memberId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations getActivityMembers" })
        @GET("v3/conversations/{conversationId}/activities/{activityId}/members")
        CompletableFuture<Response<ResponseBody>> getActivityMembers(
            @Path("conversationId") String conversationId,
            @Path("activityId") String activityId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations uploadAttachment" })
        @POST("v3/conversations/{conversationId}/attachments")
        CompletableFuture<Response<ResponseBody>> uploadAttachment(
            @Path("conversationId") String conversationId,
            @Body AttachmentData attachmentUpload,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations sendConversationHistory" })
        @POST("v3/conversations/{conversationId}/activities/history")
        CompletableFuture<Response<ResponseBody>> sendConversationHistory(
            @Path("conversationId") String conversationId,
            @Body Transcript history,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversationPagedMembers" })
        @GET("v3/conversations/{conversationId}/pagedmembers")
        CompletableFuture<Response<ResponseBody>> getConversationPagedMembers(
            @Path("conversationId") String conversationId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversationPagedMembers" })
        @GET("v3/conversations/{conversationId}/pagedmembers?continuationToken={continuationToken}")
        CompletableFuture<Response<ResponseBody>> getConversationPagedMembers(
            @Path("conversationId") String conversationId,
            @Path("continuationToken") String continuationToken,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );
    }

    /**
     * Implementation of getConversations.
     *
     * @see Conversations#getConversations
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
    public CompletableFuture<ConversationsResult> getConversations(String continuationToken) {
        return service
            .getConversations(continuationToken, client.getAcceptLanguage(), client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return getConversationsDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getConversations", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ConversationsResult> getConversationsDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException {

        return client.restClient()
            .responseBuilderFactory()
            .<ConversationsResult, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ConversationsResult>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of createConversation.
     *
     * @see Conversations#createConversation
     */
    @Override
    public CompletableFuture<ConversationResourceResponse> createConversation(
        ConversationParameters parameters
    ) {
        if (parameters == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter parameters is required and cannot be null."
            ));
        }
        Validator.validate(parameters);

        return service
            .createConversation(parameters, client.getAcceptLanguage(), client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return createConversationDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException(
                        "createConversation",
                        responseBodyResponse
                    );
                }
            });
    }

    private ServiceResponse<ConversationResourceResponse> createConversationDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<ConversationResourceResponse, ErrorResponseException>newInstance(
                client.serializerAdapter()
            )
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ConversationResourceResponse>() {
            }.getType())
            .register(
                HttpURLConnection.HTTP_CREATED, new TypeToken<ConversationResourceResponse>() {
                }.getType()
            )
            .register(
                HttpURLConnection.HTTP_ACCEPTED, new TypeToken<ConversationResourceResponse>() {
                }.getType()
            )
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of sendToConversation.
     *
     * @see Conversations#sendToConversation
     */
    @Override
    public CompletableFuture<ResourceResponse> sendToConversation(
        String conversationId,
        Activity activity
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (activity == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter activity is required and cannot be null."
            ));
        }
        Validator.validate(activity);

        return service.sendToConversation(
            conversationId, activity, client.getAcceptLanguage(), client.getUserAgent()
        ).thenApply(responseBodyResponse -> {
            try {
                return sendToConversationDelegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException("sendToConversation", responseBodyResponse);
            }
        });
    }

    private ServiceResponse<ResourceResponse> sendToConversationDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<ResourceResponse, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_CREATED, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_ACCEPTED, new TypeToken<ResourceResponse>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of updateActivity.
     *
     * @see Conversations#updateActivity
     */
    @Override
    public CompletableFuture<ResourceResponse> updateActivity(
        String conversationId,
        String activityId,
        Activity activity
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (activityId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter activityId is required and cannot be null."
            ));
        }
        if (activity == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter activity is required and cannot be null."
            ));
        }

        return Async.tryCompletable(() -> {
            Validator.validate(activity);
            return service.updateActivity(
                conversationId, activityId, activity, client.getAcceptLanguage(),
                client.getUserAgent()
            )

                .thenApply(responseBodyResponse -> {
                    try {
                        return updateActivityDelegate(responseBodyResponse).body();
                    } catch (ErrorResponseException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new ErrorResponseException(
                            "updateActivity", responseBodyResponse);
                    }
                });
        });
    }

    private ServiceResponse<ResourceResponse> updateActivityDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<ResourceResponse, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_CREATED, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_ACCEPTED, new TypeToken<ResourceResponse>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of replyToActivity.
     *
     * @see Conversations#replyToActivity
     */
    @Override
    public CompletableFuture<ResourceResponse> replyToActivity(
        String conversationId,
        String activityId,
        Activity activity
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (activityId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter activityId is required and cannot be null."
            ));
        }
        if (activity == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter activity is required and cannot be null."
            ));
        }
        Validator.validate(activity);

        return service.replyToActivity(
            conversationId,
            activityId,
            activity,
            client.getAcceptLanguage(),
            client.getUserAgent(),
            conversationId
        )

            .thenApply(responseBodyResponse -> {
                try {
                    return replyToActivityDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("replyToActivity", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ResourceResponse> replyToActivityDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<ResourceResponse, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_CREATED, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_ACCEPTED, new TypeToken<ResourceResponse>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of deleteActivity.
     *
     * @see Conversations#deleteActivity
     */
    @Override
    public CompletableFuture<Void> deleteActivity(String conversationId, String activityId) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (activityId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter activityId is required and cannot be null."
            ));
        }

        return service.deleteActivity(
            conversationId, activityId, client.getAcceptLanguage(), client.getUserAgent()
        ).thenApply(responseBodyResponse -> {
            try {
                return deleteActivityDelegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException("deleteActivity", responseBodyResponse);
            }
        });
    }

    private ServiceResponse<Void> deleteActivityDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<Void, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<Void>() {
            }.getType())
            .register(HttpURLConnection.HTTP_ACCEPTED, new TypeToken<Void>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of getConversationMembers.
     *
     * @see Conversations#getConversationMembers
     */
    @Override
    public CompletableFuture<List<ChannelAccount>> getConversationMembers(String conversationId) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }

        return service.getConversationMembers(
            conversationId, client.getAcceptLanguage(), client.getUserAgent()
        ).thenApply(responseBodyResponse -> {
            try {
                return getConversationMembersDelegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException(
                    "getConversationMembers",
                    responseBodyResponse
                );
            }
        });
    }

    private ServiceResponse<List<ChannelAccount>> getConversationMembersDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<List<ChannelAccount>, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<List<ChannelAccount>>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of getConversationMember.
     *
     * @see Conversations#getConversationMember
     */
    @Override
    public CompletableFuture<ChannelAccount> getConversationMember(
        String userId,
        String conversationId
    ) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }

        return service.getConversationMember(
            userId, conversationId, client.getAcceptLanguage(), client.getUserAgent()
        ).thenApply(responseBodyResponse -> {
            try {
                return getConversationMemberDelegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException(
                    "getConversationMember",
                    responseBodyResponse
                );
            }
        });
    }

    private ServiceResponse<ChannelAccount> getConversationMemberDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return ((ServiceResponseBuilder<ChannelAccount, ErrorResponseException>) client.restClient()
            .responseBuilderFactory()
            .<ChannelAccount, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ChannelAccount>() {
            }.getType())
            .registerError(ErrorResponseException.class))
            .withThrowOnGet404(true)
            .build(response);
    }

    /**
     * Implementation of deleteConversationMember.
     *
     * @see Conversations#deleteConversationMember
     */
    @Override
    public CompletableFuture<Void> deleteConversationMember(
        String conversationId,
        String memberId
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (memberId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter memberId is required and cannot be null."
            ));
        }

        return service.deleteConversationMember(
            conversationId, memberId, client.getAcceptLanguage(), client.getUserAgent()
        )

            .thenApply(responseBodyResponse -> {
                try {
                    return deleteConversationMemberDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException(
                        "deleteConversationMember",
                        responseBodyResponse
                    );
                }
            });
    }

    private ServiceResponse<Void> deleteConversationMemberDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<Void, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<Void>() {
            }.getType())
            .register(HttpURLConnection.HTTP_NO_CONTENT, new TypeToken<Void>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of getActivityMembers.
     *
     * @see Conversations#getActivityMembers
     */
    @Override
    public CompletableFuture<List<ChannelAccount>> getActivityMembers(
        String conversationId,
        String activityId
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (activityId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter activityId is required and cannot be null."
            ));
        }

        return service.getActivityMembers(
            conversationId, activityId, client.getAcceptLanguage(), client.getUserAgent()
        ).thenApply(responseBodyResponse -> {
            try {
                return getActivityMembersDelegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException("getActivityMembers", responseBodyResponse);
            }
        });
    }

    private ServiceResponse<List<ChannelAccount>> getActivityMembersDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<List<ChannelAccount>, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<List<ChannelAccount>>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of uploadAttachment.
     *
     * @see Conversations#uploadAttachment
     */
    @Override
    public CompletableFuture<ResourceResponse> uploadAttachment(
        String conversationId,
        AttachmentData attachmentUpload
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (attachmentUpload == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter attachmentUpload is required and cannot be null."
            ));
        }
        Validator.validate(attachmentUpload);

        return service.uploadAttachment(
            conversationId, attachmentUpload, client.getAcceptLanguage(), client.getUserAgent()
        )

            .thenApply(responseBodyResponse -> {
                try {
                    return uploadAttachmentDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("uploadAttachment", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ResourceResponse> uploadAttachmentDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<ResourceResponse, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_CREATED, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_ACCEPTED, new TypeToken<ResourceResponse>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of sendConversationHistory.
     *
     * @see Conversations#sendConversationHistory
     */
    @Override
    public CompletableFuture<ResourceResponse> sendConversationHistory(
        String conversationId,
        Transcript history
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (history == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter history is required and cannot be null."
            ));
        }
        Validator.validate(history);

        return service.sendConversationHistory(
            conversationId, history, client.getAcceptLanguage(), client.getUserAgent()
        )

            .thenApply(responseBodyResponse -> {
                try {
                    return sendConversationHistoryDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException(
                        "sendConversationHistory",
                        responseBodyResponse
                    );
                }
            });
    }

    private ServiceResponse<ResourceResponse> sendConversationHistoryDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<ResourceResponse, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_CREATED, new TypeToken<ResourceResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_ACCEPTED, new TypeToken<ResourceResponse>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of getConversationPagedMembers.
     *
     * @see Conversations#getConversationPagedMembers(String conversationId)
     */
    @Override
    public CompletableFuture<PagedMembersResult> getConversationPagedMembers(
        String conversationId
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }

        return service.getConversationPagedMembers(
            conversationId, client.getAcceptLanguage(), client.getUserAgent()
        ).thenApply(responseBodyResponse -> {
            try {
                return getConversationPagedMembersDelegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException(
                    "getConversationPagedMembers",
                    responseBodyResponse
                );
            }
        });
    }

    private ServiceResponse<PagedMembersResult> getConversationPagedMembersDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<PagedMembersResult, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<PagedMembersResult>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of getConversationPagedMembers.
     *
     * @see Conversations#getConversationPagedMembers(String conversationId, String
     *      continuationToken)
     *
     * @param conversationId    Conversation ID
     * @param continuationToken The continuationToken from a previous call.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws RuntimeException         all other wrapped checked exceptions if the
     *                                  request fails to be sent
     * @return the PagedMembersResult object if successful.
     */
    public CompletableFuture<PagedMembersResult> getConversationPagedMembers(
        String conversationId,
        String continuationToken
    ) {
        if (conversationId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter conversationId is required and cannot be null."
            ));
        }
        if (continuationToken == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter continuationToken is required and cannot be null."
            ));
        }

        return service.getConversationPagedMembers(
            conversationId, continuationToken, client.getAcceptLanguage(), client.getUserAgent()
        ).thenApply(responseBodyResponse -> {
            try {
                return getConversationPagedMembers2Delegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException(
                    "getConversationPagedMembers",
                    responseBodyResponse
                );
            }
        });
    }

    private ServiceResponse<PagedMembersResult> getConversationPagedMembers2Delegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<PagedMembersResult, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<PagedMembersResult>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }
}
