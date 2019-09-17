package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationAccount;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

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
