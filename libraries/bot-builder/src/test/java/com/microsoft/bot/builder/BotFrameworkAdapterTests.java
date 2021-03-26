// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.SimpleChannelProvider;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityEventNames;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.CallerIdConstants;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.DeliveryModes;
import com.microsoft.bot.schema.ExpectedReplies;
import com.microsoft.bot.schema.ResourceResponse;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CompletableFuture;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BotFrameworkAdapterTests {
    @Test
    public void TenantIdShouldBeSetInConversationForTeams() {
        Activity activity = processActivity(Channels.MSTEAMS, "theTenantId", null);
        Assert.assertEquals("theTenantId", activity.getConversation().getTenantId());
    }

    @Test
    public void TenantIdShouldNotChangeInConversationForTeamsIfPresent() {
        Activity activity = processActivity(Channels.MSTEAMS, "theTenantId", "shouldNotBeReplaced");
        Assert.assertEquals("shouldNotBeReplaced", activity.getConversation().getTenantId());
    }

    @Test
    public void TenantIdShouldNotBeSetInConversationIfNotTeams() {
        Activity activity = processActivity(Channels.DIRECTLINE, "theTenantId", null);
        Assert.assertNull(activity.getConversation().getTenantId());
    }

    @Test
    public void CreateConversationOverloadProperlySetsTenantId() {
        // Arrange
        final String ActivityIdValue = "SendActivityId";
        final String ConversationIdValue = "NewConversationId";
        final String TenantIdValue = "theTenantId";
        final String EventActivityName = ActivityEventNames.CREATE_CONVERSATION;

        // so we can provide a mock ConnectorClient.
        class TestBotFrameworkAdapter extends BotFrameworkAdapter {
            public TestBotFrameworkAdapter(CredentialProvider withCredentialProvider) {
                super(withCredentialProvider);
            }

            @Override
            protected CompletableFuture<ConnectorClient> getOrCreateConnectorClient(
                String serviceUrl,
                AppCredentials usingAppCredentials
            ) {
                Conversations conv = mock(Conversations.class);
                ConversationResourceResponse response = new ConversationResourceResponse();
                response.setActivityId(ActivityIdValue);
                response.setId(ConversationIdValue);
                when(conv.createConversation(any())).thenReturn(
                    CompletableFuture.completedFuture(response)
                );

                ConnectorClient client = mock(ConnectorClient.class);
                when(client.getConversations()).thenReturn(conv);

                return CompletableFuture.completedFuture(client);
            }
        }

        CredentialProvider mockCredentialProvider = mock(CredentialProvider.class);
        BotFrameworkAdapter adapter = new TestBotFrameworkAdapter(mockCredentialProvider);

        ObjectNode channelData = JsonNodeFactory.instance.objectNode();
        channelData.set(
            "tenant",
            JsonNodeFactory.instance.objectNode()
                .set("id", JsonNodeFactory.instance.textNode(TenantIdValue))
        );

        Activity activity = new Activity("Test");
        activity.setChannelId(Channels.MSTEAMS);
        activity.setServiceUrl("https://fake.service.url");
        activity.setChannelData(channelData);
        ConversationAccount conversation = new ConversationAccount();
        conversation.setTenantId(TenantIdValue);
        activity.setConversation(conversation);

        Activity activityConversation = new Activity(ActivityTypes.MESSAGE);
        activityConversation.setChannelData(activity.getChannelData());
        ConversationParameters parameters = new ConversationParameters();
        parameters.setActivity(activityConversation);

        ConversationReference reference = activity.getConversationReference();
        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(null, null);

        Activity[] newActivity = new Activity[] {null};
        BotCallbackHandler updateParameters = (turnContext -> {
            newActivity[0] = turnContext.getActivity();
            return CompletableFuture.completedFuture(null);
        });

        adapter.createConversation(
            activity.getChannelId(),
            activity.getServiceUrl(),
            credentials,
            parameters,
            updateParameters,
            reference
        ).join();

        Assert.assertEquals(
            TenantIdValue,
            ((JsonNode) newActivity[0].getChannelData()).get("tenant").get("tenantId").textValue()
        );
        Assert.assertEquals(ActivityIdValue, newActivity[0].getId());
        Assert.assertEquals(ConversationIdValue, newActivity[0].getConversation().getId());
        Assert.assertEquals(TenantIdValue, newActivity[0].getConversation().getTenantId());
        Assert.assertEquals(EventActivityName, newActivity[0].getName());
    }

    private Activity processActivity(
        String channelId,
        String channelDataTenantId,
        String conversationTenantId
    ) {
        ClaimsIdentity mockClaims = new ClaimsIdentity("anonymous");
        CredentialProvider mockCredentials = new SimpleCredentialProvider();

        BotFrameworkAdapter sut = new BotFrameworkAdapter(mockCredentials);

        ObjectNode channelData = new ObjectMapper().createObjectNode();
        ObjectNode tenantId = new ObjectMapper().createObjectNode();
        tenantId.put("id", channelDataTenantId);
        channelData.set("tenant", tenantId);

        Activity[] activity = new Activity[] {null};
        Activity activityTest = new Activity("test");
        activityTest.setChannelId(channelId);
        activityTest.setServiceUrl("https://smba.trafficmanager.net/amer/");
        activityTest.setChannelData(channelData);
        ConversationAccount conversation = new ConversationAccount();
        conversation.setTenantId(conversationTenantId);
        activityTest.setConversation(conversation);
        sut.processActivity(mockClaims, activityTest, (context) -> {
            activity[0] = context.getActivity();
            return CompletableFuture.completedFuture(null);
        }).join();

        return activity[0];
    }

    @Test
    public void OutgoingActivityIdNotSent() {
        CredentialProvider mockCredentials = mock(CredentialProvider.class);
        BotFrameworkAdapter adapter = new BotFrameworkAdapter(mockCredentials);

        Activity incoming_activity = new Activity("test");
        incoming_activity.setId("testid");
        incoming_activity.setChannelId(Channels.DIRECTLINE);
        incoming_activity.setServiceUrl("https://fake.service.url");
        incoming_activity.setConversation(new ConversationAccount("cid"));

        Activity reply = MessageFactory.text("test");
        reply.setId("TestReplyId");

        MemoryConnectorClient mockConnector = new MemoryConnectorClient();
        TurnContext turnContext = new TurnContextImpl(adapter, incoming_activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, mockConnector);
        turnContext.sendActivity(reply).join();

        Assert.assertEquals(
            1,
            ((MemoryConversations) mockConnector.getConversations()).getSentActivities().size()
        );
        Assert.assertNull(
            ((MemoryConversations) mockConnector.getConversations()).getSentActivities().get(0).getId()
        );
    }

    @Test
    public void processActivityCreatesCorrectCredsAndClient_anon() {
        processActivityCreatesCorrectCredsAndClient(
            null,
            null,
            null,
            AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE,
            0,
            1
        );
    }

    @Test
    public void processActivityCreatesCorrectCredsAndClient_public() {
        processActivityCreatesCorrectCredsAndClient(
            "00000000-0000-0000-0000-000000000001",
            CallerIdConstants.PUBLIC_AZURE_CHANNEL,
            null,
            AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE,
            1,
            1
        );
    }

    @Test
    public void processActivityCreatesCorrectCredsAndClient_gov() {
        processActivityCreatesCorrectCredsAndClient(
            "00000000-0000-0000-0000-000000000001",
            CallerIdConstants.US_GOV_CHANNEL,
            GovernmentAuthenticationConstants.CHANNELSERVICE,
            GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE,
            1,
            1
        );
    }

    private void processActivityCreatesCorrectCredsAndClient(
        String botAppId,
        String expectedCallerId,
        String channelService,
        String expectedScope,
        int expectedAppCredentialsCount,
        int expectedClientCredentialsCount
    ) {
        HashMap<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, botAppId);
        claims.put(AuthenticationConstants.APPID_CLAIM, botAppId);
        claims.put(AuthenticationConstants.VERSION_CLAIM, "1.0");
        ClaimsIdentity identity = new ClaimsIdentity("anonymous", claims);

        SimpleCredentialProvider credentialProvider = new SimpleCredentialProvider();
        credentialProvider.setAppId(botAppId);
        String serviceUrl = "https://smba.trafficmanager.net/amer/";

        BotFrameworkAdapter sut = new BotFrameworkAdapter(
            credentialProvider,
            new SimpleChannelProvider(channelService),
            null,
            null
        );

        BotCallbackHandler callback = turnContext -> {
            getAppCredentialsAndAssertValues(
                turnContext,
                botAppId,
                expectedScope,
                expectedAppCredentialsCount
            );

            getConnectorClientAndAssertValues(
                turnContext,
                botAppId,
                expectedScope,
                serviceUrl,
                expectedClientCredentialsCount
            );

            Assert.assertEquals(expectedCallerId, turnContext.getActivity().getCallerId());

            return CompletableFuture.completedFuture(null);
        };

        Activity activityTest = new Activity("test");
        activityTest.setChannelId(Channels.EMULATOR);
        activityTest.setServiceUrl(serviceUrl);
        sut.processActivity(identity, activityTest, callback).join();
    }

    @Test
    public void ShouldNotLogContinueConversation() {
        TranscriptStore transcriptStore = new MemoryTranscriptStore();
        TranscriptLoggerMiddleware sut = new TranscriptLoggerMiddleware(transcriptStore);

        String conversationId = UUID.randomUUID().toString();
        TestAdapter adapter = new TestAdapter(TestAdapter.createConversationReference(conversationId, "User1", "Bot"))
            .use(sut);

        Activity continueConversation = new Activity(ActivityTypes.EVENT);
        continueConversation.setName(ActivityEventNames.CONTINUE_CONVERSATION);

        new TestFlow(adapter, turnContext -> {
            return turnContext.sendActivity("bar").thenApply(resourceResponse -> null);
        })
            .send("foo")
            .assertReply(activity -> {
                Assert.assertEquals("bar", activity.getText());
                PagedResult<Activity> activities = transcriptStore.getTranscriptActivities(activity.getChannelId(), conversationId).join();
                Assert.assertEquals(2, activities.getItems().size());
            })
            .send(continueConversation)
            .assertReply(activity -> {
                // Ensure the event hasn't been added to the transcript.
                PagedResult<Activity> activities = transcriptStore.getTranscriptActivities(activity.getChannelId(), conversationId).join();

                Assert.assertFalse(activities.getItems().stream().anyMatch(a -> a.isType(ActivityTypes.EVENT) && StringUtils
                    .equals(a.getName(), ActivityEventNames.CONTINUE_CONVERSATION)));
                Assert.assertEquals(3, activities.getItems().size());
            })
            .startTest().join();
    }

    private static void getAppCredentialsAndAssertValues(
        TurnContext turnContext,
        String expectedAppId,
        String expectedScope,
        int credsCount
    ) {
        if (credsCount > 0) {
            Map<String, AppCredentials> credsCache =
                ((BotFrameworkAdapter) turnContext.getAdapter()).getCredentialsCache();
            AppCredentials creds = credsCache
                .get(BotFrameworkAdapter.keyForAppCredentials(expectedAppId, expectedScope));

            Assert
                .assertEquals("Unexpected credentials cache count", credsCount, credsCache.size());

            Assert.assertNotNull("Credentials not found", creds);
            Assert.assertEquals("Unexpected app id", expectedAppId, creds.getAppId());
            Assert.assertEquals("Unexpected scope", expectedScope, creds.oAuthScope());
        }
    }

    private static void getConnectorClientAndAssertValues(
        TurnContext turnContext,
        String expectedAppId,
        String expectedScope,
        String expectedUrl,
        int clientCount
    ) {
        Map<String, ConnectorClient> clientCache =
            ((BotFrameworkAdapter) turnContext.getAdapter()).getConnectorClientCache();

        String cacheKey = expectedAppId == null
            ? BotFrameworkAdapter.keyForConnectorClient(expectedUrl, null, null)
            : BotFrameworkAdapter.keyForConnectorClient(expectedUrl, expectedAppId, expectedScope);
        ConnectorClient client = clientCache.get(cacheKey);

        Assert.assertNotNull("ConnectorClient not in cache", client);
        Assert.assertEquals("Unexpected credentials cache count", clientCount, clientCache.size());
        AppCredentials creds = (AppCredentials) client.credentials();
        Assert.assertEquals(
            "Unexpected app id",
            expectedAppId,
            creds == null ? null : creds.getAppId()
        );
        Assert.assertEquals(
            "Unexpected scope",
            expectedScope,
            creds == null ? null : creds.oAuthScope()
        );
        Assert.assertEquals("Unexpected base url", expectedUrl, client.baseUrl());
    }

    @Test
    public void DeliveryModeExpectReplies() {
        BotFrameworkAdapter adapter = new BotFrameworkAdapter(new SimpleCredentialProvider());

        MockConnectorClient mockConnector = new MockConnectorClient("Windows/3.1", new MockAppCredentials("awesome"));
        adapter.addConnectorClientToCache("http://tempuri.org/whatever", null, null, mockConnector);

        BotCallbackHandler callback = turnContext -> {
            turnContext.sendActivity(MessageFactory.text("activity 1")).join();
            turnContext.sendActivity(MessageFactory.text("activity 2")).join();
            turnContext.sendActivity(MessageFactory.text("activity 3")).join();
            return CompletableFuture.completedFuture(null);
        };

        Activity inboundActivity = new Activity(ActivityTypes.MESSAGE);
        inboundActivity.setChannelId(Channels.EMULATOR);
        inboundActivity.setServiceUrl("http://tempuri.org/whatever");
        inboundActivity.setDeliveryMode(DeliveryModes.EXPECT_REPLIES.toString());
        inboundActivity.setText("hello world");

        InvokeResponse invokeResponse = adapter.processActivity((String) null, inboundActivity, callback).join();

        Assert.assertEquals((int) HttpURLConnection.HTTP_OK, invokeResponse.getStatus());
        List<Activity> activities = ((ExpectedReplies)invokeResponse.getBody()).getActivities();
        Assert.assertEquals(3, activities.size());
        Assert.assertEquals("activity 1", activities.get(0).getText());
        Assert.assertEquals("activity 2", activities.get(1).getText());
        Assert.assertEquals("activity 3", activities.get(2).getText());
        Assert.assertEquals(0, ((MemoryConversations) mockConnector.getConversations()).getSentActivities().size());
    }

    @Test
    public void DeliveryModeNormal() {
        BotFrameworkAdapter adapter = new BotFrameworkAdapter(new SimpleCredentialProvider());

        MockConnectorClient mockConnector = new MockConnectorClient("Windows/3.1", new MockAppCredentials("awesome"));
        adapter.addConnectorClientToCache("http://tempuri.org/whatever", null, null, mockConnector);

        BotCallbackHandler callback = turnContext -> {
            turnContext.sendActivity(MessageFactory.text("activity 1")).join();
            turnContext.sendActivity(MessageFactory.text("activity 2")).join();
            turnContext.sendActivity(MessageFactory.text("activity 3")).join();
            return CompletableFuture.completedFuture(null);
        };

        Activity inboundActivity = new Activity(ActivityTypes.MESSAGE);
        inboundActivity.setChannelId(Channels.EMULATOR);
        inboundActivity.setServiceUrl("http://tempuri.org/whatever");
        inboundActivity.setDeliveryMode(DeliveryModes.NORMAL.toString());
        inboundActivity.setText("hello world");
        inboundActivity.setConversation(new ConversationAccount("conversationId"));

        InvokeResponse invokeResponse = adapter.processActivity((String) null, inboundActivity, callback).join();

        Assert.assertNull(invokeResponse);
        Assert.assertEquals(3, ((MemoryConversations) mockConnector.getConversations()).getSentActivities().size());
    }

    // should be same as DeliverModes.NORMAL
    @Test
    public void DeliveryModeNull() {
        BotFrameworkAdapter adapter = new BotFrameworkAdapter(new SimpleCredentialProvider());

        MockConnectorClient mockConnector = new MockConnectorClient("Windows/3.1", new MockAppCredentials("awesome"));
        adapter.addConnectorClientToCache("http://tempuri.org/whatever", null, null, mockConnector);

        BotCallbackHandler callback = turnContext -> {
            turnContext.sendActivity(MessageFactory.text("activity 1")).join();
            turnContext.sendActivity(MessageFactory.text("activity 2")).join();
            turnContext.sendActivity(MessageFactory.text("activity 3")).join();
            return CompletableFuture.completedFuture(null);
        };

        Activity inboundActivity = new Activity(ActivityTypes.MESSAGE);
        inboundActivity.setChannelId(Channels.EMULATOR);
        inboundActivity.setServiceUrl("http://tempuri.org/whatever");
        inboundActivity.setText("hello world");
        inboundActivity.setConversation(new ConversationAccount("conversationId"));

        InvokeResponse invokeResponse = adapter.processActivity((String) null, inboundActivity, callback).join();

        Assert.assertNull(invokeResponse);
        Assert.assertEquals(3, ((MemoryConversations) mockConnector.getConversations()).getSentActivities().size());
    }
}
