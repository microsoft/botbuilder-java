// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.connector.authentication.SimpleChannelProvider;

/**
 * Channel provider which uses Configuration to lookup the channel service
 * property.
 *
 * This will populate the SimpleChannelProvider.ChannelService from a
 * configuration entry with the key of "ChannelService".
 */
public class ConfigurationChannelProvider extends SimpleChannelProvider {
    /**
     * Initializes a new instance using {@link Configuration}.
     * 
     * @param configuration The configuration to use.
     */
    public ConfigurationChannelProvider(Configuration configuration) {
        super(configuration.getProperty("ChannelService"));
    }
}
