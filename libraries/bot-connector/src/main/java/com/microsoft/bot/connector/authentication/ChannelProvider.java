// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

/**
 * ChannelProvider interface. This interface allows Bots to provide their own
 * implementation for the configuration parameters to connect to a Bot.
 * Framework channel service.
 */
public interface ChannelProvider {
    /**
     * Gets the channel service property for this channel provider.
     *
     * @return The channel service property for the channel provider.
     */
    CompletableFuture<String> getChannelService();

    /**
     * Gets a value of whether this provider represents a channel on Government
     * Azure.
     *
     * @return True if this channel provider represents a channel on Government
     *         Azure.
     */
    boolean isGovernment();

    /**
     * Gets a value of whether this provider represents a channel on Public Azure.
     *
     * @return True if this channel provider represents a channel on Public Azure.
     */
    boolean isPublicAzure();
}
