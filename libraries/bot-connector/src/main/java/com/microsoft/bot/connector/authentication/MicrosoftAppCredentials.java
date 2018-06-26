// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import rx.Completable;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ea.async.Async.await;
import static com.microsoft.bot.connector.authentication.AuthenticationConstants.ToChannelFromBotLoginUrl;
import static com.microsoft.bot.connector.authentication.AuthenticationConstants.ToChannelFromBotOAuthScope;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.joining;

public class MicrosoftAppCredentials implements ServiceClientCredentials {
    private String appId;
    private String appPassword;

    private OkHttpClient client;
    private ObjectMapper mapper;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType FORM_ENCODE = MediaType.parse("application/x-www-form-urlencoded");

    private String currentToken = null;
    private long expiredTime = 0;
    private static final Object cacheSync = new Object();
    protected static final HashMap<String, OAuthResponse> cache = new HashMap<String, OAuthResponse>();

    public final String OAuthEndpoint = AuthenticationConstants.ToChannelFromBotLoginUrl;
    public final String OAuthScope = AuthenticationConstants.ToChannelFromBotOAuthScope;



    public String getTokenCacheKey() {
        return String.format("%s-cache", this.appId);
    }

    public MicrosoftAppCredentials(String appId, String appPassword) {
        this.appId = appId;
        this.appPassword = appPassword;
        this.client = new OkHttpClient.Builder().build();
        this.mapper = new ObjectMapper().findAndRegisterModules();
    }
    public static final MicrosoftAppCredentials Empty = new MicrosoftAppCredentials(null, null);

    public String microsoftAppId() {
        return this.appId;
    }
    public MicrosoftAppCredentials withMicrosoftAppId(String appId) {
        this.appId = appId;
        return this;
    }
    public String getToken(Request request) throws IOException {
        if (System.currentTimeMillis() < expiredTime) {
            return currentToken;
        }
        Request reqToken = request.newBuilder()
                .url(ToChannelFromBotLoginUrl)
                .post(new FormBody.Builder()
                        .add("grant_type", "client_credentials")
                        .add("client_id", this.appId)
                        .add("client_secret", this.appPassword)
                        .add("scope", ToChannelFromBotOAuthScope)
                        .build()).build();
        Response response = this.client.newCall(reqToken).execute();
        if (response.isSuccessful()) {
            String payload = response.body().string();
            AuthenticationResponse authResponse = this.mapper.readValue(payload, AuthenticationResponse.class);
            this.expiredTime = System.currentTimeMillis() + (authResponse.expiresIn * 1000);
            this.currentToken = authResponse.accessToken;
        }
        return this.currentToken;
    }
    public CompletableFuture<String> GetTokenAsync() throws IOException, URISyntaxException {
        return this.GetTokenAsync(false);
    }

    /**
     * Apply the credentials to the HTTP request.
     * @param request The HTTP request.@param cancellationToken Cancellation token.
     */
    public CompletableFuture<Response> ProcessHttpRequestAsync(boolean applyCredentials, String httpVerb, String url) throws InvalidParameterException, IOException, URISyntaxException {
        return ProcessHttpRequestAsync(applyCredentials, httpVerb, url, null);
    }
    public CompletableFuture<Response> ProcessHttpRequestAsync(boolean applyCredentials, String httpVerb, String url, RequestBody body) throws InvalidParameterException, IOException, URISyntaxException {
        Request.Builder httpRequestBuilder = new Request.Builder();
        switch (httpVerb.toLowerCase()) {
            case "get":
                httpRequestBuilder.get();
                break;
            case "post":
                if (body == null)
                    throw new InvalidParameterException("Attempting to POST with no body provided");
                httpRequestBuilder.post(body);
                break;
            case "delete":
                if (body == null)
                    httpRequestBuilder.delete();
                else
                    httpRequestBuilder.delete(body);
                break;

            default:
                throw new InvalidParameterException(String.format("Do not support %s http verb yet", httpVerb));

        }
        httpRequestBuilder.url(url);

        // Resolve the token if required
        if (ShouldSetToken(url))
            httpRequestBuilder.addHeader("Authorization", await(GetTokenAsync()));

        Request request = httpRequestBuilder.build();

        // Convert to CompletableFuture
        OkHttpClient client = this.client;
        if (applyCredentials) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            this.applyCredentialsFilter(clientBuilder);
            client = clientBuilder.build();
        }
        Call call = client.newCall(request);
        ResponseFuture result = new ResponseFuture(call);
        call.enqueue(result);
        return result.future;
    }


    private boolean ShouldSetToken(String url)
    {
        if (isTrustedServiceUrl(url))
        {
            return true;
        }
        return false;
    }



    public CompletableFuture<String> GetTokenAsync(boolean forceRefresh) throws IOException, URISyntaxException {
        if (forceRefresh == false) {
            // check the global cache for the token. If we have it, and it's valid, we're done.
            OAuthResponse oAuthToken = null;
            boolean found = false;
            synchronized (this.cacheSync) {
                if (this.cache.containsKey(this.getTokenCacheKey())) {
                    oAuthToken = this.cache.get(this.getTokenCacheKey());
                    found = true;
                }
            }
            // we have the token. Is it valid?
            if (found && oAuthToken.getExpirationTime().getMillis() > DateTime.now(DateTimeZone.UTC).getMillis())
            {
                return completedFuture(oAuthToken.getAccessToken());
            }
        }
        // We need to refresh the token, because:
        // 1. The user requested it via the forceRefresh parameter
        // 2. We have it, but it's expired
        // 3. We don't have it in the cache.

        OAuthResponse token = await(this.RefreshTokenAsync());
        synchronized (cacheSync)
        {
            this.cache.put(getTokenCacheKey(), token);
        }

        return completedFuture(token.getAccessToken());
    }

    private CompletableFuture<Response> PostAsync(String endpoint, HashMap<String, String> content) throws JsonProcessingException, URISyntaxException {
        String bodyText = this.mapper.writeValueAsString(content);

        RequestBody body = RequestBody.create(this.FORM_ENCODE, this.MakeFormBody(content));
        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();
        Call call = client.newCall(request);
        ResponseFuture result = new ResponseFuture(call);
        call.enqueue(result);
        return result.future;
    }
    private String MakeFormBody(HashMap<String, String> values) throws URISyntaxException, JsonProcessingException {
        String formBody = values.keySet().stream()
                .map(key -> {
                    try {
                        return key + "=" + URLEncoder.encode(values.get(key), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(joining("&", "", ""));
        return formBody;
    }

    // Corresponds to https://docs.microsoft.com/en-us/azure/bot-service/rest-api/bot-framework-rest-connector-authentication?view=azure-bot-service-4.0
    // Step 1: Request an access token from the MSA/AAD v2 login service
    private CompletableFuture<OAuthResponse> RefreshTokenAsync() throws IOException, URISyntaxException {
        HashMap content = new HashMap<String, String>();
        content.put("grant_type", "client_credentials");
        content.put("client_id", (this.appId==null) ? "" : this.appId);
        content.put("client_secret", (this.appPassword==null) ? "" : this.appPassword );
        content.put("scope", this.OAuthScope);

        try (Response response = await(this.PostAsync(this.OAuthEndpoint, content)))
        {
            ResponseBody body = null;
            if (response.code() < 200 || response.code() >= 300 )
                throw new IOException(String.format("Bad response : %s", response.code()) );
            body = response.body();
            OAuthResponse oauthresponse = this.mapper.readValue(body.string(), OAuthResponse.class);
            DateTime modifiedExpiration = DateTime.now(DateTimeZone.UTC).plusSeconds(oauthresponse.getExpiresIn()).minusSeconds(60);
            oauthresponse.withExpirationTime(modifiedExpiration);
            return completedFuture(oauthresponse);
        }
    }

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new MicrosoftAppCredentialsInterceptor(this));
    }

    private static class AuthenticationResponse {
        @JsonProperty(value = "token_type")
        String tokenType;
        @JsonProperty(value = "expires_in")
        long expiresIn;
        @JsonProperty(value = "ext_expires_in")
        long extExpiresIn;
        @JsonProperty(value = "access_token")
        String accessToken;
    }


    public static void trustServiceUrl(URI serviceUrl) {
        trustServiceUrl(serviceUrl.toString(), LocalDateTime.now().plusDays(1));
    }

    public static void trustServiceUrl(String serviceUrl) {
        trustServiceUrl(serviceUrl, LocalDateTime.now().plusDays(1));
    }

    public static void trustServiceUrl(String serviceUrl, LocalDateTime expirationTime) {
        try {
            URL url = new URL(serviceUrl);
            trustServiceUrl(url, expirationTime);
        } catch (MalformedURLException e) { }
    }

    public static void trustServiceUrl(URL serviceUrl, LocalDateTime expirationTime) {
        trustHostNames.putIfAbsent(serviceUrl.getHost(), expirationTime);
    }

    public static boolean isTrustedServiceUrl(String serviceUrl) {
        try {
            URL url = new URL(serviceUrl);
            return isTrustedServiceUrl(url);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static boolean isTrustedServiceUrl(URL url) {
        return !trustHostNames.getOrDefault(url.getHost(), LocalDateTime.MIN).isBefore(LocalDateTime.now().minusMinutes(5));
    }
    public static boolean isTrustedServiceUrl(HttpUrl url) {
        return !trustHostNames.getOrDefault(url.host(), LocalDateTime.MIN).isBefore(LocalDateTime.now().minusMinutes(5));
    }

    private static ConcurrentMap<String, LocalDateTime> trustHostNames = new ConcurrentHashMap<>();

    static {
        trustHostNames.putIfAbsent("state.botframework.com", LocalDateTime.MAX);
    }
}
