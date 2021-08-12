// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.dialogs.prompts;

import com.microsoft.bot.builder.ConnectorClientBuilder;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserTokenProvider;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ConnectorFactory;
import com.microsoft.bot.connector.authentication.UserTokenClient;
import com.microsoft.bot.schema.SignInResource;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenResponse;

import java.util.concurrent.CompletableFuture;

public final class UserTokenAccess {

    private UserTokenAccess() {
    }

    public static CompletableFuture<TokenResponse> getUserToken(
        TurnContext turnContext,
        OAuthPromptSettings settings,
        String magicCode) {

        UserTokenClient userTokenClient = turnContext.getTurnState().get(UserTokenClient.class);

        if (userTokenClient != null) {
            return userTokenClient.getUserToken(
                turnContext.getActivity().getFrom().getId(),
                settings.getConnectionName(),
                turnContext.getActivity().getChannelId(),
                magicCode);
        } else if (turnContext.getAdapter() instanceof UserTokenProvider) {
            return ((UserTokenProvider) turnContext.getAdapter()).getUserToken(
                turnContext,
                settings.getOAuthAppCredentials(),
                settings.getConnectionName(),
                magicCode);
        } else {
            throw new UnsupportedOperationException("OAuth prompt is not supported by the current adapter");
        }
    }

    public static CompletableFuture<SignInResource> getSignInResource(
        TurnContext turnContext,
        OAuthPromptSettings settings) {

        UserTokenClient userTokenClient = turnContext.getTurnState().get(UserTokenClient.class);

        if (userTokenClient != null) {
            return userTokenClient.getSignInResource(
                settings.getConnectionName(),
                turnContext.getActivity(),
                null);
        } else if (turnContext.getAdapter() instanceof UserTokenProvider) {
            return ((UserTokenProvider) turnContext.getAdapter()).getSignInResource(
                turnContext,
                settings.getOAuthAppCredentials(),
                settings.getConnectionName(),
                turnContext.getActivity().getFrom().getId(),
                null);
        } else {
            throw new UnsupportedOperationException("OAuth prompt is not supported by the current adapter");
        }
    }

    public static CompletableFuture<Void> signOutUser(
        TurnContext turnContext,
        OAuthPromptSettings settings) {

        UserTokenClient userTokenClient = turnContext.getTurnState().get(UserTokenClient.class);

        if (userTokenClient != null) {
            return userTokenClient.signOutUser(
                turnContext.getActivity().getFrom().getId(),
                settings.getConnectionName(),
                turnContext.getActivity().getChannelId());
        } else if (turnContext.getAdapter() instanceof UserTokenProvider) {
            String id = "";
            if (turnContext.getActivity() != null
                && turnContext.getActivity() != null
                && turnContext.getActivity().getFrom() != null) {
                id = turnContext.getActivity().getFrom().getId();
            }

            return ((UserTokenProvider) turnContext.getAdapter()).signOutUser(
                turnContext,
                settings.getOAuthAppCredentials(),
                settings.getConnectionName(),
                id);
        } else {
            throw new UnsupportedOperationException("OAuth prompt is not supported by the current adapter");
        }
    }

    public static CompletableFuture<TokenResponse> exchangeToken(
        TurnContext turnContext,
        OAuthPromptSettings settings,
        TokenExchangeRequest tokenExchangeRequest) {

        UserTokenClient userTokenClient = turnContext.getTurnState().get(UserTokenClient.class);

        if (userTokenClient != null) {
            String userId = turnContext.getActivity().getFrom().getId();
            String channelId = turnContext.getActivity().getChannelId();
            return userTokenClient.exchangeToken(userId, settings.getConnectionName(), channelId, tokenExchangeRequest);
        } else if (turnContext.getAdapter() instanceof UserTokenProvider) {
            return ((UserTokenProvider) turnContext.getAdapter()).exchangeToken(
                turnContext,
                settings.getConnectionName(),
                turnContext.getActivity().getFrom().getId(),
                tokenExchangeRequest);
        } else {
            throw new UnsupportedOperationException("OAuth prompt is not supported by the current adapter");
        }
    }

    public static CompletableFuture<ConnectorClient> createConnectorClient(
        TurnContext turnContext,
        String serviceUrl,
        ClaimsIdentity claimsIdentity,
        String audience) {

        ConnectorFactory connectorFactory = turnContext.getTurnState().get(ConnectorFactory.class);

        if (connectorFactory != null) {
            return connectorFactory.create(serviceUrl, audience);
        } else if (turnContext.getAdapter() instanceof ConnectorClientBuilder) {
            ConnectorClientBuilder connectorClientProvider = (ConnectorClientBuilder) turnContext.getAdapter();
            return connectorClientProvider.createConnectorClient(serviceUrl, claimsIdentity, audience);
        } else {
            throw new UnsupportedOperationException("OAuth prompt is not supported by the current adapter");
        }
    }
}
