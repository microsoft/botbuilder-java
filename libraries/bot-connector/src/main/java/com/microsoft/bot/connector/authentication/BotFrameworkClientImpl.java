// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.skills.BotFrameworkClient;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.RoleTypes;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.TypedInvokeResponse;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class BotFrameworkClientImpl extends BotFrameworkClient {
    private final ServiceClientCredentialsFactory credentialsFactory;
    private final OkHttpClient httpClient;
    private final String loginEndpoint;

    private final Logger logger = LoggerFactory.getLogger(BotFrameworkClientImpl.class);

    public BotFrameworkClientImpl(
        ServiceClientCredentialsFactory withCredentialsFactory,
        String withLoginEndpoint,
        OkHttpClient withHttpClient
    ) {
        credentialsFactory = withCredentialsFactory;
        loginEndpoint = withLoginEndpoint;
        httpClient = withHttpClient != null ? withHttpClient : new OkHttpClient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> CompletableFuture<TypedInvokeResponse<T>> postActivity(
        String fromBotId,
        String toBotId,
        URI toUri,
        URI serviceUri,
        String conversationId,
        Activity activity,
        Class<T> type
    ) {
        // We are not checking fromBotId and toBotId for null to address BB-dotnet issue #5577
        // (https://github.com/microsoft/botbuilder-dotnet/issues/5577)

        if (toUri == null) {
            throw new IllegalArgumentException("toUri cannot be null");
        }

        if (serviceUri == null) {
            throw new IllegalArgumentException("serviceUri cannot be null");
        }

        if (conversationId == null) {
            throw new IllegalArgumentException("conversationId cannot be null");
        }

        if (activity == null) {
            throw new IllegalArgumentException("activity cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        logger.info(String.format("post to skill '%s' at '%s'", toBotId, toUri));

        return credentialsFactory.createCredentials(fromBotId, toBotId, loginEndpoint, true)
            .thenCompose(credentials -> {
            // Clone the activity so we can modify it before sending without impacting the original object.
            Activity activityClone = Activity.clone(activity);

            // Apply the appropriate addressing to the newly created Activity.
            ConversationReference conversationReference = new ConversationReference();
            conversationReference.setServiceUrl(activityClone.getServiceUrl());
            conversationReference.setActivityId(activityClone.getId());
            conversationReference.setChannelId(activityClone.getChannelId());
            conversationReference.setLocale(activityClone.getLocale());

            ConversationAccount conversationAccount = new ConversationAccount();
            conversationAccount.setId(activityClone.getConversation().getId());
            conversationAccount.setName(activityClone.getConversation().getName());
            conversationAccount.setConversationType(activityClone.getConversation().getConversationType());
            conversationAccount.setAadObjectId(activityClone.getConversation().getAadObjectId());
            conversationAccount.setIsGroup(activityClone.getConversation().isGroup());
            for (String key : activityClone.getProperties().keySet()) {
                conversationAccount.setProperties(key, activityClone.getProperties().get(key));
            }
            conversationAccount.setRole(activityClone.getConversation().getRole());
            conversationAccount.setTenantId(activityClone.getConversation().getTenantId());

            conversationReference.setConversation(conversationAccount);
            activityClone.setRelatesTo(conversationReference);
            activityClone.getConversation().setId(conversationId);
            // Fixes: https://github.com/microsoft/botframework-sdk/issues/5785
            if (activityClone.getRecipient() == null) {
                activityClone.setRecipient(new ChannelAccount());
            }
            activityClone.getRecipient().setRole(RoleTypes.SKILL);

            // Create the HTTP request from the cloned Activity and send it to the Skill.
            String jsonContent = "";
            try {
                ObjectMapper mapper = new JacksonAdapter().serializer();
                jsonContent = mapper.writeValueAsString(activity);
            } catch (JsonProcessingException e) {
                return Async.completeExceptionally(
                    new RuntimeException("securePostActivity: Unable to serialize the Activity"));
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonContent);
            Request request = buildRequest(toUri, body);

            // Add the auth header to the HTTP request.
            credentials.applyCredentialsFilter(httpClient.newBuilder());

            try {
                Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful()) {
                    // On success assuming either JSON that can be deserialized to T or empty.
                    String bodyString = response.body().string();
                    T result = Serialization.getAs(bodyString, type);
                    TypedInvokeResponse<T> returnValue = new TypedInvokeResponse<T>(response.code(), result);
                    return CompletableFuture.completedFuture(returnValue);
                } else {
                    // Otherwise we can assume we don't have a T to deserialize
                    // So just log the content so it's not lost.
                    logger.error(String.format(
                        "Bot Framework call failed to '%s' returning '%d' and '%s'",
                        toUri,
                        response.code(),
                        response.body())
                    );

                    // We want to at least propagate the status code because that is what InvokeResponse expects.
                    TypedInvokeResponse<T> returnValue = new TypedInvokeResponse<>(
                        response.code(),
                        (T) response.body().string());
                    return CompletableFuture.completedFuture(returnValue);
                }

            } catch (IOException e) {
                return Async.completeExceptionally(e);
            }
        });
    }

    private Request buildRequest(URI url, RequestBody body) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url.toString()).newBuilder();
        Request.Builder requestBuilder = new Request.Builder().url(httpBuilder.build());
        requestBuilder.post(body);
        return requestBuilder.build();
    }
}
