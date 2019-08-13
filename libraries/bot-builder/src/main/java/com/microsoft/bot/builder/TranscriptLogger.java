package com.microsoft.bot.builder;


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.microsoft.bot.schema.models.Activity;

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
    void LogActivityAsync(Activity activity);
}
