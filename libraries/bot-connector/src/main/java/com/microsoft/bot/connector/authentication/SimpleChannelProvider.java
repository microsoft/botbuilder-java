// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

public class SimpleChannelProvider implements ChannelProvider {
    private String channelService;

    /**
     * Creates a SimpleChannelProvider with no ChannelService which will use Public Azure.
     */
    public SimpleChannelProvider() {

    }

    /**
     * Creates a SimpleChannelProvider with the specified ChannelService.
     *
     * @param channelService The ChannelService to use. Null or empty strings represent Public Azure,
     *                       the string 'https://botframework.us' represents US Government Azure, and
     *                       other values are for private channels.
     */
    public SimpleChannelProvider(String channelService) {
        this.channelService = channelService;
    }

    @Override
    public CompletableFuture<String> getChannelService() {
        return CompletableFuture.completedFuture(channelService);
    }

    @Override
    public boolean isGovernment() {
        return GovernmentAuthenticationConstants.CHANNELSERVICE.equalsIgnoreCase(channelService);
    }

    @Override
    public boolean isPublicAzure() {
        return StringUtils.isEmpty(channelService);
    }
}
