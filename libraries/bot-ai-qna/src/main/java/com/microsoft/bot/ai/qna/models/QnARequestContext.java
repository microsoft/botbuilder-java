// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The context associated with QnA. Used to mark if the current prompt is
 * relevant with a previous question or not.
 */
public class QnARequestContext {
    @JsonProperty("previousQnAId")
    private Integer previousQnAId;

    @JsonProperty("previousUserQuery")
    private String previousUserQuery = new String();

    /**
     * Gets the previous QnA Id that was returned.
     *
     * @return The previous QnA Id.
     */
    public Integer getPreviousQnAId() {
        return this.previousQnAId;
    }

    /**
     * Sets the previous QnA Id that was returned.
     *
     * @param withPreviousQnAId The previous QnA Id.
     */
    public void setPreviousQnAId(Integer withPreviousQnAId) {
        this.previousQnAId = withPreviousQnAId;
    }

    /**
     * Gets the previous user query/question.
     *
     * @return The previous user query.
     */
    public String getPreviousUserQuery() {
        return this.previousUserQuery;
    }

    /**
     * Sets the previous user query/question.
     *
     * @param withPreviousUserQuery The previous user query.
     */
    public void setPreviousUserQuery(String withPreviousUserQuery) {
        this.previousUserQuery = withPreviousUserQuery;
    }
}
