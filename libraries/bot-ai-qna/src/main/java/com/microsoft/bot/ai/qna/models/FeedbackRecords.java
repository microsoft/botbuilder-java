// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Active learning feedback records.
 */
public class FeedbackRecords {
    @JsonProperty("feedbackRecords")
    private FeedbackRecord[] records;

    /**
     * Gets the list of feedback records.
     *
     * @return List of {@link FeedbackRecord}.
     */
    public FeedbackRecord[] getRecords() {
        return this.records;
    }

    /**
     * Sets the list of feedback records.
     *
     * @param withRecords List of {@link FeedbackRecord}.
     */
    public void setRecords(FeedbackRecord[] withRecords) {
        this.records = withRecords;
    }
}
