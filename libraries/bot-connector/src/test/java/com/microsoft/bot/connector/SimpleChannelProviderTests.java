// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.SimpleChannelProvider;
import org.junit.Assert;
import org.junit.Test;

public class SimpleChannelProviderTests {
    @Test
    public void PublicChannelProvider() {
        SimpleChannelProvider channel = new SimpleChannelProvider();
        Assert.assertTrue(channel.isPublicAzure());
        Assert.assertFalse(channel.isGovernment());
    }

    @Test
    public void GovernmentChannelProvider() {
        SimpleChannelProvider channel = new SimpleChannelProvider(GovernmentAuthenticationConstants.CHANNELSERVICE);
        Assert.assertFalse(channel.isPublicAzure());
        Assert.assertTrue(channel.isGovernment());
    }

    @Test
    public void GetChannelService() {
        try {
            SimpleChannelProvider channel = new SimpleChannelProvider(GovernmentAuthenticationConstants.CHANNELSERVICE);
            String service = channel.getChannelService().join();
            Assert.assertEquals(service, GovernmentAuthenticationConstants.CHANNELSERVICE);
        } catch (Throwable t) {
            Assert.fail("Should not have thrown " + t.getClass().getName());
        }
    }
}
