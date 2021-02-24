// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an individual result from a knowledge base query.
 */
public class QueryResult {
    @JsonProperty("questions")
    private String[] questions;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("score")
    private Float score;

    @JsonProperty("metadata")
    private Metadata[] metadata;

    @JsonProperty("source")
    private String source;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("context")
    private QnAResponseContext context;

    /**
     * Gets the list of questions indexed in the QnA Service for the given answer.
     *
     * @return The list of questions indexed in the QnA Service for the given
     *         answer.
     */
    public String[] getQuestions() {
        return this.questions;
    }

    /**
     * Sets the list of questions indexed in the QnA Service for the given answer.
     *
     * @param withQuestions The list of questions indexed in the QnA Service for the
     *                      given answer.
     */
    public void setQuestions(String[] withQuestions) {
        this.questions = withQuestions;
    }

    /**
     * Gets the answer text.
     *
     * @return The answer text.
     */
    public String getAnswer() {
        return this.answer;
    }

    /**
     * Sets the answer text.
     *
     * @param withAnswer The answer text.
     */
    public void setAnswer(String withAnswer) {
        this.answer = withAnswer;
    }

    /**
     * Gets the answer's score, from 0.0 (least confidence) to 1.0 (greatest
     * confidence).
     *
     * @return The answer's score, from 0.0 (least confidence) to 1.0 (greatest
     *         confidence).
     */
    public Float getScore() {
        return this.score;
    }

    /**
     * Sets the answer's score, from 0.0 (least confidence) to 1.0 (greatest
     * confidence).
     *
     * @param withScore The answer's score, from 0.0 (least confidence) to 1.0
     *                  (greatest confidence).
     */
    public void setScore(Float withScore) {
        this.score = withScore;
    }

    /**
     * Gets metadata that is associated with the answer.
     *
     * @return Metadata that is associated with the answer.
     */
    public Metadata[] getMetadata() {
        return this.metadata;
    }

    /**
     * Sets metadata that is associated with the answer.
     *
     * @param withMetadata Metadata that is associated with the answer.
     */
    public void setMetadata(Metadata[] withMetadata) {
        this.metadata = withMetadata;
    }

    /**
     * Gets the source from which the QnA was extracted.
     *
     * @return The source from which the QnA was extracted.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Sets the source from which the QnA was extracted.
     *
     * @param withSource The source from which the QnA was extracted.
     */
    public void setSource(String withSource) {
        this.source = withSource;
    }

    /**
     * Gets the index of the answer in the knowledge base. V3 uses 'qnaId', V4 uses
     * 'id'.
     *
     * @return The index of the answer in the knowledge base. V3 uses 'qnaId', V4
     *         uses 'id'.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the index of the answer in the knowledge base. V3 uses 'qnaId', V4 uses
     * 'id'.
     *
     * @param withId The index of the answer in the knowledge base. V3 uses 'qnaId',
     *               V4 uses 'id'.
     */
    public void setId(Integer withId) {
        this.id = withId;
    }

    /**
     * Gets context for multi-turn responses.
     *
     * @return The context from which the QnA was extracted.
     */
    public QnAResponseContext getContext() {
        return this.context;
    }

    /**
     * Sets context for multi-turn responses.
     *
     * @param withContext The context from which the QnA was extracted.
     */
    public void setContext(QnAResponseContext withContext) {
        this.context = withContext;
    }
}
