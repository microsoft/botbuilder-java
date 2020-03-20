package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.schema.teams.TeamChannelData;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ActivityTest {
    @Test
    public void GetConversationReference() {
        Activity activity = createActivity();
        ConversationReference conversationReference = activity.getConversationReference();

        Assert.assertEquals(activity.getId(), conversationReference.getActivityId());
        Assert.assertEquals(activity.getFrom().getId(), conversationReference.getUser().getId());
        Assert.assertEquals(activity.getRecipient().getId(), conversationReference.getBot().getId());
        Assert.assertEquals(activity.getConversation().getId(), conversationReference.getConversation().getId());
        Assert.assertEquals(activity.getChannelId(), conversationReference.getChannelId());
        Assert.assertEquals(activity.getServiceUrl(), conversationReference.getServiceUrl());
    }

    @Test
    public void GetReplyConversationReference() {
        Activity activity = createActivity();

        ResourceResponse reply = new ResourceResponse() {{
            setId("1234");
        }};

        ConversationReference conversationReference = activity.getReplyConversationReference(reply);

        Assert.assertEquals(reply.getId(), conversationReference.getActivityId());
        Assert.assertEquals(activity.getFrom().getId(), conversationReference.getUser().getId());
        Assert.assertEquals(activity.getRecipient().getId(), conversationReference.getBot().getId());
        Assert.assertEquals(activity.getConversation().getId(), conversationReference.getConversation().getId());
        Assert.assertEquals(activity.getChannelId(), conversationReference.getChannelId());
        Assert.assertEquals(activity.getServiceUrl(), conversationReference.getServiceUrl());
    }

    @Test
    public void ApplyConversationReference_isIncoming() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference() {{
            setChannelId("cr_123");
            setServiceUrl("cr_serviceUrl");
            setConversation(new ConversationAccount(){{
                setId("cr_456");
            }});
            setUser(new ChannelAccount() {{
                setId("cr_abc");
            }});
            setBot(new ChannelAccount() {{
                setId("cr_def");
            }});
            setActivityId("cr_12345");
        }};

        activity.applyConversationReference(conversationReference, true);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getUser().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getBot().getId(), activity.getRecipient().getId());
        Assert.assertEquals(conversationReference.getActivityId(), activity.getId());
    }

    @Test
    public void ApplyConversationReference() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference() {{
            setChannelId("123");
            setServiceUrl("serviceUrl");
            setConversation(new ConversationAccount(){{
                setId("456");
            }});
            setUser(new ChannelAccount() {{
                setId("abc");
            }});
            setBot(new ChannelAccount() {{
                setId("def");
            }});
            setActivityId("12345");
        }};

        activity.applyConversationReference(conversationReference, false);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getBot().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getUser().getId(), activity.getRecipient().getId());
        Assert.assertEquals(conversationReference.getActivityId(), activity.getReplyToId());
    }

    @Test
    public void CreateTraceAllowsNullRecipient() {
        Activity activity = createActivity();
        activity.setRecipient(null);
        Activity trace = activity.createTrace("test");

        Assert.assertNull(trace.getFrom().getId());
    }

    private Activity createActivity() {
        ChannelAccount account1 = new ChannelAccount() {{
           setId("ChannelAccount_Id_1");
           setName("ChannelAccount_Name_1");
           setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
           setRole(RoleTypes.USER);
        }};

        ChannelAccount account2 = new ChannelAccount() {{
            setId("ChannelAccount_Id_2");
            setName("ChannelAccount_Name_2");
            setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
            setRole(RoleTypes.USER);
        }};

        ConversationAccount conversationAccount = new ConversationAccount() {{
            setConversationType("a");
            setId("123");
            setIsGroup(true);
            setName("Name");
            setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
        }};

        Activity activity = new Activity() {{
            setId("123");
            setFrom(account1);
            setRecipient(account2);
            setConversation(conversationAccount);
            setChannelId("ChannelId123");
            setServiceUrl("ServiceUrl123");
        }};

        return activity;
    }

    private static final String serializedActivity = "{\n"+
        "  \"attachments\": [],\n"+
        "  \"channelId\": \"directlinespeech\",\n"+
        "  \"conversation\":\n"+
        "  {\n"+
        "    \"id\": \"b18a1c99-7a29-4801-ac0c-579f2c36d52c\",\n"+
        "    \"isGroup\": false\n"+
        "  },\n"+
        "  \"entities\": [],\n"+
        "  \"from\":\n"+
        "  {\n"+
        "    \"id\": \"ConnectedCarAssistant\"\n"+
        "  },\n"+
        "  \"id\": \"9f90f0f5-be7d-410c-ad4a-5826751b26b1\",\n"+
        "  \"locale\": \"en-us\",\n"+
        "  \"name\": \"WebviewPreFetch\",\n"+
        "  \"recipient\":\n"+
        "  {\n"+
        "    \"id\": \"ef3de4593d4cc9b8\",\n"+
        "    \"role\": \"user\"\n"+
        "  },\n"+
        "  \"replyToId\": \"4d807515-46c1-44a1-b0f8-88457e3c13f2\",\n"+
        "  \"serviceUrl\": \"urn:botframework:websocket:directlinespeech\",\n"+
        "  \"text\": \"\",\n"+
        "  \"timestamp\": \"2019-11-14T17:50:06.8447816Z\",\n"+
        "  \"type\": \"event\",\n"+
        "  \"value\":\n"+
        "  {\n"+
        "    \"headers\":\n"+
        "    {\n"+
        "      \"opal-sessionid\": \"b18a1c99-7a29-4801-ac0c-579f2c36d52c\",\n"+
        "      \"x-Search-ClientId\": \"ef3de4593d4cc9b8\",\n"+
        "      \"x-Search-Market\": \"en-us\",\n"+
        "      \"x-Uqu-RefererType\": \"1\",\n"+
        "      \"x-Uqu-ResponseFormat\": \"0\"\n"+
        "    },\n"+
        "    \"uri\": \"https://www.bing.com/commit/v1?q=pull+down+the+driver+side&visualResponsePreference=0&uqurequestid=4D80751546C144A1B0F888457E3C13F2\",\n"+
        "    \"userAgent\": \"Mozilla/5.0 (Linux; Android 7.1.1; TB-8704V) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.11 Safari/537.36 BingMobileApp/36 BMABuild/Production BMAConfig/0\"\n"+
        "  }\n"+
        "}\n";

    @Test
    public void DeserializeActivity() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(this.serializedActivity, Activity.class);

        Assert.assertNotNull(activity.getTimestamp());
        Assert.assertEquals("b18a1c99-7a29-4801-ac0c-579f2c36d52c", activity.getConversation().getId());
        Assert.assertNotNull(activity.getValue());
    }

    private static final String serializedActivityFromTeams = "{" +
        " \"channelId\": \"msteams\"," +
        " \"channelData\": {" +
        "   \"teamsChannelId\": \"19:123cb42aa5a0a7e56f83@thread.skype\"," +
        "   \"teamsTeamId\": \"19:104f2cb42aa5a0a7e56f83@thread.skype\"," +
        "   \"channel\": {" +
        "     \"id\": \"19:4104f2cb42aa5a0a7e56f83@thread.skype\"," +
        "     \"name\": \"General\" " +
        "   }," +
        "   \"team\": {" +
        "     \"id\": \"19:aab4104f2cb42aa5a0a7e56f83@thread.skype\"," +
        "     \"name\": \"Kahoot\", " +
        "     \"aadGroupId\": \"0ac65971-e8a0-49a1-8d41-26089125ea30\"" +
        "   }," +
        "   \"notification\": {" +
        "     \"alert\": \"true\"" +
        "   }," +
        "   \"eventType\":\"teamMemberAdded\", " +
        "   \"tenant\": {" +
        "     \"id\": \"0-b827-4bb0-9df1-e02faba7ac20\"" +
        "   }" +
        " }" +
        "}";

    private static final String serializedActivityFromTeamsWithoutTeamsChannelIdorTeamId = "{" +
        " \"channelId\": \"msteams\"," +
        " \"channelData\": {" +
        "   \"channel\": {" +
        "     \"id\": \"channel_id\"," +
        "     \"name\": \"channel_name\" " +
        "   }," +
        "   \"team\": {" +
        "     \"id\": \"team_id\"," +
        "     \"name\": \"team_name\", " +
        "     \"aadGroupId\": \"aad_groupid\"" +
        "   }," +
        "   \"notification\": {" +
        "     \"alert\": \"true\"" +
        "   }," +
        "   \"eventType\":\"teamMemberAdded\", " +
        "   \"tenant\": {" +
        "     \"id\": \"tenant_id\"" +
        "   }" +
        " }" +
        "}";



    @Test
    public void GetInformationForMicrosoftTeams() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(ActivityTest.serializedActivityFromTeams, Activity.class);
        Assert.assertEquals("19:123cb42aa5a0a7e56f83@thread.skype", activity.teamsGetChannelId());
        Assert.assertEquals("19:104f2cb42aa5a0a7e56f83@thread.skype", activity.teamsGetTeamId());
        Assert.assertEquals(true, activity.isTeamsActivity());

        activity = objectMapper.readValue(
            ActivityTest.serializedActivityFromTeamsWithoutTeamsChannelIdorTeamId, Activity.class);

        Assert.assertEquals("channel_id", activity.teamsGetChannelId());
        Assert.assertEquals("team_id", activity.teamsGetTeamId());

        TeamChannelData teamsChannelData = activity.getChannelData(TeamChannelData.class);
        Assert.assertEquals("channel_id", teamsChannelData.getChannel().getId());
        Assert.assertEquals("channel_name", teamsChannelData.getChannel().getName());
        Assert.assertEquals("team_id", teamsChannelData.getTeam().getId());
        Assert.assertEquals("team_name", teamsChannelData.getTeam().getName());
        Assert.assertEquals("aad_groupid", teamsChannelData.getTeam().getAadGroupId());
        Assert.assertEquals(true, teamsChannelData.getNotification().getAlert());
        Assert.assertEquals("teamMemberAdded", teamsChannelData.getEventType());
        Assert.assertEquals("tenant_id", teamsChannelData.getTenant().getId());
    }

}
