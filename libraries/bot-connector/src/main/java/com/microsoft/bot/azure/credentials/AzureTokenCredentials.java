/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.azure.credentials;

import com.microsoft.bot.azure.AzureEnvironment;
import com.microsoft.bot.azure.AzureEnvironment.Endpoint;
import com.microsoft.bot.rest.credentials.TokenCredentials;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Proxy;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AzureTokenCredentials represents a credentials object with access to Azure
 * Resource management.
 */
public abstract class AzureTokenCredentials extends TokenCredentials {
    private final AzureEnvironment environment;
    private final String domain;
    private String defaultSubscription;

    private Proxy proxy;
    private SSLSocketFactory sslSocketFactory;

    /**
     * Initializes a new instance of the AzureTokenCredentials.
     *
     * @param environment the Azure environment to use
     * @param domain the tenant or domain the credential is authorized to
     */
    public AzureTokenCredentials(AzureEnvironment environment, String domain) {
        super("Bearer", null);
        this.environment = (environment == null) ? AzureEnvironment.AZURE : environment;
        this.domain = domain;
    }

    @Override
    protected final String getToken(Request request) throws IOException {
        String host = request.url().toString().toLowerCase();
        String resource = environment().managementEndpoint();
        for (Map.Entry<String, String> endpoint : environment().endpoints().entrySet()) {
            if (host.contains(endpoint.getValue())) {
                if (endpoint.getKey().equals(Endpoint.KEYVAULT.identifier())) {
                    resource = String.format("https://%s/", endpoint.getValue().replaceAll("^\\.*", ""));
                    break;
                } else if (endpoint.getKey().equals(Endpoint.GRAPH.identifier())) {
                    resource = environment().graphEndpoint();
                    break;
                } else if (endpoint.getKey().equals(Endpoint.LOG_ANALYTICS.identifier())) {
                    resource = environment().logAnalyticsEndpoint();
                    break;
                } else if (endpoint.getKey().equals(Endpoint.APPLICATION_INSIGHTS.identifier())) {
                    resource = environment().applicationInsightsEndpoint();
                    break;
                } else if (endpoint.getKey().equals(Endpoint.DATA_LAKE_STORE.identifier())
                               || endpoint.getKey().equals(Endpoint.DATA_LAKE_ANALYTICS.identifier())) {
                    resource = environment().dataLakeEndpointResourceId();
                    break;
                }
            }
        }
        return getToken(resource);
    }

    /**
     * Override this method to provide the mechanism to get a token.
     *
     * @param resource the resource the access token is for
     * @return the token to access the resource
     * @throws IOException exceptions from IO
     */
    public abstract String getToken(String resource) throws IOException;

    /**
     * Override this method to provide the domain or tenant ID the token is valid in.
     *
     * @return the domain or tenant ID string
     */
    public String domain() {
        return domain;
    }

    /**
     * @return the environment details the credential has access to.
     */
    public AzureEnvironment environment() {
        return environment;
    }

    /**
     * @return The default subscription ID, if any
     */
    public String defaultSubscriptionId() {
        return defaultSubscription;
    }

    /**
     * Set default subscription ID.
     *
     * @param subscriptionId the default subscription ID.
     * @return the credentials object itself.
     */
    public AzureTokenCredentials withDefaultSubscriptionId(String subscriptionId) {
        this.defaultSubscription = subscriptionId;
        return this;
    }

    /**
     * @return the proxy being used for accessing Active Directory.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * @return the ssl socket factory.
     */
    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * @param proxy the proxy being used for accessing Active Directory
     * @return the credential itself
     */
    public AzureTokenCredentials withProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * @param sslSocketFactory the ssl socket factory
     * @return the credential itself
     */
    public AzureTokenCredentials withSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new AzureTokenCredentialsInterceptor(this));
        clientBuilder.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                String authenticateHeader = response.header("WWW-Authenticate");
                if (authenticateHeader != null && !authenticateHeader.isEmpty()) {
                    Pattern pattern = Pattern.compile("resource=\"([a-zA-Z0-9.:/-_]+)\"");
                    Matcher matcher = pattern.matcher(authenticateHeader);
                    if (matcher.find()) {
                        String resource = matcher.group(1);
                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + getToken(resource))
                                .build();
                    }
                }
                // Otherwise cannot satisfy the challenge
                return null;
            }
        });
    }
}
