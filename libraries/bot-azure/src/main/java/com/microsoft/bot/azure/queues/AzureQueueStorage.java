// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure.queues;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.SendMessageResult;
import com.microsoft.bot.builder.QueueStorage;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.schema.Activity;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Service used to add messages to an Azure.Storage.Queues.
 */
public class AzureQueueStorage extends QueueStorage {
    private Boolean createQueueIfNotExists = true;
    private final QueueClient queueClient;

    /**
     * Initializes a new instance of the {@link AzureQueueStorage} class.
     * 
     * @param queuesStorageConnectionString Azure Storage connection string.
     * @param queueName                     Name of the storage queue where entities
     *                                      will be queued.
     */
    public AzureQueueStorage(String queuesStorageConnectionString, String queueName) {
        if (StringUtils.isBlank(queuesStorageConnectionString)) {
            throw new IllegalArgumentException("queuesStorageConnectionString is required.");
        }

        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("queueName is required.");
        }

        queueClient =
            new QueueClientBuilder().connectionString(queuesStorageConnectionString).queueName(queueName).buildClient();
    }

    /**
     * Queue an Activity to an Azure.Storage.Queues.QueueClient. The visibility
     * timeout specifies how long the message should be invisible to Dequeue and
     * Peek operations. The message content must be a UTF-8 encoded string that is
     * up to 64KB in size.
     * 
     * @param activity          This is expected to be an {@link Activity} retrieved
     *                          from a call to
     *                          activity.GetConversationReference().GetContinuationActivity().
     *                          This enables restarting the conversation using
     *                          BotAdapter.ContinueConversationAsync.
     * @param visibilityTimeout Default value of 0. Cannot be larger than 7 days.
     * @param timeToLive        Specifies the time-to-live interval for the message.
     * @return {@link SendMessageResult} as a Json string, from the QueueClient
     *         SendMessageAsync operation.
     */
    @Override
    public CompletableFuture<String> queueActivity(
        Activity activity,
        @Nullable Duration visibilityTimeout,
        @Nullable Duration timeToLive
    ) {
        return CompletableFuture.supplyAsync(() -> {
            if (createQueueIfNotExists) {
                try {
                    queueClient.create();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // This is an optimization flag to check if the container creation call has been
                // made.
                // It is okay if this is called more than once.
                createQueueIfNotExists = false;
            }

            try {
                JacksonAdapter jacksonAdapter = new JacksonAdapter();
                String serializedActivity = jacksonAdapter.serialize(activity);
                byte[] encodedBytes = serializedActivity.getBytes(StandardCharsets.UTF_8);
                String encodedString = Base64.getEncoder().encodeToString(encodedBytes);

                SendMessageResult receipt = queueClient.sendMessage(encodedString);
                return jacksonAdapter.serialize(receipt);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
