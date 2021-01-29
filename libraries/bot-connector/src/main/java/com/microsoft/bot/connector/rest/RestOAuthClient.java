// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.rest;

import com.microsoft.bot.connector.BotSignIn;
import com.microsoft.bot.connector.OAuthClient;
import com.microsoft.bot.connector.UserToken;
import com.microsoft.bot.restclient.RestClient;
import com.microsoft.bot.restclient.ServiceClient;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;

/**
 * Rest OAuth client.
 */
public class RestOAuthClient extends ServiceClient implements OAuthClient {
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
     * 
     * @param restClient The RestClient to use.
     */
    public RestOAuthClient(RestClient restClient) {
        super(restClient);
        initialize();
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param baseUrl     the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public RestOAuthClient(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Gets the BotSignIns object to access its operations.
     * 
     * @return the BotSignIns object.
     */
    @Override
    public BotSignIn getBotSignIn() {
        return botSignIn;
    }

    /**
     * Gets the UserTokens object to access its operations.
     * 
     * @return the UserTokens object.
     */
    @Override
    public UserToken getUserToken() {
        return userToken;
    }

    /**
     * Post construction initialization.
     */
    protected void initialize() {
        botSignIn = new RestBotSignIn(restClient().retrofit(), this);
        userToken = new RestUserToken(restClient().retrofit(), this);
    }
}
