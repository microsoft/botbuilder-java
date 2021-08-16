// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.*;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.RoleTypes;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class JwtTokenValidationTests {

    private static final String APPID = "2cd87869-38a0-4182-9251-d056e8f0ac24";
    private static final String APPPASSWORD = "2.30Vs3VQLKt974F";

    private static String getHeaderToken() {
        return String.format("Bearer %s", new MicrosoftAppCredentials(APPID, APPPASSWORD).getToken().join());
    }

    private static String getGovHeaderToken() {
        return String.format("Bearer %s", new MicrosoftGovernmentAppCredentials(APPID, APPPASSWORD).getToken().join());
    }

//    @Test
//    public void ConnectorAuthHeaderCorrectAppIdAndServiceUrlShouldValidate() throws IOException, ExecutionException, InterruptedException {
//        String header = getHeaderToken();
//        CredentialProvider credentials = new SimpleCredentialProvider(APPID, "");
//        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(
//            header,
//            credentials,
//            new SimpleChannelProvider(),
//            "",
//            "https://webchat.botframework.com/").join();
//
//        Assert.assertTrue(identity.isAuthenticated());
//    }

//    @Test
//    public void Connector_AuthHeader_CorrectAppIdAndServiceUrl_WithGovChannelService_ShouldValidate() throws IOException, ExecutionException, InterruptedException {
//        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(
//            APPID,
//            APPPASSWORD,
//            GovernmentAuthenticationConstants.CHANNELSERVICE
//        );
//    }

//    @Test
//    public void ConnectorAuthHeaderBotAppIdDiffersShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
//        String header = getHeaderToken();
//        CredentialProvider credentials = new SimpleCredentialProvider("00000000-0000-0000-0000-000000000000", "");
//
//        try {
//            JwtTokenValidation.validateAuthHeader(
//                header,
//                credentials,
//                new SimpleChannelProvider(),
//                "",
//                null).join();
//        } catch (CompletionException e) {
//            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
//        }
//    }

//    @Test
//    public void ConnectorAuthHeaderBotWithNoCredentialsShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
//        // token received and auth disabled
//        String header = getHeaderToken();
//        CredentialProvider credentials = new SimpleCredentialProvider("", "");
//
//        try {
//            JwtTokenValidation.validateAuthHeader(
//                header,
//                credentials,
//                new SimpleChannelProvider(),
//                "",
//                null).join();
//        } catch (CompletionException e) {
//            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
//        }
//    }

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
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("authHeader"));
        }
    }

//    @Test
//    public void EmulatorMsaHeaderCorrectAppIdAndServiceUrlShouldValidate() throws IOException, ExecutionException, InterruptedException {
//        String header = getHeaderToken();
//        CredentialProvider credentials = new SimpleCredentialProvider(APPID, "");
//        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(
//            header,
//            credentials,
//            new SimpleChannelProvider(),
//            "",
//            "https://webchat.botframework.com/").join();
//
//        Assert.assertTrue(identity.isAuthenticated());
//    }

//    @Test
//    public void EmulatorMsaHeaderBotAppIdDiffersShouldNotValidate() throws IOException, ExecutionException, InterruptedException {
//        String header = getHeaderToken();
//        CredentialProvider credentials = new SimpleCredentialProvider("00000000-0000-0000-0000-000000000000", "");
//
//        try {
//            JwtTokenValidation.validateAuthHeader(
//                header,
//                credentials,
//                new SimpleChannelProvider(),
//                "",
//                null).join();
//        } catch (CompletionException e) {
//            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
//        }
//    }

//    @Test
//    public void Emulator_AuthHeader_CorrectAppIdAndServiceUrl_WithGovChannelService_ShouldValidate() throws IOException, ExecutionException, InterruptedException {
//        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(
//            "2cd87869-38a0-4182-9251-d056e8f0ac24",         // emulator creds
//            "2.30Vs3VQLKt974F",
//            GovernmentAuthenticationConstants.CHANNELSERVICE);
//    }

//    @Test
//    public void Emulator_AuthHeader_CorrectAppIdAndServiceUrl_WithPrivateChannelService_ShouldValidate() throws IOException, ExecutionException, InterruptedException {
//        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(
//            "2cd87869-38a0-4182-9251-d056e8f0ac24",         // emulator creds
//            "2.30Vs3VQLKt974F",
//            "TheChannel");
//    }

    /**
     * Tests with a valid Token and invalid service url; and ensures that Service url is NOT added to Trusted service url list.
     */
//    @Test
//    public void ChannelMsaHeaderInvalidServiceUrlShouldNotBeTrusted() throws IOException, ExecutionException, InterruptedException {
//        String header = getHeaderToken();
//        CredentialProvider credentials = new SimpleCredentialProvider("7f74513e-6f96-4dbc-be9d-9a81fea22b88", "");
//
//        try {
//            Activity activity = new Activity(ActivityTypes.MESSAGE);
//            activity.setServiceUrl("https://webchat.botframework.com/");
//            JwtTokenValidation.authenticateRequest(
//                activity,
//                header,
//                credentials,
//                new SimpleChannelProvider()).join();
//            Assert.fail("Should have thrown AuthenticationException");
//        } catch (CompletionException e) {
//            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
//        }
//    }

    /**
     * Tests with no authentication header and makes sure the service URL is not added to the trusted list.
     */
    @Test
    public void ChannelAuthenticationDisabledShouldBeAnonymous() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new SimpleCredentialProvider("", "");

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setServiceUrl("https://webchat.botframework.com/");
        ClaimsIdentity identity = JwtTokenValidation.authenticateRequest(
            activity,
            header,
            credentials,
            new SimpleChannelProvider()).join();
        Assert.assertEquals("anonymous", identity.getIssuer());
    }

    /**
     * Tests with no authentication header and makes sure the service URL is not added to the trusted list.
     */
    @Test
    public void ChannelAuthenticationDisabledAndSkillShouldBeAnonymous() throws ExecutionException, InterruptedException {
        String header = "";
        CredentialProvider credentials = new SimpleCredentialProvider("", "");

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setServiceUrl("https://webchat.botframework.com/");
        activity.setChannelId(Channels.EMULATOR);
        activity.setRelatesTo(new ConversationReference());
        ChannelAccount skillAccount = new ChannelAccount();
        skillAccount.setRole(RoleTypes.SKILL);
        activity.setRecipient(skillAccount);
        ClaimsIdentity identity = JwtTokenValidation.authenticateRequest(
            activity,
            header,
            credentials,
            new SimpleChannelProvider()).join();
        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_AUTH_TYPE, identity.getType());
        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_SKILL_APPID, JwtTokenValidation.getAppIdFromClaims(identity.claims()));
    }


    @Test
    public void ChannelNoHeaderAuthenticationEnabledShouldThrow() throws IOException, ExecutionException, InterruptedException {
        try {
            String header = "";
            CredentialProvider credentials = new SimpleCredentialProvider(APPID, APPPASSWORD);
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setServiceUrl("https://smba.trafficmanager.net/amer-client-ss.msg/");
            JwtTokenValidation.authenticateRequest(
                activity,
                header,
                credentials,
                new SimpleChannelProvider()).join();
            Assert.fail("Should have thrown AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void EnterpriseChannelValidation_Succeeds() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
        } catch (CompletionException e) {
            Assert.fail("Should not have thrown " + e.getCause().getClass().getName());
        }
    }

    @Test
    public void EnterpriseChannelValidation_NoAuthentication_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(null, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void EnterpriseChannelValidation_NoAudienceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void EnterpriseChannelValidation_NoAudienceClaimValue_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, "");
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void EnterpriseChannelValidation_WrongAudienceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, "abc");
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void EnterpriseChannelValidation_NoServiceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        ClaimsIdentity identity = new ClaimsIdentity(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void EnterpriseChannelValidation_NoServiceClaimValue_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, "");
        ClaimsIdentity identity = new ClaimsIdentity(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void EnterpriseChannelValidation_WrongServiceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, null);

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, "other");
        ClaimsIdentity identity = new ClaimsIdentity(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            EnterpriseChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an AuthenticationException");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_Succeeds() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
        } catch (Exception e) {
            Assert.fail("Should not have thrown " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
        }
    }

    @Test
    public void GovernmentChannelValidation_NoAuthentication_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(null, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_NoAudienceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_NoAudienceClaimValue_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, "");
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_WrongAudienceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, "abc");
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_WrongAudienceClaimIssuer_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
        ClaimsIdentity identity = new ClaimsIdentity("https://wrongissuer.com", claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_NoServiceClaim_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        ClaimsIdentity identity = new ClaimsIdentity(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_NoServiceClaimValue_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, "");
        ClaimsIdentity identity = new ClaimsIdentity(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void GovernmentChannelValidation_WrongServiceClaimValue_Fails() {
        String appId = "1234567890";
        String serviceUrl = "https://webchat.botframework.com/";
        CredentialProvider credentials = new SimpleCredentialProvider(appId, "");

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, appId);
        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, "other");
        ClaimsIdentity identity = new ClaimsIdentity(GovernmentAuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER, claims);

        try {
            GovernmentChannelValidation.validateIdentity(identity, credentials, serviceUrl).join();
            Assert.fail("Should have thrown an Authorization exception");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof AuthenticationException);
        }
    }

    private void JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(String appId, String pwd, String channelService) throws IOException, ExecutionException, InterruptedException {
        String header = "Bearer " + new MicrosoftAppCredentials(appId, pwd).getToken().join();
        JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(header, appId, pwd, channelService);
    }

    private void JwtTokenValidation_ValidateAuthHeader_WithChannelService_Succeeds(String header, String appId, String pwd, String channelService) {
        CredentialProvider credentials = new SimpleCredentialProvider(appId, pwd);
        ChannelProvider channel = new SimpleChannelProvider(channelService);

        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(header, credentials, channel, null, "https://webchat.botframework.com/").join();

        Assert.assertTrue(identity.isAuthenticated());
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
