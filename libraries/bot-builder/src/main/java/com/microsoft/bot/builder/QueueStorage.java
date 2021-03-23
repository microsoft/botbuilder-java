// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * A base class for enqueueing an Activity for later processing.
 */
public abstract class QueueStorage {

    /**
     * Enqueues an Activity for later processing. The visibility timeout specifies how long the message
     * should be invisible to Dequeue and Peek operations.
     * @param activity The {@link Activity} to be queued for later processing.
     * @param visibilityTimeout Visibility timeout. Optional with a default value of 0. Cannot be larger than 7 days.
     * @param timeToLive Specifies the time-to-live interval for the message.
     * @return A result string.
     */
    public abstract CompletableFuture<String> queueActivity(Activity activity,
                                                            @Nullable Duration visibilityTimeout,
                                                            @Nullable Duration timeToLive);
}
