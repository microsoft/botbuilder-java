package com.microsoft.bot.connector;

import com.microsoft.bot.connector.base.TestBase;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.rest.RestClient;

public class BotConnectorTestBase extends TestBase {
    protected RestConnectorClient connector;
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
        bot = new ChannelAccount().withId(botId);
        user = new ChannelAccount().withId(userId);
    }

    @Override
    protected void cleanUpResources() {
    }
}
