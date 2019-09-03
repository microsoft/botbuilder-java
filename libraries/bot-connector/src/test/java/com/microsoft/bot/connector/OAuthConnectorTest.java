package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.OAuthClient;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


public class OAuthConnectorTest extends OAuthTestBase  {


    private RestConnectorClient mockConnectorClient;
    private MicrosoftAppCredentials credentials;

    public OAuthConnectorTest() throws IOException, ExecutionException, InterruptedException, URISyntaxException {
        super(RunCondition.BOTH);

        this.credentials = new MicrosoftAppCredentials(clientId, clientSecret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void OAuthClient_ShouldThrowOnInvalidUrl() throws MalformedURLException, URISyntaxException {

        OAuthClient test = new OAuthClient(this.connector, "http://localhost");
        Assert.assertTrue( "Exception not thrown", false);
    }


    @Test(expected = IllegalArgumentException.class)
    public void GetUserToken_ShouldThrowOnEmptyUserId() throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        OAuthClient client = new OAuthClient(this.connector, "https://localhost");
        client.GetUserTokenAsync("", "mockConnection", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void GetUserToken_ShouldThrowOnEmptyConnectionName() throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        OAuthClient client = new OAuthClient(this.connector, "https://localhost");
        client.GetUserTokenAsync("userid", "", "");
    }
/*
   TODO: Need to set up a bot and login with AADv2 to perform new recording (or convert the C# recordings)
    @Test
    public void GetUserToken_ShouldReturnTokenWithNoMagicCode() throws URISyntaxException, MalformedURLException {

        CompletableFuture<Void> authTest = this.UseOAuthClientFor((client) ->
        {
            TokenResponse token = null;
            try {
                System.out.println("This is a test  asdfasdfasdf");
                token = await(client.GetUserTokenAsync("default-user", "mygithubconnection", ""));
                if (null==token) {
                    System.out.println(String.format("This is a test 2 - NULL TOKEN"));
                    System.out.flush();
                }
                else {
                    System.out.println(String.format("This is a test 2 - %s", token.token()));
                    System.out.flush();

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Assert.assertNotNull(token);
            Assert.assertFalse(StringUtils.isNotBlank(token.token()));
            return completedFuture(null);
        }, "OAuthConnectorTest", "GetUserToken_ShouldReturnTokenWithNoMagicCode");
        await(authTest);
        }

        @Test
public async Task GetUserToken_ShouldReturnNullOnInvalidConnectionString() throws URISyntaxException {
        await UseOAuthClientFor(async client =>
        {
        var token = await client.GetUserTokenAsync("default-user", "mygithubconnection1", "");
        Assert.Null(token);
        });
        }

        // @Test - Disabled due to bug in service
        //public async Task GetSignInLinkAsync_ShouldReturnValidUrl()
        //{
        //    var activity = new Activity()
        //    {
        //        Id = "myid",
        //        From = new ChannelAccount() { Id = "fromId" },
        //        ServiceUrl = "https://localhost"
        //    };
        //    await UseOAuthClientFor(async client =>
        //     {
        //         var uri = await client.GetSignInLinkAsync(activity, "mygithubconnection");
        //         Assert.False(string.IsNullOrEmpty(uri));
        //         Uri uriResult;
        //         Assert.True(Uri.TryCreate(uri, UriKind.Absolute, out uriResult) && uriResult.Scheme == Uri.UriSchemeHttps);
        //     });
        //}

        @Test
public async Task SignOutUser_ShouldThrowOnEmptyUserId() throws URISyntaxException {
        var client = new OAuthClient(mockConnectorClient, "https://localhost");
        await Assert.ThrowsAsync<ArgumentNullException>(() => client.SignOutUserAsync("", "mockConnection"));
        }

        @Test
public async Task SignOutUser_ShouldThrowOnEmptyConnectionName() throws URISyntaxException {
        var client = new OAuthClient(mockConnectorClient, "https://localhost");
        await Assert.ThrowsAsync<ArgumentNullException>(() => client.SignOutUserAsync("userid", ""));
        }

        @Test
public async Task GetSigninLink_ShouldThrowOnEmptyConnectionName() throws URISyntaxException {
        var activity = new Activity();
        var client = new OAuthClient(mockConnectorClient, "https://localhost");
        await Assert.ThrowsAsync<ArgumentNullException>(() => client.GetSignInLinkAsync(activity, ""));
        }

        @Test
public async Task GetSigninLink_ShouldThrowOnNullActivity() throws URISyntaxException {
        var client = new OAuthClient(mockConnectorClient, "https://localhost");
        await Assert.ThrowsAsync<ArgumentNullException>(() => client.GetSignInLinkAsync(null, "mockConnection"));
        }
        */
        }

