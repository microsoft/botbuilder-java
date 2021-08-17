// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.azure.blobs.BlobsTranscriptStore;
import com.microsoft.bot.builder.PagedResult;
import com.microsoft.bot.builder.TranscriptInfo;
import com.microsoft.bot.builder.TranscriptLoggerMiddleware;
import com.microsoft.bot.builder.TranscriptStore;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * These tests require Azure Storage Emulator v5.7 The emulator must be
 * installed at this path C:\Program Files (x86)\Microsoft SDKs\Azure\Storage
 * Emulator\AzureStorageEmulator.exe More info:
 * https://docs.microsoft.com/azure/storage/common/storage-use-emulator
 */
public class TranscriptStoreTests {

    @Rule
    public TestName TEST_NAME = new TestName();

    protected String blobStorageEmulatorConnectionString = "AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;DefaultEndpointsProtocol=http;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;";

    private String channelId = "test";

    private static final String[] CONVERSATION_IDS = { "qaz", "wsx", "edc", "rfv", "tgb", "yhn", "ujm", "123", "456",
            "789", "ZAQ", "XSW", "CDE", "VFR", "BGT", "NHY", "NHY", "098", "765", "432", "zxc", "vbn", "mlk", "jhy",
            "yui", "kly", "asd", "asw", "aaa", "zzz", };

    private static final String[] CONVERSATION_SPECIAL_IDS = { "asd !&/#.'+:?\"", "ASD@123<>|}{][", "$%^;\\*()_" };

    private String getContainerName() {
        return String.format("blobstranscript%s", TEST_NAME.getMethodName().toLowerCase());
    }

    private TranscriptStore getTranscriptStore() {
        return new BlobsTranscriptStore(blobStorageEmulatorConnectionString, getContainerName());
    }

    @Before
    public void beforeTest() {
        org.junit.Assume.assumeTrue(AzureEmulatorUtils.isStorageEmulatorAvailable());
    }

    @After
    public void testCleanup() {
        if (AzureEmulatorUtils.isStorageEmulatorAvailable()) {
            BlobContainerClient containerClient = new BlobContainerClientBuilder()
                    .connectionString(blobStorageEmulatorConnectionString).containerName(getContainerName())
                    .buildClient();

            if (containerClient.exists()) {
                containerClient.delete();
            }
        }
    }

    // These tests require Azure Storage Emulator v5.7
    @Test
    public void blobTranscriptParamTest() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new BlobsTranscriptStore(null, getContainerName()));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new BlobsTranscriptStore(blobStorageEmulatorConnectionString, null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new BlobsTranscriptStore(new String(), getContainerName()));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new BlobsTranscriptStore(blobStorageEmulatorConnectionString, new String()));
    }

    @Test
    public void transcriptsEmptyTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        String unusedChannelId = UUID.randomUUID().toString();
        PagedResult<TranscriptInfo> transcripts = transcriptStore.listTranscripts(unusedChannelId).join();
        Assert.assertEquals(0, transcripts.getItems().size());
    }

    @Test
    public void activityEmptyTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        for (String convoId : CONVERSATION_SPECIAL_IDS) {
            PagedResult<Activity> activities = transcriptStore.getTranscriptActivities(channelId, convoId).join();
            Assert.assertEquals(0, activities.getItems().size());
        }
    }

    @Test
    public void activityAddTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        Activity[] loggedActivities = new Activity[5];
        List<Activity> activities = new ArrayList<Activity>();
        for (int i = 0; i < 5; i++) {
            Activity a = TranscriptStoreTests.createActivity(i, i, CONVERSATION_IDS);
            transcriptStore.logActivity(a).join();
            activities.add(a);
            loggedActivities[i] = transcriptStore.getTranscriptActivities(channelId, CONVERSATION_IDS[i]).join()
                    .getItems().get(0);
        }

        Assert.assertEquals(5, loggedActivities.length);
    }

    @Test
    public void transcriptRemoveTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        for (int i = 0; i < 5; i++) {
            Activity a = TranscriptStoreTests.createActivity(i, i, CONVERSATION_IDS);
            transcriptStore.logActivity(a).join();
            transcriptStore.deleteTranscript(a.getChannelId(), a.getConversation().getId()).join();

            PagedResult<Activity> loggedActivities = transcriptStore
                    .getTranscriptActivities(channelId, CONVERSATION_IDS[i]).join();

            Assert.assertEquals(0, loggedActivities.getItems().size());
        }
    }

    @Test
    public void activityAddSpecialCharsTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        Activity[] loggedActivities = new Activity[CONVERSATION_SPECIAL_IDS.length];
        List<Activity> activities = new ArrayList<Activity>();
        for (int i = 0; i < CONVERSATION_SPECIAL_IDS.length; i++) {
            Activity a = TranscriptStoreTests.createActivity(i, i, CONVERSATION_SPECIAL_IDS);
            transcriptStore.logActivity(a).join();
            activities.add(a);
            int pos = i;
            transcriptStore.getTranscriptActivities(channelId, CONVERSATION_SPECIAL_IDS[i]).thenAccept(result -> {
                loggedActivities[pos] = result.getItems().get(0);
            });
        }

        Assert.assertEquals(activities.size(), loggedActivities.length);
    }

    @Test
    public void transcriptRemoveSpecialCharsTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        for (int i = 0; i < CONVERSATION_SPECIAL_IDS.length; i++) {
            Activity a = TranscriptStoreTests.createActivity(i, i, CONVERSATION_SPECIAL_IDS);
            transcriptStore.deleteTranscript(a.getChannelId(), a.getConversation().getId()).join();

            PagedResult<Activity> loggedActivities = transcriptStore
                    .getTranscriptActivities(channelId, CONVERSATION_SPECIAL_IDS[i]).join();
            Assert.assertEquals(0, loggedActivities.getItems().size());
        }
    }

    @Test
    public void activityAddPagedResultTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        String cleanChannel = UUID.randomUUID().toString();

        List<Activity> activities = new ArrayList<Activity>();

        for (int i = 0; i < CONVERSATION_IDS.length; i++) {
            Activity a = TranscriptStoreTests.createActivity(0, i, CONVERSATION_IDS);
            a.setChannelId(cleanChannel);

            transcriptStore.logActivity(a).join();
            activities.add(a);
        }

        PagedResult<Activity> loggedPagedResult = transcriptStore
                .getTranscriptActivities(cleanChannel, CONVERSATION_IDS[0]).join();
        String ct = loggedPagedResult.getContinuationToken();
        Assert.assertEquals(20, loggedPagedResult.getItems().size());
        Assert.assertNotNull(ct);
        Assert.assertTrue(loggedPagedResult.getContinuationToken().length() > 0);
        loggedPagedResult = transcriptStore.getTranscriptActivities(cleanChannel, CONVERSATION_IDS[0], ct).join();
        ct = loggedPagedResult.getContinuationToken();
        Assert.assertEquals(10, loggedPagedResult.getItems().size());
        Assert.assertNull(ct);
    }

    @Test
    public void transcriptRemovePagedTest() {
        TranscriptStore transcriptStore = getTranscriptStore();
        int i;
        for (i = 0; i < CONVERSATION_SPECIAL_IDS.length; i++) {
            Activity a = TranscriptStoreTests.createActivity(i, i, CONVERSATION_IDS);
            transcriptStore.deleteTranscript(a.getChannelId(), a.getConversation().getId()).join();
        }

        PagedResult<Activity> loggedActivities = transcriptStore.getTranscriptActivities(channelId, CONVERSATION_IDS[i])
                .join();
        Assert.assertEquals(0, loggedActivities.getItems().size());
    }

    @Test
    public void nullParameterTests() {
        TranscriptStore store = getTranscriptStore();

        Assert.assertThrows(IllegalArgumentException.class, () -> store.logActivity(null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> store.getTranscriptActivities(null, CONVERSATION_IDS[0]));
        Assert.assertThrows(IllegalArgumentException.class, () -> store.getTranscriptActivities(channelId, null));
    }

    @Test
    public void logActivities() {
        TranscriptStore transcriptStore = getTranscriptStore();
        ConversationReference conversation = TestAdapter.createConversationReference(UUID.randomUUID().toString(),
                "User1", "Bot");
        TestAdapter adapter = new TestAdapter(conversation).use(new TranscriptLoggerMiddleware(transcriptStore));
        new TestFlow(adapter, turnContext -> {
            delay(500);
            Activity typingActivity = new Activity(ActivityTypes.TYPING);
            typingActivity.setRelatesTo(turnContext.getActivity().getRelatesTo());
            turnContext.sendActivity(typingActivity).join();
            delay(500);
            turnContext.sendActivity(String.format("echo:%s", turnContext.getActivity().getText())).join();
            return CompletableFuture.completedFuture(null);
        }).send("foo").assertReply(activity -> Assert.assertTrue(activity.isType(ActivityTypes.TYPING)))
                .assertReply("echo:foo").send("bar")
                .assertReply(activity -> Assert.assertTrue(activity.isType(ActivityTypes.TYPING)))
                .assertReply("echo:bar").startTest().join();

        PagedResult<Activity> pagedResult = null;
        try {
            pagedResult = this.getPagedResult(conversation, 6, null).join();
        } catch (TimeoutException ex) {
            Assert.fail();
        }
        Assert.assertEquals(6, pagedResult.getItems().size());
        Assert.assertTrue(pagedResult.getItems().get(0).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("foo", pagedResult.getItems().get(0).getText());
        Assert.assertNotNull(pagedResult.getItems().get(1));
        Assert.assertTrue(pagedResult.getItems().get(1).isType(ActivityTypes.TYPING));
        Assert.assertTrue(pagedResult.getItems().get(2).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("echo:foo", pagedResult.getItems().get(2).getText());
        Assert.assertTrue(pagedResult.getItems().get(3).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("bar", pagedResult.getItems().get(3).getText());
        Assert.assertNotNull(pagedResult.getItems().get(4));
        Assert.assertTrue(pagedResult.getItems().get(4).isType(ActivityTypes.TYPING));
        Assert.assertTrue(pagedResult.getItems().get(5).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("echo:bar", pagedResult.getItems().get(5).getText());
        for (Activity activity : pagedResult.getItems()) {
            Assert.assertTrue(!StringUtils.isBlank(activity.getId()));
            Assert.assertTrue(activity.getTimestamp().isAfter(OffsetDateTime.MIN));
        }
    }

    @Test
    public void logUpdateActivities() {
        TranscriptStore transcriptStore = getTranscriptStore();
        ConversationReference conversation = TestAdapter.createConversationReference(UUID.randomUUID().toString(),
                "User1", "Bot");
        TestAdapter adapter = new TestAdapter(conversation).use(new TranscriptLoggerMiddleware(transcriptStore));
        final Activity[] activityToUpdate = { null };
        new TestFlow(adapter, turnContext -> {
            delay(500);
            if (turnContext.getActivity().getText().equals("update")) {
                activityToUpdate[0].setText("new response");
                turnContext.updateActivity(activityToUpdate[0]).join();
            } else {
                Activity activity = turnContext.getActivity().createReply("response");
                ResourceResponse response = turnContext.sendActivity(activity).join();
                activity.setId(response.getId());

                ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
                try {
                    // clone the activity, so we can use it to do an update
                    activityToUpdate[0] = objectMapper.readValue(objectMapper.writeValueAsString(activity),
                            Activity.class);
                } catch (JsonProcessingException ex) {
                    ex.printStackTrace();
                }
            }
            return CompletableFuture.completedFuture(null);
        }).send("foo").send("update").assertReply("new response").startTest().join();

        PagedResult<Activity> pagedResult = null;
        try {
            pagedResult = this.getPagedResult(conversation, 3, null).join();
        } catch (TimeoutException ex) {
            Assert.fail();
        }

        Assert.assertEquals(3, pagedResult.getItems().size());
        Assert.assertTrue(pagedResult.getItems().get(0).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("foo", pagedResult.getItems().get(0).getText());
        Assert.assertTrue(pagedResult.getItems().get(1).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("new response", pagedResult.getItems().get(1).getText());
        Assert.assertTrue(pagedResult.getItems().get(2).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("update", pagedResult.getItems().get(2).getText());
    }

    @Test
    public void logMissingUpdateActivity() {
        TranscriptStore transcriptStore = getTranscriptStore();
        ConversationReference conversation = TestAdapter.createConversationReference(UUID.randomUUID().toString(),
                "User1", "Bot");
        TestAdapter adapter = new TestAdapter(conversation).use(new TranscriptLoggerMiddleware(transcriptStore));
        final String[] fooId = { new String() };
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        new TestFlow(adapter, turnContext -> {
            fooId[0] = turnContext.getActivity().getId();
            Activity updateActivity = null;
            try {
                // clone the activity, so we can use it to do an update
                updateActivity = objectMapper.readValue(objectMapper.writeValueAsString(turnContext.getActivity()),
                        Activity.class);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
            updateActivity.setText("updated response");
            ResourceResponse response = turnContext.updateActivity(updateActivity).join();
            return CompletableFuture.completedFuture(null);
        }).send("foo").startTest().join();

        delay(3000);

        PagedResult<Activity> pagedResult = null;
        try {
            pagedResult = this.getPagedResult(conversation, 2, null).join();
        } catch (TimeoutException ex) {
            Assert.fail();
        }

        Assert.assertEquals(2, pagedResult.getItems().size());
        Assert.assertTrue(pagedResult.getItems().get(0).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals(fooId[0], pagedResult.getItems().get(0).getId());
        Assert.assertEquals("foo", pagedResult.getItems().get(0).getText());
        Assert.assertTrue(pagedResult.getItems().get(1).isType(ActivityTypes.MESSAGE));
        Assert.assertTrue(pagedResult.getItems().get(1).getId().startsWith("g_"));
        Assert.assertEquals("updated response", pagedResult.getItems().get(1).getText());
    }

    @Test
    public void testDateLogUpdateActivities() {
        TranscriptStore transcriptStore = getTranscriptStore();
        OffsetDateTime dateTimeStartOffset1 = OffsetDateTime.now();
        ConversationReference conversation = TestAdapter.createConversationReference(UUID.randomUUID().toString(),
                "User1", "Bot");
        TestAdapter adapter = new TestAdapter(conversation).use(new TranscriptLoggerMiddleware(transcriptStore));
        final Activity[] activityToUpdate = { null };
        new TestFlow(adapter, turnContext -> {
            if (turnContext.getActivity().getText().equals("update")) {
                activityToUpdate[0].setText("new response");
                turnContext.updateActivity(activityToUpdate[0]).join();
            } else {
                Activity activity = turnContext.getActivity().createReply("response");

                ResourceResponse response = turnContext.sendActivity(activity).join();
                activity.setId(response.getId());

                ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
                try {
                    // clone the activity, so we can use it to do an update
                    activityToUpdate[0] = objectMapper.readValue(objectMapper.writeValueAsString(activity),
                            Activity.class);
                } catch (JsonProcessingException ex) {
                    ex.printStackTrace();
                }
            }
            return CompletableFuture.completedFuture(null);
        }).send("foo").send("update").assertReply("new response").startTest().join();

        try {
            TimeUnit.MILLISECONDS.sleep(5000);
        } catch (InterruptedException e) {
            // Empty error
        }

        // Perform some queries
        PagedResult<Activity> pagedResult = transcriptStore.getTranscriptActivities(conversation.getChannelId(),
                conversation.getConversation().getId(), null, dateTimeStartOffset1).join();
        Assert.assertEquals(3, pagedResult.getItems().size());
        Assert.assertTrue(pagedResult.getItems().get(0).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("foo", pagedResult.getItems().get(0).getText());
        Assert.assertTrue(pagedResult.getItems().get(1).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("new response", pagedResult.getItems().get(1).getText());
        Assert.assertTrue(pagedResult.getItems().get(2).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("update", pagedResult.getItems().get(2).getText());

        // Perform some queries
        pagedResult = transcriptStore.getTranscriptActivities(conversation.getChannelId(),
                conversation.getConversation().getId(), null, OffsetDateTime.MIN).join();
        Assert.assertEquals(3, pagedResult.getItems().size());
        Assert.assertTrue(pagedResult.getItems().get(0).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("foo", pagedResult.getItems().get(0).getText());
        Assert.assertTrue(pagedResult.getItems().get(1).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("new response", pagedResult.getItems().get(1).getText());
        Assert.assertTrue(pagedResult.getItems().get(2).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("update", pagedResult.getItems().get(2).getText());

        // Perform some queries
        pagedResult = transcriptStore.getTranscriptActivities(conversation.getChannelId(),
                conversation.getConversation().getId(), null, OffsetDateTime.MAX).join();
        Assert.assertEquals(0, pagedResult.getItems().size());
    }

    @Test
    public void logDeleteActivities() {
        TranscriptStore transcriptStore = getTranscriptStore();

        ConversationReference conversation = TestAdapter.createConversationReference(UUID.randomUUID().toString(),
                "User1", "Bot");
        TestAdapter adapter = new TestAdapter(conversation).use(new TranscriptLoggerMiddleware(transcriptStore));
        final String[] activityId = { null };
        new TestFlow(adapter, turnContext -> {
            delay(500);
            if (turnContext.getActivity().getText().equals("deleteIt")) {
                turnContext.deleteActivity(activityId[0]).join();
            } else {
                Activity activity = turnContext.getActivity().createReply("response");
                ResourceResponse response = turnContext.sendActivity(activity).join();
                activityId[0] = response.getId();
            }
            return CompletableFuture.completedFuture(null);
        }).send("foo").assertReply("response").send("deleteIt").startTest().join();

        PagedResult<Activity> pagedResult = null;
        try {
            pagedResult = this.getPagedResult(conversation, 3, null).join();
        } catch (TimeoutException ex) {
            Assert.fail();
        }

        Assert.assertEquals(3, pagedResult.getItems().size());
        Assert.assertTrue(pagedResult.getItems().get(0).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("foo", pagedResult.getItems().get(0).getText());
        Assert.assertNotNull(pagedResult.getItems().get(1));
        Assert.assertTrue(pagedResult.getItems().get(1).isType(ActivityTypes.MESSAGE_DELETE));
        Assert.assertTrue(pagedResult.getItems().get(2).isType(ActivityTypes.MESSAGE));
        Assert.assertEquals("deleteIt", pagedResult.getItems().get(2).getText());
    }

    protected static Activity createActivity(Integer i, Integer j, String[] CONVERSATION_IDS) {
        return TranscriptStoreTests.createActivity(j, CONVERSATION_IDS[i]);
    }

    private static Activity createActivity(Integer j, String conversationId) {
        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setId(conversationId);
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setId(StringUtils.leftPad(String.valueOf(j + 1), 2, "0"));
        activity.setChannelId("test");
        activity.setText("test");
        activity.setConversation(conversationAccount);
        activity.setTimestamp(OffsetDateTime.now());
        activity.setFrom(new ChannelAccount("testUser"));
        activity.setRecipient(new ChannelAccount("testBot"));
        return activity;
    }

    /**
     * There are some async oddities within TranscriptLoggerMiddleware that make it
     * difficult to set a short delay when running this tests that ensures the
     * TestFlow completes while also logging transcripts. Some tests will not pass
     * without longer delays, but this method minimizes the delay required.
     *
     * @param conversation   ConversationReference to pass to
     *                       GetTranscriptActivitiesAsync() that contains ChannelId
     *                       and Conversation.Id.
     * @param expectedLength Expected length of pagedResult array.
     * @param maxTimeout     Maximum time to wait to retrieve pagedResult.
     * @return PagedResult.
     * @throws TimeoutException
     */
    private CompletableFuture<PagedResult<Activity>> getPagedResult(ConversationReference conversation,
            Integer expectedLength, Integer maxTimeout) throws TimeoutException {
        TranscriptStore transcriptStore = getTranscriptStore();
        if (maxTimeout == null) {
            maxTimeout = 10000;
        }

        PagedResult<Activity> pagedResult = null;
        for (int timeout = 0; timeout < maxTimeout; timeout += 500) {
            delay(500);
            try {
                pagedResult = transcriptStore
                        .getTranscriptActivities(conversation.getChannelId(), conversation.getConversation().getId())
                        .join();
                if (pagedResult.getItems().size() >= expectedLength) {
                    break;
                }
            } catch (NoSuchElementException ex) {
            } catch (NullPointerException e) {
            }
        }

        if (pagedResult == null) {
            return Async.completeExceptionally(new TimeoutException("Unable to retrieve pagedResult in time"));
        }

        return CompletableFuture.completedFuture(pagedResult);
    }

    /**
     * Time period delay.
     *
     * @param delay Time to delay.
     */
    private void delay(Integer delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // Empty error
        }
    }
}
