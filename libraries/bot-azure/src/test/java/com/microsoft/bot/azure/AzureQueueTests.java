// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.azure.queues.AzureQueueStorage;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.QueueStorage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogManager;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityEventNames;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationReference;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.restclient.serializer.JacksonAdapter;

public class AzureQueueTests {
    private static final Integer DEFAULT_DELAY = 2000;
    private final String connectionString = "UseDevelopmentStorage=true";

    // These tests require Azure Storage Emulator v5.7
    public QueueClient containerInit(String name) {
        QueueClient queue = new QueueClientBuilder()
            .connectionString(connectionString)
            .queueName(name)
            .buildClient();
        queue.create();
        queue.clearMessages();
        return queue;
    }

    @Before
    public void beforeTest() {
        org.junit.Assume.assumeTrue(AzureEmulatorUtils.isStorageEmulatorAvailable());
    }

    @Test
    public void continueConversationLaterTests() {
            String queueName = "continueconversationlatertests";
            QueueClient queue = containerInit(queueName);

            ConversationReference cr = TestAdapter.createConversationReference("ContinueConversationLaterTests", "User1", "Bot");
            TestAdapter adapter = new TestAdapter(cr)
                .useStorage(new MemoryStorage())
                .useBotState(new ConversationState(new MemoryStorage()), new UserState(new MemoryStorage()));

            AzureQueueStorage queueStorage = new AzureQueueStorage(connectionString, queueName);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 2);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            ContinueConversationLater ccl = new ContinueConversationLater();
            ccl.setDate(sdf.format(cal.getTime()));
            ccl.setValue("foo");
            DialogManager dm = new DialogManager(ccl, "DialogStateProperty");
            dm.getInitialTurnState().replace("QueueStorage", queueStorage);

            new TestFlow(adapter, turnContext -> CompletableFuture.runAsync(() -> dm.onTurn(turnContext)))
                .send("hi")
                .startTest().join();

            try {
                Thread.sleep(DEFAULT_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail();
            }

            QueueMessageItem messages = queue.receiveMessage();
            JacksonAdapter jacksonAdapter = new JacksonAdapter();
            String messageJson = new String(Base64.decodeBase64(messages.getMessageText()));
            Activity activity = null;

            try {
                activity = jacksonAdapter.deserialize(messageJson, Activity.class);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail();
            }

            Assert.assertTrue(activity.isType(ActivityTypes.EVENT));
            Assert.assertEquals(ActivityEventNames.CONTINUE_CONVERSATION, activity.getName());
            Assert.assertEquals("foo", activity.getValue());
            Assert.assertNotNull(activity.getRelatesTo());
            ConversationReference cr2 = activity.getConversationReference();
            cr.setActivityId(null);
            cr2.setActivityId(null);

            try {
                Assert.assertEquals(jacksonAdapter.serialize(cr), jacksonAdapter.serialize(cr2));
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail();
            }
    }

    private class ContinueConversationLater extends Dialog {
        @JsonProperty("disabled")
        private Boolean disabled = false;

        @JsonProperty("date")
        private String date;

        @JsonProperty("value")
        private String value;

        /**
         * Initializes a new instance of the Dialog class.
         */
        public ContinueConversationLater() {
            super(ContinueConversationLater.class.getName());
        }

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
            if (this.disabled) {
                return dc.endDialog();
            }

            String dateString = this.date;
            LocalDateTime date = null;
            try {
                date = LocalDateTime.parse(dateString);
            } catch (DateTimeParseException ex) {
                return Async.completeExceptionally(new IllegalArgumentException("Date is invalid"));
            }

            ZonedDateTime zonedDate = date.atZone(ZoneOffset.UTC);
            ZonedDateTime now = LocalDateTime.now().atZone(ZoneOffset.UTC);
            if (zonedDate.isBefore(now)) {
                return Async.completeExceptionally(new IllegalArgumentException("Date must be in the future"));
            }

            // create ContinuationActivity from the conversation reference.
            Activity activity = dc.getContext().getActivity().getConversationReference().getContinuationActivity();
            activity.setValue(this.value);

            Duration visibility = Duration.between(zonedDate, now);
            Duration ttl = visibility.plusMinutes(2);

            QueueStorage queueStorage = dc.getContext().getTurnState().get("QueueStorage");
            if (queueStorage == null) {
                return Async.completeExceptionally(new NullPointerException("Unable to locate QueueStorage in HostContext"));
            }
            return queueStorage.queueActivity(activity, visibility, ttl).thenCompose(receipt -> {
                // return the receipt as the result
                return dc.endDialog(receipt);
            });
        }

        /**
         * Gets an optional expression which if is true will disable this action.
         * "user.age > 18".
         * @return A boolean expression.
         */
        public Boolean getDisabled() {
            return disabled;
        }

        /**
         * Sets an optional expression which if is true will disable this action.
         * "user.age > 18".
         * @param withDisabled A boolean expression.
         */
        public void setDisabled(Boolean withDisabled) {
            this.disabled = withDisabled;
        }

        /**
         * Gets the expression which resolves to the date/time to continue the conversation.
         * @return Date/time string in ISO 8601 format to continue conversation.
         */
        public String getDate() {
            return date;
        }

        /**
         * Sets the expression which resolves to the date/time to continue the conversation.
         * @param withDate Date/time string in ISO 8601 format to continue conversation.
         */
        public void setDate(String withDate) {
            this.date = withDate;
        }

        /**
         * Gets an optional value to use for EventActivity.Value.
         * @return The value to use for the EventActivity.Value payload.
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets an optional value to use for EventActivity.Value.
         * @param withValue The value to use for the EventActivity.Value payload.
         */
        public void setValue(String withValue) {
            this.value = withValue;
        }
    }
}
