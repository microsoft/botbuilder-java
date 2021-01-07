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
import com.microsoft.bot.schema.teams.AppBasedLinkQuery;
import com.microsoft.bot.schema.teams.ChannelInfo;
import com.microsoft.bot.schema.teams.FileConsentCardResponse;
import com.microsoft.bot.schema.teams.FileUploadInfo;
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

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("botid-1"));
                    }
                });
                setRecipient(new ChannelAccount("botid-1"));
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamMemberAdded");
                        setTeam(new TeamInfo("team-id"));
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

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

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("id-1"));
                    }
                });
                setRecipient(new ChannelAccount("b"));
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamMemberAdded");
                        setTeam(new TeamInfo("team-id"));
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

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

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("id-1"));
                    }
                });
                setRecipient(new ChannelAccount("b"));
                setConversation(new ConversationAccount("conversation-id"));
                setChannelId(Channels.MSTEAMS);
            }
        };

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

        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setMembersAdded(new ArrayList<ChannelAccount>() {
                    {
                        add(new TeamsChannelAccount() {
                            {
                                setId("id-1");
                                setName("name-1");
                                setAadObjectId("aadobject-1");
                                setEmail("test@microsoft.com");
                                setGivenName("given-1");
                                setSurname("surname-1");
                                setUserPrincipalName("t@microsoft.com");
                                setTenantId("testTenantId");
                                setUserRole("guest");
                            }
                        });
                    }
                });
                setRecipient(new ChannelAccount("b"));
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamMemberAdded");
                        setTeam(new TeamInfo("team-id"));
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

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
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setMembersRemoved(new ArrayList<ChannelAccount>() {
                    {
                        add(new ChannelAccount("a"));
                    }
                });
                setRecipient(new ChannelAccount("b"));
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamMemberRemoved");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsMembersRemoved", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelCreated() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("channelCreated");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelCreated", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelDeleted() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("channelDeleted");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelDeleted", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelRenamed() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("channelRenamed");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelRenamed", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsChannelRestored() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("channelRestored");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsChannelRestored", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamRenamed() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamRenamed");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamRenamed", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamArchived() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamArchived");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamArchived", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamDeleted() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamDeleted");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamDeleted", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamHardDeleted() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamHardDeleted");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamHardDeleted", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamRestored() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamRestored");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamRestored", bot.record.get(1));
    }

    @Test
    public void TestConversationUpdateTeamsTeamUnarchived() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE) {
            {
                setChannelData(new TeamsChannelData() {
                    {
                        setEventType("teamUnarchived");
                    }
                });
                setChannelId(Channels.MSTEAMS);
            }
        };

        TurnContext turnContext = new TurnContextImpl(new NotImplementedAdapter(), activity);

        TestActivityHandler bot = new TestActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertEquals(2, bot.record.size());
        Assert.assertEquals("onConversationUpdateActivity", bot.record.get(0));
        Assert.assertEquals("onTeamsTeamUnarchived", bot.record.get(1));
    }

    @Test
    public void TestFileConsentAccept() {
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("fileConsent/invoke");
                setValue(new FileConsentCardResponse() {
                    {
                        setAction("accept");
                        setUploadInfo(new FileUploadInfo() {
                            {
                                setUniqueId("uniqueId");
                                setFileType("fileType");
                                setUploadUrl("uploadUrl");
                            }
                        });
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("fileConsent/invoke");
                setValue(new FileConsentCardResponse() {
                    {
                        setAction("decline");
                        setUploadInfo(new FileUploadInfo() {
                            {
                                setUniqueId("uniqueId");
                                setFileType("fileType");
                                setUploadUrl("uploadUrl");
                            }
                        });
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("actionableMessage/executeAction");
                setValue(new O365ConnectorCardActionQuery());
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/queryLink");
                setValue(new AppBasedLinkQuery());
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/query");
                setValue(new MessagingExtensionQuery());
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/selectItem");
                setValue(new MessagingExtensionQuery());
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/submitAction");
                setValue(new MessagingExtensionQuery());
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/submitAction");
                setValue(new MessagingExtensionAction() {
                    {
                        setBotMessagePreviewAction("edit");
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/submitAction");
                setValue(new MessagingExtensionAction() {
                    {
                        setBotMessagePreviewAction("send");
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/fetchTask");
                setValue(new MessagingExtensionAction() {
                    {
                        setCommandId("testCommand");
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/querySettingUrl");
                setValue(new MessagingExtensionAction() {
                    {
                        setCommandId("testCommand");
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("composeExtension/setting");
                setValue(new MessagingExtensionAction() {
                    {
                        setCommandId("testCommand");
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("task/fetch");
                setValue(new TaskModuleRequest() {
                    {
                        setData(new HashMap<String, Object>() {
                            {
                                put("key", "value");
                                put("type", "task / fetch");
                            }
                        });
                        setContext(new TaskModuleRequestContext() {
                            {
                                setTheme("default");
                            }
                        });
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("task/submit");
                setValue(new TaskModuleRequest() {
                    {
                        setData(new HashMap<String, Object>() {
                            {
                                put("key", "value");
                                put("type", "task / fetch");
                            }
                        });
                        setContext(new TaskModuleRequestContext() {
                            {
                                setTheme("default");
                            }
                        });
                    }
                });
            }
        };

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
        Activity activity = new Activity(ActivityTypes.INVOKE) {
            {
                setName("signin/verifyState");
            }
        };

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
    }

    private static ConnectorClient getConnectorClient(String baseUri, AppCredentials credentials) {
        Conversations mockConversations = Mockito.mock(Conversations.class);

        // createConversation
        Mockito.when(
            mockConversations.createConversation(Mockito.any(ConversationParameters.class))
        ).thenReturn(CompletableFuture.completedFuture(new ConversationResourceResponse() {
            {
                setId("team-id");
                setServiceUrl("https://serviceUrl/");
                setActivityId("activityId123");
            }
        }));

        // getConversationMembers (Team)
        Mockito.when(mockConversations.getConversationMembers("team-id")).thenReturn(
            CompletableFuture.completedFuture(new ArrayList<ChannelAccount>() {
                {
                    add(new ChannelAccount() {
                        {
                            setId("id-1");
                            setName("name-1");
                            setProperties(
                                "objectId",
                                JsonNodeFactory.instance.textNode("objectId-1")
                            );
                            setProperties(
                                "givenName",
                                JsonNodeFactory.instance.textNode("givenName-1")
                            );
                            setProperties(
                                "surname",
                                JsonNodeFactory.instance.textNode("surname-1")
                            );
                            setProperties("email", JsonNodeFactory.instance.textNode("email-1"));
                            setProperties(
                                "userPrincipalName",
                                JsonNodeFactory.instance.textNode("userPrincipalName-1")
                            );
                            setProperties(
                                "tenantId",
                                JsonNodeFactory.instance.textNode("tenantId-1")
                            );
                        }
                    });
                    add(new ChannelAccount() {
                        {
                            setId("id-2");
                            setName("name-2");
                            setProperties(
                                "objectId",
                                JsonNodeFactory.instance.textNode("objectId-2")
                            );
                            setProperties(
                                "givenName",
                                JsonNodeFactory.instance.textNode("givenName-2")
                            );
                            setProperties(
                                "surname",
                                JsonNodeFactory.instance.textNode("surname-2")
                            );
                            setProperties("email", JsonNodeFactory.instance.textNode("email-2"));
                            setProperties(
                                "userPrincipalName",
                                JsonNodeFactory.instance.textNode("userPrincipalName-2")
                            );
                            setProperties(
                                "tenantId",
                                JsonNodeFactory.instance.textNode("tenantId-2")
                            );
                        }
                    });
                }
            })
        );

        // getConversationMembers (Group chat)
        Mockito.when(mockConversations.getConversationMembers("conversation-id")).thenReturn(
            CompletableFuture.completedFuture(new ArrayList<ChannelAccount>() {
                {
                    add(new ChannelAccount() {
                        {
                            setId("id-3");
                            setName("name-3");
                            setProperties(
                                "objectId",
                                JsonNodeFactory.instance.textNode("objectId-3")
                            );
                            setProperties(
                                "givenName",
                                JsonNodeFactory.instance.textNode("givenName-3")
                            );
                            setProperties(
                                "surname",
                                JsonNodeFactory.instance.textNode("surname-3")
                            );
                            setProperties("email", JsonNodeFactory.instance.textNode("email-3"));
                            setProperties(
                                "userPrincipalName",
                                JsonNodeFactory.instance.textNode("userPrincipalName-3")
                            );
                            setProperties(
                                "tenantId",
                                JsonNodeFactory.instance.textNode("tenantId-3")
                            );
                        }
                    });
                    add(new ChannelAccount() {
                        {
                            setId("id-4");
                            setName("name-4");
                            setProperties(
                                "objectId",
                                JsonNodeFactory.instance.textNode("objectId-4")
                            );
                            setProperties(
                                "givenName",
                                JsonNodeFactory.instance.textNode("givenName-4")
                            );
                            setProperties(
                                "surname",
                                JsonNodeFactory.instance.textNode("surname-4")
                            );
                            setProperties("email", JsonNodeFactory.instance.textNode("email-4"));
                            setProperties(
                                "userPrincipalName",
                                JsonNodeFactory.instance.textNode("userPrincipalName-4")
                            );
                            setProperties(
                                "tenantId",
                                JsonNodeFactory.instance.textNode("tenantId-4")
                            );
                        }
                    });
                }
            })
        );

        // getConversationMember (Team)
        Mockito.when(mockConversations.getConversationMember("id-1", "team-id")).thenReturn(
            CompletableFuture.completedFuture(
                new ChannelAccount() {
                    {
                        setId("id-1");
                        setName("name-1");
                        setProperties(
                            "objectId",
                            JsonNodeFactory.instance.textNode("objectId-1")
                        );
                        setProperties(
                            "givenName",
                            JsonNodeFactory.instance.textNode("givenName-1")
                        );
                        setProperties(
                            "surname",
                            JsonNodeFactory.instance.textNode("surname-1")
                        );
                        setProperties("email", JsonNodeFactory.instance.textNode("email-1"));
                        setProperties(
                            "userPrincipalName",
                            JsonNodeFactory.instance.textNode("userPrincipalName-1")
                        );
                        setProperties(
                            "tenantId",
                            JsonNodeFactory.instance.textNode("tenantId-1")
                        );
                    }
                }
            )
        );

        // getConversationMember (Group chat)
        Mockito.when(mockConversations.getConversationMember("id-1", "conversation-id")).thenReturn(
            CompletableFuture.completedFuture(
                new ChannelAccount() {
                    {
                        setId("id-1");
                        setName("name-1");
                        setProperties(
                            "objectId",
                            JsonNodeFactory.instance.textNode("objectId-1")
                        );
                        setProperties(
                            "givenName",
                            JsonNodeFactory.instance.textNode("givenName-1")
                        );
                        setProperties(
                            "surname",
                            JsonNodeFactory.instance.textNode("surname-1")
                        );
                        setProperties("email", JsonNodeFactory.instance.textNode("email-1"));
                        setProperties(
                            "userPrincipalName",
                            JsonNodeFactory.instance.textNode("userPrincipalName-1")
                        );
                        setProperties(
                            "tenantId",
                            JsonNodeFactory.instance.textNode("tenantId-1")
                        );
                    }
                }
            )
        );

        ConnectorClient mockConnectorClient = Mockito.mock(ConnectorClient.class);
        Mockito.when(mockConnectorClient.getConversations()).thenReturn(mockConversations);
        Mockito.when(mockConnectorClient.baseUrl()).thenReturn(baseUri);
        Mockito.when(mockConnectorClient.credentials()).thenReturn(credentials);

        return mockConnectorClient;
    }
}
