// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.schema.teams.TeamsChannelData;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityTest {
    @Test
    public void GetConversationReference() {
        Activity activity = createActivity();
        ConversationReference conversationReference = activity.getConversationReference();

        Assert.assertEquals(activity.getId(), conversationReference.getActivityId());
        Assert.assertEquals(activity.getFrom().getId(), conversationReference.getUser().getId());
        Assert.assertEquals(activity.getRecipient().getId(), conversationReference.getBot().getId());
        Assert.assertEquals(activity.getConversation().getId(), conversationReference.getConversation().getId());
        Assert.assertEquals(activity.getLocale(), conversationReference.getLocale());
        Assert.assertEquals(activity.getChannelId(), conversationReference.getChannelId());
        Assert.assertEquals(activity.getServiceUrl(), conversationReference.getServiceUrl());
    }

    @Test
    public void GetReplyConversationReference() {
        Activity activity = createActivity();

        ResourceResponse reply = new ResourceResponse() {
            {
                setId("1234");
            }
        };

        ConversationReference conversationReference = activity.getReplyConversationReference(reply);

        Assert.assertEquals(reply.getId(), conversationReference.getActivityId());
        Assert.assertEquals(activity.getFrom().getId(), conversationReference.getUser().getId());
        Assert.assertEquals(activity.getRecipient().getId(), conversationReference.getBot().getId());
        Assert.assertEquals(activity.getConversation().getId(), conversationReference.getConversation().getId());
        Assert.assertEquals(activity.getLocale(), conversationReference.getLocale());
        Assert.assertEquals(activity.getChannelId(), conversationReference.getChannelId());
        Assert.assertEquals(activity.getServiceUrl(), conversationReference.getServiceUrl());
    }

    @Test
    public void ApplyConversationReference_isIncoming() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference() {
            {
                setChannelId("cr_123");
                setServiceUrl("cr_serviceUrl");
                setConversation(new ConversationAccount() {
                    {
                        setId("cr_456");
                    }
                });
                setUser(new ChannelAccount() {
                    {
                        setId("cr_abc");
                    }
                });
                setBot(new ChannelAccount() {
                    {
                        setId("cr_def");
                    }
                });
                setActivityId("cr_12345");
                setLocale("en-uS"); // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
                                    // tests stay in English
            }
        };

        activity.applyConversationReference(conversationReference, true);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals(conversationReference.getLocale(), activity.getLocale());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getUser().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getBot().getId(), activity.getRecipient().getId());
        Assert.assertEquals(conversationReference.getActivityId(), activity.getId());
    }

    @Test
    public void ApplyConversationReference() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference() {
            {
                setChannelId("123");
                setServiceUrl("serviceUrl");
                setConversation(new ConversationAccount() {
                    {
                        setId("456");
                    }
                });
                setUser(new ChannelAccount() {
                    {
                        setId("abc");
                    }
                });
                setBot(new ChannelAccount() {
                    {
                        setId("def");
                    }
                });
                setActivityId("12345");
                setLocale("en-uS"); // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
                                    // tests stay in English
            }
        };

        activity.applyConversationReference(conversationReference, false);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals(conversationReference.getLocale(), activity.getLocale());
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
        ChannelAccount account1 = new ChannelAccount() {
            {
                setId("ChannelAccount_Id_1");
                setName("ChannelAccount_Name_1");
                setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
                setRole(RoleTypes.USER);
            }
        };

        ChannelAccount account2 = new ChannelAccount() {
            {
                setId("ChannelAccount_Id_2");
                setName("ChannelAccount_Name_2");
                setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
                setRole(RoleTypes.USER);
            }
        };

        ConversationAccount conversationAccount = new ConversationAccount() {
            {
                setConversationType("a");
                setId("123");
                setIsGroup(true);
                setName("Name");
                setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
            }
        };

        Activity activity = new Activity() {
            {
                setId("123");
                setFrom(account1);
                setRecipient(account2);
                setConversation(conversationAccount);
                setChannelId("ChannelId123");
                setLocale("en-uS"); // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
                                    // tests stay in English
                setServiceUrl("ServiceUrl123");
            }
        };

        return activity;
    }

    private Attachment CreateAttachment() {
        Attachment attachment = new Attachment();
        attachment.setContent("test-content");
        attachment.setContentType("application/json");

        return attachment;
    }

    private static final String serializedActivity = "{\n" + "  \"attachments\": [],\n"
            + "  \"channelId\": \"directlinespeech\",\n" + "  \"conversation\":\n" + "  {\n"
            + "    \"id\": \"b18a1c99-7a29-4801-ac0c-579f2c36d52c\",\n" + "    \"isGroup\": false\n" + "  },\n"
            + "  \"entities\": [],\n" + "  \"from\":\n" + "  {\n" + "    \"id\": \"ConnectedCarAssistant\"\n" + "  },\n"
            + "  \"id\": \"9f90f0f5-be7d-410c-ad4a-5826751b26b1\",\n" + "  \"locale\": \"en-us\",\n"
            + "  \"name\": \"WebviewPreFetch\",\n" + "  \"recipient\":\n" + "  {\n"
            + "    \"id\": \"ef3de4593d4cc9b8\",\n" + "    \"role\": \"user\"\n" + "  },\n"
            + "  \"replyToId\": \"4d807515-46c1-44a1-b0f8-88457e3c13f2\",\n"
            + "  \"serviceUrl\": \"urn:botframework:websocket:directlinespeech\",\n" + "  \"text\": \"\",\n"
            + "  \"timestamp\": \"2019-11-14T17:50:06.8447816Z\",\n" + "  \"type\": \"event\",\n" + "  \"value\":\n"
            + "  {\n" + "    \"headers\":\n" + "    {\n"
            + "      \"opal-sessionid\": \"b18a1c99-7a29-4801-ac0c-579f2c36d52c\",\n"
            + "      \"x-Search-ClientId\": \"ef3de4593d4cc9b8\",\n" + "      \"x-Search-Market\": \"en-us\",\n"
            + "      \"x-Uqu-RefererType\": \"1\",\n" + "      \"x-Uqu-ResponseFormat\": \"0\"\n" + "    },\n"
            + "    \"uri\": \"https://www.bing.com/commit/v1?q=pull+down+the+driver+side&visualResponsePreference=0&uqurequestid=4D80751546C144A1B0F888457E3C13F2\",\n"
            + "    \"userAgent\": \"Mozilla/5.0 (Linux; Android 7.1.1; TB-8704V) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.11 Safari/537.36 BingMobileApp/36 BMABuild/Production BMAConfig/0\"\n"
            + "  }\n" + "}\n";

    @Test
    public void DeserializeActivity() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(this.serializedActivity, Activity.class);

        Assert.assertNotNull(activity.getTimestamp());
        Assert.assertEquals("b18a1c99-7a29-4801-ac0c-579f2c36d52c", activity.getConversation().getId());
        Assert.assertNotNull(activity.getValue());
    }

    private static final String serializedActivityWithDifferentTimeZone = "{\n" + "  \"attachments\": [],\n"
        + "  \"channelId\": \"directlinespeech\",\n" + "  \"conversation\":\n" + "  {\n"
        + "    \"id\": \"b18a1c99-7a29-4801-ac0c-579f2c36d52c\",\n" + "    \"isGroup\": false\n" + "  },\n"
        + "  \"entities\": [],\n" + "  \"from\":\n" + "  {\n" + "    \"id\": \"ConnectedCarAssistant\"\n" + "  },\n"
        + "  \"id\": \"9f90f0f5-be7d-410c-ad4a-5826751b26b1\",\n" + "  \"locale\": \"en-us\",\n"
        + "  \"name\": \"WebviewPreFetch\",\n" + "  \"recipient\":\n" + "  {\n"
        + "    \"id\": \"ef3de4593d4cc9b8\",\n" + "    \"role\": \"user\"\n" + "  },\n"
        + "  \"replyToId\": \"4d807515-46c1-44a1-b0f8-88457e3c13f2\",\n"
        + "  \"serviceUrl\": \"urn:botframework:websocket:directlinespeech\",\n" + "  \"text\": \"\",\n"
        + "  \"timestamp\": \"2019-11-14T17:50:06.8447816+02:00\",\n" + "  \"type\": \"event\",\n" + "  \"value\":\n"
        + "  {\n" + "    \"headers\":\n" + "    {\n"
        + "      \"opal-sessionid\": \"b18a1c99-7a29-4801-ac0c-579f2c36d52c\",\n"
        + "      \"x-Search-ClientId\": \"ef3de4593d4cc9b8\",\n" + "      \"x-Search-Market\": \"en-us\",\n"
        + "      \"x-Uqu-RefererType\": \"1\",\n" + "      \"x-Uqu-ResponseFormat\": \"0\"\n" + "    },\n"
        + "    \"uri\": \"https://www.bing.com/commit/v1?q=pull+down+the+driver+side&visualResponsePreference=0&uqurequestid=4D80751546C144A1B0F888457E3C13F2\",\n"
        + "    \"userAgent\": \"Mozilla/5.0 (Linux; Android 7.1.1; TB-8704V) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.11 Safari/537.36 BingMobileApp/36 BMABuild/Production BMAConfig/0\"\n"
        + "  }\n" + "}\n";

    @Test
    public void DeserializeActivityWithDifferentTimeZone() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(this.serializedActivityWithDifferentTimeZone, Activity.class);

        Assert.assertNotNull(activity.getTimestamp());
        Assert.assertEquals("b18a1c99-7a29-4801-ac0c-579f2c36d52c", activity.getConversation().getId());
        Assert.assertNotNull(activity.getValue());
    }

    private static final String serializedActivityFromTeams = "{" + " \"channelId\": \"msteams\","
            + " \"channelData\": {" + "   \"teamsChannelId\": \"19:123cb42aa5a0a7e56f83@thread.skype\","
            + "   \"teamsTeamId\": \"19:104f2cb42aa5a0a7e56f83@thread.skype\"," + "   \"channel\": {"
            + "     \"id\": \"19:4104f2cb42aa5a0a7e56f83@thread.skype\"," + "     \"name\": \"General\" " + "   },"
            + "   \"team\": {" + "     \"id\": \"19:aab4104f2cb42aa5a0a7e56f83@thread.skype\","
            + "     \"name\": \"Kahoot\", " + "     \"aadGroupId\": \"0ac65971-e8a0-49a1-8d41-26089125ea30\"" + "   },"
            + "   \"notification\": {" + "     \"alert\": \"true\"" + "   }," + "   \"eventType\":\"teamMemberAdded\", "
            + "   \"tenant\": {" + "     \"id\": \"0-b827-4bb0-9df1-e02faba7ac20\"" + "   }" + " }" + "}";

    private static final String serializedActivityFromTeamsWithoutTeamsChannelIdorTeamId = "{"
            + " \"channelId\": \"msteams\"," + " \"channelData\": {" + "   \"channel\": {"
            + "     \"id\": \"channel_id\"," + "     \"name\": \"channel_name\" " + "   }," + "   \"team\": {"
            + "     \"id\": \"team_id\"," + "     \"name\": \"team_name\", " + "     \"aadGroupId\": \"aad_groupid\""
            + "   }," + "   \"notification\": {" + "     \"alert\": \"true\"" + "   },"
            + "   \"eventType\":\"teamMemberAdded\", " + "   \"tenant\": {" + "     \"id\": \"tenant_id\"" + "   }"
            + " }" + "}";

    @Test
    public void GetInformationForMicrosoftTeams() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(ActivityTest.serializedActivityFromTeams, Activity.class);
        Assert.assertEquals("19:123cb42aa5a0a7e56f83@thread.skype", activity.teamsGetChannelId());
        Assert.assertEquals("19:104f2cb42aa5a0a7e56f83@thread.skype", activity.teamsGetTeamId());
        Assert.assertEquals(true, activity.isTeamsActivity());

        activity = objectMapper.readValue(ActivityTest.serializedActivityFromTeamsWithoutTeamsChannelIdorTeamId,
                Activity.class);

        Assert.assertEquals("channel_id", activity.teamsGetChannelId());
        Assert.assertEquals("team_id", activity.teamsGetTeamId());

        TeamsChannelData teamsChannelData = activity.getChannelData(TeamsChannelData.class);
        Assert.assertEquals("channel_id", teamsChannelData.getChannel().getId());
        Assert.assertEquals("channel_name", teamsChannelData.getChannel().getName());
        Assert.assertEquals("team_id", teamsChannelData.getTeam().getId());
        Assert.assertEquals("team_name", teamsChannelData.getTeam().getName());
        Assert.assertEquals("aad_groupid", teamsChannelData.getTeam().getAadGroupId());
        Assert.assertEquals(true, teamsChannelData.getNotification().getAlert());
        Assert.assertEquals("teamMemberAdded", teamsChannelData.getEventType());
        Assert.assertEquals("tenant_id", teamsChannelData.getTenant().getId());
    }

    @Test
    public void CreateMessageActivity() {
        Activity activity = Activity.createMessageActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.MESSAGE);
    }

    @Test
    public void CreateContactRelationUpdateActivity() {
        Activity activity = Activity.createContactRelationUpdateActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.CONTACT_RELATION_UPDATE);
    }

    @Test
    public void CreateConversationUpdateActivity() {
        Activity activity = Activity.createConversationUpdateActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.CONVERSATION_UPDATE);
    }

    @Test
    public void CreateTypingActivity() {
        Activity activity = Activity.createTypingActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.TYPING);
    }

    @Test
    public void CreateHandoffActivity() {
        Activity activity = Activity.createHandoffActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.HANDOFF);
    }

    @Test
    public void CreateEndOfConversationActivity() {
        Activity activity = Activity.createEndOfConversationActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.END_OF_CONVERSATION);
    }

    @Test
    public void CreateEventActivity() {
        Activity activity = Activity.createEventActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.EVENT);
    }

    @Test
    public void CreateInvokeActivity() {
        Activity activity = Activity.createInvokeActivity();

        Assert.assertEquals(activity.getType(), ActivityTypes.INVOKE);
    }

    @Test
    public void CreateTraceActivity() {
        String name = "test-activity";
        String valueType = "string";
        String value = "test-value";
        String label = "test-label";

        Activity activity = Activity.createTraceActivity(name, valueType, value, label);

        Assert.assertEquals(activity.getType(), ActivityTypes.TRACE);
        Assert.assertEquals(activity.getName(), name);
        Assert.assertEquals(activity.getValueType(), valueType);
        Assert.assertEquals(activity.getValue(), value);
        Assert.assertEquals(activity.getLabel(), label);
    }

    @Test
    public void CreateTraceActivityWithoutValueType() {
        String name = "test-activity";
        String value = "test-value";
        String label = "test-label";

        Activity activity = Activity.createTraceActivity(name, null, value, label);

        Assert.assertEquals(activity.getType(), ActivityTypes.TRACE);
        Assert.assertEquals(activity.getValueType(), value.getClass().getTypeName());
        Assert.assertEquals(activity.getLabel(), label);
    }

    @Test
    public void CreateReply() {
        Activity activity = createActivity();

        String text = "test-reply";
        String locale = "en-us";

        Activity reply = activity.createReply(text, locale);

        Assert.assertEquals(reply.getType(), ActivityTypes.MESSAGE);
        Assert.assertEquals(reply.getText(), text);
        Assert.assertEquals(reply.getLocale(), locale);
    }

    @Test
    public void CreateReplyWithoutArguments() {
        Activity activity = createActivity();

        Activity reply = activity.createReply();

        Assert.assertEquals(reply.getType(), ActivityTypes.MESSAGE);
        Assert.assertEquals(reply.getText(), "");
        Assert.assertEquals(reply.getLocale(), activity.getLocale());
    }

    @Test
    public void HasContentIsFalseWhenActivityTextHasNoContent() {
        Activity activity = createActivity();

        boolean result = activity.hasContent();

        Assert.assertEquals(result, false);
    }

    @Test
    public void HasContentIsTrueWhenActivityTextHasContent() {
        Activity activity = createActivity();

        activity.setText("test-text");

        boolean result = activity.hasContent();

        Assert.assertEquals(result, true);
    }

    @Test
    public void HasContentIsTrueWhenActivitySummaryContent() {
        Activity activity = createActivity();

        activity.setText(null);
        activity.setSummary("test-summary");

        boolean result = activity.hasContent();

        Assert.assertEquals(result, true);
    }

    @Test
    public void HasContentIsTrueWhenActivityAttachmentsHaveContent() {
        Activity activity = createActivity();
        ArrayList<Attachment> attachments = new ArrayList<>();
        attachments.add(CreateAttachment());

        activity.setText(null);
        activity.setSummary(null);
        activity.setAttachments(attachments);

        boolean result = activity.hasContent();

        Assert.assertEquals(result, true);
    }

    @Test
    public void HasContentIsTrueWhenActivityChannelDataHasContent() {
        Activity activity = createActivity();

        activity.setText(null);
        activity.setSummary(null);
        activity.setAttachments(null);
        activity.setChannelData("test-channelData");

        boolean result = activity.hasContent();

        Assert.assertEquals(result, true);
    }

    @Test
    public void GetMentions() {
        ArrayList<Entity> mentions = new ArrayList<Entity>();

        mentions.add(new Entity() {
            {
                setType("mention");
            }
        });
        mentions.add(new Entity() {
            {
                setType("reaction");
            }
        });

        Activity activity = createActivity();

        activity.setEntities(mentions);

        List<Mention> mentionsResult = activity.getMentions();

        Assert.assertEquals(mentionsResult.size(), 1);
        Assert.assertEquals(mentionsResult.get(0).getType(), "mention");
    }

    @Test
    public void CreateTrace() {
        Activity activity = createActivity();

        String name = "test-activity";
        String value = "test-value";
        String valueType = "string";
        String label = "test-label";

        Activity trace = activity.createTrace(name, value, valueType, label);

        Assert.assertEquals(trace.getType(), ActivityTypes.TRACE);
        Assert.assertEquals(trace.getName(), name);
        Assert.assertEquals(trace.getValue(), value);
        Assert.assertEquals(trace.getValueType(), valueType);
        Assert.assertEquals(trace.getLabel(), label);
    }

    @Test
    public void IsFromStreamingConnection() {
        ArrayList<String> nonStreaming = new ArrayList<>();
        nonStreaming.add("http://yayay.com");
        nonStreaming.add("https://yayay.com");
        nonStreaming.add("HTTP://yayay.com");
        nonStreaming.add("HTTPS://yayay.com");

        ArrayList<String> streaming = new ArrayList<>();
        streaming.add("urn:botframework:WebSocket:wss://beep.com");
        streaming.add("urn:botframework:WebSocket:http://beep.com");
        streaming.add("URN:botframework:WebSocket:wss://beep.com");
        streaming.add("URN:botframework:WebSocket:http://beep.com");

        Activity activity = createActivity();
        activity.setServiceUrl(null);

        Assert.assertFalse(activity.isFromStreamingConnection());

        nonStreaming.forEach(s ->
        {
            activity.setServiceUrl(s);
            Assert.assertFalse(activity.isFromStreamingConnection());
        });

        streaming.forEach(s ->
        {
            activity.setServiceUrl(s);
            Assert.assertTrue(activity.isFromStreamingConnection());
        });
    }
}
