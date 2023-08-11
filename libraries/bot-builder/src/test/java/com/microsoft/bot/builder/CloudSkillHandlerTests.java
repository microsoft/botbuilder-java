// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.skills.BotFrameworkSkill;
import com.microsoft.bot.builder.skills.CloudSkillHandler;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryBase;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryOptions;
import com.microsoft.bot.builder.skills.SkillConversationReference;
import com.microsoft.bot.connector.authentication.AuthenticateRequestResult;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ConnectorFactory;
import com.microsoft.bot.connector.authentication.UserTokenClient;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.CallerIdConstants;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class CloudSkillHandlerTests {

    private static final String TEST_SKILL_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String TEST_AUTH_HEADER = ""; // Empty since claims extraction is being mocked

    @Test
    public void testSendAndReplyToConversation() {
        List<String[]> theoryCases = new ArrayList<>();
        theoryCases.add(new String[]{ActivityTypes.MESSAGE, null});
        theoryCases.add(new String[]{ActivityTypes.MESSAGE, "replyToId"});
        theoryCases.add(new String[]{ActivityTypes.EVENT, null});
        theoryCases.add(new String[]{ActivityTypes.EVENT, "replyToId"});
        theoryCases.add(new String[]{ActivityTypes.END_OF_CONVERSATION, null});
        theoryCases.add(new String[]{ActivityTypes.END_OF_CONVERSATION, "replyToId"});

        for (String[] theoryCase : theoryCases) {
            String activityType = theoryCase[0];
            String replyToId = theoryCase[1];

            // Arrange
            CloudSkillHandlerTestMocks mockObjects = new CloudSkillHandlerTestMocks();
            Activity activity = new Activity(activityType);
            activity.setReplyToId(replyToId);
            String conversationId =  mockObjects.createAndApplyConversationId(activity).join();

            // Act
            CloudSkillHandler sut = new CloudSkillHandler(
                mockObjects.getAdapter(),
                mockObjects.getBot(),
                mockObjects.getConversationIdFactory(),
                mockObjects.getAuth());

            ResourceResponse response = replyToId == null
                ? sut.handleSendToConversation(TEST_AUTH_HEADER, conversationId, activity).join()
                : sut.handleReplyToActivity(TEST_AUTH_HEADER, conversationId, replyToId, activity).join();

            // Assert
            // Assert the turnContext
            Assert.assertEquals(
                CallerIdConstants.BOT_TO_BOT_PREFIX.concat(TEST_SKILL_ID),
                mockObjects.getTurnContext().getActivity().getCallerId());
            Assert.assertNotNull(
                mockObjects.getTurnContext().getTurnState().get(CloudSkillHandler.SKILL_CONVERSATION_REFERENCE_KEY));

            // Assert based on activity type,
            if (activityType.equals(ActivityTypes.MESSAGE)) {
                // Should be sent to the channel and not to the bot.
                Assert.assertNotNull(mockObjects.getChannelActivity());
                Assert.assertNull(mockObjects.getBotActivity());

                // We should get the resourceId returned by the mock.
                Assert.assertEquals("resourceId", response.getId());

                // Assert the activity sent to the channel.
                Assert.assertEquals(activityType, mockObjects.getChannelActivity().getType());
                Assert.assertNull(mockObjects.getChannelActivity().getCallerId());
                Assert.assertEquals(replyToId, mockObjects.getChannelActivity().getReplyToId());
            } else {
                // Should be sent to the bot and not to the channel.
                Assert.assertNull(mockObjects.getChannelActivity());
                Assert.assertNotNull(mockObjects.getBotActivity());

                // If the activity is bounced back to the bot we will get a GUID and not the mocked resourceId.
                Assert.assertNotEquals("resourceId", response.getId());

                // Assert the activity sent back to the bot.
                Assert.assertEquals(activityType, mockObjects.getBotActivity().getType());
                Assert.assertEquals(replyToId, mockObjects.getBotActivity().getReplyToId());
            }
        }
    }

    @Test
    public void testCommandActivities() {
        List<String[]> theoryCases = new ArrayList<>();
        theoryCases.add(new String[]{ActivityTypes.COMMAND, "application/myApplicationCommand", null});
        theoryCases.add(new String[]{ActivityTypes.COMMAND, "application/myApplicationCommand", "replyToId"});
        theoryCases.add(new String[]{ActivityTypes.COMMAND, "other/myBotCommand", null});
        theoryCases.add(new String[]{ActivityTypes.COMMAND, "other/myBotCommand", "replyToId"});
        theoryCases.add(new String[]{ActivityTypes.COMMAND_RESULT, "application/myApplicationCommandResult", null});
        theoryCases.add(new String[]{ActivityTypes.COMMAND_RESULT, "application/myApplicationCommandResult", "replyToId"});
        theoryCases.add(new String[]{ActivityTypes.COMMAND_RESULT, "other/myBotCommand", null});
        theoryCases.add(new String[]{ActivityTypes.COMMAND_RESULT, "other/myBotCommand", "replyToId"});

        for (String[] theoryCase : theoryCases) {
            String commandActivityType = theoryCase[0];
            String name = theoryCase[1];
            String replyToId = theoryCase[2];

            // Arrange
            CloudSkillHandlerTestMocks mockObjects = new CloudSkillHandlerTestMocks();
            Activity activity = new Activity(commandActivityType);
            activity.setName(name);
            activity.setReplyToId(replyToId);
            String conversationId = mockObjects.createAndApplyConversationId(activity).join();

            // Act
            CloudSkillHandler sut = new CloudSkillHandler(
                mockObjects.getAdapter(),
                mockObjects.getBot(),
                mockObjects.getConversationIdFactory(),
                mockObjects.getAuth());

            ResourceResponse response = replyToId == null
                ? sut.handleSendToConversation(TEST_AUTH_HEADER, conversationId, activity).join()
                : sut.handleReplyToActivity(TEST_AUTH_HEADER, conversationId, replyToId, activity).join();

            // Assert
            // Assert the turnContext
            Assert.assertEquals(
                CallerIdConstants.BOT_TO_BOT_PREFIX.concat(TEST_SKILL_ID),
                mockObjects.getTurnContext().getActivity().getCallerId());
            Assert.assertNotNull(
                mockObjects.getTurnContext().getTurnState().get(CloudSkillHandler.SKILL_CONVERSATION_REFERENCE_KEY));

            if (StringUtils.startsWith(name, "application/")) {
                // Should be sent to the channel and not to the bot.
                Assert.assertNotNull(mockObjects.getChannelActivity());
                Assert.assertNull(mockObjects.getBotActivity());

                // We should get the resourceId returned by the mock.
                Assert.assertEquals("resourceId", response.getId());
            } else {
                // Should be sent to the bot and not to the channel.
                Assert.assertNull(mockObjects.getChannelActivity());
                Assert.assertNotNull(mockObjects.getBotActivity());

                // If the activity is bounced back to the bot we will get a GUID and not the mocked resourceId.
                Assert.assertNotEquals("resourceId", response.getId());
            }
        }
    }

    @Test
    public void testDeleteActivity() {
        // Arrange
        CloudSkillHandlerTestMocks mockObjects = new CloudSkillHandlerTestMocks();
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        String conversationId = mockObjects.createAndApplyConversationId(activity).join();
        String activityToDelete = UUID.randomUUID().toString();

        // Act
        CloudSkillHandler sut = new CloudSkillHandler(
            mockObjects.getAdapter(),
            mockObjects.getBot(),
            mockObjects.getConversationIdFactory(),
            mockObjects.getAuth());
        sut.handleDeleteActivity(TEST_AUTH_HEADER, conversationId, activityToDelete).join();

        // Assert
        Assert.assertNotNull(mockObjects.getTurnContext().getTurnState().get(CloudSkillHandler.SKILL_CONVERSATION_REFERENCE_KEY));
        Assert.assertEquals(activityToDelete, mockObjects.getActivityIdToDelete());
    }

    @Test
    public void testUpdateActivity() {
        // Arrange
        CloudSkillHandlerTestMocks mockObjects = new CloudSkillHandlerTestMocks();
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText(String.format("TestUpdate %s.", LocalDateTime.now()));
        String conversationId = mockObjects.createAndApplyConversationId(activity).join();
        String activityToUpdate = UUID.randomUUID().toString();

        // Act
        CloudSkillHandler sut = new CloudSkillHandler(
            mockObjects.getAdapter(),
            mockObjects.getBot(),
            mockObjects.getConversationIdFactory(),
            mockObjects.getAuth());
        ResourceResponse response = sut.handleUpdateActivity(TEST_AUTH_HEADER, conversationId, activityToUpdate, activity).join();

        // Assert
        Assert.assertEquals("resourceId", response.getId());
        Assert.assertNotNull(mockObjects.getTurnContext().getTurnState().get(CloudSkillHandler.SKILL_CONVERSATION_REFERENCE_KEY));
        Assert.assertEquals(activityToUpdate, mockObjects.getTurnContext().getActivity().getId());
        Assert.assertEquals(activity.getText(), mockObjects.getUpdateActivity().getText());
    }

    /**
     * Helper class with mocks for adapter, bot and auth needed to instantiate CloudSkillHandler and run tests.
     * This class also captures the turnContext and activities sent back to the bot and the channel so we can run asserts on them.
     */
    private static class CloudSkillHandlerTestMocks {
        private static final String TEST_BOT_ID = UUID.randomUUID().toString().replace("-", "");
        private static final String TEST_BOT_ENDPOINT = "http://testbot.com/api/messages";
        private static final String TEST_SKILL_ENDPOINT = "http://testskill.com/api/messages";

        private final SkillConversationIdFactoryBase conversationIdFactory;
        private final BotAdapter adapter;
        private final BotFrameworkAuthentication auth;
        private final Bot bot;
        private TurnContext turnContext;
        private Activity channelActivity;
        private Activity botActivity;
        private Activity updateActivity;
        private String activityToDelete;

        public CloudSkillHandlerTestMocks() {
            adapter = createMockAdapter();
            auth = createMockBotFrameworkAuthentication();
            bot = createMockBot();
            conversationIdFactory = new TestSkillConversationIdFactory();
        }

        public SkillConversationIdFactoryBase getConversationIdFactory() {
            return conversationIdFactory;
        }

        public BotAdapter getAdapter() {
            return adapter;
        }

        public BotFrameworkAuthentication getAuth() { return auth; }

        public Bot getBot() { return bot; }

        // Gets the TurnContext created to call the bot.
        public TurnContext getTurnContext() {
            return turnContext;
        }

        /**
         * @return the Activity sent to the channel.
         */
        public Activity getChannelActivity() {
            return channelActivity;
        }

        /**
         * @return the Activity sent to the Bot.
         */
        public Activity getBotActivity() {
            return botActivity;
        }

        /**
         * @return the update activity.
         */
        public Activity getUpdateActivity() {
            return updateActivity;
        }

        /**
         * @return the Activity sent to the Bot.
         */
        public String getActivityIdToDelete() {
            return activityToDelete;
        }

        public CompletableFuture<String> createAndApplyConversationId(Activity activity) {
            ConversationReference conversationReference = new ConversationReference();
            ConversationAccount conversationAccount = new ConversationAccount();
            conversationAccount.setId(TEST_BOT_ID);
            conversationReference.setConversation(conversationAccount);
            conversationReference.setServiceUrl(TEST_BOT_ENDPOINT);

            activity.applyConversationReference(conversationReference);

            BotFrameworkSkill skill = new BotFrameworkSkill();
            skill.setAppId(TEST_SKILL_ID);
            skill.setId("skill");

            try {
                skill.setSkillEndpoint(new URI(TEST_SKILL_ENDPOINT));
            }
            catch (URISyntaxException ignored) {
            }

            SkillConversationIdFactoryOptions options = new SkillConversationIdFactoryOptions();
            options.setFromBotOAuthScope(TEST_BOT_ID);
            options.setFromBotId(TEST_BOT_ID);
            options.setActivity(activity);
            options.setBotFrameworkSkill(skill);

            return getConversationIdFactory().createSkillConversationId(options);
        }

        private BotAdapter createMockAdapter() {
            return new BotAdapter() {

                // Mock the adapter sendActivities method
                @Override
                public CompletableFuture<ResourceResponse[]> sendActivities(TurnContext context, List<Activity> activities) {
                    // (this for the cases where activity is sent back to the parent or channel)
                    // Capture the activity sent to the channel
                    channelActivity = activities.get(0);
                    // Do nothing, we don't want the activities sent to the channel in the tests.
                    return CompletableFuture.completedFuture(new ResourceResponse[]{new ResourceResponse("resourceId")});
                }

                // Mock the updateActivity method
                @Override
                public CompletableFuture<ResourceResponse> updateActivity(TurnContext context, Activity activity) {
                    updateActivity = activity;
                    return CompletableFuture.completedFuture(new ResourceResponse("resourceId"));
                }

                // Mock the deleteActivity method
                @Override
                public CompletableFuture<Void> deleteActivity(TurnContext context, ConversationReference reference) {
                    // Capture the activity id to delete so we can assert it.
                    activityToDelete = reference.getActivityId();
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletableFuture<Void> continueConversation(
                    ClaimsIdentity claimsIdentity,
                    ConversationReference reference,
                    String audience,
                    BotCallbackHandler callback
                ) {
                    // Mock the adapter ContinueConversationAsync method
                    // This code block catches and executes the custom bot callback created by the service handler.
                    turnContext = new TurnContextImpl(adapter, reference.getContinuationActivity());
                    return callback.invoke(turnContext).thenApply(val -> null);
                }
            };
        }

        private Bot createMockBot() {
            return new Bot() {
                @Override
                public CompletableFuture<Void> onTurn(TurnContext turnContext) {
                    botActivity = turnContext.getActivity();
                    return CompletableFuture.completedFuture(null);
                }
            };
        }

        private BotFrameworkAuthentication createMockBotFrameworkAuthentication() {
            return new BotFrameworkAuthentication() {
                public CompletableFuture<ClaimsIdentity> authenticateChannelRequest(String authHeader) {
                    HashMap<String, String> claims = new HashMap<>();
                    claims.put(AuthenticationConstants.AUDIENCE_CLAIM, TEST_BOT_ID);
                    claims.put(AuthenticationConstants.APPID_CLAIM, TEST_SKILL_ID);
                    claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, TEST_BOT_ENDPOINT);
                    ClaimsIdentity claimsIdentity = new ClaimsIdentity(AuthenticationConstants.ANONYMOUS_AUTH_TYPE, AuthenticationConstants.ANONYMOUS_AUTH_TYPE, claims);

                    return CompletableFuture.completedFuture(claimsIdentity);
                }

                @Override
                public CompletableFuture<AuthenticateRequestResult> authenticateRequest(Activity activity, String authHeader) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletableFuture<AuthenticateRequestResult> authenticateStreamingRequest(String authHeader, String channelIdHeader) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public ConnectorFactory createConnectorFactory(ClaimsIdentity claimsIdentity) {
                    return null;
                }

                @Override
                public CompletableFuture<UserTokenClient> createUserTokenClient(ClaimsIdentity claimsIdentity) {
                    return CompletableFuture.completedFuture(null);
                }
            };
        }
    }

    private static class TestSkillConversationIdFactory extends SkillConversationIdFactoryBase {
        private final ConcurrentHashMap<String, String> conversationRefs = new ConcurrentHashMap<>();

        public CompletableFuture<String> createSkillConversationId(SkillConversationIdFactoryOptions options) {
            SkillConversationReference skillConversationReference = new SkillConversationReference();
            skillConversationReference.setConversationReference(options.getActivity().getConversationReference());
            skillConversationReference.setOAuthScope(options.getFromBotOAuthScope());

            String key =
                String.format(
                    "%s-%s-%s-%s-skillconvo",
                    options.getFromBotId(),
                    options.getBotFrameworkSkill().getAppId(),
                    skillConversationReference.getConversationReference().getConversation().getId(),
                    skillConversationReference.getConversationReference().getChannelId());

            JacksonAdapter jacksonAdapter = new JacksonAdapter();
            try {
                conversationRefs.putIfAbsent(key, jacksonAdapter.serialize(skillConversationReference));
            }
            catch (IOException ignored) {
            }

            return CompletableFuture.completedFuture(key);
        }

        @Override
        public CompletableFuture<SkillConversationReference> getSkillConversationReference(String skillConversationId) {
            SkillConversationReference conversationReference = null;
            try {
                JacksonAdapter jacksonAdapter = new JacksonAdapter();
                conversationReference = jacksonAdapter.deserialize(
                    conversationRefs.get(skillConversationId),
                    SkillConversationReference.class);
            }
            catch (IOException ignored) {
            }

            return CompletableFuture.completedFuture(conversationReference);
        }

        @Override
        public CompletableFuture<Void> deleteConversationReference(String skillConversationId) {
            conversationRefs.remove(skillConversationId);
            return CompletableFuture.completedFuture(null);
        }
    }
}
