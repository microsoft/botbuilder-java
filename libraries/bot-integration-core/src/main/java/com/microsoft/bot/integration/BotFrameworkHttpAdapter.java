// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.ChannelValidation;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.GovernmentChannelValidation;
import com.microsoft.bot.schema.Activity;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

public class BotFrameworkHttpAdapter extends BotFrameworkAdapter {
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

    public BotFrameworkHttpAdapter(CredentialProvider withCredentialProvider,
                                   ChannelProvider withChannelProvider) {
        super(
            withCredentialProvider,
            withChannelProvider,
            null,
            null
        );
    }

    public CompletableFuture<Void> processIncomingActivity(String authHeader, Activity activity, Bot bot) {
        return processActivity(authHeader, activity, turnContext -> bot.onTurn(turnContext))
            .thenApply(invokeResponse -> null);
    }
}
