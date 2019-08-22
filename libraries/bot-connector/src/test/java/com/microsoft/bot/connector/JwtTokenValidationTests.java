// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.*;
import com.microsoft.bot.schema.models.Activity;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class JwtTokenValidationTests {

    private static final String APPID = "2cd87869-38a0-4182-9251-d056e8f0ac24";
    private static final String APPPASSWORD = "2.30Vs3VQLKt974F";

    private static String getHeaderToken() throws ExecutionException, InterruptedException {
        return String.format("Bearer %s", new MicrosoftAppCredentials(APPID, APPPASSWORD).getToken().get().getAccessToken());
    }

    private static String getGovHeaderToken() throws ExecutionException, InterruptedException {
        return String.format("Bearer %s", new MicrosoftGovernmentAppCredentials(APPID, APPPASSWORD).getToken().get().getAccessToken());
    }

    @Test
    public void ConnectorAuthHeaderCorrectAppIdAndServiceUrlShouldValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new SimpleCredentialProvider(APPID, "");
        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(
            header,
            credentials,
            new SimpleChannelProvider(),
            "",
            "https://webchat.botframework.com/").join();

        Assert.assertTrue(identity.isAuthenticated());
    }

    @Test
    public void Connector_AuthHeader_CorrectAppIdAndServiceUrl_WithGovChannelService_ShouldValidate() throws IOException, ExecutionException, InterruptedException {
        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(
            APPID,
            APPPASSWORD,
            GovernmentAuthenticationConstants.CHANNELSERVICE
        );
    }

    @Test
    public void ConnectorAuthHeaderBotAppIdDiffersShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new SimpleCredentialProvider("00000000-0000-0000-0000-000000000000", "");

        try {
            JwtTokenValidation.validateAuthHeader(
                header,
                credentials,
                new SimpleChannelProvider(),
                "",
                null).join();
        } catch (CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    @Test
    public void ConnectorAuthHeaderBotWithNoCredentialsShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
        // token received and auth disabled
        String header = getHeaderToken();
        CredentialProvider credentials = new SimpleCredentialProvider("", "");

        try {
            JwtTokenValidation.validateAuthHeader(
                header,
                credentials,
                new SimpleChannelProvider(),
                "",
                null).join();
        } catch (CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    @Test
    public void EmptyHeaderBotWithNoCredentialsShouldThrow() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new SimpleCredentialProvider("", "");

        try {
            JwtTokenValidation.validateAuthHeader(
                header,
                credentials,
                new SimpleChannelProvider(),
                "",
                null).join();
            Assert.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("authHeader"));
        }
    }

    @Test
    public void EmulatorMsaHeaderCorrectAppIdAndServiceUrlShouldValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new SimpleCredentialProvider(APPID, "");
        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(
            header,
            credentials,
            new SimpleChannelProvider(),
            "",
            "https://webchat.botframework.com/").join();

        Assert.assertTrue(identity.isAuthenticated());
    }

    @Test
    public void EmulatorMsaHeaderBotAppIdDiffersShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new SimpleCredentialProvider("00000000-0000-0000-0000-000000000000", "");

        try {
            JwtTokenValidation.validateAuthHeader(
                header,
                credentials,
                new SimpleChannelProvider(),
                "",
                null).join();
        } catch (CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    @Test
    public void Emulator_AuthHeader_CorrectAppIdAndServiceUrl_WithGovChannelService_ShouldValidate() throws IOException, ExecutionException, InterruptedException {
        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(
            "2cd87869-38a0-4182-9251-d056e8f0ac24",         // emulator creds
            "2.30Vs3VQLKt974F",
            GovernmentAuthenticationConstants.CHANNELSERVICE);
    }

    @Test
    public void Emulator_AuthHeader_CorrectAppIdAndServiceUrl_WithPrivateChannelService_ShouldValidate() throws IOException, ExecutionException, InterruptedException {
        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(
            "2cd87869-38a0-4182-9251-d056e8f0ac24",         // emulator creds
            "2.30Vs3VQLKt974F",
            "TheChannel");
    }

    /**
     * Tests with a valid Token and service url; and ensures that Service url is added to Trusted service url list.
     */
    @Test
    public void ChannelMsaHeaderValidServiceUrlShouldBeTrusted() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new SimpleCredentialProvider(APPID, "");
        JwtTokenValidation.authenticateRequest(
            new Activity().withServiceUrl("https://smba.trafficmanager.net/amer-client-ss.msg/"),
            header,
            credentials,
            new SimpleChannelProvider()).join();

        Assert.assertTrue(MicrosoftAppCredentials.isTrustedServiceUrl("https://smba.trafficmanager.net/amer-client-ss.msg/"));
    }

    /**
     * Tests with a valid Token and invalid service url; and ensures that Service url is NOT added to Trusted service url list.
     */
    @Test
    public void ChannelMsaHeaderInvalidServiceUrlShouldNotBeTrusted() throws IOException, ExecutionException, InterruptedException {
        String header = getHeaderToken();
        CredentialProvider credentials = new SimpleCredentialProvider("7f74513e-6f96-4dbc-be9d-9a81fea22b88", "");

        try {
            JwtTokenValidation.authenticateRequest(
                new Activity().withServiceUrl("https://webchat.botframework.com/"),
                header,
                credentials,
                new SimpleChannelProvider()).join();
            Assert.fail("Should have thrown AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
            Assert.assertFalse(MicrosoftAppCredentials.isTrustedServiceUrl("https://webchat.botframework.com/"));
        }
    }

    /**
     * Tests with no authentication header and makes sure the service URL is not added to the trusted list.
     */
    @Test
    public void ChannelAuthenticationDisabledShouldBeAnonymous() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new SimpleCredentialProvider("", "");

        ClaimsIdentity identity = JwtTokenValidation.authenticateRequest(
            new Activity().withServiceUrl("https://webchat.botframework.com/"),
            header,
            credentials,
            new SimpleChannelProvider()).join();
        Assert.assertEquals("anonymous", identity.getIssuer());
    }

    @Test
    public void ChannelNoHeaderAuthenticationEnabledShouldThrow() throws IOException, ExecutionException, InterruptedException {
        try {
            String header = "";
            CredentialProvider credentials = new SimpleCredentialProvider(APPID, APPPASSWORD);
            JwtTokenValidation.authenticateRequest(
                new Activity().withServiceUrl("https://smba.trafficmanager.net/amer-client-ss.msg/"),
                header,
                credentials,
                new SimpleChannelProvider()).join();
            Assert.fail("Should have thrown AuthenticationException");
        } catch(CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }

        Assert.assertFalse(MicrosoftAppCredentials.isTrustedServiceUrl("https://smba.trafficmanager.net/amer-client-ss.msg/"));
    }

    /**
     * Tests with no authentication header and makes sure the service URL is not added to the trusted list.
     */
    @Test
    public void ChannelAuthenticationDisabledServiceUrlShouldNotBeTrusted() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new SimpleCredentialProvider("", "");

        ClaimsIdentity identity = JwtTokenValidation.authenticateRequest(
            new Activity().withServiceUrl("https://webchat.botframework.com/"),
            header,
            credentials,
            new SimpleChannelProvider()).join();
        Assert.assertFalse(MicrosoftAppCredentials.isTrustedServiceUrl("https://webchat.botframework.com/"));
    }

    @Test
    public void EnterpriseChannelValidation_Succeeds() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity("anonymous", claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
        } catch (Exception e) {
            Assert.fail("Should not have thrown " + e.getCause().getClass().getName());
        }
    }

    @Test
    public void EnterpriseChannelValidation_NoAuthentication_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(null, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (Exception e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    @Test
    public void EnterpriseChannelValidation_NoAudienceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity("anonymous", claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (Exception e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    @Test
    public void EnterpriseChannelValidation_WrongAudienceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, "abc");
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity("anonymous", claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (Exception e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    @Test
    public void GovernmentChannelValidation_Succeeds() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity("anonymous", claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.assertTrue(true);
        } catch(Exception e) {
            Assert.fail("Should not have thrown " + e.getCause().getClass().getName());
        }
    }

    @Test
    public void GovernmentChannelValidation_NoAuthentication_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(null, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch(CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    @Test
    public void GovernmentChannelValidation_WrongAudienceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, "abc");
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(null, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch(CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }

    /*
    @Test
    public void GovernmentChannelValidation_WrongAudienceClaimIssuer_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(null, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch(CompletionException e) {
            Assert.assertTrue(StringUtils.equals(e.getCause().getClass().getName(), AuthenticationException.class.getName()));
        }
    }
    */

    private void JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(String appId, String pwd, String channelService) throws IOException, ExecutionException, InterruptedException {
        ChannelProvider channel = new SimpleChannelProvider(channelService);
        String header = channel.isGovernment()?getGovHeaderToken():getHeaderToken();

        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(header, appId, pwd, channel);
    }

    private void JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(String header, String appId, String pwd, ChannelProvider channel) {
        CredentialProvider credentials = new SimpleCredentialProvider(appId, pwd);

        try {
            ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(
                header,
                credentials,
                channel,
                "",
                "https://webchat.botframework.com/").join();

            Assert.assertTrue(identity.isAuthenticated());
        } catch(Exception e) {
            Assert.fail("Should not have thrown " + e.getClass().getName());
        }
    }

    private void JwtTokenValidation_ValidateAuthHeader_WithChannelService_Throws(String header, String appId, String pwd, String channelService) throws ExecutionException, InterruptedException {
        CredentialProvider credentials = new SimpleCredentialProvider(appId, pwd);
        ChannelProvider channel = new SimpleChannelProvider(channelService);

        try {
            JwtTokenValidation.validateAuthHeader(
                header,
                credentials,
                channel,
                "",
                "https://webchat.botframework.com/").join();
            Assert.fail("Should have thrown AuthenticationException");
        } catch (AuthenticationException e) {
            Assert.assertTrue(true);
        }
    }
}
