// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.SimpleAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import com.microsoft.bot.connector.teams.TeamsConnectorClient;
import com.microsoft.bot.connector.teams.TeamsOperations;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.Pair;
import com.microsoft.bot.schema.teams.ChannelInfo;
import com.microsoft.bot.schema.teams.ConversationList;
import com.microsoft.bot.schema.teams.MeetingDetails;
import com.microsoft.bot.schema.teams.MeetingInfo;
import com.microsoft.bot.schema.teams.TeamDetails;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import com.microsoft.bot.schema.teams.TeamsMeetingInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TeamsInfoTests {
    @Test
    public void TestSendMessageToTeamsChannel() {
        String baseUri = "https://test.coffee";
        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(
            "big-guid-here",
            "appPasswordHere"
        );
        ConnectorClient connectorClient = getConnectorClient(baseUri, credentials);

        Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Test-SendMessageToTeamsChannelAsync");
        activity.setChannelId(Channels.MSTEAMS);
        TeamsChannelData data = new TeamsChannelData();
        data.setTeam(new TeamInfo("team-id"));
        activity.setChannelData(data);

        TurnContext turnContext = new TurnContextImpl(
            new TestBotFrameworkAdapter(
                new SimpleCredentialProvider("big-guid-here", "appPasswordHere")
            ),
            activity
        );
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);
        turnContext.getTurnState().add(
            BotFrameworkAdapter.TEAMSCONNECTOR_CLIENT_KEY,
            getTeamsConnectorClient(connectorClient.baseUrl(), credentials)
        );
        turnContext.getActivity().setServiceUrl("https://test.coffee");

        ActivityHandler handler = new TestTeamsActivityHandler();
        handler.onTurn(turnContext).join();
    }

    @Test
    public void TestGetTeamDetails() {
        String baseUri = "https://test.coffee";
        MicrosoftAppCredentials credentials = MicrosoftAppCredentials.empty();
        ConnectorClient connectorClient = getConnectorClient(baseUri, credentials);

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Test-GetTeamDetailsAsync");
        activity.setChannelId(Channels.MSTEAMS);
        TeamsChannelData data = new TeamsChannelData();
        data.setTeam(new TeamInfo("team-id"));
        activity.setChannelData(data);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);
        turnContext.getTurnState().add(
            BotFrameworkAdapter.TEAMSCONNECTOR_CLIENT_KEY,
            getTeamsConnectorClient(connectorClient.baseUrl(), credentials)
        );
        turnContext.getActivity().setServiceUrl("https://test.coffee");

        ActivityHandler handler = new TestTeamsActivityHandler();
        handler.onTurn(turnContext).join();
    }

    @Test
    public void TestTeamGetMembers() {
        String baseUri = "https://test.coffee";
        MicrosoftAppCredentials credentials = MicrosoftAppCredentials.empty();
        ConnectorClient connectorClient = getConnectorClient(baseUri, credentials);

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Test-Team-GetMembersAsync");
        activity.setChannelId(Channels.MSTEAMS);
        TeamsChannelData data = new TeamsChannelData();
        data.setTeam(new TeamInfo("team-id"));
        activity.setChannelData(data);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);
        turnContext.getTurnState().add(
            BotFrameworkAdapter.TEAMSCONNECTOR_CLIENT_KEY,
            getTeamsConnectorClient(connectorClient.baseUrl(), credentials)
        );
        turnContext.getActivity().setServiceUrl("https://test.coffee");

        ActivityHandler handler = new TestTeamsActivityHandler();
        handler.onTurn(turnContext).join();
    }

    @Test
    public void TestGroupChatGetMembers() {
        String baseUri = "https://test.coffee";
        MicrosoftAppCredentials credentials = MicrosoftAppCredentials.empty();
        ConnectorClient connectorClient = getConnectorClient(baseUri, credentials);

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Test-GroupChat-GetMembersAsync");
        activity.setChannelId(Channels.MSTEAMS);
        activity.setConversation(new ConversationAccount("conversation-id"));

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);
        turnContext.getTurnState().add(
            BotFrameworkAdapter.TEAMSCONNECTOR_CLIENT_KEY,
            getTeamsConnectorClient(connectorClient.baseUrl(), credentials)
        );
        turnContext.getActivity().setServiceUrl("https://test.coffee");

        ActivityHandler handler = new TestTeamsActivityHandler();
        handler.onTurn(turnContext).join();
    }

    @Test
    public void TestGetChannels() {
        String baseUri = "https://test.coffee";
        MicrosoftAppCredentials credentials = MicrosoftAppCredentials.empty();
        ConnectorClient connectorClient = getConnectorClient(baseUri, credentials);

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Test-GetChannelsAsync");
        activity.setChannelId(Channels.MSTEAMS);
        TeamsChannelData data = new TeamsChannelData();
        data.setTeam(new TeamInfo("team-id"));
        activity.setChannelData(data);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);
        turnContext.getTurnState().add(
            BotFrameworkAdapter.TEAMSCONNECTOR_CLIENT_KEY,
            getTeamsConnectorClient(connectorClient.baseUrl(), credentials)
        );
        turnContext.getActivity().setServiceUrl("https://test.coffee");

        ActivityHandler handler = new TestTeamsActivityHandler();
        handler.onTurn(turnContext).join();
    }

    @Test
    public void TestGetMeetingInfo() {
        String baseUri = "https://test.coffee";
        MicrosoftAppCredentials credentials = MicrosoftAppCredentials.empty();
        ConnectorClient connectorClient = getConnectorClient(baseUri, credentials);

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("Test-GetMeetingInfoAsync");
        activity.setChannelId(Channels.MSTEAMS);
        TeamsChannelData data = new TeamsChannelData();
        data.setMeeting(new TeamsMeetingInfo("meeting-id"));
        activity.setChannelData(data);

        TurnContext turnContext = new TurnContextImpl(new SimpleAdapter(), activity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);
        turnContext.getTurnState().add(
            BotFrameworkAdapter.TEAMSCONNECTOR_CLIENT_KEY,
            getTeamsConnectorClient(connectorClient.baseUrl(), credentials)
        );
        turnContext.getActivity().setServiceUrl("https://test.coffee");

        ActivityHandler handler = new TestTeamsActivityHandler();
        handler.onTurn(turnContext).join();
    }

    private class TestBotFrameworkAdapter extends BotFrameworkAdapter {

        public TestBotFrameworkAdapter(CredentialProvider withCredentialProvider) {
            super(withCredentialProvider);
        }

        @Override
        protected CompletableFuture<ConnectorClient> getOrCreateConnectorClient(
            String serviceUrl,
            AppCredentials usingAppCredentials
        ) {
            return CompletableFuture.completedFuture(
                TeamsInfoTests.getConnectorClient(serviceUrl, usingAppCredentials)
            );
        }
    }

    private static class TestTeamsActivityHandler extends TeamsActivityHandler {
        @Override
        public CompletableFuture<Void> onTurn(TurnContext turnContext) {
            return super.onTurn(turnContext).thenCompose(aVoid -> {
                switch (turnContext.getActivity().getText()) {
                    case "Test-GetTeamDetailsAsync":
                        return callGetTeamDetails(turnContext);

                    case "Test-Team-GetMembersAsync":
                        return callTeamGetMembers(turnContext);

                    case "Test-GroupChat-GetMembersAsync":
                        return callGroupChatGetMembers(turnContext);

                    case "Test-GetChannelsAsync":
                        return callGetChannels(turnContext);

                    case "Test-SendMessageToTeamsChannelAsync":
                        return callSendMessageToTeamsChannel(turnContext);

                    case "Test-GetMeetingInfoAsync":
                        return callTeamsInfoGetMeetingInfo(turnContext);

                    default:
                        Assert.fail();
                }

                CompletableFuture<Void> result = new CompletableFuture<>();
                result.completeExceptionally(
                    new AssertionError(
                        "Unknown Activity Text sent to TestTeamsActivityHandler.onTurn"
                    )
                );
                return result;
            });
        }

        private CompletableFuture<Void> callSendMessageToTeamsChannel(TurnContext turnContext) {
            Activity message = MessageFactory.text("hi");
            String channelId = "channelId123";
            MicrosoftAppCredentials creds = new MicrosoftAppCredentials(
                "big-guid-here",
                "appPasswordHere"
            );
            Pair<ConversationReference, String> reference = TeamsInfo.sendMessageToTeamsChannel(
                turnContext,
                message,
                channelId,
                creds
            ).join();

            Assert.assertEquals("activityId123", reference.getLeft().getActivityId());
            Assert.assertEquals("channelId123", reference.getLeft().getChannelId());
            Assert.assertEquals("https://test.coffee", reference.getLeft().getServiceUrl());
            Assert.assertEquals("activityId123", reference.getRight());

            return CompletableFuture.completedFuture(null);
        }

        private CompletableFuture<Void> callGetTeamDetails(TurnContext turnContext) {
            TeamDetails teamDetails = TeamsInfo.getTeamDetails(turnContext, null).join();

            Assert.assertEquals("team-id", teamDetails.getId());
            Assert.assertEquals("team-name", teamDetails.getName());
            Assert.assertEquals("team-aadgroupid", teamDetails.getAadGroupId());

            return CompletableFuture.completedFuture(null);
        }

        private CompletableFuture<Void> callTeamGetMembers(TurnContext turnContext) {
            List<TeamsChannelAccount> members = TeamsInfo.getMembers(turnContext).join();

            Assert.assertEquals("id-1", members.get(0).getId());
            Assert.assertEquals("name-1", members.get(0).getName());
            Assert.assertEquals("givenName-1", members.get(0).getGivenName());
            Assert.assertEquals("surname-1", members.get(0).getSurname());
            Assert.assertEquals("userPrincipalName-1", members.get(0).getUserPrincipalName());

            Assert.assertEquals("id-2", members.get(1).getId());
            Assert.assertEquals("name-2", members.get(1).getName());
            Assert.assertEquals("givenName-2", members.get(1).getGivenName());
            Assert.assertEquals("surname-2", members.get(1).getSurname());
            Assert.assertEquals("userPrincipalName-2", members.get(1).getUserPrincipalName());

            return CompletableFuture.completedFuture(null);
        }

        private CompletableFuture<Void> callGroupChatGetMembers(TurnContext turnContext) {
            List<TeamsChannelAccount> members = TeamsInfo.getMembers(turnContext).join();

            Assert.assertEquals("id-3", members.get(0).getId());
            Assert.assertEquals("name-3", members.get(0).getName());
            Assert.assertEquals("givenName-3", members.get(0).getGivenName());
            Assert.assertEquals("surname-3", members.get(0).getSurname());
            Assert.assertEquals("userPrincipalName-3", members.get(0).getUserPrincipalName());

            Assert.assertEquals("id-4", members.get(1).getId());
            Assert.assertEquals("name-4", members.get(1).getName());
            Assert.assertEquals("givenName-4", members.get(1).getGivenName());
            Assert.assertEquals("surname-4", members.get(1).getSurname());
            Assert.assertEquals("userPrincipalName-4", members.get(1).getUserPrincipalName());

            return CompletableFuture.completedFuture(null);
        }

        private CompletableFuture<Void> callGetChannels(TurnContext turnContext) {
            List<ChannelInfo> channels = TeamsInfo.getTeamChannels(turnContext, null).join();

            Assert.assertEquals("channel-id-1", channels.get(0).getId());

            Assert.assertEquals("channel-id-2", channels.get(1).getId());
            Assert.assertEquals("channel-name-2", channels.get(1).getName());

            Assert.assertEquals("channel-id-3", channels.get(2).getId());
            Assert.assertEquals("channel-name-3", channels.get(2).getName());

            return CompletableFuture.completedFuture(null);
        }

        private CompletableFuture<Void> callTeamsInfoGetMeetingInfo(TurnContext turnContext) {
            MeetingInfo meeting = TeamsInfo.getMeetingInfo(turnContext, null).join();

            Assert.assertEquals("meeting-id", meeting.getDetails().getId());
            Assert.assertEquals("organizer-id", meeting.getOrganizer().getId());
            Assert.assertEquals("meetingConversationId-1", meeting.getConversation().getId());

            return CompletableFuture.completedFuture(null);
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


        ArrayList<ChannelAccount> channelAccounts1 = new ArrayList<ChannelAccount>();
        ChannelAccount channelAccount1 = new ChannelAccount();
        channelAccount1.setId("id-1");
        channelAccount1.setName("name-1");
        channelAccount1.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-1"));
        channelAccount1.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-1"));
        channelAccount1.setProperties("surname", JsonNodeFactory.instance.textNode("surname-1"));
        channelAccount1.setProperties("email", JsonNodeFactory.instance.textNode("email-1"));
        channelAccount1.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-1"));
        channelAccount1.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-1"));
        channelAccounts1.add(channelAccount1);
        ChannelAccount channelAccount2 = new ChannelAccount();
        channelAccount2.setId("id-2");
        channelAccount2.setName("name-2");
        channelAccount2.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-2"));
        channelAccount2.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-2"));
        channelAccount2.setProperties("surname", JsonNodeFactory.instance.textNode("surname-2"));
        channelAccount2.setProperties("email", JsonNodeFactory.instance.textNode("email-2"));
        channelAccount2.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-2"));
        channelAccount2.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-2"));
        channelAccounts1.add(channelAccount2);
        // getConversationMembers (Team)
        Mockito.when(mockConversations.getConversationMembers("team-id")).thenReturn(
            CompletableFuture.completedFuture(channelAccounts1)
        );


        ArrayList<ChannelAccount> channelAccounts2 = new ArrayList<ChannelAccount>();
        ChannelAccount channelAccount3 = new ChannelAccount();
        channelAccount3.setId("id-3");
        channelAccount3.setName("name-3");
        channelAccount3.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-3"));
        channelAccount3.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-3"));
        channelAccount3.setProperties("surname", JsonNodeFactory.instance.textNode("surname-3"));
        channelAccount3.setProperties("email", JsonNodeFactory.instance.textNode("email-3"));
        channelAccount3.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-3"));
        channelAccount3.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-3"));
        channelAccounts2.add(channelAccount3);
        ChannelAccount channelAccount4 = new ChannelAccount();
        channelAccount4.setId("id-4");
        channelAccount4.setName("name-4");
        channelAccount4.setProperties("objectId", JsonNodeFactory.instance.textNode("objectId-4"));
        channelAccount4.setProperties("givenName", JsonNodeFactory.instance.textNode("givenName-4"));
        channelAccount4.setProperties("surname", JsonNodeFactory.instance.textNode("surname-4"));
        channelAccount4.setProperties("email", JsonNodeFactory.instance.textNode("email-4"));
        channelAccount4.setProperties("userPrincipalName", JsonNodeFactory.instance.textNode("userPrincipalName-4"));
        channelAccount4.setProperties("tenantId", JsonNodeFactory.instance.textNode("tenantId-4"));
        channelAccounts2.add(channelAccount4);
        // getConversationMembers (Group chat)
        Mockito.when(mockConversations.getConversationMembers("conversation-id")).thenReturn(
            CompletableFuture.completedFuture(channelAccounts2)
        );

        ConnectorClient mockConnectorClient = Mockito.mock(ConnectorClient.class);
        Mockito.when(mockConnectorClient.getConversations()).thenReturn(mockConversations);
        Mockito.when(mockConnectorClient.baseUrl()).thenReturn(baseUri);
        Mockito.when(mockConnectorClient.credentials()).thenReturn(credentials);

        return mockConnectorClient;
    }

    private static TeamsConnectorClient getTeamsConnectorClient(
        String baseUri,
        AppCredentials credentials
    ) {
        TeamsOperations mockOperations = Mockito.mock(TeamsOperations.class);


        ConversationList list = new ConversationList();
        ArrayList<ChannelInfo> conversations = new ArrayList<ChannelInfo>();
        conversations.add(new ChannelInfo("channel-id-1"));
        conversations.add(new ChannelInfo("channel-id-2", "channel-name-2"));
        conversations.add(new ChannelInfo("channel-id-3", "channel-name-3"));
        list.setConversations(conversations);
        // fetchChannelList
        Mockito.when(mockOperations.fetchChannelList(Mockito.anyString())).thenReturn(
            CompletableFuture.completedFuture(list)
        );

        TeamDetails details = new TeamDetails();
        details.setId("team-id");
        details.setName("team-name");
        details.setAadGroupId("team-aadgroupid");
        // fetchTeamDetails
        Mockito.when(mockOperations.fetchTeamDetails(Mockito.anyString())).thenReturn(
            CompletableFuture.completedFuture(details)
        );

        // fetchTeamDetails
        MeetingInfo meetingInfo = new MeetingInfo();
        MeetingDetails meetingDetails = new MeetingDetails();
        meetingDetails.setId("meeting-id");
        meetingInfo.setDetails(meetingDetails);

        TeamsChannelAccount organizer = new TeamsChannelAccount();
        organizer.setId("organizer-id");
        meetingInfo.setOrganizer(organizer);

        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setId("meetingConversationId-1");
        meetingInfo.setConversation(conversationAccount);

        Mockito.when(mockOperations.fetchMeetingInfo(Mockito.anyString())).thenReturn(
            CompletableFuture.completedFuture(meetingInfo)
        );

        TeamsConnectorClient mockConnectorClient = Mockito.mock(TeamsConnectorClient.class);
        Mockito.when(mockConnectorClient.getTeams()).thenReturn(mockOperations);
        Mockito.when(mockConnectorClient.baseUrl()).thenReturn(baseUri);
        Mockito.when(mockConnectorClient.credentials()).thenReturn(credentials);

        return mockConnectorClient;
    }
}
