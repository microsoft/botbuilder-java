// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.OAuthClient;
import com.microsoft.bot.connector.rest.RestOAuthClient;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import com.microsoft.bot.schema.AadResourceUrls;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.SignInResource;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTokenClientImpl extends UserTokenClient {

    private final String appId;
    private final OAuthClient client;
    /**
     * The... ummm... logger.
     */
    private final Logger logger = LoggerFactory.getLogger(UserTokenClientImpl.class);

    public UserTokenClientImpl(String withAppId, ServiceClientCredentials credentials, String oauthEndpoint) {
        super();
        this.appId = withAppId;
        this.client = new RestOAuthClient(oauthEndpoint, credentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<TokenResponse> getUserToken(String userId, String connectionName, String channelId,
            String magicCode) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (connectionName == null) {
            throw new IllegalArgumentException("connectionName cannot be null");
        }

        logger.info(String.format("getToken ConnectionName: %s", connectionName));
        return client.getUserToken().getToken(userId, connectionName, channelId, magicCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<SignInResource> getSignInResource(String connectionName, Activity activity,
            String finalRedirect) {
        if (connectionName == null) {
            throw new IllegalArgumentException("connectionName cannot be null");
        }
        if (activity == null) {
            throw new IllegalArgumentException("activity cannot be null");
        }

        logger.info(String.format("getSignInResource ConnectionName: %s", connectionName));
        String state = createTokenExchangeState(appId, connectionName, activity);
        return client.getBotSignIn().getSignInResource(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> signOutUser(String userId, String connectionName, String channelId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (connectionName == null) {
            throw new IllegalArgumentException("connectionName cannot be null");
        }

        logger.info(String.format("signOutUser ConnectionName: %s", connectionName));
        return client.getUserToken().signOut(userId, connectionName, channelId).thenApply(signOutResult -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<TokenStatus>> getTokenStatus(String userId, String channelId, String includeFilter) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (channelId == null) {
            throw new IllegalArgumentException("channelId cannot be null");
        }

        logger.info("getTokenStatus");
        return client.getUserToken().getTokenStatus(userId, channelId, includeFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(String userId, String connectionName,
            List<String> resourceUrls, String channelId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (connectionName == null) {
            throw new IllegalArgumentException("connectionName cannot be null");
        }

        logger.info(String.format("getAadTokens ConnectionName: %s", connectionName));
        return client.getUserToken().getAadTokens(userId, connectionName, new AadResourceUrls(resourceUrls), channelId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<TokenResponse> exchangeToken(String userId, String connectionName, String channelId,
            TokenExchangeRequest exchangeRequest) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (connectionName == null) {
            throw new IllegalArgumentException("connectionName cannot be null");
        }

        logger.info(String.format("exchangeToken ConnectionName: %s", connectionName));
        return client.getUserToken().exchangeToken(userId, connectionName, channelId, exchangeRequest);
    }
}
