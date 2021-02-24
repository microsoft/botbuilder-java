// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.base.TestBase;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.restclient.RestClient;

public class BotConnectorTestBase extends TestBase {
    protected ConnectorClient connector;
    protected ChannelAccount bot;
    protected ChannelAccount user;

    public BotConnectorTestBase() {
        super(RunCondition.BOTH);
    }

    public BotConnectorTestBase(RunCondition runCondition) {
        super(runCondition);
    }

    @Override
    protected void initializeClients(RestClient restClient, String botId, String userId) {
        connector = new RestConnectorClient(restClient);
        bot = new ChannelAccount(botId);
        user = new ChannelAccount(userId);
    }

    @Override
    protected void cleanUpResources() {
    }
}
