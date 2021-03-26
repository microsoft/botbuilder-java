// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.dialogs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TypedInvokeResponse;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.skills.BotFrameworkClient;
import com.microsoft.bot.builder.skills.BotFrameworkSkill;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryBase;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryOptions;
import com.microsoft.bot.builder.skills.SkillConversationReference;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.DeliveryModes;
import com.microsoft.bot.schema.ExpectedReplies;
import com.microsoft.bot.schema.OAuthCard;
import com.microsoft.bot.schema.TokenExchangeResource;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for SkillsDialog.
 */
public class SkillDialogTests {

    @Test
    public void ConstructorValidationTests() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new SkillDialog(null, null));
    }

    @Test
    public void BeginDialogOptionsValidation() {
        SkillDialogOptions dialogOptions = new SkillDialogOptions();
        SkillDialog sut = new SkillDialog(dialogOptions, null);

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
                DialogTestClient client = new DialogTestClient(Channels.TEST, sut, null, null, null);
                client.sendActivity("irrelevant").join();
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
                DialogTestClient client = new DialogTestClient(Channels.TEST, sut, new HashMap<String, String>(), null,
                        null);
                client.sendActivity("irrelevant").join();
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
                DialogTestClient client = new DialogTestClient(Channels.TEST, sut, new BeginSkillDialogOptions(), null,
                        null);
                client.sendActivity("irrelevant").join();
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });
    }

    @Test
    public void BeginDialogCallsSkill_null() {
        beginDialogCallsSkill(null);
    }

    @Test
    public void BeginDialogCallsSkill_Expect_Replies() {
        beginDialogCallsSkill(DeliveryModes.EXPECT_REPLIES.toString());
    }

    class MockFrameworkClient extends BotFrameworkClient {

        int returnStatus = 200;
        ExpectedReplies expectedReplies = null;;

        MockFrameworkClient() {

        }

        MockFrameworkClient(int returnStatus) {
            this.returnStatus = returnStatus;
        }

        MockFrameworkClient(int returnStatus, ExpectedReplies expectedReplies) {
            this.returnStatus = returnStatus;
            this.expectedReplies = expectedReplies;
        }

        @Override
        public <T> CompletableFuture<TypedInvokeResponse<T>> postActivity(
            String fromBotId,
            String toBotId,
            URI toUri,
            URI serviceUrl,
            String conversationId,
            Activity activity,
            Class<T> type
        ) {
            fromBotIdSent = fromBotId;
            toBotIdSent = toBotId;
            toUriSent = toUri;
            activitySent = activity;
            List<Activity> activities = new ArrayList<Activity>();
            activities.add(MessageFactory.text("dummy activity"));
            ExpectedReplies activityList = new ExpectedReplies(activities);
            if (expectedReplies != null) {
                TypedInvokeResponse<T> response = new TypedInvokeResponse(returnStatus, expectedReplies);
                return CompletableFuture.completedFuture(response);
            } else {
                TypedInvokeResponse<T> response = new TypedInvokeResponse(returnStatus, activityList);
                return CompletableFuture.completedFuture(response);
            }
        }

        public String fromBotIdSent;
        public String toBotIdSent;
        public URI toUriSent;
        public Activity activitySent;
    }

    class MockFrameworkClientExtended extends BotFrameworkClient {

        int returnStatus = 200;
        ExpectedReplies expectedReplies = null;
        int iterationCount = 0;

        MockFrameworkClientExtended(int returnStatus, ExpectedReplies expectedReplies) {
            this.returnStatus = returnStatus;
            this.expectedReplies = expectedReplies;
        }

        @Override
        public <T> CompletableFuture<TypedInvokeResponse<T>> postActivity(
            String fromBotId,
            String toBotId,
            URI toUri,
            URI serviceUrl,
            String conversationId,
            Activity activity,
            Class<T> type
        ) {
            fromBotIdSent = fromBotId;
            toBotIdSent = toBotId;
            toUriSent = toUri;
            activitySent = activity;
            List<Activity> activities = new ArrayList<Activity>();
            activities.add(MessageFactory.text("dummy activity"));
            ExpectedReplies activityList = new ExpectedReplies(activities);
            if (iterationCount == 0) {
                TypedInvokeResponse<T> response = new TypedInvokeResponse(200, expectedReplies);
                iterationCount++;
                return CompletableFuture.completedFuture(response);
            } else {
                TypedInvokeResponse<T> response = new TypedInvokeResponse(returnStatus, null);
                return CompletableFuture.completedFuture(response);
            }
        }

        public String fromBotIdSent;
        public String toBotIdSent;
        public URI toUriSent;
        public Activity activitySent;
    }




    public void beginDialogCallsSkill(String deliveryMode) {

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient();

        // Use Memory for conversation state
        ConversationState conversationState = new ConversationState(new MemoryStorage());
        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);

        // Create the SkillDialogInstance and the activity to send.
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        Activity activityToSend = Activity.createMessageActivity();
        activityToSend.setDeliveryMode(deliveryMode);
        activityToSend.setText(UUID.randomUUID().toString());
        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
                                        Channels.TEST,
                                        sut,
                                        skillDialogOptions,
                                        null,
                                        conversationState);

        Assert.assertEquals(0, ((SimpleConversationIdFactory) dialogOptions.getConversationIdFactory()).createCount);

        // Send something to the dialog to start it
        client.sendActivity("irrelevant").join();

        // Assert results and data sent to the SkillClient for fist turn
        Assert.assertEquals(1, ((SimpleConversationIdFactory) dialogOptions.getConversationIdFactory()).createCount);
        Assert.assertEquals(dialogOptions.getBotId(), mockSkillClient.fromBotIdSent);
        Assert.assertEquals(dialogOptions.getSkill().getAppId(), mockSkillClient.toBotIdSent);
        Assert.assertEquals(dialogOptions.getSkill().getSkillEndpoint().toString(),
                                        mockSkillClient.toUriSent.toString());
        Assert.assertEquals(activityToSend.getText(), mockSkillClient.activitySent.getText());
        Assert.assertEquals(DialogTurnStatus.WAITING, client.getDialogTurnResult().getStatus());

        // Send a second message to continue the dialog
        client.sendActivity("Second message").join();
        Assert.assertEquals(1, ((SimpleConversationIdFactory) dialogOptions.getConversationIdFactory()).createCount);

        // Assert results for second turn
        Assert.assertEquals("Second message", mockSkillClient.activitySent.getText());
        Assert.assertEquals(DialogTurnStatus.WAITING, client.getDialogTurnResult().getStatus());

        // Send EndOfConversation to the dialog
        client.sendActivity(Activity.createEndOfConversationActivity()).join();

        // Assert we are done.
        Assert.assertEquals(DialogTurnStatus.COMPLETE, client.getDialogTurnResult().getStatus());
    }

    @Test
    public void ShouldHandleInvokeActivities() {

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient();

        // Use Memory for conversation state
        ConversationState conversationState = new ConversationState(new MemoryStorage());
        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);

        Activity activityToSend = Activity.createInvokeActivity();
        activityToSend.setName(UUID.randomUUID().toString());

        // Create the SkillDialogInstance and the activity to send.
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
                                        Channels.TEST,
                                        sut,
                                        skillDialogOptions,
                                        null,
                                        conversationState);

        // Send something to the dialog to start it
        client.sendActivity("irrelevant").join();

        // Assert results and data sent to the SkillClient for fist turn
        Assert.assertEquals(dialogOptions.getBotId(), mockSkillClient.fromBotIdSent);
        Assert.assertEquals(dialogOptions.getSkill().getAppId(), mockSkillClient.toBotIdSent);
        Assert.assertEquals(dialogOptions.getSkill().getSkillEndpoint().toString(),
                                        mockSkillClient.toUriSent.toString());
        Assert.assertEquals(activityToSend.getName(), mockSkillClient.activitySent.getName());
        Assert.assertEquals(DeliveryModes.EXPECT_REPLIES.toString(), mockSkillClient.activitySent.getDeliveryMode());
        Assert.assertEquals(activityToSend.getText(), mockSkillClient.activitySent.getText());
        Assert.assertEquals(DialogTurnStatus.WAITING, client.getDialogTurnResult().getStatus());

        // Send a second message to continue the dialog
        client.sendActivity("Second message").join();

        // Assert results for second turn
        Assert.assertEquals("Second message", mockSkillClient.activitySent.getText());
        Assert.assertEquals(DialogTurnStatus.WAITING, client.getDialogTurnResult().getStatus());

        // Send EndOfConversation to the dialog
        client.sendActivity(Activity.createEndOfConversationActivity()).join();

        // Assert we are done.
        Assert.assertEquals(DialogTurnStatus.COMPLETE, client.getDialogTurnResult().getStatus());
    }

    @Test
    public void CancelDialogSendsEoC() {
        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient();

        // Use Memory for conversation state
        ConversationState conversationState = new ConversationState(new MemoryStorage());
        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);

        Activity activityToSend = Activity.createMessageActivity();
        activityToSend.setName(UUID.randomUUID().toString());

        // Create the SkillDialogInstance and the activity to send.
        SkillDialog sut = new SkillDialog(dialogOptions, null);

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
                                        Channels.TEST,
                                        sut,
                                        skillDialogOptions,
                                        null,
                                        conversationState);

        // Send something to the dialog to start it
        client.sendActivity("irrelevant").join();

        // Cancel the dialog so it sends an EoC to the skill
         client.getDialogContext().cancelAllDialogs();

        Assert.assertEquals(ActivityTypes.END_OF_CONVERSATION, mockSkillClient.activitySent.getType());
    }

    @Test
    public void ShouldThrowHttpExceptionOnPostFailure() {

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient(500);

        // Use Memory for conversation state
        ConversationState conversationState = new ConversationState(new MemoryStorage());
        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);

        Activity activityToSend = Activity.createMessageActivity();
        activityToSend.setName(UUID.randomUUID().toString());

        // Create the SkillDialogInstance and the activity to send.
        SkillDialog sut = new SkillDialog(dialogOptions, null);

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
                                        Channels.TEST,
                                        sut,
                                        skillDialogOptions,
                                        null,
                                        conversationState);

        // Send something to the dialog
        Assert.assertThrows(Exception.class, () -> client.sendActivity("irrelevant").join());
    }

    @Test
    public void ShouldInterceptOAuthCardsForSso() {
        String connectionName = "connectionName";
        List<Activity> replyList = new ArrayList<Activity>();
        replyList.add(createOAuthCardAttachmentActivity("https://test"));
        ExpectedReplies firstResponse = new ExpectedReplies();
        firstResponse.setActivities(replyList);

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient(200, firstResponse);

        ConversationState conversationState = new ConversationState(new MemoryStorage());
        TestAdapter testAdapter = new TestAdapter(Channels.TEST)
            .use(new AutoSaveStateMiddleware(conversationState));

        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, connectionName);
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        Activity activityToSend = createSendActivity();

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
            testAdapter,
            sut,
            skillDialogOptions,
            null,
            conversationState);

        testAdapter.addExchangeableToken(connectionName, Channels.TEST, "user1", "https://test", "https://test1");
        Activity finalActivity =  client.sendActivity("irrelevant").join();
        Assert.assertNull(finalActivity);
    }

    @Test
    public void ShouldNotInterceptOAuthCardsForEmptyConnectionName() {
        String connectionName = "connectionName";
        List<Activity> replyList = new ArrayList<Activity>();
        replyList.add(createOAuthCardAttachmentActivity("https://test"));
        ExpectedReplies firstResponse = new ExpectedReplies();
        firstResponse.setActivities(replyList);

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient(200, firstResponse);

        ConversationState conversationState = new ConversationState(new MemoryStorage());
        TestAdapter testAdapter = new TestAdapter(Channels.TEST)
            .use(new AutoSaveStateMiddleware(conversationState));

        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        Activity activityToSend = createSendActivity();

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
            testAdapter,
            sut,
            skillDialogOptions,
            null,
            conversationState);

        testAdapter.addExchangeableToken(connectionName, Channels.TEST, "user1", "https://test", "https://test1");
        Activity finalActivity =  client.sendActivity("irrelevant").join();
        Assert.assertNotNull(finalActivity);
        Assert.assertTrue(finalActivity.getAttachments().size() == 1);
    }

    @Test
    public void ShouldNotInterceptOAuthCardsForEmptyToken() {
        List<Activity> replyList = new ArrayList<Activity>();
        replyList.add(createOAuthCardAttachmentActivity("https://test"));
        ExpectedReplies firstResponse = new ExpectedReplies();
        firstResponse.setActivities(replyList);

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient(200, firstResponse);

        ConversationState conversationState = new ConversationState(new MemoryStorage());
        TestAdapter testAdapter = new TestAdapter(Channels.TEST)
            .use(new AutoSaveStateMiddleware(conversationState));

        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        Activity activityToSend = createSendActivity();

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
            testAdapter,
            sut,
            skillDialogOptions,
            null,
            conversationState);

        Activity finalActivity =  client.sendActivity("irrelevant").join();
        Assert.assertNotNull(finalActivity);
        Assert.assertTrue(finalActivity.getAttachments().size() == 1);
    }

    @Test
    public void ShouldNotInterceptOAuthCardsForTokenException() {
        String connectionName = "connectionName";
        List<Activity> replyList = new ArrayList<Activity>();
        replyList.add(createOAuthCardAttachmentActivity("https://test"));
        ExpectedReplies firstResponse = new ExpectedReplies();
        firstResponse.setActivities(replyList);

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient(200, firstResponse);

        ConversationState conversationState = new ConversationState(new MemoryStorage());
        TestAdapter testAdapter = new TestAdapter(Channels.TEST)
            .use(new AutoSaveStateMiddleware(conversationState));

        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        Activity activityToSend = createSendActivity();

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
            testAdapter,
            sut,
            skillDialogOptions,
            null,
            conversationState);

        testAdapter.throwOnExchangeRequest(connectionName, Channels.TEST, "user1", "https://test");
        Activity finalActivity =  client.sendActivity("irrelevant").join();
        Assert.assertNotNull(finalActivity);
        Assert.assertTrue(finalActivity.getAttachments().size() == 1);
    }

    @Test
    public void ShouldNotInterceptOAuthCardsForBadRequest() {
        List<Activity> replyList = new ArrayList<Activity>();
        replyList.add(createOAuthCardAttachmentActivity("https://test"));
        ExpectedReplies firstResponse = new ExpectedReplies();
        firstResponse.setActivities(replyList);

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClientExtended mockSkillClient = new MockFrameworkClientExtended(409, firstResponse);

        ConversationState conversationState = new ConversationState(new MemoryStorage());
        TestAdapter testAdapter = new TestAdapter(Channels.TEST)
            .use(new AutoSaveStateMiddleware(conversationState));

        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, null);
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        Activity activityToSend = createSendActivity();

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
            testAdapter,
            sut,
            skillDialogOptions,
            null,
            conversationState);

        Activity finalActivity =  client.sendActivity("irrelevant").join();
        Assert.assertNotNull(finalActivity);
        Assert.assertTrue(finalActivity.getAttachments().size() == 1);
    }

    @Test
    public void EndOfConversationFromExpectRepliesCallsDeleteConversationReference() {
        List<Activity> replyList = new ArrayList<Activity>();
        replyList.add(Activity.createEndOfConversationActivity());
        ExpectedReplies firstResponse = new ExpectedReplies();
        firstResponse.setActivities(replyList);

        // Create a mock skill client to intercept calls and capture what is sent.
        MockFrameworkClient mockSkillClient = new MockFrameworkClient(200, firstResponse);

        ConversationState conversationState = new ConversationState(new MemoryStorage());

        SkillDialogOptions dialogOptions = createSkillDialogOptions(conversationState, mockSkillClient, "");
        SkillDialog sut = new SkillDialog(dialogOptions, null);
        Activity activityToSend = Activity.createMessageActivity();
        activityToSend.setDeliveryMode(DeliveryModes.EXPECT_REPLIES.toString());
        activityToSend.setText(UUID.randomUUID().toString());

        BeginSkillDialogOptions skillDialogOptions = new BeginSkillDialogOptions();
        skillDialogOptions.setActivity(activityToSend);
        DialogTestClient client = new DialogTestClient(
            Channels.TEST,
            sut,
            skillDialogOptions,
            null,
            conversationState);

        // Send something to the dialog to start it
        client.sendActivity("hello").join();

        SimpleConversationIdFactory factory = null;
        if (dialogOptions.getConversationIdFactory() != null
            && dialogOptions.getConversationIdFactory() instanceof SimpleConversationIdFactory){
                factory = (SimpleConversationIdFactory) dialogOptions.getConversationIdFactory();
            }
        Assert.assertNotNull(factory);
        Assert.assertEquals(factory.getConversationRefs().size(), 0);
        Assert.assertEquals(1, factory.getCreateCount());
    }


    private static Activity createOAuthCardAttachmentActivity(String uri) {

        OAuthCard oauthCard = new OAuthCard();
        TokenExchangeResource tokenExchangeResource = new TokenExchangeResource();
        tokenExchangeResource.setUri(uri);
        oauthCard.setTokenExchangeResource(tokenExchangeResource);
        Attachment attachment = new Attachment();
        attachment.setContentType(OAuthCard.CONTENTTYPE);
        attachment.setContent(oauthCard);

        Activity attachmentActivity = MessageFactory.attachment(attachment);
        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setId(UUID.randomUUID().toString());
        attachmentActivity.setConversation(conversationAccount);
        attachmentActivity.setFrom(new ChannelAccount("blah", "name"));

        return attachmentActivity;
    }

    /**
     * Helper to create a {@link SkillDialogOptions} for the skillDialog.
     *
     * @param conversationState The conversation state Object.
     * @param mockSkillClient   The skill client mock.
     *
     * @return A Skill Dialog Options Object.
     */
    private SkillDialogOptions createSkillDialogOptions(ConversationState conversationState,
            BotFrameworkClient mockSkillClient, String connectionName) {
        SkillDialogOptions dialogOptions = new SkillDialogOptions();
        dialogOptions.setBotId(UUID.randomUUID().toString());
        try {
            dialogOptions.setSkillHostEndpoint(new URI("http://test.contoso.com/skill/messages"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        dialogOptions.setConversationIdFactory(new SimpleConversationIdFactory());
        dialogOptions.setConversationState(conversationState);
        dialogOptions.setSkillClient(mockSkillClient);
        BotFrameworkSkill skill = new BotFrameworkSkill();
        skill.setAppId(UUID.randomUUID().toString());
        try {
            skill.setSkillEndpoint(new URI("http://testskill.contoso.com/api/messages"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        dialogOptions.setSkill(skill);
        dialogOptions.setConnectionName(connectionName);
        return dialogOptions;
    }

    // private static Mock<BotFrameworkClient> CreateMockSkillClient(
    // Action<String, String, Uri, Uri, String, Activity, CancellationToken>
    // captureAction,
    // int returnStatus=200,List<Activity>expectedReplies) {
    // var mockSkillClient=new Mock<BotFrameworkClient>();var activityList=new
    // ExpectedReplies(expectedReplies??new
    // List<Activity>{MessageFactory.Text("dummy activity")});

    // if(captureAction!=null){mockSkillClient.Setup(x->x.PostActivity<ExpectedReplies>(It.IsAny<String>(),It.IsAny<String>(),It.IsAny<Uri>(),It.IsAny<Uri>(),It.IsAny<String>(),It.IsAny<Activity>(),It.IsAny<CancellationToken>())).Returns(Task.FromResult(new
    // InvokeResponse<ExpectedReplies>{Status=returnStatus,Body=activityList})).Callback(captureAction);}else{mockSkillClient.Setup(x->x.PostActivity<ExpectedReplies>(It.IsAny<String>(),It.IsAny<String>(),It.IsAny<Uri>(),It.IsAny<Uri>(),It.IsAny<String>(),It.IsAny<Activity>(),It.IsAny<CancellationToken>())).Returns(Task.FromResult(new
    // InvokeResponse<ExpectedReplies>{Status=returnStatus,Body=activityList}));}

    // return mockSkillClient;
    // }

    private Activity createSendActivity() {
        Activity activityToSend = Activity.createMessageActivity();
        activityToSend.setDeliveryMode(DeliveryModes.EXPECT_REPLIES.toString());
        activityToSend.setText(UUID.randomUUID().toString());
        return activityToSend;
    }

    /**
     * Simple factory to that extends SkillConversationIdFactoryBase.
     */
    protected class SimpleConversationIdFactory extends SkillConversationIdFactoryBase {

        // Helper property to assert how many times instanceof
        // CreateSkillConversationIdAsync called.
        private int createCount;
        private Map<String, SkillConversationReference> conversationRefs = new HashMap<String, SkillConversationReference>();

        protected SimpleConversationIdFactory() {

        }

        @Override
        public CompletableFuture<String> createSkillConversationId(SkillConversationIdFactoryOptions options) {
            createCount++;

            String key = Integer.toString(String.format("%s%s", options.getActivity().getConversation().getId(),
                    options.getActivity().getServiceUrl()).hashCode());
            SkillConversationReference skillConversationReference = new SkillConversationReference();
            skillConversationReference.setConversationReference(options.getActivity().getConversationReference());
            skillConversationReference.setOAuthScope(options.getFromBotOAuthScope());
            conversationRefs.put(key, skillConversationReference);
            return CompletableFuture.completedFuture(key);
        }

        @Override
        public CompletableFuture<SkillConversationReference> getSkillConversationReference(String skillConversationId) {
            return CompletableFuture.completedFuture(conversationRefs.get(skillConversationId));
        }

        @Override
        public CompletableFuture<Void> deleteConversationReference(String skillConversationId) {

            conversationRefs.remove(skillConversationId);
            return CompletableFuture.completedFuture(null);
        }

        /**
         * @return the ConversationRefs value as a Map<String,
         *         SkillConversationReference>.
         */
        public Map<String, SkillConversationReference> getConversationRefs() {
            return this.conversationRefs;
        }

        /**
         * @param withConversationRefs The ConversationRefs value.
         */
        private void setConversationRefs(Map<String, SkillConversationReference> withConversationRefs) {
            this.conversationRefs = withConversationRefs;
        }

        /**
         * @return the CreateCount value as a int.
         */
        public int getCreateCount() {
            return this.createCount;
        }

        /**
         * @param withCreateCount The CreateCount value.
         */
        private void setCreateCount(int withCreateCount) {
            this.createCount = withCreateCount;
        }
    }
}
