package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ConversationResourceResponse;
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
        final String EventActivityName = "CreateConversation";

        // so we can provide a mock ConnectorClient.
        class TestBotFrameworkAdapter extends BotFrameworkAdapter {
            public TestBotFrameworkAdapter(CredentialProvider withCredentialProvider) {
                super(withCredentialProvider);
            }

            @Override
            protected CompletableFuture<ConnectorClient> getOrCreateConnectorClient(String serviceUrl,
                                                                                    AppCredentials usingAppCredentials) {
                Conversations conv = mock(Conversations.class);
                when(conv.createConversation(any())).thenReturn(CompletableFuture.completedFuture(new ConversationResourceResponse() {{
                    setActivityId(ActivityIdValue);
                    setId(ConversationIdValue);
                }}));

                ConnectorClient client = mock(ConnectorClient.class);
                when(client.getConversations()).thenReturn(conv);

                return CompletableFuture.completedFuture(client);
            }
        }

        CredentialProvider mockCredentialProvider = mock(CredentialProvider.class);
        BotFrameworkAdapter adapter = new TestBotFrameworkAdapter(mockCredentialProvider);

        ObjectNode channelData = JsonNodeFactory.instance.objectNode();
        channelData.set("tenant", JsonNodeFactory.instance.objectNode().set("id", JsonNodeFactory.instance.textNode(TenantIdValue)));

        Activity activity = new Activity("Test") {{
            setChannelId(Channels.MSTEAMS);
            setServiceUrl("https://fake.service.url");
            setChannelData(channelData);
            setConversation(new ConversationAccount() {{
                setTenantId(TenantIdValue);
            }});
        }};

        ConversationParameters parameters = new ConversationParameters() {{
           setActivity(new Activity() {{
               setChannelData(activity.getChannelData());
           }});
        }};

        ConversationReference reference = activity.getConversationReference();
        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(null, null);

        Activity[] newActivity = new Activity[] { null };
        BotCallbackHandler updateParameters = (turnContext -> {
            newActivity[0] = turnContext.getActivity();
            return CompletableFuture.completedFuture(null);
        });

        adapter.createConversation(activity.getChannelId(), activity.getServiceUrl(), credentials, parameters, updateParameters, reference).join();

        Assert.assertEquals(TenantIdValue, ((JsonNode) newActivity[0].getChannelData()).get("tenant").get("tenantId").textValue());
        Assert.assertEquals(ActivityIdValue, newActivity[0].getId());
        Assert.assertEquals(ConversationIdValue, newActivity[0].getConversation().getId());
        Assert.assertEquals(TenantIdValue, newActivity[0].getConversation().getTenantId());
        Assert.assertEquals(EventActivityName, newActivity[0].getName());
    }

    private Activity processActivity(String channelId, String channelDataTenantId, String conversationTenantId) {
        ClaimsIdentity mockClaims = new ClaimsIdentity("anonymous");
        CredentialProvider mockCredentials = new SimpleCredentialProvider();

        BotFrameworkAdapter sut = new BotFrameworkAdapter(mockCredentials);

        ObjectNode channelData = new ObjectMapper().createObjectNode();
        ObjectNode tenantId = new ObjectMapper().createObjectNode();
        tenantId.put("id", channelDataTenantId);
        channelData.set("tenant", tenantId);

        Activity[] activity = new Activity[] { null };
        sut.processActivity(
            mockClaims,
            new Activity("test") {{
                setChannelId(channelId);
                setServiceUrl("https://smba.trafficmanager.net/amer/");
                setChannelData(channelData);
                setConversation(new ConversationAccount() {{
                    setTenantId(conversationTenantId);
                }});
            }},
            (context) -> {
                activity[0] = context.getActivity();
                return CompletableFuture.completedFuture(null);
            }).join();

        return activity[0];
    }
}
