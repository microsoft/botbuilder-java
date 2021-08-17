// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import com.microsoft.bot.schema.teams.TeamsMeetingInfo;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

        activity.setType(ActivityTypes.CONVERSATION_UPDATE);
        conversationReference = activity.getConversationReference();
        Assert.assertNull(conversationReference.getActivityId());

    }

    @Test
    public void GetReplyConversationReference() {
        Activity activity = createActivity();

        ResourceResponse reply = new ResourceResponse();
        reply.setId("1234");

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

        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setChannelId("cr_123");
        conversationReference.setServiceUrl("cr_serviceUrl");
        ConversationAccount conversation = new ConversationAccount();
        conversation.setId("cr_456");
        conversationReference.setConversation(conversation);
        ChannelAccount userAccount = new ChannelAccount();
        userAccount.setId("cr_abc");
        conversationReference.setUser(userAccount);
        ChannelAccount botAccount = new ChannelAccount();
        botAccount.setId("cr_def");
        conversationReference.setBot(botAccount);
        conversationReference.setActivityId("cr_12345");
        // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
        // tests stay in English
        conversationReference.setLocale("en-uS");

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

        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setChannelId("123");
        conversationReference.setServiceUrl("serviceUrl");
        ConversationAccount conversation = new ConversationAccount();
        conversation.setId("456");
        conversationReference.setConversation(conversation);
        ChannelAccount userAccount = new ChannelAccount();
        userAccount.setId("abc");
        conversationReference.setUser(userAccount);
        ChannelAccount botAccount = new ChannelAccount();
        botAccount.setId("def");
        conversationReference.setBot(botAccount);
        conversationReference.setActivityId("12345");
        // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
        // tests stay in English
        conversationReference.setLocale("en-uS");

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
    public void ApplyConversationReferenceOverload() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setChannelId("123");
        conversationReference.setServiceUrl("serviceUrl");
        ConversationAccount conversation = new ConversationAccount();
        conversation.setId("456");
        conversationReference.setConversation(conversation);
        ChannelAccount userAccount = new ChannelAccount();
        userAccount.setId("abc");
        conversationReference.setUser(userAccount);
        ChannelAccount botAccount = new ChannelAccount();
        botAccount.setId("def");
        conversationReference.setBot(botAccount);
        conversationReference.setActivityId("12345");
        // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
        // tests stay in English
        conversationReference.setLocale("en-uS");

        activity.applyConversationReference(conversationReference);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals(conversationReference.getLocale(), activity.getLocale());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getBot().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getUser().getId(), activity.getRecipient().getId());
        Assert.assertEquals(conversationReference.getActivityId(), activity.getReplyToId());
    }

    @Test
    public void ApplyConversationReferenceOverloadAlternatePaths() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setChannelId("123");
        conversationReference.setServiceUrl("serviceUrl");
        ConversationAccount conversation = new ConversationAccount();
        conversation.setId("456");
        conversationReference.setConversation(conversation);
        ChannelAccount userAccount = new ChannelAccount();
        userAccount.setId("abc");
        conversationReference.setUser(userAccount);
        ChannelAccount botAccount = new ChannelAccount();
        botAccount.setId("def");
        conversationReference.setBot(botAccount);
        conversationReference.setActivityId(null);
        conversationReference.setLocale(null);

        activity.applyConversationReference(conversationReference, false);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals("en-uS", activity.getLocale());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getBot().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getUser().getId(), activity.getRecipient().getId());
        Assert.assertEquals(conversationReference.getActivityId(), activity.getReplyToId());

        activity.applyConversationReference(conversationReference, true);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals("en-uS", activity.getLocale());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getUser().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getBot().getId(), activity.getRecipient().getId());
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
        ChannelAccount account1 = new ChannelAccount();
        account1.setId("ChannelAccount_Id_1");
        account1.setName("ChannelAccount_Name_1");
        account1.setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
        account1.setRole(RoleTypes.USER);

        ChannelAccount account2 = new ChannelAccount();
        account2.setId("ChannelAccount_Id_2");
        account2.setName("ChannelAccount_Name_2");
        account2.setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
        account2.setRole(RoleTypes.USER);

        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setConversationType("a");
        conversationAccount.setId("123");
        conversationAccount.setIsGroup(true);
        conversationAccount.setName("Name");
        conversationAccount.setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));

        Activity activity = new Activity();
        activity.setId("123");
        activity.setFrom(account1);
        activity.setRecipient(account2);
        activity.setConversation(conversationAccount);
        activity.setChannelId("directline");
        // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
        // tests stay in English
        activity.setLocale("en-uS");
        activity.setServiceUrl("ServiceUrl123");

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

    private static final String serializedActivityFromTeamsWithoutNotificationTeamsChannelIdOrTeamId = "{ "
            + " \"channelId\": \"msteams\", \"channelData\": { \"channel\": { \"id\": \"channel_id\", \"name\": "
            + "\"channel_name\" }, \"team\": { \"id\": \"team_id\", \"name\": \"team_name\", \"aadGroupId\": "
            + "\"aad_groupid\" }, \"eventType\": \"teamMemberAdded\", \"tenant\": { \"id\": \"tenant_id\" } } }";

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
    public void GetTeamsChannelIdBadChannelData() {
        Activity activity = new Activity();
        activity.setChannelData("badChannelData");
        String channelId = activity.teamsGetChannelId();
        Assert.assertNull(channelId);
    }

    @Test
    public void GetTeamsTeamIdBadChannelData() {
        Activity activity = new Activity();
        activity.setChannelData("badChannelData");
        String channelId = activity.teamsGetTeamId();
        Assert.assertNull(channelId);
    }

    @Test
    public void GetTeamsTeamIdNullChannelData() {
        Activity activity = new Activity();
        String channelId = activity.teamsGetTeamId();
        Assert.assertNull(channelId);
    }

    @Test
    public void GetTeamsGetInfo() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Activity activity = objectMapper.readValue(
            ActivityTest.serializedActivityFromTeamsWithoutTeamsChannelIdorTeamId, Activity.class);

        TeamInfo teamsInfo = activity.teamsGetTeamInfo();
        Assert.assertNotNull(teamsInfo);
    }

    @Test
    public void GetTeamsGetInfoBadChannelData() {
        Activity activity = new Activity();
        activity.setChannelData("badChannelData");
        TeamInfo teamInfo = activity.teamsGetTeamInfo();
        Assert.assertNull(teamInfo);
    }
    @Test
    public void TeamsNotifyUser() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(
            ActivityTest.serializedActivityFromTeamsWithoutNotificationTeamsChannelIdOrTeamId, Activity.class);

        TeamsChannelData channelData = activity.teamsGetChannelData();
        Assert.assertNull(channelData.getNotification());
        activity.teamsNotifyUser();
        Assert.assertNotNull(activity.teamsGetChannelData().getNotification());
    }

    @Test
    public void TeamsNotifyUserBadChannelData() throws JsonProcessingException, IOException {
        Activity activity = new Activity();
        activity.setChannelData("badChannelData");

        TeamsChannelData channelData = activity.teamsGetChannelData();
        Assert.assertNull(channelData);
        activity.teamsNotifyUser();
        Assert.assertNotNull(activity.teamsGetChannelData().getNotification());
    }

    @Test
    public void TeamsNotifyUserAlertInMeeting() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(
            ActivityTest.serializedActivityFromTeamsWithoutNotificationTeamsChannelIdOrTeamId, Activity.class);

        TeamsChannelData channelData = activity.teamsGetChannelData();
        Assert.assertNull(channelData.getNotification());
        activity.teamsNotifyUser(true, "externalresourceURL");
        Assert.assertNotNull(activity.teamsGetChannelData().getNotification());
        Assert.assertEquals(activity.teamsGetChannelData().getNotification().getExternalResourceUrl(),
                            "externalresourceURL");
        Assert.assertTrue(activity.teamsGetChannelData().getNotification().getAlertInMeeting());
    }

    @Test
    public void TeamsNotifyUserAlertInMeetingBadChannelData() throws JsonProcessingException, IOException {
        Activity activity = new Activity();
        activity.setChannelData("badChannelData");

        Assert.assertNull(activity.teamsGetChannelData());
        activity.teamsNotifyUser(true, "externalresourceURL");
        Assert.assertNotNull(activity.teamsGetChannelData().getNotification());
        Assert.assertEquals(activity.teamsGetChannelData().getNotification().getExternalResourceUrl(),
                            "externalresourceURL");
        Assert.assertTrue(activity.teamsGetChannelData().getNotification().getAlertInMeeting());
    }


    @Test
    public void TeamsGetMeetingInfoNull() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Activity activity = objectMapper.readValue(
            ActivityTest.serializedActivityFromTeamsWithoutNotificationTeamsChannelIdOrTeamId, Activity.class);

        TeamsMeetingInfo meetingInfo = activity.teamsGetMeetingInfo();
        Assert.assertNull(meetingInfo);
    }

    @Test
    public void TeamsGetMeetingInfo() throws JsonProcessingException, IOException {
        Activity activity = new Activity();
        TeamsChannelData channelData = new TeamsChannelData();
        TeamsMeetingInfo meeting = new TeamsMeetingInfo();
        meeting.setId("meetingId");
        channelData.setMeeting(meeting);
        activity.setChannelData(channelData);

        TeamsMeetingInfo meetingInfo = activity.teamsGetMeetingInfo();
        Assert.assertNotNull(meetingInfo);
        Assert.assertEquals(meetingInfo.getId(), "meetingId");
    }

    @Test
    public void TeamsGetMeetingInfoBadChannelData() throws JsonProcessingException, IOException {
        Activity activity = new Activity();
        activity.setChannelData("badChannelData");

        TeamsMeetingInfo meetingInfo = activity.teamsGetMeetingInfo();
        Assert.assertNull(meetingInfo);
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

        Activity secondActivity = Activity.createTraceActivity(name);
        Assert.assertEquals(secondActivity.getType(), ActivityTypes.TRACE);
        Assert.assertEquals(secondActivity.getName(), name);

        Activity thirdActivity = Activity.createTraceActivity(name, null, value, label);
        Assert.assertEquals(thirdActivity.getType(), ActivityTypes.TRACE);
        Assert.assertEquals(thirdActivity.getName(), name);

        Assert.assertTrue(thirdActivity.isType(ActivityTypes.TRACE));
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

        activity.setFrom(null);
        activity.setRecipient(null);
        activity.setConversation(null);
        Activity reply2 = activity.createReply(text);
        Assert.assertEquals(reply2.getType(), ActivityTypes.MESSAGE);
        Assert.assertEquals(reply2.getText(), text);
        Assert.assertEquals(reply2.getLocale(), "en-uS");
        Assert.assertTrue(reply2.getFrom() != null);
        Assert.assertTrue(reply2.getRecipient() != null);
        Assert.assertTrue(reply2.getConversation() != null);
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
        activity.setChannelData("test-channelData");

        boolean result = activity.hasContent();

        Assert.assertEquals(result, true);
    }

    @Test
    public void GetMentions() {
        ArrayList<Entity> mentions = new ArrayList<Entity>();

        Entity mentionEntity = new Entity();
        mentionEntity.setType("mention");
        mentions.add(mentionEntity);
        Entity reactionEntity = new Entity();
        reactionEntity.setType("reaction");
        mentions.add(reactionEntity);

        Activity activity = createActivity();

        activity.setEntities(mentions);

        List<Mention> mentionsResult = activity.getMentions();

        Assert.assertEquals(mentionsResult.size(), 1);
        Assert.assertEquals(mentionsResult.get(0).getType(), "mention");
    }

    @Test
    public void GetMentionsNull() {
        Activity activity = createActivity();
        activity.setEntities(null);
        Assert.assertTrue(activity.getMentions() != null);
    }

    @Test
    public void CreateTraceForConversationUpdateActivity() {
        Activity activity = createActivity();
        activity.setType(ActivityTypes.CONVERSATION_UPDATE);
        Activity trace = activity.createTrace("test");
        Assert.assertNull(trace.getReplyToId());
    }

    @Test
    public void CreateReplyForConversationUpdateActivity() {
        Activity activity = createActivity();
        activity.setType(ActivityTypes.CONVERSATION_UPDATE);
        Activity reply = activity.createReply("test");
        Assert.assertNull(reply.getReplyToId());
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

        Activity secondActivity = createActivity();
        secondActivity.setRecipient(null);
        secondActivity.setFrom(null);
        Activity secondTrace = secondActivity.createTrace(name, value, null, label);
        Assert.assertEquals(secondTrace.getType(), ActivityTypes.TRACE);
        Assert.assertEquals(secondTrace.getName(), name);
        Assert.assertEquals(secondTrace.getValue(), value);
        Assert.assertEquals(secondTrace.getValueType(), value.getClass().getTypeName());
        Assert.assertEquals(secondTrace.getLabel(), label);
        Assert.assertTrue(secondTrace.getRecipient() != null);
        Assert.assertTrue(secondTrace.getFrom() != null);
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

    private JsonNode getTestNode() {
        String json = "{ \"item1\" : \"value1\" } ";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Test
    public void ActivityCloneTest() throws JsonProcessingException {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setAction("TestAction");

        Attachment attachment = new Attachment();
        attachment.setContentType("testContentType");
        attachment.setContentUrl("testContentUrl");
        attachment.setContent("testContent");
        attachment.setName("testName");
        attachment.setThumbnailUrl("testThumbnailUrl");
        attachment.setProperties("testProperty", getTestNode());
        activity.setAttachment(attachment);

        activity.setCallerId("testCallerId");
        activity.setChannelData("testChannelData");
        activity.setCode(EndOfConversationCodes.BOT_TIMED_OUT);

        ConversationAccount conversation = new ConversationAccount("testConversation");
        activity.setConversation(conversation);

        activity.setDeliveryMode("testDeliveryMode");

        List<Entity> entityList = new ArrayList<Entity>();
        Entity entity1 = new Entity();
        entity1.setType("testEntity");
        entityList.add(entity1);
        activity.setEntities(entityList);

        LocalDateTime expiration = LocalDateTime.now();
        activity.setExpiration(expiration);

        ChannelAccount fromChannel = new ChannelAccount("fromChannel");
        activity.setFrom(fromChannel);

        activity.setHistoryDisclosed(true);
        activity.setId("testId");
        activity.setImportance("testImportance");
        activity.setInputHint(InputHints.ACCEPTING_INPUT);
        activity.setLabel("testLabel");

        List<String> listen = new ArrayList<String>();
        listen.add("listen1");
        listen.add("listen2");
        activity.setListenFor(listen);

        activity.setLocalTimeZone("testLocalTimeZone");
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        activity.setLocalTimestamp(offsetDateTime);
        activity.setLocale("testLocale");

        List<ChannelAccount> membersAdded = new ArrayList<ChannelAccount>();
        ChannelAccount firstMember = new ChannelAccount("firstMember");
        ChannelAccount secondMember = new ChannelAccount("secondMember");
        membersAdded.add(firstMember);
        membersAdded.add(secondMember);
        activity.setMembersAdded(membersAdded);

        List<ChannelAccount> membersRemoved = new ArrayList<ChannelAccount>();
        ChannelAccount firstMemberRemoved = new ChannelAccount("firstMember");
        ChannelAccount secondMemberRemoved = new ChannelAccount("secondMember");
        membersRemoved.add(firstMemberRemoved);
        membersRemoved.add(secondMemberRemoved);
        activity.setMembersRemoved(membersRemoved);

        List<Mention> mentions = new ArrayList<Mention>();
        Mention firstMention = new Mention();
        firstMention.setText("testTest");
        firstMention.setMentioned(firstMember);
        Mention secondMention = new Mention();
        secondMention.setText("testTest");
        secondMention.setMentioned(firstMember);
        mentions.add(secondMention);
        activity.setMentions(mentions);

        activity.setName("testName");
        activity.setProperties("testProperty", getTestNode());

        List<MessageReaction> reactionsAdded = new ArrayList<MessageReaction>();
        MessageReaction firstReaction = new MessageReaction();
        firstReaction.setType("testType");
        reactionsAdded.add(firstReaction);
        MessageReaction secondReaction = new MessageReaction();
        secondReaction.setType("testType");
        reactionsAdded.add(secondReaction);
        activity.setReactionsAdded(reactionsAdded);

        List<MessageReaction> reactionsRemoved = new ArrayList<MessageReaction>();
        MessageReaction firstReactionRemoved = new MessageReaction();
        firstReactionRemoved.setType("testType");
        reactionsRemoved.add(firstReactionRemoved);
        MessageReaction secondReactionRemoved = new MessageReaction();
        secondReactionRemoved.setType("testType");
        reactionsRemoved.add(secondReactionRemoved);
        activity.setReactionsRemoved(reactionsRemoved);

        ChannelAccount recipientRemoved = new ChannelAccount();
        recipientRemoved.setId("testRecipient");
        activity.setRecipient(recipientRemoved);

        ConversationReference relatesToReference = new ConversationReference();
        relatesToReference.setActivityId("testActivityId");
        activity.setRelatesTo(relatesToReference);

        activity.setReplyToId("testReplyToId");
        activity.setServiceUrl("testServiceUrl");
        activity.setText("testText");
        activity.setTextFormat(TextFormatTypes.MARKDOWN);

        List<TextHighlight> textHighlights = new ArrayList<TextHighlight>();
        TextHighlight firstTextHighlight = new TextHighlight();
        firstTextHighlight.setText("testText");
        textHighlights.add(firstTextHighlight);
        TextHighlight secondTextHighlight = new TextHighlight();
        secondTextHighlight.setText("testText");
        textHighlights.add(secondTextHighlight);
        activity.setTextHighlights(textHighlights);

        OffsetDateTime timestamp = OffsetDateTime.now();
        activity.setTimestamp(timestamp);

        activity.setTopicName("testTopicName");
        activity.setType("testType");
        activity.setValue("testValue");
        activity.setValueType("testValueType");

        Activity clonedActivity = Activity.clone(activity);

        Assert.assertEquals(activity.getAction(), clonedActivity.getAction());
        Assert.assertEquals(activity.getCallerId(), clonedActivity.getCallerId());
        Assert.assertEquals(activity.getChannelData(), clonedActivity.getChannelData());
        Assert.assertEquals(activity.getDeliveryMode(), clonedActivity.getDeliveryMode());
        Assert.assertEquals(activity.getId(), clonedActivity.getId());
        Assert.assertEquals(activity.getImportance(), clonedActivity.getImportance());
        Assert.assertEquals(activity.getLabel(), clonedActivity.getLabel());
        Assert.assertEquals(activity.getLocalTimezone(), clonedActivity.getLocalTimezone());
        Assert.assertEquals(activity.getLocale(), clonedActivity.getLocale());
        Assert.assertEquals(activity.getName(), clonedActivity.getName());
        Assert.assertEquals(activity.getReplyToId(), clonedActivity.getReplyToId());
        Assert.assertEquals(activity.getServiceUrl(), clonedActivity.getServiceUrl());
        Assert.assertEquals(activity.getSpeak(), clonedActivity.getSpeak());
        Assert.assertEquals(activity.getSummary(), clonedActivity.getSummary());
        Assert.assertEquals(activity.getText(), clonedActivity.getText());
        Assert.assertEquals(activity.getTopicName(), clonedActivity.getTopicName());
        Assert.assertEquals(activity.getType(), clonedActivity.getType());
        Assert.assertEquals(activity.getValue(), clonedActivity.getValue());
        Assert.assertEquals(activity.getValueType(), clonedActivity.getValueType());
        Assert.assertEquals(activity.getAttachmentLayout(), clonedActivity.getAttachmentLayout());
        Assert.assertEquals(activity.getAttachments().get(0).getName(),
                            clonedActivity.getAttachments().get(0).getName());
        Assert.assertEquals(activity.getChannelData(ChannelAccount.class).getId(),
                            clonedActivity.getChannelData(ChannelAccount.class).getId());
        Assert.assertEquals(activity.getCode(), clonedActivity.getCode());
        Assert.assertEquals(activity.getConversation().getName(), clonedActivity.getConversation().getName());
        Assert.assertEquals(activity.getConversationReference().getChannelId(),
                            clonedActivity.getConversationReference().getChannelId());
        Assert.assertEquals(activity.getEntities().get(0).getType(), clonedActivity.getEntities().get(0).getType());
        Assert.assertEquals(activity.getExpiration(), clonedActivity.getExpiration());
        Assert.assertEquals(activity.getFrom().getId(), clonedActivity.getFrom().getId());
        Assert.assertEquals(activity.getInputHint(), clonedActivity.getInputHint());
        Assert.assertEquals(activity.getListenFor(), clonedActivity.getListenFor());
        Assert.assertEquals(activity.getLocalTimestamp(), clonedActivity.getLocalTimestamp());
        Assert.assertEquals(activity.getMembersAdded().get(0).getId(), clonedActivity.getMembersAdded().get(0).getId());
        Assert.assertEquals(activity.getMembersRemoved().get(0).getId(),
                            clonedActivity.getMembersRemoved().get(0).getId());
        Assert.assertEquals(activity.getMentions().get(0).getText(), clonedActivity.getMentions().get(0).getText());
        Assert.assertEquals(activity.getProperties(), clonedActivity.getProperties());
        Assert.assertEquals(activity.getReactionsAdded().get(0).getType(),
                            clonedActivity.getReactionsAdded().get(0).getType());
        Assert.assertEquals(activity.getReactionsRemoved().get(0).getType(),
                            clonedActivity.getReactionsRemoved().get(0).getType());
        Assert.assertEquals(activity.getRecipient().getId(), clonedActivity.getRecipient().getId());
        Assert.assertEquals(activity.getRelatesTo().getActivityId(), clonedActivity.getRelatesTo().getActivityId());
        // add activity.getReplyConversationReference(reply)
        Assert.assertEquals(activity.getSuggestedActions(), clonedActivity.getSuggestedActions());
        Assert.assertEquals(activity.getTextFormat(), clonedActivity.getTextFormat());
        Assert.assertEquals(activity.getTextHighlights(), clonedActivity.getTextHighlights());
        Assert.assertEquals(activity.getTimestamp(), clonedActivity.getTimestamp());
    }

    @Test
    public void EnsureCloneAddsIdIfMissing() {
        Activity testActivity = new Activity(ActivityTypes.COMMAND);
        Assert.assertTrue(testActivity.getId() == null);
        Activity clonedActivity = Activity.clone(testActivity);
        Assert.assertTrue(clonedActivity.getId() != null);
    }

    @Test
    public void TryGetChannelData() {
        Activity activity = createActivity();
        ResultPair<TeamsChannelData> channelData = activity.tryGetChannelData(
            TeamsChannelData.class
        );

        activity.setChannelData(new TeamsChannelData());
        channelData = activity.tryGetChannelData(
            TeamsChannelData.class
        );
        Assert.assertTrue(channelData.getLeft());

        activity.setChannelData(null);
        Assert.assertNull(activity.teamsGetChannelData());
    }

    @Test
    public void TryGetChannelDataBadChannelData() {
        Activity activity = createActivity();
        activity.setChannelData("badChannelData");
        ResultPair<TeamsChannelData> channelData = activity.tryGetChannelData(
            TeamsChannelData.class
        );
        Assert.assertFalse(channelData.getLeft());
        Assert.assertNull(channelData.getRight());
    }

    @Test
    public void RemoveRecipientMention() {
        Activity activity = createActivity();
        activity.setText("<at>firstName</at> lastName\n");
        String expectedStrippedName = "lastName";

        List<Mention> mentionList = new ArrayList<Mention>();
        Mention mention = new Mention();
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId(activity.getRecipient().getId());
        channelAccount.setName("firstName");
        mention.setMentioned(channelAccount);
        mentionList.add(mention);
        activity.setMentions(mentionList);

        String strippedActivityText = activity.removeRecipientMention();
        Assert.assertEquals(strippedActivityText, expectedStrippedName);
    }

    @Test
    public void RemoveRecipientMentionImmutable() {
        Activity activity = createActivity();
        activity.setText("<at>firstName</at> lastName\n");
        String expectedStrippedName = "lastName";

        List<Mention> mentionList = new ArrayList<Mention>();
        Mention mention = new Mention();
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId(activity.getRecipient().getId());
        channelAccount.setName("firstName");
        mention.setMentioned(channelAccount);
        mentionList.add(mention);
        activity.setMentions(mentionList);

        String strippedActivityText = Activity.removeRecipientMentionImmutable(activity);
        Assert.assertEquals(strippedActivityText, expectedStrippedName);
    }

    @Test
    public void RemoveRecipientMentionNoRecipient() {
        Activity activity = createActivity();
        activity.setText("<at>firstName</at> lastName\n");
        String expectedStrippedName = "<at>firstName</at> lastName\n";

        List<Mention> mentionList = new ArrayList<Mention>();
        Mention mention = new Mention();
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId(activity.getRecipient().getId());
        channelAccount.setName("firstName");
        mention.setMentioned(channelAccount);
        mentionList.add(mention);
        activity.setMentions(mentionList);
        activity.setRecipient(null);

        String strippedActivityText = activity.removeRecipientMention();
        Assert.assertEquals(strippedActivityText, expectedStrippedName);
    }

    @Test
    public void RemoveRecipientMentionImmutableNoRecipient() {
        Activity activity = createActivity();
        activity.setText("<at>firstName</at> lastName\n");
        String expectedStrippedName = "<at>firstName</at> lastName\n";

        List<Mention> mentionList = new ArrayList<Mention>();
        Mention mention = new Mention();
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId(activity.getRecipient().getId());
        channelAccount.setName("firstName");
        mention.setMentioned(channelAccount);
        mentionList.add(mention);
        activity.setMentions(mentionList);
        activity.setRecipient(null);

        String strippedActivityText = Activity.removeRecipientMentionImmutable(activity);
        Assert.assertEquals(strippedActivityText, expectedStrippedName);
    }

    @Test
    public void RemoveRecipientMentionText() {
        Activity activity = createActivity();
        activity.setText("<at>firstName</at> lastName\n");
        String expectedStrippedName = "<at>firstName</at>";

        List<Mention> mentionList = new ArrayList<Mention>();
        Mention mention = new Mention();
        mention.setText("lastName");
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId(activity.getRecipient().getId());
        channelAccount.setName("firstName");
        mention.setMentioned(channelAccount);
        mentionList.add(mention);
        activity.setMentions(mentionList);

        String strippedActivityText = activity.removeRecipientMention();
        Assert.assertEquals(strippedActivityText, expectedStrippedName);
    }

    @Test
    public void RemoveRecipientMentionTextNoId() {
        Activity activity = createActivity();
        activity.setText("<at>firstName</at> lastName\n");
        String expectedStrippedName = "<at>firstName</at> lastName\n";

        List<Mention> mentionList = new ArrayList<Mention>();
        Mention mention = new Mention();
        mention.setText("lastName");
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId(activity.getRecipient().getId());
        channelAccount.setName("firstName");
        mention.setMentioned(channelAccount);
        mentionList.add(mention);
        activity.setMentions(mentionList);

        String strippedActivityText = Activity.removeMentionTextImmutable(activity, null);
        Assert.assertEquals(strippedActivityText, expectedStrippedName);
    }

    @Test
    public void RemoveRecipientMentionTextNoText() {
        Activity activity = createActivity();
        activity.setText("");
        String expectedStrippedName = "";

        List<Mention> mentionList = new ArrayList<Mention>();
        Mention mention = new Mention();
        mention.setText("lastName");
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setId(activity.getRecipient().getId());
        channelAccount.setName("firstName");
        mention.setMentioned(channelAccount);
        mentionList.add(mention);
        activity.setMentions(mentionList);

        String strippedActivityText = Activity.removeMentionTextImmutable(activity, "lastName");
        Assert.assertEquals(strippedActivityText, expectedStrippedName);
    }


    @Test
    public void IsActivity() {
        class MyActivity extends Activity {
            @Override
            public boolean isActivity(String activityType) {
                return super.isActivity(activityType);
            }
        }

        MyActivity activity = new MyActivity();
        activity.setType(ActivityTypes.COMMAND);

        Assert.assertTrue(activity.isActivity(ActivityTypes.COMMAND));
    }

    @Test
    public void IsActivityNoType() {
        class MyActivity extends Activity {
            @Override
            public boolean isActivity(String activityType) {
                return super.isActivity(activityType);
            }
        }

        MyActivity activity = new MyActivity();

        Assert.assertFalse(activity.isActivity(ActivityTypes.COMMAND));
    }

    @Test
    public void IsActivityExtendedType() {
        class MyActivity extends Activity {
            @Override
            public boolean isActivity(String activityType) {
                return super.isActivity(activityType);
            }
        }

        MyActivity activity = new MyActivity();
        activity.setType("TestType/subtype");

        Assert.assertTrue(activity.isActivity("TestType"));
    }

    @Test
    public void IsActivityExtendedTypeNoMatch() {
        class MyActivity extends Activity {
            @Override
            public boolean isActivity(String activityType) {
                return super.isActivity(activityType);
            }
        }

        MyActivity activity = new MyActivity();
        activity.setType("TestTypesubtype");

        Assert.assertFalse(activity.isActivity("TestType"));
    }

    @Test
    public void IsActivityNoMatch() {
        class MyActivity extends Activity {
            @Override
            public boolean isActivity(String activityType) {
                return super.isActivity(activityType);
            }
        }

        MyActivity activity = new MyActivity();
        activity.setType("DifferentType");

        Assert.assertFalse(activity.isActivity("TestType"));
    }

    @Test
    public void IsActivityShorterTypeName() {
        class MyActivity extends Activity {
            @Override
            public boolean isActivity(String activityType) {
                return super.isActivity(activityType);
            }
        }

        MyActivity activity = new MyActivity();
        activity.setType("Test");

        Assert.assertFalse(activity.isActivity("TestType"));
    }
}
