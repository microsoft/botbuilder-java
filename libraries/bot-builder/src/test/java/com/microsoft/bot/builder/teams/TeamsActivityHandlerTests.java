// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.SimpleAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.teams.AppBasedLinkQuery;
import com.microsoft.bot.schema.teams.ChannelInfo;
import com.microsoft.bot.schema.teams.FileConsentCardResponse;
import com.microsoft.bot.schema.teams.FileUploadInfo;
import com.microsoft.bot.schema.teams.MeetingEndEventDetails;
import com.microsoft.bot.schema.teams.MeetingStartEventDetails;
import com.microsoft.bot.schema.teams.MessagingExtensionAction;
import com.microsoft.bot.schema.teams.MessagingExtensionActionResponse;
import com.microsoft.bot.schema.teams.MessagingExtensionQuery;
import com.microsoft.bot.schema.teams.MessagingExtensionResponse;
import com.microsoft.bot.schema.teams.O365ConnectorCardActionQuery;
import com.microsoft.bot.schema.teams.TaskModuleRequest;
import com.microsoft.bot.schema.teams.TaskModuleRequestContext;
import com.microsoft.bot.schema.teams.TaskModuleResponse;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import java.io.IOException;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class TeamsActivityHandlerTests {
    @Test
    public void TestConversationUpdateBotTeamsMemberAdded() {
        String baseUri = "https://test.coffee";
        ConnectorClient connectorClient = getConnectorClient(
            "http://localhost/",
            MicrosoftAppCredentials.empty()
        );

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("botid-1"));
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("botid-1"));
        TeamsChannelData channelData = new TeamsChannelData();
        channelData.setEventType("teamMemberAdded");
        channelData.setTeam(new TeamInfo("team-id"));
        activity.setChannelData(channelData);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMembersAdded", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsMemberAdded() {
        String baseUri = "https://test.coffee";
        ConnectorClient connectorClient = getConnectorClient(
            "http://localhost/",
            MicrosoftAppCredentials.empty()
        );

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("id-1"));
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("b"));
        TeamsChannelData channelData = new TeamsChannelData();
        channelData.setEventType("teamMemberAdded");
        channelData.setTeam(new TeamInfo("team-id"));
        activity.setChannelData(channelData);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMembersAdded", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsMemberAddedNoTeam() {
        String baseUri = "https://test.coffee";
        ConnectorClient connectorClient = getConnectorClient(
            "http://localhost/",
            MicrosoftAppCredentials.empty()
        );

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("id-1"));
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("b"));
        activity.setConversation(new ConversationAccount("conversation-id"));
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMembersAdded", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsMemberAddedFullDetailsInEvent() {
        String baseUri = "https://test.coffee";
        ConnectorClient connectorClient = getConnectorClient(
            "http://localhost/",
            MicrosoftAppCredentials.empty()
        );

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        TeamsChannelAccount teams = new TeamsChannelAccount();
        teams.setId("id-1");
        teams.setName("name-1");
        teams.setAadObjectId("aadobject-1");
        teams.setEmail("test@microsoft.com");
        teams.setGivenName("given-1");
        teams.setSurname("surname-1");
        teams.setUserPrincipalName("t@microsoft.com");
        teams.setTenantId("testTenantId");
        teams.setUserRole("guest");
        members.add(teams);
        activity.setMembersAdded(members);
        activity.setRecipient(new ChannelAccount("b"));
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamMemberAdded");
        data.setTeam(new TeamInfo("team-id"));
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        // serialize to json and back to verify we can get back to the
        // correct Activity. i.e., In this case, mainly the TeamsChannelAccount.
        try {
            JacksonAdapter jacksonAdapter = new JacksonAdapter();
            String json = jacksonAdapter.serialize(activity);
            activity = jacksonAdapter.deserialize(json, Activity.class);
        } catch (Throwable t) {
            Assert.fail("Should not have thrown in serialization test.");
        }

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMembersAdded", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsMemberRemoved() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        ArrayList<ChannelAccount> members = new ArrayList<ChannelAccount>();
        members.add(new ChannelAccount("a"));
        activity.setMembersRemoved(members);
        activity.setRecipient(new ChannelAccount("b"));
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamMemberRemoved");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMembersRemoved", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelCreated() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("channelCreated");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelCreated", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelDeleted() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("channelDeleted");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelDeleted", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelRenamed() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("channelRenamed");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelRenamed", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelRestored() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("channelRestored");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelRestored", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamRenamed() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamRenamed");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamRenamed", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamArchived() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamArchived");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamArchived", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamDeleted() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamDeleted");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamDeleted", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamHardDeleted() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamHardDeleted");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamHardDeleted", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamRestored() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamRestored");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamRestored", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamUnarchived() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        TeamsChannelData data = new TeamsChannelData();
        data.setEventType("teamUnarchived");
        activity.setChannelData(data);
        activity.setChannelId(Channels.MSTEAMS);

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamUnarchived", bot.record.get(1));
    }

    @Test
    public void TestFileConsentAccept() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("fileConsent/invoke");
        FileUploadInfo fileInfo = new FileUploadInfo();
        fileInfo.setUniqueId("uniqueId");
        fileInfo.setFileType("fileType");
        fileInfo.setUploadUrl("uploadUrl");
        FileConsentCardResponse response = new FileConsentCardResponse();
        response.setAction("accept");
        response.setUploadInfo(fileInfo);
        activity.setValue(response);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(3, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsFileConsent", bot.record.get(1));
        Assert.assertEquals("onTeamsFileConsentAccept", bot.record.get(2));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestFileConsentDecline() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("fileConsent/invoke");
        FileUploadInfo fileInfo = new FileUploadInfo();
        fileInfo.setUniqueId("uniqueId");
        fileInfo.setFileType("fileType");
        fileInfo.setUploadUrl("uploadUrl");
        FileConsentCardResponse response = new FileConsentCardResponse();
        response.setAction("decline");
        response.setUploadInfo(fileInfo);
        activity.setValue(response);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(3, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsFileConsent", bot.record.get(1));
        Assert.assertEquals("onTeamsFileConsentDecline", bot.record.get(2));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestActionableMessageExecuteAction() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("actionableMessage/executeAction");
        activity.setValue(new O365ConnectorCardActionQuery());

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsO365ConnectorCardAction", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestComposeExtensionQueryLink() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/queryLink");
        activity.setValue(new AppBasedLinkQuery());

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsAppBasedLinkQuery", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestComposeExtensionQuery() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/query");
        activity.setValue(new MessagingExtensionQuery());

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMessagingExtensionQuery", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionSelectItemAsync() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/selectItem");
        activity.setValue(new MessagingExtensionQuery());

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMessagingExtensionSelectItem", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionSubmitAction() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        activity.setValue(new MessagingExtensionQuery());

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(3, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMessagingExtensionSubmitActionDispatch", bot.record.get(1));
        Assert.assertEquals("onTeamsMessagingExtensionSubmitAction", bot.record.get(2));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionSubmitActionPreviewActionEdit() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setBotMessagePreviewAction("edit");
        activity.setValue(action);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(3, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMessagingExtensionSubmitActionDispatch", bot.record.get(1));
        Assert.assertEquals("onTeamsMessagingExtensionBotMessagePreviewEdit", bot.record.get(2));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionSubmitActionPreviewActionSend() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setBotMessagePreviewAction("send");
        activity.setValue(action);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(3, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMessagingExtensionSubmitActionDispatch", bot.record.get(1));
        Assert.assertEquals("onTeamsMessagingExtensionBotMessagePreviewSend", bot.record.get(2));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionFetchTask() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/fetchTask");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setCommandId("testCommand");
        activity.setValue(action);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMessagingExtensionFetchTask", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionConfigurationQuerySettingUrl() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/querySettingUrl");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setCommandId("testCommand");
        activity.setValue(action);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals(
            "onTeamsMessagingExtensionConfigurationQuerySettingUrl",
            bot.record.get(1)
        );
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionConfigurationSetting() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/setting");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setCommandId("testCommand");
        activity.setValue(action);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMessagingExtensionConfigurationSetting", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestTaskModuleFetch() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("task/fetch");
        TaskModuleRequestContext context = new TaskModuleRequestContext();
        context.setTheme("default");
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("key", "value");
        data.put("type", "task / fetch");
        TaskModuleRequest request = new TaskModuleRequest();
        request.setData(data);
        request.setContext(context);
        activity.setValue(request);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTaskModuleFetch", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestTaskModuleSubmit() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("task/submit");
        TaskModuleRequestContext context = new TaskModuleRequestContext();
        context.setTheme("default");
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("key", "value");
        data.put("type", "task / fetch");
        TaskModuleRequest request = new TaskModuleRequest();
        request.setData(data);
        request.setContext(context);
        activity.setValue(request);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTaskModuleSubmit", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestSigninVerifyState() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("signin/verifyState");

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onInvokeActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsSigninVerifyState", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            200,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestOnEventActivity() {
        // Arrange
        Activity activity = new Activity(ActivityTypes.EVENT);
        activity.setChannelId(Channels.DIRECTLINE);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);

        // Act
        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        // Assert
        Assert.assertEquals(1, bot.record.size());
        Assert.assertEquals("onEventActivity", bot.record.get(0));
    }

    @Test
    public void TestMeetingStartEvent() throws IOException {
        // Arrange
        Activity activity = new Activity(ActivityTypes.EVENT);
        activity.setChannelId(Channels.MSTEAMS);
        activity.setName("application/vnd.microsoft.meetingStart");
        activity.setValue(Serialization.jsonToTree("{\"StartTime\": \"2021-06-05T00:01:02.0Z\"}"));

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        // Act
        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        // Assert
        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onEventActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMeetingStart", bot.record.get(1));

        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getText().contains("00:01:02"));
    }

    @Test
    public void TestMeetingEndEvent() throws IOException {
        // Arrange
        Activity activity = new Activity(ActivityTypes.EVENT);
        activity.setChannelId(Channels.MSTEAMS);
        activity.setName("application/vnd.microsoft.meetingEnd");
        activity.setValue(Serialization.jsonToTree("{\"EndTime\": \"2021-06-05T01:02:03.0Z\"}"));

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        // Act
        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        // Assert
        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onEventActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMeetingEnd", bot.record.get(1));
        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getText().contains("1:02:03"));
    }

    private static class NotImplementedAdapter extends BotAdapter {

        @Override
        public CompletableFuture<ResourceResponse[]> sendActivities(
            TurnContext context,
            List<Activity> activities
        ) {
            CompletableFuture<ResourceResponse[]> result = new CompletableFuture<>();
            result.completeExceptionally(new NotImplementedException("sendActivities"));
            return result;
        }

        @Override
        public CompletableFuture<ResourceResponse> updateActivity(
            TurnContext context,
            Activity activity
        ) {
            CompletableFuture<ResourceResponse> result = new CompletableFuture<>();
            result.completeExceptionally(new NotImplementedException("updateActivity"));
            return result;
        }

        @Override
        public CompletableFuture<Void> deleteActivity(
            TurnContext context,
            ConversationReference reference
        ) {
            CompletableFuture<Void> result = new CompletableFuture<>();
            result.completeExceptionally(new NotImplementedException("deleteActivity"));
            return result;
        }
    }

    private static class TestActivityHandler extends TeamsActivityHandler {
        public List<String> record = new ArrayList<>();

        @Override
        protected CompletableFuture<InvokeResponse> onInvokeActivity(TurnContext turnContext) {
            record.add("onInvokeActivity");
            return super.onInvokeActivity(turnContext);
        }

        @Override
        protected CompletableFuture<InvokeResponse> onTeamsCardActionInvoke(
            TurnContext turnContext
        ) {
            record.add("onTeamsCardActionInvoke");
            return super.onTeamsCardActionInvoke(turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsSigninVerifyState(TurnContext turnContext) {
            record.add("onTeamsSigninVerifyState");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<InvokeResponse> onTeamsFileConsent(
            TurnContext turnContext,
            FileConsentCardResponse fileConsentCardResponse
        ) {
            record.add("onTeamsFileConsent");
            return super.onTeamsFileConsent(turnContext, fileConsentCardResponse);
        }

        @Override
        protected CompletableFuture<Void> onTeamsFileConsentAccept(
            TurnContext turnContext,
            FileConsentCardResponse fileConsentCardResponse
        ) {
            record.add("onTeamsFileConsentAccept");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onTeamsFileConsentDecline(
            TurnContext turnContext,
            FileConsentCardResponse fileConsentCardResponse
        ) {
            record.add("onTeamsFileConsentDecline");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionQuery(
            TurnContext turnContext,
            MessagingExtensionQuery query
        ) {
            record.add("onTeamsMessagingExtensionQuery");
            return CompletableFuture.completedFuture(new MessagingExtensionResponse());
        }

        @Override
        protected CompletableFuture<Void> onTeamsO365ConnectorCardAction(
            TurnContext turnContext,
            O365ConnectorCardActionQuery query
        ) {
            record.add("onTeamsO365ConnectorCardAction");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<MessagingExtensionResponse> onTeamsAppBasedLinkQuery(
            TurnContext turnContext,
            AppBasedLinkQuery query
        ) {
            record.add("onTeamsAppBasedLinkQuery");
            return CompletableFuture.completedFuture(new MessagingExtensionResponse());
        }

        @Override
        protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionSelectItem(
            TurnContext turnContext,
            Object query
        ) {
            record.add("onTeamsMessagingExtensionSelectItem");
            return CompletableFuture.completedFuture(new MessagingExtensionResponse());
        }

        @Override
        protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionFetchTask(
            TurnContext turnContext,
            MessagingExtensionAction action
        ) {
            record.add("onTeamsMessagingExtensionFetchTask");
            return CompletableFuture.completedFuture(new MessagingExtensionActionResponse());
        }

        @Override
        protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitActionDispatch(
            TurnContext turnContext,
            MessagingExtensionAction action
        ) {
            record.add("onTeamsMessagingExtensionSubmitActionDispatch");
            return super.onTeamsMessagingExtensionSubmitActionDispatch(turnContext, action);
        }

        @Override
        protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitAction(
            TurnContext turnContext,
            MessagingExtensionAction action
        ) {
            record.add("onTeamsMessagingExtensionSubmitAction");
            return CompletableFuture.completedFuture(new MessagingExtensionActionResponse());
        }

        @Override
        protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewEdit(
            TurnContext turnContext,
            MessagingExtensionAction action
        ) {
            record.add("onTeamsMessagingExtensionBotMessagePreviewEdit");
            return CompletableFuture.completedFuture(new MessagingExtensionActionResponse());
        }

        @Override
        protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewSend(
            TurnContext turnContext,
            MessagingExtensionAction action
        ) {
            record.add("onTeamsMessagingExtensionBotMessagePreviewSend");
            return CompletableFuture.completedFuture(new MessagingExtensionActionResponse());
        }

        @Override
        protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionConfigurationQuerySettingUrl(
            TurnContext turnContext,
            MessagingExtensionQuery query
        ) {
            record.add("onTeamsMessagingExtensionConfigurationQuerySettingUrl");
            return CompletableFuture.completedFuture(new MessagingExtensionResponse());
        }

        @Override
        protected CompletableFuture<Void> onTeamsMessagingExtensionConfigurationSetting(
            TurnContext turnContext,
            Object settings
        ) {
            record.add("onTeamsMessagingExtensionConfigurationSetting");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<TaskModuleResponse> onTeamsTaskModuleFetch(
            TurnContext turnContext,
            TaskModuleRequest taskModuleRequest
        ) {
            record.add("onTeamsTaskModuleFetch");
            return CompletableFuture.completedFuture(new TaskModuleResponse());
        }

        @Override
        protected CompletableFuture<Void> onTeamsMessagingExtensionCardButtonClicked(
            TurnContext turnContext,
            Object cardData
        ) {
            record.add("onTeamsMessagingExtensionCardButtonClicked");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<TaskModuleResponse> onTeamsTaskModuleSubmit(
            TurnContext turnContext,
            TaskModuleRequest taskModuleRequest
        ) {
            record.add("onTeamsTaskModuleSubmit");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
            record.add("onConversationUpdateActivity");
            return super.onConversationUpdateActivity(turnContext);
        }

        @Override
        protected CompletableFuture<Void> onMembersAdded(
            List<ChannelAccount> membersAdded,
            TurnContext turnContext
        ) {
            record.add("onMembersAdded");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onMembersRemoved(
            List<ChannelAccount> membersRemoved,
            TurnContext turnContext
        ) {
            record.add("onMembersRemoved");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onTeamsMembersAdded(
            List<TeamsChannelAccount> membersAdded,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsMembersAdded");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onTeamsMembersRemoved(
            List<TeamsChannelAccount> membersRemoved,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsMembersRemoved");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onTeamsChannelCreated(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsChannelCreated");
            return super.onTeamsChannelCreated(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsChannelDeleted(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsChannelDeleted");
            return super.onTeamsChannelDeleted(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsChannelRenamed(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsChannelRenamed");
            return super.onTeamsChannelRenamed(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsChannelRestored(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsChannelRestored");
            return super.onTeamsChannelRestored(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsTeamRenamed(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsTeamRenamed");
            return super.onTeamsTeamRenamed(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsTeamArchived(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsTeamArchived");
            return super.onTeamsTeamArchived(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsTeamDeleted(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsTeamDeleted");
            return super.onTeamsTeamDeleted(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsTeamHardDeleted(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsTeamHardDeleted");
            return super.onTeamsTeamHardDeleted(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsTeamRestored(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsTeamRestored");
            return super.onTeamsTeamRestored(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsTeamUnarchived(
            ChannelInfo channelInfo,
            TeamInfo teamInfo,
            TurnContext turnContext
        ) {
            record.add("onTeamsTeamUnarchived");
            return super.onTeamsTeamUnarchived(channelInfo, teamInfo, turnContext);
        }

        @Override
        protected CompletableFuture<Void> onEventActivity(
            TurnContext turnContext
        ) {
            record.add("onEventActivity");
            return super.onEventActivity(turnContext);
        }

        @Override
        protected CompletableFuture<Void> onTeamsMeetingStart(
            MeetingStartEventDetails meeting,
            TurnContext turnContext
        ) {
            record.add("onTeamsMeetingStart");
            return turnContext.sendActivity(meeting.getStartTime().toString())
                .thenCompose(resourceResponse -> super.onTeamsMeetingStart(meeting, turnContext));
        }

        @Override
        protected CompletableFuture<Void> onTeamsMeetingEnd(
            MeetingEndEventDetails meeting,
            TurnContext turnContext
        ) {
            record.add("onTeamsMeetingEnd");
            return turnContext.sendActivity(meeting.getEndTime().toString())
                .thenCompose(resourceResponse -> super.onTeamsMeetingEnd(meeting, turnContext));
        }
    }

    private static ConnectorClient getConnectorClient(String baseUri, AppCredentials credentials) {
        Conversations mockConversations = Mockito.mock(Conversations.class);

        ConversationResourceResponse response = new ConversationResourceResponse();
        response.setId("team-id");
        response.setServiceUrl("https://serviceUrl/");
        response.setActivityId("activityId123");

        // createConversation
        Mockito.when(
            mockConversations.createConversation(Mockito.any(ConversationParameters.class))
        ).thenReturn(CompletableFuture.completedFuture(response));


        ArrayList<ChannelAccount> channelAccount1 = new ArrayList<ChannelAccount>();
        ChannelAccount account1 = new ChannelAccount();
        account1.setId("id-1");
        account1.setName("name-1");
        account1.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-1"));
        account1.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-1"));
        account1.setProperties("surname", JsonNodeFactory.instance.textNode("surname-1"));
        account1.setProperties("email", JsonNodeFactory.instance.textNode("email-1"));
        account1.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-1"));
        account1.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-1"));
        channelAccount1.add(account1);

        ChannelAccount account2 = new ChannelAccount();
        account2.setId("id-2");
        account2.setName("name-2");
        account2.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-2"));
        account2.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-2"));
        account2.setProperties("surname",JsonNodeFactory.instance.textNode("surname-2"));
        account2.setProperties("email", JsonNodeFactory.instance.textNode("email-2"));
        account2.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-2"));
        account2.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-2"));
        channelAccount1.add(account2);
        // getConversationMembers (Team)
        Mockito.when(mockConversations.getConversationMembers("team-id")).thenReturn(
            CompletableFuture.completedFuture(channelAccount1)
        );


        ArrayList<ChannelAccount> channelAccount2 = new ArrayList<ChannelAccount>();
        ChannelAccount channelAccount3 = new ChannelAccount();
        channelAccount3.setId("id-3");
        channelAccount3.setName("name-3");
        channelAccount3.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-3"));
        channelAccount3.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-3"));
        channelAccount3.setProperties("surname", JsonNodeFactory.instance.textNode("surname-3"));
        channelAccount3.setProperties("email", JsonNodeFactory.instance.textNode("email-3"));
        channelAccount3.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-3"));
        channelAccount3.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-3"));
        channelAccount2.add(channelAccount3);
        ChannelAccount channelAccount4 = new ChannelAccount();
        channelAccount4.setId("id-4");
        channelAccount4.setName("name-4");
        channelAccount4.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-4"));
        channelAccount4.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-4"));
        channelAccount4.setProperties("surname", JsonNodeFactory.instance.textNode("surname-4"));
        channelAccount4.setProperties("email", JsonNodeFactory.instance.textNode("email-4"));
        channelAccount4.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-4"));
        channelAccount4.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-4"));
        channelAccount2.add(channelAccount4);
        // getConversationMembers (Group chat)
        Mockito.when(mockConversations.getConversationMembers("conversation-id")).thenReturn(
            CompletableFuture.completedFuture(channelAccount2)
        );


        ChannelAccount channelAccount5 = new ChannelAccount();
        channelAccount5.setId("id-1");
        channelAccount5.setName("name-1");
        channelAccount5.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-1"));
        channelAccount5.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-1"));
        channelAccount5.setProperties("surname", JsonNodeFactory.instance.textNode("surname-1"));
        channelAccount5.setProperties("email", JsonNodeFactory.instance.textNode("email-1"));
        channelAccount5.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-1"));
        channelAccount5.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-1"));
        // getConversationMember (Team)
        Mockito.when(mockConversations.getConversationMember("id-1", "team-id")).thenReturn(
            CompletableFuture.completedFuture(channelAccount5)
        );


        ChannelAccount channelAccount6 = new ChannelAccount();
        channelAccount6.setId("id-1");
        channelAccount6.setName("name-1");
        channelAccount6.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-1"));
        channelAccount6.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-1"));
        channelAccount6.setProperties("surname", JsonNodeFactory.instance.textNode("surname-1"));
        channelAccount6.setProperties("email", JsonNodeFactory.instance.textNode("email-1"));
        channelAccount6.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-1"));
        channelAccount6.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-1"));
        // getConversationMember (Group chat)
        Mockito.when(mockConversations.getConversationMember("id-1", "conversation-id")).thenReturn(
            CompletableFuture.completedFuture(channelAccount6)
        );

        ConnectorClient mockConnectorClient = Mockito.mock(ConnectorClient.class);
        Mockito.when(mockConnectorClient.getConversations()).thenReturn(mockConversations);
        Mockito.when(mockConnectorClient.baseUrl()).thenReturn(baseUri);
        Mockito.when(mockConnectorClient.credentials()).thenReturn(credentials);

        return mockConnectorClient;
    }
}
