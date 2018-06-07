/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * NOT GENERATED.
 * This uses Java 8 CompletionStage for async processing instead of JavaRX/Guava
 */

package com.microsoft.bot.connector.implementation;

import retrofit2.Retrofit;
import com.microsoft.bot.connector.Conversations;
import com.google.common.reflect.TypeToken;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.AttachmentData;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.bot.schema.models.ConversationParameters;
import com.microsoft.bot.schema.models.ConversationResourceResponse;
import com.microsoft.bot.schema.models.ConversationsResult;
import com.microsoft.bot.connector.models.ErrorResponseException;
import com.microsoft.bot.schema.models.ResourceResponse;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
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
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in Conversations.
 */
public class ConversationsImpl implements Conversations {
    /** The Retrofit service to perform REST calls. */
    private ConversationsService service;
    /** The service client containing this operation class. */
    private ConnectorClientImpl client;

    /**
     * Initializes an instance of ConversationsImpl.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ConversationsImpl(Retrofit retrofit, ConnectorClientImpl client) {
        this.service = retrofit.create(ConversationsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Conversations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ConversationsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversations" })
        @GET("v3/conversations")
        Observable<Response<ResponseBody>> getConversations(@Query("continuationToken") String continuationToken, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations createConversation" })
        @POST("v3/conversations")
        Observable<Response<ResponseBody>> createConversation(@Body ConversationParameters parameters, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations sendToConversation" })
        @POST("v3/conversations/{conversationId}/activities")
        Observable<Response<ResponseBody>> sendToConversation(@Path("conversationId") String conversationId, @Body Activity activity, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations updateActivity" })
        @PUT("v3/conversations/{conversationId}/activities/{activityId}")
        Observable<Response<ResponseBody>> updateActivity(@Path("conversationId") String conversationId, @Path("activityId") String activityId, @Body Activity activity, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations replyToActivity" })
        @POST("v3/conversations/{conversationId}/activities/{activityId}")
        Observable<Response<ResponseBody>> replyToActivity(@Path("conversationId") String conversationId, @Path("activityId") String activityId, @Body Activity activity, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations deleteActivity" })
        @HTTP(path = "v3/conversations/{conversationId}/activities/{activityId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteActivity(@Path("conversationId") String conversationId, @Path("activityId") String activityId, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations getConversationMembers" })
        @GET("v3/conversations/{conversationId}/members")
        Observable<Response<ResponseBody>> getConversationMembers(@Path("conversationId") String conversationId, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations deleteConversationMember" })
        @HTTP(path = "v3/conversations/{conversationId}/members/{memberId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteConversationMember(@Path("conversationId") String conversationId, @Path("memberId") String memberId, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations getActivityMembers" })
        @GET("v3/conversations/{conversationId}/activities/{activityId}/members")
        Observable<Response<ResponseBody>> getActivityMembers(@Path("conversationId") String conversationId, @Path("activityId") String activityId, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.bot.schema.Conversations uploadAttachment" })
        @POST("v3/conversations/{conversationId}/attachments")
        Observable<Response<ResponseBody>> uploadAttachment(@Path("conversationId") String conversationId, @Body AttachmentData attachmentUpload, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ConversationsResult object if successful.
     */
    public ConversationsResult getConversations() {
        return getConversationsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ConversationsResult> getConversationsAsync(final ServiceCallback<ConversationsResult> serviceCallback) {
        return ServiceFuture.fromResponse(getConversationsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ConversationsResult object
     */
    public Observable<ConversationsResult> getConversationsAsync() {
        return getConversationsWithServiceResponseAsync().map(new Func1<ServiceResponse<ConversationsResult>, ConversationsResult>() {
            @Override
            public ConversationsResult call(ServiceResponse<ConversationsResult> response) {
                return response.body();
            }
        });
    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ConversationsResult object
     */
    public Observable<ServiceResponse<ConversationsResult>> getConversationsWithServiceResponseAsync() {
        final String continuationToken = null;
        return service.getConversations(continuationToken, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ConversationsResult>>>() {
                @Override
                public Observable<ServiceResponse<ConversationsResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ConversationsResult> clientResponse = getConversationsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @param continuationToken skip or continuation token
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ConversationsResult object if successful.
     */
    public ConversationsResult getConversations(String continuationToken) {
        return getConversationsWithServiceResponseAsync(continuationToken).toBlocking().single().body();
    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @param continuationToken skip or continuation token
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ConversationsResult> getConversationsAsync(String continuationToken, final ServiceCallback<ConversationsResult> serviceCallback) {
        return ServiceFuture.fromResponse(getConversationsWithServiceResponseAsync(continuationToken), serviceCallback);
    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @param continuationToken skip or continuation token
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ConversationsResult object
     */
    public Observable<ConversationsResult> getConversationsAsync(String continuationToken) {
        return getConversationsWithServiceResponseAsync(continuationToken).map(new Func1<ServiceResponse<ConversationsResult>, ConversationsResult>() {
            @Override
            public ConversationsResult call(ServiceResponse<ConversationsResult> response) {
                return response.body();
            }
        });
    }

    /**
     * GetConversations.
     * List the Conversations in which this bot has participated.
     GET from this method with a skip token
     The return value is a ConversationsResult, which contains an array of ConversationMembers and a skip token.  If the skip token is not empty, then
     there are further values to be returned. Call this method again with the returned token to get more values.
     Each ConversationMembers object contains the ID of the conversation and an array of ChannelAccounts that describe the members of the conversation.
     *
     * @param continuationToken skip or continuation token
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ConversationsResult object
     */
    public Observable<ServiceResponse<ConversationsResult>> getConversationsWithServiceResponseAsync(String continuationToken) {
        return service.getConversations(continuationToken, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ConversationsResult>>>() {
                @Override
                public Observable<ServiceResponse<ConversationsResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ConversationsResult> clientResponse = getConversationsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ConversationsResult> getConversationsDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException {
        return this.client.restClient().responseBuilderFactory().<ConversationsResult, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ConversationsResult>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * CreateConversation.
     * Create a new Conversation.
     POST to this method with a
     * Bot being the bot creating the conversation
     * IsGroup set to true if this is not a direct message (default is false)
     * Members array contining the members you want to have be in the conversation.
     The return value is a ResourceResponse which contains a conversation id which is suitable for use
     in the message payload and REST API uris.
     Most channels only support the semantics of bots initiating a direct message conversation.  An example of how to do that would be:
     ```
     var resource = await connector.conversations.CreateConversation(new ConversationParameters(){ Bot = bot, members = new ChannelAccount[] { new ChannelAccount("user1") } );
     await connect.Conversations.SendToConversationAsync(resource.Id, new Activity() ... ) ;
     ```.
     *
     * @param parameters Parameters to create the conversation from
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ConversationResourceResponse object if successful.
     */
    public ConversationResourceResponse createConversation(ConversationParameters parameters) {
        return createConversationWithServiceResponseAsync(parameters).toBlocking().single().body();
    }

    /**
     * CreateConversation.
     * Create a new Conversation.
     POST to this method with a
     * Bot being the bot creating the conversation
     * IsGroup set to true if this is not a direct message (default is false)
     * Members array contining the members you want to have be in the conversation.
     The return value is a ResourceResponse which contains a conversation id which is suitable for use
     in the message payload and REST API uris.
     Most channels only support the semantics of bots initiating a direct message conversation.  An example of how to do that would be:
     ```
     var resource = await connector.conversations.CreateConversation(new ConversationParameters(){ Bot = bot, members = new ChannelAccount[] { new ChannelAccount("user1") } );
     await connect.Conversations.SendToConversationAsync(resource.Id, new Activity() ... ) ;
     ```.
     *
     * @param parameters Parameters to create the conversation from
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ConversationResourceResponse> createConversationAsync(ConversationParameters parameters, final ServiceCallback<ConversationResourceResponse> serviceCallback) {
        return ServiceFuture.fromResponse(createConversationWithServiceResponseAsync(parameters), serviceCallback);
    }

    /**
     * CreateConversation.
     * Create a new Conversation.
     POST to this method with a
     * Bot being the bot creating the conversation
     * IsGroup set to true if this is not a direct message (default is false)
     * Members array contining the members you want to have be in the conversation.
     The return value is a ResourceResponse which contains a conversation id which is suitable for use
     in the message payload and REST API uris.
     Most channels only support the semantics of bots initiating a direct message conversation.  An example of how to do that would be:
     ```
     var resource = await connector.conversations.CreateConversation(new ConversationParameters(){ Bot = bot, members = new ChannelAccount[] { new ChannelAccount("user1") } );
     await connect.Conversations.SendToConversationAsync(resource.Id, new Activity() ... ) ;
     ```.
     *
     * @param parameters Parameters to create the conversation from
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ConversationResourceResponse object
     */
    public Observable<ConversationResourceResponse> createConversationAsync(ConversationParameters parameters) {
        return createConversationWithServiceResponseAsync(parameters).map(new Func1<ServiceResponse<ConversationResourceResponse>, ConversationResourceResponse>() {
            @Override
            public ConversationResourceResponse call(ServiceResponse<ConversationResourceResponse> response) {
                return response.body();
            }
        });
    }

    /**
     * CreateConversation.
     * Create a new Conversation.
     POST to this method with a
     * Bot being the bot creating the conversation
     * IsGroup set to true if this is not a direct message (default is false)
     * Members array contining the members you want to have be in the conversation.
     The return value is a ResourceResponse which contains a conversation id which is suitable for use
     in the message payload and REST API uris.
     Most channels only support the semantics of bots initiating a direct message conversation.  An example of how to do that would be:
     ```
     var resource = await connector.conversations.CreateConversation(new ConversationParameters(){ Bot = bot, members = new ChannelAccount[] { new ChannelAccount("user1") } );
     await connect.Conversations.SendToConversationAsync(resource.Id, new Activity() ... ) ;
     ```.
     *
     * @param parameters Parameters to create the conversation from
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ConversationResourceResponse object
     */
    public Observable<ServiceResponse<ConversationResourceResponse>> createConversationWithServiceResponseAsync(ConversationParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        Validator.validate(parameters);
        return service.createConversation(parameters, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ConversationResourceResponse>>>() {
                @Override
                public Observable<ServiceResponse<ConversationResourceResponse>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ConversationResourceResponse> clientResponse = createConversationDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ConversationResourceResponse> createConversationDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ConversationResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ConversationResourceResponse>() { }.getType())
                .register(201, new TypeToken<ConversationResourceResponse>() { }.getType())
                .register(202, new TypeToken<ConversationResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * SendToConversation.
     * This method allows you to send an activity to the end of a conversation.
     This is slightly different from ReplyToActivity().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activity Activity to send
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ResourceResponse object if successful.
     */
    public ResourceResponse sendToConversation(String conversationId, Activity activity) {
        return sendToConversationWithServiceResponseAsync(conversationId, activity).toBlocking().single().body();
    }

    /**
     * SendToConversation.
     * This method allows you to send an activity to the end of a conversation.
     This is slightly different from ReplyToActivity().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activity Activity to send
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ResourceResponse> sendToConversationAsync(String conversationId, Activity activity, final ServiceCallback<ResourceResponse> serviceCallback) {
        return ServiceFuture.fromResponse(sendToConversationWithServiceResponseAsync(conversationId, activity), serviceCallback);
    }

    /**
     * SendToConversation.
     * This method allows you to send an activity to the end of a conversation.
     This is slightly different from ReplyToActivity().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activity Activity to send
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ResourceResponse> sendToConversationAsync(String conversationId, Activity activity) {
        return sendToConversationWithServiceResponseAsync(conversationId, activity).map(new Func1<ServiceResponse<ResourceResponse>, ResourceResponse>() {
            @Override
            public ResourceResponse call(ServiceResponse<ResourceResponse> response) {
                return response.body();
            }
        });
    }

    /**
     * SendToConversation.
     * This method allows you to send an activity to the end of a conversation.
     This is slightly different from ReplyToActivity().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activity Activity to send
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ServiceResponse<ResourceResponse>> sendToConversationWithServiceResponseAsync(String conversationId, Activity activity) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity is required and cannot be null.");
        }
        Validator.validate(activity);
        return service.sendToConversation(conversationId, activity, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ResourceResponse>>>() {
                @Override
                public Observable<ServiceResponse<ResourceResponse>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ResourceResponse> clientResponse = sendToConversationDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ResourceResponse> sendToConversationDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * UpdateActivity.
     * Edit an existing activity.
     Some channels allow you to edit an existing activity to reflect the new state of a bot conversation.
     For example, you can remove buttons after someone has clicked "Approve" button.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to update
     * @param activity replacement Activity
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ResourceResponse object if successful.
     */
    public ResourceResponse updateActivity(String conversationId, String activityId, Activity activity) {
        return updateActivityWithServiceResponseAsync(conversationId, activityId, activity).toBlocking().single().body();
    }

    /**
     * UpdateActivity.
     * Edit an existing activity.
     Some channels allow you to edit an existing activity to reflect the new state of a bot conversation.
     For example, you can remove buttons after someone has clicked "Approve" button.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to update
     * @param activity replacement Activity
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ResourceResponse> updateActivityAsync(String conversationId, String activityId, Activity activity, final ServiceCallback<ResourceResponse> serviceCallback) {
        return ServiceFuture.fromResponse(updateActivityWithServiceResponseAsync(conversationId, activityId, activity), serviceCallback);
    }

    /**
     * UpdateActivity.
     * Edit an existing activity.
     Some channels allow you to edit an existing activity to reflect the new state of a bot conversation.
     For example, you can remove buttons after someone has clicked "Approve" button.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to update
     * @param activity replacement Activity
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ResourceResponse> updateActivityAsync(String conversationId, String activityId, Activity activity) {
        return updateActivityWithServiceResponseAsync(conversationId, activityId, activity).map(new Func1<ServiceResponse<ResourceResponse>, ResourceResponse>() {
            @Override
            public ResourceResponse call(ServiceResponse<ResourceResponse> response) {
                return response.body();
            }
        });
    }

    /**
     * UpdateActivity.
     * Edit an existing activity.
     Some channels allow you to edit an existing activity to reflect the new state of a bot conversation.
     For example, you can remove buttons after someone has clicked "Approve" button.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to update
     * @param activity replacement Activity
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ServiceResponse<ResourceResponse>> updateActivityWithServiceResponseAsync(String conversationId, String activityId, Activity activity) {
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
        return service.updateActivity(conversationId, activityId, activity, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ResourceResponse>>>() {
                @Override
                public Observable<ServiceResponse<ResourceResponse>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ResourceResponse> clientResponse = updateActivityDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ResourceResponse> updateActivityDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * ReplyToActivity.
     * This method allows you to reply to an activity.
     This is slightly different from SendToConversation().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId the reply is to (OPTIONAL)
     * @param activity Activity to send
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ResourceResponse object if successful.
     */
    public ResourceResponse replyToActivity(String conversationId, String activityId, Activity activity) {
        return replyToActivityWithServiceResponseAsync(conversationId, activityId, activity).toBlocking().single().body();
    }

    /**
     * ReplyToActivity.
     * This method allows you to reply to an activity.
     This is slightly different from SendToConversation().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId the reply is to (OPTIONAL)
     * @param activity Activity to send
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ResourceResponse> replyToActivityAsync(String conversationId, String activityId, Activity activity, final ServiceCallback<ResourceResponse> serviceCallback) {
        return ServiceFuture.fromResponse(replyToActivityWithServiceResponseAsync(conversationId, activityId, activity), serviceCallback);
    }

    /**
     * ReplyToActivity.
     * This method allows you to reply to an activity.
     This is slightly different from SendToConversation().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId the reply is to (OPTIONAL)
     * @param activity Activity to send
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ResourceResponse> replyToActivityAsync(String conversationId, String activityId, Activity activity) {
        return replyToActivityWithServiceResponseAsync(conversationId, activityId, activity).map(new Func1<ServiceResponse<ResourceResponse>, ResourceResponse>() {
            @Override
            public ResourceResponse call(ServiceResponse<ResourceResponse> response) {
                return response.body();
            }
        });
    }

    /**
     * ReplyToActivity.
     * This method allows you to reply to an activity.
     This is slightly different from SendToConversation().
     * SendToConverstion(conversationId) - will append the activity to the end of the conversation according to the timestamp or semantics of the channel.
     * ReplyToActivity(conversationId,ActivityId) - adds the activity as a reply to another activity, if the channel supports it. If the channel does not support nested replies, ReplyToActivity falls back to SendToConversation.
     Use ReplyToActivity when replying to a specific activity in the conversation.
     Use SendToConversation in all other cases.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId the reply is to (OPTIONAL)
     * @param activity Activity to send
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ServiceResponse<ResourceResponse>> replyToActivityWithServiceResponseAsync(String conversationId, String activityId, Activity activity) {
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
        return service.replyToActivity(conversationId, activityId, activity, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ResourceResponse>>>() {
                @Override
                public Observable<ServiceResponse<ResourceResponse>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ResourceResponse> clientResponse = replyToActivityDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ResourceResponse> replyToActivityDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * DeleteActivity.
     * Delete an existing activity.
     Some channels allow you to delete an existing activity, and if successful this method will remove the specified activity.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to delete
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void deleteActivity(String conversationId, String activityId) {
        deleteActivityWithServiceResponseAsync(conversationId, activityId).toBlocking().single().body();
    }

    /**
     * DeleteActivity.
     * Delete an existing activity.
     Some channels allow you to delete an existing activity, and if successful this method will remove the specified activity.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to delete
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> deleteActivityAsync(String conversationId, String activityId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(deleteActivityWithServiceResponseAsync(conversationId, activityId), serviceCallback);
    }

    /**
     * DeleteActivity.
     * Delete an existing activity.
     Some channels allow you to delete an existing activity, and if successful this method will remove the specified activity.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to delete
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> deleteActivityAsync(String conversationId, String activityId) {
        return deleteActivityWithServiceResponseAsync(conversationId, activityId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * DeleteActivity.
     * Delete an existing activity.
     Some channels allow you to delete an existing activity, and if successful this method will remove the specified activity.
     *
     * @param conversationId Conversation ID
     * @param activityId activityId to delete
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> deleteActivityWithServiceResponseAsync(String conversationId, String activityId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activityId == null) {
            throw new IllegalArgumentException("Parameter activityId is required and cannot be null.");
        }
        return service.deleteActivity(conversationId, activityId, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = deleteActivityDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> deleteActivityDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * GetConversationMembers.
     * Enumerate the members of a converstion.
     This REST API takes a ConversationId and returns an array of ChannelAccount objects representing the members of the conversation.
     *
     * @param conversationId Conversation ID
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;ChannelAccount&gt; object if successful.
     */
    public List<ChannelAccount> getConversationMembers(String conversationId) {
        return getConversationMembersWithServiceResponseAsync(conversationId).toBlocking().single().body();
    }

    /**
     * GetConversationMembers.
     * Enumerate the members of a converstion.
     This REST API takes a ConversationId and returns an array of ChannelAccount objects representing the members of the conversation.
     *
     * @param conversationId Conversation ID
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<ChannelAccount>> getConversationMembersAsync(String conversationId, final ServiceCallback<List<ChannelAccount>> serviceCallback) {
        return ServiceFuture.fromResponse(getConversationMembersWithServiceResponseAsync(conversationId), serviceCallback);
    }

    /**
     * GetConversationMembers.
     * Enumerate the members of a converstion.
     This REST API takes a ConversationId and returns an array of ChannelAccount objects representing the members of the conversation.
     *
     * @param conversationId Conversation ID
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;ChannelAccount&gt; object
     */
    public Observable<List<ChannelAccount>> getConversationMembersAsync(String conversationId) {
        return getConversationMembersWithServiceResponseAsync(conversationId).map(new Func1<ServiceResponse<List<ChannelAccount>>, List<ChannelAccount>>() {
            @Override
            public List<ChannelAccount> call(ServiceResponse<List<ChannelAccount>> response) {
                return response.body();
            }
        });
    }

    /**
     * GetConversationMembers.
     * Enumerate the members of a converstion.
     This REST API takes a ConversationId and returns an array of ChannelAccount objects representing the members of the conversation.
     *
     * @param conversationId Conversation ID
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;ChannelAccount&gt; object
     */
    public Observable<ServiceResponse<List<ChannelAccount>>> getConversationMembersWithServiceResponseAsync(String conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        return service.getConversationMembers(conversationId, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<ChannelAccount>>>>() {
                @Override
                public Observable<ServiceResponse<List<ChannelAccount>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<ChannelAccount>> clientResponse = getConversationMembersDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<ChannelAccount>> getConversationMembersDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<ChannelAccount>, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<ChannelAccount>>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * DeleteConversationMember.
     * Deletes a member from a converstion.
     This REST API takes a ConversationId and a memberId (of type string) and removes that member from the conversation. If that member was the last member
     of the conversation, the conversation will also be deleted.
     *
     * @param conversationId Conversation ID
     * @param memberId ID of the member to delete from this conversation
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void deleteConversationMember(String conversationId, String memberId) {
        deleteConversationMemberWithServiceResponseAsync(conversationId, memberId).toBlocking().single().body();
    }

    /**
     * DeleteConversationMember.
     * Deletes a member from a converstion.
     This REST API takes a ConversationId and a memberId (of type string) and removes that member from the conversation. If that member was the last member
     of the conversation, the conversation will also be deleted.
     *
     * @param conversationId Conversation ID
     * @param memberId ID of the member to delete from this conversation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> deleteConversationMemberAsync(String conversationId, String memberId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(deleteConversationMemberWithServiceResponseAsync(conversationId, memberId), serviceCallback);
    }

    /**
     * DeleteConversationMember.
     * Deletes a member from a converstion.
     This REST API takes a ConversationId and a memberId (of type string) and removes that member from the conversation. If that member was the last member
     of the conversation, the conversation will also be deleted.
     *
     * @param conversationId Conversation ID
     * @param memberId ID of the member to delete from this conversation
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> deleteConversationMemberAsync(String conversationId, String memberId) {
        return deleteConversationMemberWithServiceResponseAsync(conversationId, memberId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * DeleteConversationMember.
     * Deletes a member from a converstion.
     This REST API takes a ConversationId and a memberId (of type string) and removes that member from the conversation. If that member was the last member
     of the conversation, the conversation will also be deleted.
     *
     * @param conversationId Conversation ID
     * @param memberId ID of the member to delete from this conversation
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> deleteConversationMemberWithServiceResponseAsync(String conversationId, String memberId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("Parameter memberId is required and cannot be null.");
        }
        return service.deleteConversationMember(conversationId, memberId, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = deleteConversationMemberDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> deleteConversationMemberDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * GetActivityMembers.
     * Enumerate the members of an activity.
     This REST API takes a ConversationId and a ActivityId, returning an array of ChannelAccount objects representing the members of the particular activity in the conversation.
     *
     * @param conversationId Conversation ID
     * @param activityId Activity ID
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;ChannelAccount&gt; object if successful.
     */
    public List<ChannelAccount> getActivityMembers(String conversationId, String activityId) {
        return getActivityMembersWithServiceResponseAsync(conversationId, activityId).toBlocking().single().body();
    }

    /**
     * GetActivityMembers.
     * Enumerate the members of an activity.
     This REST API takes a ConversationId and a ActivityId, returning an array of ChannelAccount objects representing the members of the particular activity in the conversation.
     *
     * @param conversationId Conversation ID
     * @param activityId Activity ID
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<ChannelAccount>> getActivityMembersAsync(String conversationId, String activityId, final ServiceCallback<List<ChannelAccount>> serviceCallback) {
        return ServiceFuture.fromResponse(getActivityMembersWithServiceResponseAsync(conversationId, activityId), serviceCallback);
    }

    /**
     * GetActivityMembers.
     * Enumerate the members of an activity.
     This REST API takes a ConversationId and a ActivityId, returning an array of ChannelAccount objects representing the members of the particular activity in the conversation.
     *
     * @param conversationId Conversation ID
     * @param activityId Activity ID
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;ChannelAccount&gt; object
     */
    public Observable<List<ChannelAccount>> getActivityMembersAsync(String conversationId, String activityId) {
        return getActivityMembersWithServiceResponseAsync(conversationId, activityId).map(new Func1<ServiceResponse<List<ChannelAccount>>, List<ChannelAccount>>() {
            @Override
            public List<ChannelAccount> call(ServiceResponse<List<ChannelAccount>> response) {
                return response.body();
            }
        });
    }

    /**
     * GetActivityMembers.
     * Enumerate the members of an activity.
     This REST API takes a ConversationId and a ActivityId, returning an array of ChannelAccount objects representing the members of the particular activity in the conversation.
     *
     * @param conversationId Conversation ID
     * @param activityId Activity ID
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;ChannelAccount&gt; object
     */
    public Observable<ServiceResponse<List<ChannelAccount>>> getActivityMembersWithServiceResponseAsync(String conversationId, String activityId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (activityId == null) {
            throw new IllegalArgumentException("Parameter activityId is required and cannot be null.");
        }
        return service.getActivityMembers(conversationId, activityId, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<ChannelAccount>>>>() {
                @Override
                public Observable<ServiceResponse<List<ChannelAccount>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<ChannelAccount>> clientResponse = getActivityMembersDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<ChannelAccount>> getActivityMembersDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<ChannelAccount>, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<ChannelAccount>>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * UploadAttachment.
     * Upload an attachment directly into a channel's blob storage.
     This is useful because it allows you to store data in a compliant store when dealing with enterprises.
     The response is a ResourceResponse which contains an AttachmentId which is suitable for using with the attachments API.
     *
     * @param conversationId Conversation ID
     * @param attachmentUpload Attachment data
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ResourceResponse object if successful.
     */
    public ResourceResponse uploadAttachment(String conversationId, AttachmentData attachmentUpload) {
        return uploadAttachmentWithServiceResponseAsync(conversationId, attachmentUpload).toBlocking().single().body();
    }

    /**
     * UploadAttachment.
     * Upload an attachment directly into a channel's blob storage.
     This is useful because it allows you to store data in a compliant store when dealing with enterprises.
     The response is a ResourceResponse which contains an AttachmentId which is suitable for using with the attachments API.
     *
     * @param conversationId Conversation ID
     * @param attachmentUpload Attachment data
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ResourceResponse> uploadAttachmentAsync(String conversationId, AttachmentData attachmentUpload, final ServiceCallback<ResourceResponse> serviceCallback) {
        return ServiceFuture.fromResponse(uploadAttachmentWithServiceResponseAsync(conversationId, attachmentUpload), serviceCallback);
    }

    /**
     * UploadAttachment.
     * Upload an attachment directly into a channel's blob storage.
     This is useful because it allows you to store data in a compliant store when dealing with enterprises.
     The response is a ResourceResponse which contains an AttachmentId which is suitable for using with the attachments API.
     *
     * @param conversationId Conversation ID
     * @param attachmentUpload Attachment data
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ResourceResponse> uploadAttachmentAsync(String conversationId, AttachmentData attachmentUpload) {
        return uploadAttachmentWithServiceResponseAsync(conversationId, attachmentUpload).map(new Func1<ServiceResponse<ResourceResponse>, ResourceResponse>() {
            @Override
            public ResourceResponse call(ServiceResponse<ResourceResponse> response) {
                return response.body();
            }
        });
    }

    /**
     * UploadAttachment.
     * Upload an attachment directly into a channel's blob storage.
     This is useful because it allows you to store data in a compliant store when dealing with enterprises.
     The response is a ResourceResponse which contains an AttachmentId which is suitable for using with the attachments API.
     *
     * @param conversationId Conversation ID
     * @param attachmentUpload Attachment data
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ResourceResponse object
     */
    public Observable<ServiceResponse<ResourceResponse>> uploadAttachmentWithServiceResponseAsync(String conversationId, AttachmentData attachmentUpload) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Parameter conversationId is required and cannot be null.");
        }
        if (attachmentUpload == null) {
            throw new IllegalArgumentException("Parameter attachmentUpload is required and cannot be null.");
        }
        Validator.validate(attachmentUpload);
        return service.uploadAttachment(conversationId, attachmentUpload, this.client.acceptLanguage(), this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ResourceResponse>>>() {
                @Override
                public Observable<ServiceResponse<ResourceResponse>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ResourceResponse> clientResponse = uploadAttachmentDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ResourceResponse> uploadAttachmentDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ResourceResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ResourceResponse>() { }.getType())
                .register(201, new TypeToken<ResourceResponse>() { }.getType())
                .register(202, new TypeToken<ResourceResponse>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

}
