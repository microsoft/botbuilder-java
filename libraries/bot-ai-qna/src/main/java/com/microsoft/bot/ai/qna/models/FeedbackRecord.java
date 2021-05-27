// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Active learning feedback record.
 */
public class FeedbackRecord {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userQuestion")
    private String userQuestion;

    @JsonProperty("qnaId")
    private Integer qnaId;

    /**
     * Gets the feedback record's user ID.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Sets the feedback record's user ID.
     *
     * @param withUserId The user ID.
     */
    public void setUserId(String withUserId) {
        this.userId = withUserId;
    }

    /**
     * Gets the question asked by the user.
     *
     * @return The user question.
     */
    public String getUserQuestion() {
        return this.userQuestion;
    }

    /**
     * Sets question asked by the user.
     *
     * @param withUserQuestion The user question.
     */
    public void setUserQuestion(String withUserQuestion) {
        this.userQuestion = withUserQuestion;
    }

    /**
     * Gets the QnA ID.
     *
     * @return The QnA ID.
     */
    public Integer getQnaId() {
        return this.qnaId;
    }

    /**
     * Sets the QnA ID.
     *
     * @param withQnaId The QnA ID.
     */
    public void setQnaId(Integer withQnaId) {
        this.qnaId = withQnaId;
    }
}
