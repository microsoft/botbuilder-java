// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import org.junit.Assert;
import org.junit.Test;

public class SimpleCredentialProviderTests {
    @Test
    public void ValidAppIdAsync() {
        SimpleCredentialProvider credentialProvider = new SimpleCredentialProvider("appid", "pwd");

        Assert.assertTrue(credentialProvider.isValidAppIdAsync("appid").join());
        Assert.assertFalse(credentialProvider.isValidAppIdAsync("wrongappid").join());
    }

    @Test
    public void AppPasswordAsync() {
        SimpleCredentialProvider credentialProvider = new SimpleCredentialProvider("appid", "pwd");

        Assert.assertEquals(credentialProvider.getAppPasswordAsync("appid").join(), "pwd");
        Assert.assertNull(credentialProvider.getAppPasswordAsync("wrongappid").join());
    }

    @Test
    public void AuthenticationDisabledAsync() {
        Assert.assertFalse(new SimpleCredentialProvider("appid", "pwd").isAuthenticationDisabledAsync().join());
        Assert.assertTrue(new SimpleCredentialProvider(null, null).isAuthenticationDisabledAsync().join());
    }
}
