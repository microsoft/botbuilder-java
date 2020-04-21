// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.bot.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base abstraction for AAD credentials for auth and caching.
 *
 * <p>
 * Subclasses must provide the impl for {@link #buildAuthenticator}
 * </p>
 */
public abstract class AppCredentials implements ServiceClientCredentials {
    private static final int EXPIRATION_SLACK = 5;
    private static final int EXPIRATION_DAYS = 1;
    private static ConcurrentMap<String, LocalDateTime> trustHostNames = new ConcurrentHashMap<>();

    static {
        trustHostNames.put("api.botframework.com", LocalDateTime.MAX);
        trustHostNames.put("token.botframework.com", LocalDateTime.MAX);
        trustHostNames.put("api.botframework.azure.us", LocalDateTime.MAX);
        trustHostNames.put("token.botframework.azure.us", LocalDateTime.MAX);
    }

    private String appId;
    private String authTenant;
    private Authenticator authenticator;

    /**
     * Initializes a new instance of the AppCredentials class.
     *
     * @param withChannelAuthTenant Optional. The oauth token tenant.
     */
    public AppCredentials(String withChannelAuthTenant) {
        setChannelAuthTenant(withChannelAuthTenant);
    }

    /**
     * Adds the host of service url to trusted hosts.
     *
     * @param serviceUrl The service URI.
     */
    public static void trustServiceUrl(String serviceUrl) {
        trustServiceUrl(serviceUrl, LocalDateTime.now().plusDays(EXPIRATION_DAYS));
    }

    /**
     * Adds the host of service url to trusted hosts with the specified expiration.
     *
     * <p>
     * Note: The will fail to add if the url is not valid.
     * </p>
     *
     * @param serviceUrl     The service URI.
     * @param expirationTime The expiration time after which this service url is not
     *                       trusted anymore.
     */
    public static void trustServiceUrl(String serviceUrl, LocalDateTime expirationTime) {
        try {
            URL url = new URL(serviceUrl);
            trustServiceUrl(url, expirationTime);
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(MicrosoftAppCredentials.class).error("trustServiceUrl", e);
        }
    }

    /**
     * Adds the host of service url to trusted hosts with the specified expiration.
     *
     * @param serviceUrl     The service URI.
     * @param expirationTime The expiration time after which this service url is not
     *                       trusted anymore.
     */
    public static void trustServiceUrl(URL serviceUrl, LocalDateTime expirationTime) {
        trustHostNames.put(serviceUrl.getHost(), expirationTime);
    }

    /**
     * Checks if the service url is for a trusted host or not.
     *
     * @param serviceUrl The service URI.
     * @return true if the service is trusted.
     */
    public static boolean isTrustedServiceUrl(String serviceUrl) {
        try {
            URL url = new URL(serviceUrl);
            return isTrustedServiceUrl(url);
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(AppCredentials.class).error("trustServiceUrl", e);
            return false;
        }
    }

    /**
     * Checks if the service url is for a trusted host or not.
     *
     * @param serviceUrl The service URI.
     * @return true if the service is trusted.
     */
    public static boolean isTrustedServiceUrl(URL serviceUrl) {
        return !trustHostNames.getOrDefault(serviceUrl.getHost(), LocalDateTime.MIN)
            .isBefore(LocalDateTime.now().minusMinutes(EXPIRATION_SLACK));
    }

    /**
     * Gets the App ID for this credential.
     *
     * @return The app id.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the Microsoft app ID for this credential.
     *
     * @param withAppId The app id.
     */
    public void setAppId(String withAppId) {
        appId = withAppId;
    }

    /**
     * Gets tenant to be used for channel authentication.
     *
     * @return Tenant to be used for channel authentication.
     */
    public String getChannelAuthTenant() {
        return StringUtils.isEmpty(authTenant)
            ? AuthenticationConstants.DEFAULT_CHANNEL_AUTH_TENANT
            : getAuthTenant();
    }

    /**
     * Sets tenant to be used for channel authentication.
     *
     * @param withAuthTenant Tenant to be used for channel authentication.
     */
    public void setChannelAuthTenant(String withAuthTenant) {
        try {
            // Advanced user only, see https://aka.ms/bots/tenant-restriction
            String endPointUrl = String.format(
                AuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL_TEMPLATE, withAuthTenant
            );
            new URL(endPointUrl).toString();
            setAuthTenant(withAuthTenant);
        } catch (MalformedURLException e) {
            throw new AuthenticationException("Invalid channel auth tenant: " + withAuthTenant);
        }
    }

    /**
     * OAuth endpoint to use.
     *
     * @return The OAuth endpoint.
     */
    public String oAuthEndpoint() {
        return String.format(
            AuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL_TEMPLATE, getChannelAuthTenant()
        );
    }

    /**
     * OAuth scope to use.
     *
     * @return OAuth scope.
     */
    public String oAuthScope() {
        return AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE;
    }

    /**
     * Gets the channel auth token tenant for this credential.
     *
     * @return The channel auth token tenant.
     */
    protected String getAuthTenant() {
        return authTenant;
    }

    /**
     * Sets the channel auth token tenant for this credential.
     *
     * @param withAuthTenant The auth token tenant.
     */
    protected void setAuthTenant(String withAuthTenant) {
        authTenant = withAuthTenant;
    }

    /**
     * Gets an OAuth access token.
     *
     * @return If the task is successful, the result contains the access token
     *         string.
     */
    public CompletableFuture<String> getToken() {
        CompletableFuture<String> result;

        try {
            result = getAuthenticator().acquireToken()
                .thenApply(IAuthenticationResult::accessToken);
        } catch (MalformedURLException e) {
            result = new CompletableFuture<>();
            result.completeExceptionally(new AuthenticationException(e));
        }

        return result;
    }

    /**
     * Called by the {@link AppCredentialsInterceptor} to determine if the HTTP
     * request should be modified to contain the token.
     *
     * @param url The HTTP request URL.
     * @return true if the auth token should be added to the request.
     */
    boolean shouldSetToken(String url) {
        return isTrustedServiceUrl(url);
    }

    // lazy Authenticator create.
    private Authenticator getAuthenticator() throws MalformedURLException {
        if (authenticator == null) {
            authenticator = buildAuthenticator();
        }
        return authenticator;
    }

    /**
     * Returns an appropriate Authenticator that is provided by a subclass.
     *
     * @return An Authenticator object.
     * @throws MalformedURLException If the endpoint isn't valid.
     */
    protected abstract Authenticator buildAuthenticator() throws MalformedURLException;

    /**
     * Apply the credentials to the HTTP request.
     *
     * <p>
     * Note: Provides the same functionality as dotnet ProcessHttpRequestAsync
     * </p>
     *
     * @param clientBuilder the builder for building up an {@link OkHttpClient}
     */
    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new AppCredentialsInterceptor(this));
    }
}
