package com.microsoft.bot.connector.rest;

import com.microsoft.azure.AzureServiceClient;
import com.microsoft.bot.connector.BotSignIn;
import com.microsoft.bot.connector.OAuthClient;
import com.microsoft.bot.connector.UserToken;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import org.apache.commons.lang3.StringUtils;

public class RestOAuthClient extends AzureServiceClient implements OAuthClient {
    /**
     * The BotSignIns object to access its operations.
     */
    private BotSignIn botSignIn;

    /**
     * The UserTokens object to access its operations.
     */
    private UserToken userToken;

    /**
     * Initializes an instance of ConnectorClient client.
     * @param restClient The RestClient to use.
     */
    public RestOAuthClient(RestClient restClient) {
        super(restClient);
        initialize();
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public RestOAuthClient(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Gets the BotSignIns object to access its operations.
     * @return the BotSignIns object.
     */
    @Override
    public BotSignIn getBotSignIn() {
        return botSignIn;
    }


    /**
     * Gets the UserTokens object to access its operations.
     * @return the UserTokens object.
     */
    @Override
    public UserToken getUserToken() {
        return userToken;
    }

    protected void initialize() {
        botSignIn = new RestBotSignIn(restClient().retrofit(), this);
        userToken = new RestUserToken(restClient().retrofit(), this);
    }
}
