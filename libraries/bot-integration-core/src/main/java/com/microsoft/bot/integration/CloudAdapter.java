// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.CloudAdapterBase;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthenticationFactory;
import com.microsoft.bot.schema.Activity;
import java.util.concurrent.CompletableFuture;

/**
 * An adapter that implements the Bot Framework Protocol and can be hosted in different cloud environments
 * both public and private.
 */
public class CloudAdapter extends CloudAdapterBase {

    /**
     * Initializes a new instance of the "CloudAdapter" class. (Public cloud. No auth. For testing.)
     */
    public CloudAdapter() {
        this(BotFrameworkAuthenticationFactory.create());
    }

    /**
     * Initializes a new instance of the "CloudAdapter" class.
     * @param botFrameworkAuthentication
     * The BotFrameworkAuthentication this adapter should use.
     */
    public CloudAdapter(BotFrameworkAuthentication botFrameworkAuthentication) {
        super(botFrameworkAuthentication);
    }

    /**
     * Initializes a new instance of the "CloudAdapter" class.
     * @param configuration
     * The Configuration instance.
     */
    public CloudAdapter(Configuration configuration) {
        this(new ConfigurationBotFrameworkAuthentication(configuration, null, null));
    }

    /**
     * Process the inbound HTTP request with the bot resulting in the outbound http response, this method can be called
     *  directly from a Controller.
     * @param authHeader
     * @param activity
     * @param bot The Bot implementation to use for this request.
     * @return void
     */
    public CompletableFuture<Void> processIncomingActivity(String authHeader, Activity activity, Bot bot) {
        return processActivity(authHeader, activity, bot::onTurn).thenApply(result -> null);
    }
}
