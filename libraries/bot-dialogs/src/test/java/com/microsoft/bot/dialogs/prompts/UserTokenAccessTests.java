// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextStateCollection;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ConnectorFactory;
import com.microsoft.bot.connector.authentication.UserTokenClient;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.TokenResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserTokenAccessTests {

    @Test
    public void getUserToken_ShouldThrowException() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(null);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);
        when(turnContext.getAdapter()).thenReturn(null);

        Assert.assertThrows(UnsupportedOperationException.class, () ->  UserTokenAccess.getUserToken(turnContext, new OAuthPromptSettings(), ""));
    }

    @Test
    public void getUserToken_ShouldReturnTokenResponse_WithUserTokenClientNotNull() {
        TurnContext turnContext = mock(TurnContext.class);

        UserTokenClient userTokenClient = mock(UserTokenClient.class);
        when(userTokenClient.getUserToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(new TokenResponse()));

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(userTokenClient);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);

        OAuthPromptSettings oAuthPromptSettings = mock(OAuthPromptSettings.class);
        when(oAuthPromptSettings.getConnectionName()).thenReturn("");

        Activity activity = mock(Activity.class);
        when(activity.getChannelId()).thenReturn("");

        ChannelAccount channelAccount = mock(ChannelAccount.class);
        when(channelAccount.getId()).thenReturn("");

        when(activity.getFrom()).thenReturn(channelAccount);

        when(turnContext.getActivity()).thenReturn(activity);

        Assert.assertNotNull(UserTokenAccess.getUserToken(turnContext, oAuthPromptSettings, "").join());
    }

    @Test
    public void getUserToken_ShouldReturnTokenResponse_WithInstanceOfUserTokenProvider() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(null);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);

        BotFrameworkAdapter adapter = mock(BotFrameworkAdapter.class);
        when(adapter.getUserToken(Mockito.any(TurnContext.class), Mockito.any(AppCredentials.class), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(new TokenResponse()));

        when(turnContext.getAdapter()).thenReturn(adapter);

        OAuthPromptSettings oAuthPromptSettings = mock(OAuthPromptSettings.class);
        when(oAuthPromptSettings.getOAuthAppCredentials()).thenReturn(mock(AppCredentials.class));
        when(oAuthPromptSettings.getConnectionName()).thenReturn("");

        Assert.assertNotNull(UserTokenAccess.getUserToken(turnContext, oAuthPromptSettings, "").join());
    }

    @Test
    public void getSignInResource_ShouldThrowException() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(null);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);
        when(turnContext.getAdapter()).thenReturn(null);

        Assert.assertThrows(UnsupportedOperationException.class, () -> UserTokenAccess.getSignInResource(turnContext, new OAuthPromptSettings()));
    }

    @Test
    public void signOutUser_ShouldThrowException() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(null);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);
        when(turnContext.getAdapter()).thenReturn(null);

        Assert.assertThrows(UnsupportedOperationException.class, () -> UserTokenAccess.signOutUser(turnContext, new OAuthPromptSettings()));
    }

    @Test
    public void signOutUser_ShouldSucceed_WithUserTokenClientNotNull() {
        TurnContext turnContext = mock(TurnContext.class);

        UserTokenClient userTokenClient = mock(UserTokenClient.class);
        when(userTokenClient.signOutUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(userTokenClient);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);

        ChannelAccount channelAccount = mock(ChannelAccount.class);
        when(channelAccount.getId()).thenReturn("");

        Activity activity = mock(Activity.class);
        when(activity.getFrom()).thenReturn(channelAccount);
        when(activity.getChannelId()).thenReturn("");

        when(turnContext.getActivity()).thenReturn(activity);

        OAuthPromptSettings oAuthPromptSettings = mock(OAuthPromptSettings.class);
        when(oAuthPromptSettings.getConnectionName()).thenReturn("");

        Assert.assertNull(UserTokenAccess.signOutUser(turnContext, oAuthPromptSettings).join());
    }

    @Test
    public void signOutUser_ShouldSucceed_WithInstanceOfUserTokenProvider() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(null);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);

        BotFrameworkAdapter adapter = mock(BotFrameworkAdapter.class);
        when(adapter.signOutUser(Mockito.any(TurnContext.class), Mockito.any(AppCredentials.class), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));

        when(turnContext.getAdapter()).thenReturn(adapter);

        OAuthPromptSettings oAuthPromptSettings = mock(OAuthPromptSettings.class);
        when(oAuthPromptSettings.getOAuthAppCredentials()).thenReturn(mock(AppCredentials.class));
        when(oAuthPromptSettings.getConnectionName()).thenReturn("");

        UserTokenAccess.signOutUser(turnContext, oAuthPromptSettings).join();

        Assert.assertNull(UserTokenAccess.signOutUser(turnContext, oAuthPromptSettings).join());
    }

    @Test
    public void createConnectorClient_ShouldThrowException() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(null);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);
        when(turnContext.getAdapter()).thenReturn(null);

        Assert.assertThrows(UnsupportedOperationException.class, () ->
            UserTokenAccess.createConnectorClient(turnContext, "", new ClaimsIdentity("anonymous"), ""));
    }

    @Test
    public void createConnectorClient_ShouldReturnConnectorClient_WhenConnectorFactoryNotNull() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(mock(ConnectorFactory.class));

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);

        Assert.assertNull(UserTokenAccess.createConnectorClient(turnContext, "", new ClaimsIdentity("anonymous"), ""));
    }

    @Test
    public void createConnectorClient_ShouldReturnConnectorClient_WhenInstanceOfConnectorClientBuilder() {
        TurnContext turnContext = mock(TurnContext.class);

        TurnContextStateCollection turnContextStateCollection = mock(TurnContextStateCollection.class);
        when(turnContextStateCollection.get(Mockito.any(Class.class))).thenReturn(null);

        when(turnContext.getTurnState()).thenReturn(turnContextStateCollection);

        BotFrameworkAdapter adapter = mock(BotFrameworkAdapter.class);
        when(adapter.createConnectorClient(Mockito.anyString(), Mockito.any(ClaimsIdentity.class), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(mock(ConnectorClient.class)));

        when(turnContext.getAdapter()).thenReturn(adapter);

        Assert.assertNotNull(UserTokenAccess.createConnectorClient(turnContext, "", new ClaimsIdentity("anonymous"), ""));
    }
}
