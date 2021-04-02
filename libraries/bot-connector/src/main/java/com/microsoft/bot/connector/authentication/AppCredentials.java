// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * Base abstraction for AAD credentials for auth and caching.
 *
 * <p>
 * Subclasses must provide the impl for {@link #buildAuthenticator}
 * </p>
 */
public abstract class AppCredentials implements ServiceClientCredentials {

    private String appId;
    private String authTenant;
    private String authScope;
    private Authenticator authenticator;

    /**
     * Initializes a new instance of the AppCredentials class.
     *
     * @param withChannelAuthTenant Optional. The oauth token tenant.
     */
    public AppCredentials(String withChannelAuthTenant) {
        this(withChannelAuthTenant, AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);
    }

    /**
     * Initializes a new instance of the AppCredentials class.
     *
     * @param withChannelAuthTenant Optional. The oauth token tenant.
     * @param withOAuthScope The scope for the token.
     */
    public AppCredentials(String withChannelAuthTenant, String withOAuthScope) {
        setChannelAuthTenant(withChannelAuthTenant);
        authScope = StringUtils.isEmpty(withOAuthScope)
            ? AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE
            : withOAuthScope;
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
        return authScope;
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
        if (StringUtils.isBlank(getAppId()) || getAppId().equals(AuthenticationConstants.ANONYMOUS_SKILL_APPID)) {
            return false;
        }
        return true;
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
