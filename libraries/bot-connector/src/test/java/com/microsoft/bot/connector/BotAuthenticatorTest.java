package com.microsoft.bot.connector;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.*;
import com.microsoft.bot.schema.models.Activity;
import okhttp3.Request;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class BotAuthenticatorTest {

    private static final String AppId = "2cd87869-38a0-4182-9251-d056e8f0ac24";
    private static final String AppPassword = "2.30Vs3VQLKt974F";

    @Test
    public void ConnectorAuthHeaderCorrectAppIdAndServiceUrlShouldValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new CredentialProviderImpl(AppId, "");
        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(header, credentials, "", "https://webchat.botframework.com/").get();

        Assert.assertTrue(identity.isAuthenticated());
    }

    @Test
    public void ConnectorAuthHeaderBotAppIdDiffersShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new CredentialProviderImpl("00000000-0000-0000-0000-000000000000", "");

        try {
            JwtTokenValidation.validateAuthHeader(header, credentials, "", null).get();
        } catch (AuthenticationException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid AppId passed on token"));
        }
    }

    @Test
    public void ConnectorAuthHeaderBotWithNoCredentialsShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
        // token received and auth disabled
        String header = getHeaderToken();
        CredentialProvider credentials = new CredentialProviderImpl("", "");

        try {
            JwtTokenValidation.validateAuthHeader(header, credentials, "", null).get();
        } catch (AuthenticationException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid AppId passed on token"));
        }
    }

    @Test
    public void EmptyHeaderBotWithNoCredentialsShouldThrow() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new CredentialProviderImpl("", "");

        try {
            JwtTokenValidation.validateAuthHeader(header, credentials, "", null).get();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("authHeader"));
        }
    }

    @Test
    public void EmulatorMsaHeaderCorrectAppIdAndServiceUrlShouldValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new CredentialProviderImpl(AppId, "");
        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(header, credentials, "", "https://webchat.botframework.com/").get();

        Assert.assertTrue(identity.isAuthenticated());
    }

    @Test
    public void EmulatorMsaHeaderBotAppIdDiffersShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new CredentialProviderImpl("00000000-0000-0000-0000-000000000000", "");

        try {
            JwtTokenValidation.validateAuthHeader(header, credentials, "", null).get();
        } catch (AuthenticationException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid AppId passed on token"));
        }
    }

    /**
     * Tests with a valid Token and service url; and ensures that Service url is added to Trusted service url list.
     */
    @Test
    public void ChannelMsaHeaderValidServiceUrlShouldBeTrusted() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new CredentialProviderImpl(AppId, "");
        JwtTokenValidation.authenticateRequest(
                new Activity().withServiceUrl("https://smba.trafficmanager.net/amer-client-ss.msg/"),
                header,
                credentials);

        Assert.assertTrue(MicrosoftAppCredentials.isTrustedServiceUrl("https://smba.trafficmanager.net/amer-client-ss.msg/"));
    }

    /**
     * Tests with a valid Token and invalid service url; and ensures that Service url is NOT added to Trusted service url list.
     */
    @Test
    public void ChannelMsaHeaderInvalidServiceUrlShouldNotBeTrusted() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new CredentialProviderImpl("7f74513e-6f96-4dbc-be9d-9a81fea22b88", "");

        try {
            JwtTokenValidation.authenticateRequest(
                    new Activity().withServiceUrl("https://webchat.botframework.com/"),
                    header,
                    credentials);
            Assert.fail("Should have thrown AuthenticationException");
        } catch (AuthenticationException ex) {
            Assert.assertFalse(MicrosoftAppCredentials.isTrustedServiceUrl("https://webchat.botframework.com/"));
        }

    }

    /**
     * Tests with no authentication header and makes sure the service URL is not added to the trusted list.
     */
    @Test
    public void ChannelAuthenticationDisabledShouldBeAnonymous() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new CredentialProviderImpl("", "");

        ClaimsIdentity identity = JwtTokenValidation.authenticateRequest(new Activity().withServiceUrl("https://webchat.botframework.com/"), header, credentials).get();
        Assert.assertEquals("anonymous", identity.getIssuer());
    }

    /**
     * Tests with no authentication header and makes sure the service URL is not added to the trusted list.
     */
    @Test
    public void ChannelAuthenticationDisabledServiceUrlShouldNotBeTrusted() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new CredentialProviderImpl("", "");

        ClaimsIdentity identity = JwtTokenValidation.authenticateRequest(new Activity().withServiceUrl("https://webchat.botframework.com/"), header, credentials).get();
        Assert.assertFalse(MicrosoftAppCredentials.isTrustedServiceUrl("https://webchat.botframework.com/"));
    }

    private static String getHeaderToken() throws MalformedURLException, ExecutionException, InterruptedException {
        return String.format("Bearer %s", new MicrosoftAppCredentials(AppId, AppPassword).getToken().get().getAccessToken());
    }
}
