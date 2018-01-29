package com.microsoft.bot.connector;

import com.microsoft.bot.connector.base.TestBase;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.connector.implementation.ChannelAccountInner;
import com.microsoft.rest.RestClient;

public class BotConnectorTestBase extends TestBase {
    protected ConnectorClientImpl connector;
    protected ChannelAccountInner bot;
    protected ChannelAccountInner user;

    public BotConnectorTestBase() {
        super(RunCondition.BOTH);
    }

    public BotConnectorTestBase(RunCondition runCondition) {
        super(runCondition);
    }

    @Override
    protected void initializeClients(RestClient restClient, String botId, String userId) {
        connector = new ConnectorClientImpl(restClient);
        bot = new ChannelAccountInner().withId(botId);
        user = new ChannelAccountInner().withId(userId);
    }

    @Override
    protected void cleanUpResources() {
    }
}