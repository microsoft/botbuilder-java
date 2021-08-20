// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.PasswordServiceClientCredentialFactory;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PasswordServiceClientCredentialFactoryTests {

    private final static String APP_ID = "2cd87869-38a0-4182-9251-d056e8f0ac24";
    private final static String APP_PASSWORD = "password";

    @Test
    public void shouldSetAppIdAndPasswordDuringConstruction() {
        PasswordServiceClientCredentialFactory credFactory = new PasswordServiceClientCredentialFactory(APP_ID, APP_PASSWORD);
        Assert.assertEquals(APP_ID, credFactory.getAppId());
        Assert.assertEquals(APP_PASSWORD, credFactory.getPassword());
    }

    @Test
    public void isValidAppIdShouldWork() {
        PasswordServiceClientCredentialFactory credFactory = new PasswordServiceClientCredentialFactory(APP_ID, APP_PASSWORD);
        Assert.assertTrue(credFactory.isValidAppId(APP_ID).join());
        Assert.assertFalse(credFactory.isValidAppId("invalid-app-id").join());
    }

    @Test
    public void isAuthenticationDisabledShouldWork() {
        PasswordServiceClientCredentialFactory credFactory = new PasswordServiceClientCredentialFactory(APP_ID, APP_PASSWORD);
        Assert.assertFalse(credFactory.isAuthenticationDisabled().join());
        credFactory.setAppId(null);
        Assert.assertTrue(credFactory.isAuthenticationDisabled().join());
    }

    @Test
    public void createCredentialsShouldWork() {
        PasswordServiceClientCredentialFactory credFactory = new PasswordServiceClientCredentialFactory(APP_ID, APP_PASSWORD);
        List<String> testArg1 = Arrays.asList(
            APP_ID,
            AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE,
            String.format(AuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL_TEMPLATE, AuthenticationConstants.DEFAULT_CHANNEL_AUTH_TENANT)
        );
        List<String> testArg2 = Arrays.asList(
            APP_ID,
            AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE,
            String.format(AuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL_TEMPLATE, AuthenticationConstants.DEFAULT_CHANNEL_AUTH_TENANT)
        );
        List<String> testArg3 = Arrays.asList(
            APP_ID,
            GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE,
            GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL
        );
        List<String> testArg4 = Arrays.asList(
            APP_ID,
            "CustomAudience",
            "https://custom.login-endpoint.com/custom-tenant"
        );
        List<List<String>> testArgs = Arrays.asList(testArg1, testArg2, testArg3, testArg4);
        List<ServiceClientCredentials> credentials = testArgs.stream().map(
            args -> credFactory.createCredentials(args.get(0), args.get(1), args.get(2), null).join())
            .collect(Collectors.toList());

        IntStream.range(0, credentials.size()).forEach(idx -> {
            // The PasswordServiceClientCredentialFactory generates subclasses of the AppCredentials class.
            Assert.assertEquals(APP_ID, ((MicrosoftAppCredentials) credentials.get(idx)).getAppId());
            Assert.assertEquals(testArgs.get(idx).get(1), ((MicrosoftAppCredentials) credentials.get(idx)).oAuthScope());
            Assert.assertEquals(testArgs.get(idx).get(2).toLowerCase(), ((MicrosoftAppCredentials) credentials.get(idx)).oAuthEndpoint());
        });
    }

    @Test
    public void createCredentialsShouldAlwaysReturnEmptyCredentialsWhenAuthIsDisabled() {
        PasswordServiceClientCredentialFactory credFactory = new PasswordServiceClientCredentialFactory("", "");
        ServiceClientCredentials credentials = credFactory.createCredentials(null, null, null, null).join();

        // When authentication is disabled, a MicrosoftAppCredentials with empty strings for appId and appPassword is returned.
        Assert.assertNull(((MicrosoftAppCredentials)credentials).getAppId());
        Assert.assertNull(((MicrosoftAppCredentials)credentials).getAppPassword());

        credentials = credFactory.createCredentials(APP_ID, null, null, null).join();
        Assert.assertNull(((MicrosoftAppCredentials)credentials).getAppId());
        Assert.assertNull(((MicrosoftAppCredentials)credentials).getAppPassword());
    }

    @Test
    public void createCredentialsShouldThrowWhenAppIdIsInvalid() {
        PasswordServiceClientCredentialFactory credFactory = new PasswordServiceClientCredentialFactory(APP_ID, APP_PASSWORD);
        Assert.assertThrows(IllegalArgumentException.class, () -> credFactory.createCredentials("badAppId", null, null, null).join());
    }
}
