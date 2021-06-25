// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.connector.ConversationConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.MicrosoftGovernmentAppCredentials;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.TypedInvokeResponse;
import com.microsoft.bot.builder.skills.BotFrameworkClient;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.RoleTypes;
import com.microsoft.bot.schema.Serialization;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;

/**
 * Class for posting activities securely to a bot using BotFramework HTTP
 * protocol.
 *
 * This class can be used to securely post activities to a bot using the Bot
 * Framework HTTP protocol. There are 2 usage patterns:* Forwarding activity to
 * a Skill (Bot -> Bot as a Skill) which is done via PostActivity(fromBotId,
 * toBotId, endpoint, serviceUrl, activity);* Posting an activity to yourself
 * (External service -> Bot) which is done via PostActivity(botId, endpoint,
 * activity)The latter is used by external services such as webjobs that need to
 * post activities to the bot using the bots own credentials.
 */
public class BotFrameworkHttpClient extends BotFrameworkClient {

    private static Map<String, AppCredentials> appCredentialMapCache = new HashMap<String, AppCredentials>();;

    private ChannelProvider channelProvider;

    private CredentialProvider credentialProvider;

    private OkHttpClient httpClient;

    /**
     * Initializes a new instance of the {@link BotFrameworkHttpClient} class.
     *
     * @param credentialProvider An instance of {@link CredentialProvider} .
     * @param channelProvider    An instance of {@link ChannelProvider} .
     */
    public BotFrameworkHttpClient(CredentialProvider credentialProvider, ChannelProvider channelProvider) {

        if (credentialProvider == null) {
            throw new IllegalArgumentException("credentialProvider cannot be null.");
        }
        this.credentialProvider = credentialProvider;
        this.channelProvider = channelProvider;
        this.httpClient = new OkHttpClient();
    }

    /**
     * Forwards an activity to a skill (bot).
     *
     * NOTE: Forwarding an activity to a skill will flush UserState and
     * ConversationState changes so that skill has accurate state.
     *
     * @param fromBotId      The MicrosoftAppId of the bot sending the activity.
     * @param toBotId        The MicrosoftAppId of the bot receiving the activity.
     * @param toUrl          The URL of the bot receiving the activity.
     * @param serviceUrl     The callback Url for the skill host.
     * @param conversationId A conversation D to use for the conversation with the
     *                       skill.
     * @param activity       activity to forward.
     *
     * @return task with optional invokeResponse.
     */
    @Override
    public <T extends Object> CompletableFuture<TypedInvokeResponse<T>> postActivity(String fromBotId, String toBotId,
            URI toUrl, URI serviceUrl, String conversationId, Activity activity, Class<T> type) {

        return getAppCredentials(fromBotId, toBotId).thenCompose(appCredentials -> {
            if (appCredentials == null) {
                return Async.completeExceptionally(
                        new Exception(String.format("Unable to get appCredentials to connect to the skill")));
            }

            // Get token for the skill call
            return getToken(appCredentials).thenCompose(token -> {
                // Clone the activity so we can modify it before sending without impacting the
                // original Object.
                Activity activityClone = Activity.clone(activity);

                ConversationAccount conversationAccount = new ConversationAccount();
                conversationAccount.setId(activityClone.getConversation().getId());
                conversationAccount.setName(activityClone.getConversation().getName());
                conversationAccount.setConversationType(activityClone.getConversation().getConversationType());
                conversationAccount.setAadObjectId(activityClone.getConversation().getAadObjectId());
                conversationAccount.setIsGroup(activityClone.getConversation().isGroup());
                for (String key : conversationAccount.getProperties().keySet()) {
                    activityClone.setProperties(key, conversationAccount.getProperties().get(key));
                }
                conversationAccount.setRole(activityClone.getConversation().getRole());
                conversationAccount.setTenantId(activityClone.getConversation().getTenantId());

                ConversationReference conversationReference = new ConversationReference();
                conversationReference.setServiceUrl(activityClone.getServiceUrl());
                conversationReference.setActivityId(activityClone.getId());
                conversationReference.setChannelId(activityClone.getChannelId());
                conversationReference.setLocale(activityClone.getLocale());
                conversationReference.setConversation(conversationAccount);

                activityClone.setRelatesTo(conversationReference);
                activityClone.getConversation().setId(conversationId);
                activityClone.setServiceUrl(serviceUrl.toString());
                if (activityClone.getRecipient() == null) {
                    activityClone.setRecipient(new ChannelAccount());
                }
                activityClone.getRecipient().setRole(RoleTypes.SKILL);

                return securePostActivity(toUrl, activityClone, token, type);
            });
        });
    }

    private CompletableFuture<String> getToken(AppCredentials appCredentials) {
        // Get token for the skill call
        if (appCredentials == MicrosoftAppCredentials.empty()) {
            return CompletableFuture.completedFuture(null);
        } else {
            return appCredentials.getToken();
        }
    }

    /**
     * Post Activity to the bot using the bot's credentials.
     *
     * @param botId       The MicrosoftAppId of the bot.
     * @param botEndpoint The URL of the bot.
     * @param activity    Activity to post.
     * @param type        Type of <T>.
     * @param <T>         Type of expected TypedInvokeResponse.
     *
     * @return InvokeResponse.
     */
    public <T extends Object> CompletableFuture<TypedInvokeResponse<T>> postActivity(String botId, URI botEndpoint,
            Activity activity, Class<T> type) {

        // From BotId -> BotId
        return getAppCredentials(botId, botId).thenCompose(appCredentials -> {
            if (appCredentials == null) {
                return Async.completeExceptionally(
                        new Exception(String.format("Unable to get appCredentials for the bot Id=%s", botId)));
            }

            return getToken(appCredentials).thenCompose(token -> {
                // post the activity to the url using the bot's credentials.
                return securePostActivity(botEndpoint, activity, token, type);
            });
        });
    }

    /**
     * Logic to build an {@link AppCredentials} Object to be used to acquire tokens
     * for this getHttpClient().
     *
     * @param appId      The application id.
     * @param oAuthScope The optional OAuth scope.
     *
     * @return The app credentials to be used to acquire tokens.
     */
    protected CompletableFuture<AppCredentials> buildCredentials(String appId, String oAuthScope) {
        return getCredentialProvider().getAppPassword(appId).thenCompose(appPassword -> {
                AppCredentials appCredentials = channelProvider != null && getChannelProvider().isGovernment()
                ? new MicrosoftGovernmentAppCredentials(appId, appPassword, null, oAuthScope)
                : new MicrosoftAppCredentials(appId, appPassword, null, oAuthScope);
            return CompletableFuture.completedFuture(appCredentials);
        });
    }

    private <T extends Object> CompletableFuture<TypedInvokeResponse<T>> securePostActivity(
        URI toUrl,
        Activity activity,
        String token,
        Class<T> type
    ) {
        String jsonContent = "";
        try {
            ObjectMapper mapper = new JacksonAdapter().serializer();
            jsonContent = mapper.writeValueAsString(activity);
        } catch (JsonProcessingException e) {
            return Async.completeExceptionally(
                    new RuntimeException("securePostActivity: Unable to serialize the Activity"));
        }

        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonContent);
            Request request = buildRequest(activity, toUrl, body, token);
            Response response = httpClient.newCall(request).execute();

            T result = Serialization.getAs(response.body().string(), type);
            TypedInvokeResponse<T> returnValue = new TypedInvokeResponse<T>(response.code(), result);
            return CompletableFuture.completedFuture(returnValue);
        } catch (IOException e) {
            return Async.completeExceptionally(e);
        }
    }

    private Request buildRequest(Activity activity, URI url, RequestBody body, String token) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url.toString()).newBuilder();

        Request.Builder requestBuilder = new Request.Builder().url(httpBuilder.build());
        if (token != null) {
            requestBuilder.addHeader("Authorization", String.format("Bearer %s", token));
        }
        requestBuilder.addHeader(
            ConversationConstants.CONVERSATION_ID_HTTP_HEADERNAME,
            activity.getConversation().getId()
        );
        requestBuilder.post(body);
        return requestBuilder.build();
    }

    /**
     * Gets the application credentials. App Credentials are cached so as to ensure
     * we are not refreshing token every time.
     *
     * @param appId      The application identifier (AAD Id for the bot).
     * @param oAuthScope The scope for the token, skills will use the Skill App Id.
     *
     * @return App credentials.
     */
    private CompletableFuture<AppCredentials> getAppCredentials(String appId, String oAuthScope) {
        if (StringUtils.isEmpty(appId)) {
            return CompletableFuture.completedFuture(MicrosoftAppCredentials.empty());
        }

        // If the credentials are in the cache, retrieve them from there
        String cacheKey = String.format("%s%s", appId, oAuthScope);
        AppCredentials appCredentials = null;
        appCredentials = appCredentialMapCache.get(cacheKey);
        if (appCredentials != null) {
            return CompletableFuture.completedFuture(appCredentials);
        }

        // Credentials not found in cache, build them
        return buildCredentials(appId, String.format("%s/.default", oAuthScope)).thenCompose(credentials -> {
            // Cache the credentials for later use
            appCredentialMapCache.put(cacheKey, credentials);
            return CompletableFuture.completedFuture(credentials);
        });
    }

    /**
     * Gets the Cache for appCredentials to speed up token acquisition (a token is
     * not requested unless is expired). AppCredentials are cached using appId +
     * scope (this last parameter is only used if the app credentials are used to
     * call a skill).
     *
     * @return the AppCredentialMapCache value as a static
     *         ConcurrentDictionary<String, AppCredentials>.
     */
    protected static Map<String, AppCredentials> getAppCredentialMapCache() {
        return appCredentialMapCache;
    }

    /**
     * Gets the channel provider for this adapter.
     *
     * @return the ChannelProvider value as a getChannelProvider().
     */
    protected ChannelProvider getChannelProvider() {
        return this.channelProvider;
    }

    /**
     * Gets the credential provider for this adapter.
     *
     * @return the CredentialProvider value as a getCredentialProvider().
     */
    protected CredentialProvider getCredentialProvider() {
        return this.credentialProvider;
    }

    /**
     * Gets the HttpClient for this adapter.
     *
     * @return the OkhttpClient value as a getHttpClient().
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}
