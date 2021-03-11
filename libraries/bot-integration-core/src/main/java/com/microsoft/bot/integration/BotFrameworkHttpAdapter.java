// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.ChannelValidation;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.GovernmentChannelValidation;
import com.microsoft.bot.schema.Activity;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * A BotFrameworkAdapter that receives incoming Activities via HTTP.
 */
public class BotFrameworkHttpAdapter extends BotFrameworkAdapter {
    /**
     * Construct with a Configuration. This will create a CredentialProvider and
     * ChannelProvider based on configuration values.
     *
     * @param withConfiguration The Configuration to use.
     *
     * @see ClasspathPropertiesConfiguration
     */
    public BotFrameworkHttpAdapter(Configuration withConfiguration) {
        super(
            new ConfigurationCredentialProvider(withConfiguration),
            new ConfigurationChannelProvider(withConfiguration),
            null,
            null
        );

        String openIdEndPoint = withConfiguration.getProperty("BotOpenIdMetadata");
        if (!StringUtils.isEmpty(openIdEndPoint)) {
            // Indicate which Cloud we are using, for example, Public or Sovereign.
            ChannelValidation.setOpenIdMetaDataUrl(openIdEndPoint);
            GovernmentChannelValidation.setOpenIdMetaDataUrl(openIdEndPoint);
        }
    }

        /**
     * Construct with a Configuration. This will create a CredentialProvider and
     * ChannelProvider based on configuration values.
     *
     * @param withConfiguration The Configuration to use.
     * @param withAuthenticationConfiguration The AuthenticationConfiguration to use.
     *
     * @see ClasspathPropertiesConfiguration
     */
    public BotFrameworkHttpAdapter(
        Configuration withConfiguration,
        AuthenticationConfiguration withAuthenticationConfiguration
    ) {
        super(
            new ConfigurationCredentialProvider(withConfiguration),
            withAuthenticationConfiguration,
            new ConfigurationChannelProvider(withConfiguration),
            null,
            null
        );

        String openIdEndPoint = withConfiguration.getProperty("BotOpenIdMetadata");
        if (!StringUtils.isEmpty(openIdEndPoint)) {
            // Indicate which Cloud we are using, for example, Public or Sovereign.
            ChannelValidation.setOpenIdMetaDataUrl(openIdEndPoint);
            GovernmentChannelValidation.setOpenIdMetaDataUrl(openIdEndPoint);
        }
    }

    /**
     * Constructs with CredentialProvider and ChannelProvider.
     *
     * @param withCredentialProvider The CredentialProvider to use.
     * @param withChannelProvider    The ChannelProvider to use.
     */
    public BotFrameworkHttpAdapter(
        CredentialProvider withCredentialProvider,
        ChannelProvider withChannelProvider
    ) {
        super(withCredentialProvider, withChannelProvider, null, null);
    }

    /**
     * Processes an incoming Activity.
     *
     * @param authHeader The Authorization header from the http request.
     * @param activity   The received Activity.
     * @param bot        A Bot.
     * @return A CompletableFuture.
     */
    public CompletableFuture<InvokeResponse> processIncomingActivity(
        String authHeader,
        Activity activity,
        Bot bot
    ) {
        return processActivity(authHeader, activity, bot::onTurn);
    }
}
