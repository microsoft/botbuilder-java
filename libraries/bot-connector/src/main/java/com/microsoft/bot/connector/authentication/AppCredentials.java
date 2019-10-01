// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base abstraction for AAD credentials for auth and caching.
 */
public abstract class AppCredentials implements ServiceClientCredentials {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static ConcurrentMap<String, LocalDateTime> trustHostNames = new ConcurrentHashMap<>();

    static {
        trustHostNames.put("api.botframework.com", LocalDateTime.MAX);
        trustHostNames.put("token.botframework.com", LocalDateTime.MAX);
        trustHostNames.put("api.botframework.azure.us", LocalDateTime.MAX);
        trustHostNames.put("token.botframework.azure.us", LocalDateTime.MAX);
    }

    private String appId;
    private String channelAuthTenant;
    private AdalAuthenticator authenticator;

    public AppCredentials(String withChannelAuthTenant) {
        setChannelAuthTenant(withChannelAuthTenant);
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
            LoggerFactory.getLogger(MicrosoftAppCredentials.class).error("trustServiceUrl", e);
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
            LoggerFactory.getLogger(MicrosoftAppCredentials.class).error("trustServiceUrl", e);
            return false;
        }
    }

    public static boolean isTrustedServiceUrl(URL url) {
        return !trustHostNames.getOrDefault(
            url.getHost(), LocalDateTime.MIN).isBefore(LocalDateTime.now().minusMinutes(5));
    }

    public static boolean isTrustedServiceUrl(HttpUrl url) {
        return !trustHostNames.getOrDefault(
            url.host(), LocalDateTime.MIN).isBefore(LocalDateTime.now().minusMinutes(5));
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getChannelAuthTenant() {
        return channelAuthTenant == null ? AuthenticationConstants.DEFAULT_CHANNEL_AUTH_TENANT : channelAuthTenant;
    }

    public void setChannelAuthTenant(String withAuthTenant) {
        try {
            String endPointUrl = String.format(
                AuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL_TEMPLATE, withAuthTenant);
            new URL(endPointUrl).toString();
            channelAuthTenant = withAuthTenant;
        } catch(MalformedURLException e) {
            throw new RuntimeException("Invalid channel auth tenant: " + withAuthTenant);
        }
    }

    public String oAuthEndpoint() {
        return String.format(AuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL_TEMPLATE, getChannelAuthTenant());
    }

    public String oAuthScope() {
        return AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE;
    }

    public CompletableFuture<AuthenticationResult> getToken() {
        return getAuthenticator().acquireToken();
    }

    protected boolean shouldSetToken(String url) {
        return isTrustedServiceUrl(url);
    }

    private AdalAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =  buildAuthenticator();
        }
        return authenticator;
    }

    protected abstract AdalAuthenticator buildAuthenticator();

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new AppCredentialsInterceptor(this));
    }
}
