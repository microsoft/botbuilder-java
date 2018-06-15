package com.microsoft.bot.connector;

import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ConnectorClientFuture extends ConnectorClientImpl {

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public ConnectorClientFuture(ServiceClientCredentials credentials) {
        super(credentials);
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param baseUrl     the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public ConnectorClientFuture(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public ConnectorClientFuture(RestClient restClient) {
        super(restClient);
    }

    @Override
    public String userAgent() {
        return "Microsoft-BotFramework/4.0";
    }
}
