// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthenticationFactory;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ConnectorFactory;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.PasswordServiceClientCredentialFactory;
import com.microsoft.bot.connector.authentication.SkillValidation;
import com.microsoft.bot.connector.authentication.UserTokenClient;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.instanceOf;

public class BotFrameworkAuthenticationFactoryTests {
    @Test
    public void shouldCreateAnonymousBotFrameworkAuthentication() {
        BotFrameworkAuthentication bfA = BotFrameworkAuthenticationFactory.create();
        MatcherAssert.assertThat(bfA, instanceOf(BotFrameworkAuthentication.class));
    }

    @Test
    public void shouldCreateBotFrameworkAuthenticationConfiguredForValidChannelServices() {
        BotFrameworkAuthentication bfA = BotFrameworkAuthenticationFactory.create(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
        Assert.assertEquals(
            bfA.getOriginatingAudience(),
            AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);

        BotFrameworkAuthentication gBfA = BotFrameworkAuthenticationFactory.create(
            GovernmentAuthenticationConstants.CHANNELSERVICE,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
        Assert.assertEquals(
            gBfA.getOriginatingAudience(),
            GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);
    }

    @Test
    public void shouldThrowWithAnUnknownChannelService() {
        Assert.assertThrows(IllegalArgumentException.class, () -> BotFrameworkAuthenticationFactory.create(
            "Unknown",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null));
    }

    /**
     * These tests replicate the flow in CloudAdapterBase.processProactive().
     *
     * The CloudAdapterBase's BotFrameworkAuthentication (normally and practically the ParameterizedBotFrameworkAuthentication) is
     * used to create and set on the TurnState the following values:
     * - ConnectorFactory
     * - ConnectorClient
     * - UserTokenClient
     */
    String HOST_SERVICE_URL = "https://bot.host.serviceurl";
    String HOST_AUDIENCE = "host-bot-app-id";

    @Test
    public void shouldNotThrowErrorsWhenAuthIsDisabledAndAnonymousSkillClaimsAreUsed() {
        PasswordServiceClientCredentialFactory credsFactory = new PasswordServiceClientCredentialFactory("", "");
        BotFrameworkAuthentication pBFA = BotFrameworkAuthenticationFactory.create(
            "",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            credsFactory,
            null,
            null);

        Assert.assertEquals(pBFA.getOriginatingAudience(), AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);
        ClaimsIdentity claimsIdentity = SkillValidation.createAnonymousSkillClaim();

        // The order of creation for the connectorFactory, connectorClient and userTokenClient mirrors the existing flow in
        // CloudAdapterBase.processProactive().
        ConnectorFactory connectorFactory = pBFA.createConnectorFactory(claimsIdentity);
        // When authentication is disabled, MicrosoftAppCredentials (an implementation of ServiceClientCredentials)
        // with appId and appPassword fields are created and passed to the newly created ConnectorFactory.
        ConnectorClient connectorClient = connectorFactory.create(HOST_SERVICE_URL, "UnusedAudienceWhenAuthIsDisabled").join();
        // If authentication was enabled 'UnusedAudienceWhenAuthIsDisabled' would have been used,
        // but is unnecessary with disabled authentication.
        UserTokenClient userTokenClient = pBFA.createUserTokenClient(claimsIdentity).join();
    }

    @Test
    public void shouldNotThrowErrorsWhenAuthIsDisabledAndAuthenticatedSkillClaimsAreUsed() {
        String APP_ID = "app-id";
        String APP_PASSWORD = "app-password";
        PasswordServiceClientCredentialFactory credsFactory = new PasswordServiceClientCredentialFactory(APP_ID, APP_PASSWORD);
        BotFrameworkAuthentication pBFA = BotFrameworkAuthenticationFactory.create(
            "",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            credsFactory,
            null,
            null);

        Assert.assertEquals(pBFA.getOriginatingAudience(), AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE);
        HashMap<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUTHORIZED_PARTY, HOST_AUDIENCE);
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, APP_ID);
        claims.put(AuthenticationConstants.VERSION_CLAIM, "2.0");
        ClaimsIdentity claimsIdentity = new ClaimsIdentity("anonymous", claims);

        ConnectorFactory connectorFactory = pBFA.createConnectorFactory(claimsIdentity);

        ConnectorClient connectorClient = connectorFactory.create(HOST_SERVICE_URL, HOST_AUDIENCE).join();

        UserTokenClient userTokenClient = pBFA.createUserTokenClient(claimsIdentity).join();
    }
}
