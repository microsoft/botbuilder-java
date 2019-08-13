// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.microsoft.bot.connector.authentication.AuthenticationConstants.ToChannelFromBotLoginUrl;
import static com.microsoft.bot.connector.authentication.AuthenticationConstants.ToChannelFromBotOAuthScope;

public class MicrosoftAppCredentials implements ServiceClientCredentials {
    private String appId;
    private String appPassword;

    private OkHttpClient client;
    private ObjectMapper mapper;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType FORM_ENCODE = MediaType.parse("application/x-www-form-urlencoded");

    private String currentToken = null;
    private long expiredTime = 0;
    //private static final Object cacheSync = new Object();
    protected static final HashMap<String, OAuthResponse> cache = new HashMap<String, OAuthResponse>();

    public final String OAuthEndpoint = ToChannelFromBotLoginUrl;
    public final String OAuthScope = ToChannelFromBotOAuthScope;


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


    protected boolean ShouldSetToken(String url) {
        if (isTrustedServiceUrl(url)) {
            return true;
        }
        return false;
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
        } catch (MalformedURLException e) {
            //TODO: What's missing here?
            e.printStackTrace();
        }
    }

    public static void trustServiceUrl(URL serviceUrl, LocalDateTime expirationTime) {
        trustHostNames.put(serviceUrl.getHost(), expirationTime);
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
        trustHostNames.put("state.botframework.com", LocalDateTime.MAX);
    }
}
