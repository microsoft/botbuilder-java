package com.microsoft.bot.connector;


import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.OAuthClient;
import com.microsoft.bot.connector.base.TestBase;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.rest.RestClient;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.commons.io.FileSystemUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;


public class OAuthTestBase extends TestBase
{
    protected final String clientId = "adcdf0c4-5ba0-4f46-9823-61ff9c40ca9e";
    protected final String clientSecret = "6}a4o5D]yhL!jmzI";
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
        super(RunCondition.MOCK_ONLY);
    }

    public OAuthTestBase(RunCondition runCondition) {
        super(runCondition);
    }


    @Override
    protected void initializeClients(RestClient restClient, String botId, String userId) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        this.connector = new ConnectorClientImpl(restClient);
        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(clientId, clientSecret);
        CompletableFuture<String> task = credentials.GetTokenAsync();
        this.token = task.get();

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
        await(doTest.apply(this.connector));
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


