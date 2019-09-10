// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;

import java.util.concurrent.CompletableFuture;

/**
 * Transcript logger stores activities for conversations for recall.
 */
public interface TranscriptLogger {
    /**
     * Log an activity to the transcript.
     *
     * @param activity The activity to transcribe.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> logActivity(Activity activity);
}
