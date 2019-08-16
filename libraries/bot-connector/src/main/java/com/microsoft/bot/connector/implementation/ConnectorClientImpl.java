/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 */

package com.microsoft.bot.connector.implementation;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.bot.connector.Attachments;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.retry.RetryStrategy;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Initializes a new instance of the ConnectorClientImpl class.
 */
public class ConnectorClientImpl extends AzureServiceClient implements ConnectorClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;


    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;
    private String user_agent_string;

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets Gets or sets the preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    public ConnectorClientImpl withAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /**
     * RetryStrategy as defined in Microsoft Rest Retry
     * TODO: Use this.
     */
    private RetryStrategy retryStrategy = null;
    public ConnectorClientImpl withRestRetryStrategy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
        return this;
    }
    public RetryStrategy restRetryStrategy() {
        return this.retryStrategy;
    }

    /** Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    public ConnectorClientImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
        return this;
    }

    /** When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean generateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    public ConnectorClientImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * The Attachments object to access its operations.
     */
    private Attachments attachments;

    /**
     * Gets the Attachments object to access its operations.
     * @return the Attachments object.
     */
    public Attachments attachments() {
        return this.attachments;
    }

    /**
     * The Conversations object to access its operations.
     */
    private ConversationsImpl conversations;

    /**
     * Gets the Conversations object to access its operations.
     * @return the Conversations object.
     */
    @Override
    public ConversationsImpl conversations() {
        return this.conversations;
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public ConnectorClientImpl(ServiceClientCredentials credentials) {
        this("https://api.botframework.com", credentials);
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public ConnectorClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public ConnectorClientImpl(RestClient restClient){
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.attachments = new AttachmentsImpl(restClient().retrofit(), this);
        this.conversations = new ConversationsImpl(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);


        // Format according to https://github.com/Microsoft/botbuilder-dotnet/blob/d342cd66d159a023ac435aec0fdf791f93118f5f/doc/UserAgents.md
        String build_version;
        final Properties properties = new Properties();
        try {
            InputStream propStream = ConnectorClientImpl.class.getClassLoader().getResourceAsStream("project.properties");
            properties.load(propStream);
            build_version = properties.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
            build_version = "4.0.0";
        }

        String os_version = System.getProperty("os.name");
        String java_version = System.getProperty("java.version");
        this.user_agent_string = String.format("BotBuilder/%s (JVM %s; %s)", build_version, java_version, os_version);
    }


    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */

    @Override
    public String userAgent() {
        return this.user_agent_string;
    }

    /**
     * This is a copy of what the Azure Client does to create a RestClient.  This returns
     * a RestClient.Builder so that the app can create a custom RestClient, and supply
     * it to ConnectorClient during construction.
     *
     * One use case of this is for supplying a Proxy to the RestClient.
     *
     * @param baseUrl
     * @param credentials
     * @return
     */
    public static RestClient.Builder getDefaultRestClientBuilder(String baseUrl, ServiceClientCredentials credentials){
        return new RestClient.Builder(new OkHttpClient.Builder(), new Retrofit.Builder())
                .withBaseUrl(baseUrl)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory());
    }
}
