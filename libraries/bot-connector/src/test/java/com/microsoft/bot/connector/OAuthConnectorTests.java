// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.connector.rest.RestOAuthClient;
import com.microsoft.bot.schema.AadResourceUrls;
import java.util.concurrent.CompletionException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


public class OAuthConnectorTests extends OAuthTestBase  {


    private RestConnectorClient mockConnectorClient;
    private MicrosoftAppCredentials credentials;

    public OAuthConnectorTests() throws IOException, ExecutionException, InterruptedException, URISyntaxException {
        super(RunCondition.BOTH);

        this.credentials = new MicrosoftAppCredentials(clientId, clientSecret);
    }

    @Test
    public void OAuthClient_ShouldNotThrowOnHttpUrl() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
    }

    @Test(expected = NullPointerException.class)
    public void OAuthClient_ShouldThrowOnNullCredentials() {
        OAuthClient client = new RestOAuthClient("http://localhost", null);
    }

    @Test(expected = CompletionException.class)
    public void GetUserToken_ShouldThrowOnEmptyConnectionName() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
        client.getUserToken().getToken("userid", null).join();
    }

    @Test
    public void GetUserToken_ShouldReturnNullOnInvalidConnectionstring() {
        UseOAuthClientFor(client -> {
            return client.getUserToken().getToken("default-user", "mygithubconnection1", null, null)
                .thenApply(tokenResponse -> {
                    Assert.assertNull(tokenResponse);
                    return null;
                });
        }).join();
    }

    @Test(expected = CompletionException.class)
    public void SignOutUser_ShouldThrowOnEmptyUserId() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
        client.getUserToken().signOut(null).join();
    }

    @Test(expected = CompletionException.class)
    public void GetSigninLink_ShouldThrowOnNullState() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
        client.getBotSignIn().getSignInUrl(null).join();
    }

    @Test(expected = CompletionException.class)
    public void GetTokenStatus_ShouldThrowOnNullUserId() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
        client.getUserToken().getTokenStatus(null).join();
    }

    @Test(expected = CompletionException.class)
    public void GetAadTokensAsync_ShouldThrowOnNullUserId() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
        client.getUserToken().getAadTokens(null, "connection", new AadResourceUrls()).join();
    }

    @Test(expected = CompletionException.class)
    public void GetAadTokensAsync_ShouldThrowOnNullConncetionName() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
        client.getUserToken().getAadTokens("user", null, new AadResourceUrls()).join();
    }

    @Test(expected = CompletionException.class)
    public void GetAadTokensAsync_ShouldThrowOnNullResourceUrls() {
        OAuthClient client = new RestOAuthClient("http://localhost", new BotAccessTokenStub("token"));
        client.getUserToken().getAadTokens("user", "connection", null).join();
    }
}

