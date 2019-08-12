package com.microsoft.bot.connector;


import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.OAuthClient;
import com.microsoft.bot.connector.base.TestBase;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.rest.RestClient;
import okhttp3.Request;
import org.apache.commons.io.FileSystemUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;


public class OAuthTestBase extends TestBase
{
    protected String clientId;
    protected String clientSecret ;
    protected final String userId = "U19KH8EHJ:T03CWQ0QB";
    protected final String botId = "B21UTEF8S:T03CWQ0QB";
    protected final static String hostUri = "https://slack.botframework.com";


    private String token;
    protected ConnectorClientImpl connector;

    private ChannelAccount bot;
    public ChannelAccount getBot() {
        return this.bot;
    }

    private ChannelAccount user;
    public ChannelAccount getUser() {
        return this.user;
    }


    public OAuthTestBase()  {
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

        this.connector = new ConnectorClientImpl(restClient);
        if (this.clientId != null && this.clientSecret != null) {
            MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(this.clientId, this.clientSecret);

            this.token = credentials.getToken(new Request.Builder().build());
        }
        else {
            this.token = null;
        }

        this.bot = new ChannelAccount()
                .withId(botId);
        this.user = new ChannelAccount()
                .withId(userId);



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


    public CompletableFuture UseOAuthClientFor(Function<OAuthClient, CompletableFuture<Void>> doTest) throws MalformedURLException, URISyntaxException {
        return this.UseOAuthClientFor(doTest, null, "");
    }

    public CompletableFuture UseOAuthClientFor(Function<OAuthClient, CompletableFuture<Void>> doTest, String className) throws MalformedURLException, URISyntaxException {
        return this.UseOAuthClientFor(doTest, className, "");
    }

    public CompletableFuture<Void> UseOAuthClientFor(Function<OAuthClient, CompletableFuture<Void>> doTest, String className, String methodName) throws MalformedURLException, URISyntaxException {
        return CompletableFuture.runAsync(()->{
            OAuthClient oauthClient = null;
            try {
                oauthClient = new OAuthClient(this.connector, AuthenticationConstants.OAuthUrl);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            doTest.apply(oauthClient);
        });
    }
}


