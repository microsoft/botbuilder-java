// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;


import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.base.TestBase;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.connector.rest.RestOAuthClient;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.restclient.RestClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;


public class OAuthTestBase extends TestBase {
    protected String clientId;
    protected String clientSecret;
    protected final String userId = "U19KH8EHJ:T03CWQ0QB";
    protected final String botId = "B21UTEF8S:T03CWQ0QB";
    protected final static String hostUri = "https://slack.botframework.com";


    private String token;
    protected RestConnectorClient connector;
    protected RestOAuthClient oAuthClient;

    private ChannelAccount bot;

    public ChannelAccount getBot() {
        return this.bot;
    }

    private ChannelAccount user;

    public ChannelAccount getUser() {
        return this.user;
    }


    public OAuthTestBase() {
        super(RunCondition.BOTH);
    }

    public OAuthTestBase(RunCondition runCondition) {
        super(runCondition);
    }


    @Override
    protected void initializeClients(RestClient restClient, String botId, String userId) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        this.clientId = System.getenv("AppID");
        this.clientSecret = System.getenv("AppSecret");

        if (this.getRunCondition() == RunCondition.LIVE_ONLY) {
            if (this.clientId == null) {
                System.out.print("CANNOT RUN - NO AppID set in environment!!");
                throw new IOException("Set AppID in environment to proper clientid");
            }
            if (this.clientSecret == null) {
                System.out.print("CANNOT RUN - NO AppSecret set in environment!!");
                throw new IOException("Set AppSecret in environment to proper clientid");
            }
        }

        this.connector = new RestConnectorClient(restClient);
        this.oAuthClient = new RestOAuthClient(restClient);

        if (this.clientId != null && this.clientSecret != null) {
            MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(this.clientId, this.clientSecret);
            this.token = credentials.getToken().join();
        } else {
            this.token = null;
        }

        this.bot = new ChannelAccount(botId);
        this.user = new ChannelAccount(userId);
    }

    @Override
    protected void cleanUpResources() {

    }

    public void UseClientFor(Function<ConnectorClient, CompletableFuture<Void>> doTest) {
        this.UseClientFor(doTest, null, "");
    }

    public void UseClientFor(Function<ConnectorClient, CompletableFuture<Void>> doTest, String className) {
        this.UseClientFor(doTest, className, "");
    }

    public void UseClientFor(Function<ConnectorClient, CompletableFuture<Void>> doTest, String className, String methodName) {
        doTest.apply(this.connector).join();
    }


    public CompletableFuture UseOAuthClientFor(Function<OAuthClient, CompletableFuture<Void>> doTest) {
        return this.UseOAuthClientFor(doTest, null, "");
    }

    public CompletableFuture UseOAuthClientFor(Function<OAuthClient, CompletableFuture<Void>> doTest, String className) {
        return this.UseOAuthClientFor(doTest, className, "");
    }

    public CompletableFuture<Void> UseOAuthClientFor(Function<OAuthClient, CompletableFuture<Void>> doTest, String className, String methodName) {
        return doTest.apply(oAuthClient);
    }
}


