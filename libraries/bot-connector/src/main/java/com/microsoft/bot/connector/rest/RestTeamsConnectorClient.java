/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import com.microsoft.bot.connector.UserAgent;
import com.microsoft.bot.connector.teams.TeamsConnectorClient;
import com.microsoft.bot.connector.teams.TeamsOperations;
import com.microsoft.bot.restclient.RestClient;
import com.microsoft.bot.restclient.ServiceClient;
import com.microsoft.bot.restclient.ServiceResponseBuilder;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import com.microsoft.bot.restclient.retry.RetryStrategy;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * The Bot Connector REST API allows your bot to send and receive messages to
 * channels configured in the [Bot Framework Developer
 * Portal](https://dev.botframework.com). The Connector service uses
 * industry-standard REST and JSON over HTTPS.
 *
 * Client libraries for this REST API are available. See below for a list.
 *
 * Many bots will use both the Bot Connector REST API and the associated [Bot
 * State REST API](/en-us/restapi/state). The Bot State REST API allows a bot to
 * store and retrieve state associated with Teams.
 *
 * Authentication for both the Bot Connector and Bot State REST APIs is
 * accomplished with JWT Bearer tokens, and is described in detail in the
 * [Connector Authentication](/en-us/restapi/authentication) document.
 */
public class RestTeamsConnectorClient extends ServiceClient implements TeamsConnectorClient {
    private static final int RETRY_TIMEOUT = 30;

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;
    private String userAgentString;

    private RetryStrategy retryStrategy = null;

    private TeamsOperations teamsOperations = null;

    /**
     * Initializes an instance of TeamsConnectorClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public RestTeamsConnectorClient(ServiceClientCredentials credentials) {
        this("https://api.botframework.com", credentials);
    }

    /**
     * Initializes an instance of TeamsConnectorClient client.
     *
     * @param baseUrl     the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public RestTeamsConnectorClient(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of TeamsConnectorClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    protected RestTeamsConnectorClient(RestClient restClient) {
        super(restClient);
        initialize();
    }

    /**
     * Initialize the object post-construction.
     */
    protected void initialize() {
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = RETRY_TIMEOUT;
        this.generateClientRequestId = true;
        this.teamsOperations = new RestTeamsOperations(restClient().retrofit(), this);
        this.userAgentString = UserAgent.value();

        // this.restClient().withLogLevel(LogLevel.BODY_AND_HEADERS);
    }

    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
     */
    @Override
    public RestClient getRestClient() {
        return super.restClient();
    }

    /**
     * Returns the base url for this ConnectorClient.
     *
     * @return The base url.
     */
    @Override
    public String baseUrl() {
        return getRestClient().retrofit().baseUrl().toString();
    }

    /**
     * Returns the credentials in use.
     *
     * @return The ServiceClientCredentials in use.
     */
    public ServiceClientCredentials credentials() {
        return getRestClient().credentials();
    }

    /**
     * Gets the preferred language for the response..
     *
     * @return the acceptLanguage value.
     */
    @Override
    public String getAcceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets the preferred language for the response..
     *
     * @param withAcceptLanguage the acceptLanguage value.
     */
    public void setAcceptLanguage(String withAcceptLanguage) {
        this.acceptLanguage = withAcceptLanguage;
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String getUserAgent() {
        return this.userAgentString;
    }

    /**
     * This is to override the AzureServiceClient version.
     *
     * @return The user agent. Same as {@link #getUserAgent()}
     */
    @Override
    public String userAgent() {
        return getUserAgent();
    }

    /**
     * Sets the Rest retry strategy.
     *
     * @param strategy The {@link RetryStrategy} to use.
     */
    public void setRestRetryStrategy(RetryStrategy strategy) {
        this.retryStrategy = strategy;
    }

    /**
     * Gets the Rest retry strategy.
     *
     * @return The {@link RetryStrategy} being used.
     */
    public RetryStrategy getRestRetryStrategy() {
        return this.retryStrategy;
    }

    /**
     * Gets or sets the retry timeout in seconds for Long Running Operations.
     * Default value is 30.
     */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets the retry timeout in seconds for Long Running Operations. Default value
     * is 30.
     *
     * @return the timeout value.
     */
    @Override
    public int getLongRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets the retry timeout in seconds for Long Running Operations. Default value
     * is 30.
     *
     * @param timeout the longRunningOperationRetryTimeout value.
     */
    @Override
    public void setLongRunningOperationRetryTimeout(int timeout) {
        this.longRunningOperationRetryTimeout = timeout;
    }

    /**
     * When set to true a unique x-ms-client-request-id value is generated and
     * included in each request.
     */
    private boolean generateClientRequestId;

    /**
     * When set to true a unique x-ms-client-request-id value is generated and
     * included in each request.
     *
     * @return the generateClientRequestId value.
     */
    @Override
    public boolean getGenerateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * When set to true a unique x-ms-client-request-id value is generated and
     * included in each request.
     *
     * @param requestId the generateClientRequestId value.
     */
    @Override
    public void setGenerateClientRequestId(boolean requestId) {
        this.generateClientRequestId = requestId;
    }

    /**
     * Returns an instance of TeamsOperations.
     *
     * @return A TeamsOperations instance.
     */
    @Override
    public TeamsOperations getTeams() {
        return teamsOperations;
    }

    /**
     * This is a copy of what the Azure Client does to create a RestClient. This
     * returns a RestClient.Builder so that the app can create a custom RestClient,
     * and supply it to ConnectorClient during construction.
     *
     * One use case of this is for supplying a Proxy to the RestClient. Though it is
     * recommended to set proxy information via the Java system properties.
     *
     * @param baseUrl     Service endpoint
     * @param credentials auth credentials.
     * @return A RestClient.Builder.
     */
    public static RestClient.Builder getDefaultRestClientBuilder(
        String baseUrl,
        ServiceClientCredentials credentials
    ) {
        return new RestClient.Builder(new OkHttpClient.Builder(), new Retrofit.Builder())
            .withBaseUrl(baseUrl)
            .withCredentials(credentials)
            .withSerializerAdapter(new JacksonAdapter())
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory());
    }

    /**
     * AutoDisposable close.
     */
    @Override
    public void close() throws Exception {

    }
}
