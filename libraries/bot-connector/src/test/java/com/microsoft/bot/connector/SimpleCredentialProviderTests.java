// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import org.junit.Assert;
import org.junit.Test;

public class SimpleCredentialProviderTests {
    @Test
    public void ValidAppId() {
        SimpleCredentialProvider credentialProvider = new SimpleCredentialProvider("appid", "pwd");

        Assert.assertTrue(credentialProvider.isValidAppId("appid").join());
        Assert.assertFalse(credentialProvider.isValidAppId("wrongappid").join());
    }

    @Test
    public void AppPassword() {
        SimpleCredentialProvider credentialProvider = new SimpleCredentialProvider("appid", "pwd");

        Assert.assertEquals(credentialProvider.getAppPassword("appid").join(), "pwd");
        Assert.assertNull(credentialProvider.getAppPassword("wrongappid").join());
    }

    @Test
    public void AuthenticationDisabled() {
        Assert.assertFalse(new SimpleCredentialProvider("appid", "pwd").isAuthenticationDisabled().join());
        Assert.assertTrue(new SimpleCredentialProvider(null, null).isAuthenticationDisabled().join());
    }
}
