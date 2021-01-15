// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains answers for a user query.
 */
public class QueryResults {
    @JsonProperty("answers")
    private QueryResult[] answers;

    @JsonProperty("activeLearningEnabled")
    private Boolean activeLearningEnabled;

    /**
     * Gets the answers for a user query, sorted in decreasing order of ranking
     * score.
     *
     * @return The answers for a user query, sorted in decreasing order of ranking
     *         score.
     */
    public QueryResult[] getAnswers() {
        return this.answers;
    }

    /**
     * Sets the answers for a user query, sorted in decreasing order of ranking
     * score.
     *
     * @param withAnswers The answers for a user query, sorted in decreasing order
     *                    of ranking score.
     */
    public void setAnswers(QueryResult[] withAnswers) {
        this.answers = withAnswers;
    }

    /**
     * Gets a value indicating whether gets or set for the active learning enable
     * flag.
     *
     * @return The active learning enable flag.
     */
    public Boolean getActiveLearningEnabled() {
        return this.activeLearningEnabled;
    }

    /**
     * Sets a value indicating whether gets or set for the active learning enable
     * flag.
     *
     * @param withActiveLearningEnabled The active learning enable flag.
     */
    public void setActiveLearningEnabled(Boolean withActiveLearningEnabled) {
        this.activeLearningEnabled = withActiveLearningEnabled;
    }
}
