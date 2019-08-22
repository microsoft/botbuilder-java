// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

public class MicrosoftAppCredentialsTests {
    @Test
    public void ValidUrlTrusted() {
        MicrosoftAppCredentials.trustServiceUrl("https://goodurl.com");
        Assert.assertTrue(MicrosoftAppCredentials.isTrustedServiceUrl("https://goodurl.com"));
    }

    @Test
    public void InvalidUrlTrusted() {
        MicrosoftAppCredentials.trustServiceUrl("badurl");
        Assert.assertFalse(MicrosoftAppCredentials.isTrustedServiceUrl("badurl"));
    }

    @Test
    public void TrustedUrlExpiration() throws InterruptedException {
        // There is a +5 minute window for an expired url
        MicrosoftAppCredentials.trustServiceUrl("https://goodurl.com", LocalDateTime.now().minusMinutes(6));
        Assert.assertFalse(MicrosoftAppCredentials.isTrustedServiceUrl("https://goodurl.com"));

        MicrosoftAppCredentials.trustServiceUrl("https://goodurl.com", LocalDateTime.now().minusMinutes(4));
        Assert.assertTrue(MicrosoftAppCredentials.isTrustedServiceUrl("https://goodurl.com"));
    }

    @Test
    public void ValidateAuthEndpoint() {
        try {
            // In Java, about the only thing that can cause a MalformedURLException in a missing or unknown protocol.
            // At any rate, this should validate someone didn't mess up the oAuth Endpoint for the class.
            MicrosoftAppCredentials credentials = new MicrosoftAppCredentials("2cd87869-38a0-4182-9251-d056e8f0ac24", "2.30Vs3VQLKt974F");
            new URL(credentials.oAuthEndpoint());

            credentials.setChannelAuthTenant("tenant.com");

            MicrosoftAppCredentials credentialsWithTenant =
                new MicrosoftAppCredentials("2cd87869-38a0-4182-9251-d056e8f0ac24", "2.30Vs3VQLKt974F", "tenant.com");

        } catch(MalformedURLException e) {
            Assert.fail("Should not have thrown MalformedURLException");
        }
    }

    @Test
    public void GetToken() throws InterruptedException, ExecutionException {
        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials("2cd87869-38a0-4182-9251-d056e8f0ac24", "2.30Vs3VQLKt974F");
        AuthenticationResult token = credentials.getToken().get();
        Assert.assertFalse(StringUtils.isEmpty(token.getAccessToken()));
    }
}