// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.ai.qna.models.Metadata;
import com.microsoft.bot.ai.qna.models.QnARequestContext;

/**
 * Defines options for the QnA Maker knowledge base.
 */
public class QnAMakerOptions {
    @JsonProperty("scoreThreshold")
    private Float scoreThreshold;

    @JsonProperty("timeout")
    private Double timeout = 0d;

    @JsonProperty("top")
    private Integer top = 0;

    @JsonProperty("context")
    private QnARequestContext context;

    @JsonProperty("qnAId")
    private Integer qnAId;

    @JsonProperty("strictFilters")
    private Metadata[] strictFilters;

    @Deprecated
    @JsonIgnore
    private Metadata[] metadataBoost;

    @JsonProperty("isTest")
    private Boolean isTest;

    @JsonProperty("rankerType")
    private String rankerType;

    @JsonProperty("strictFiltersJoinOperator")
    private JoinOperator strictFiltersJoinOperator;

    private static final Float SCORE_THRESHOLD = 0.3f;

    /**
     * Gets the minimum score threshold, used to filter returned results. Scores are
     * normalized to the range of 0.0 to 1.0 before filtering.
     *
     * @return The minimum score threshold, used to filter returned results.
     */
    public Float getScoreThreshold() {
        return scoreThreshold;
    }

    /**
     * Sets the minimum score threshold, used to filter returned results. Scores are
     * normalized to the range of 0.0 to 1.0 before filtering.
     *
     * @param withScoreThreshold The minimum score threshold, used to filter
     *                           returned results.
     */
    public void setScoreThreshold(Float withScoreThreshold) {
        this.scoreThreshold = withScoreThreshold;
    }

    /**
     * Gets the time in milliseconds to wait before the request times out.
     *
     * @return The time in milliseconds to wait before the request times out.
     *         Default is 100000 milliseconds. This property allows users to set
     *         Timeout without having to pass in a custom HttpClient to QnAMaker
     *         class constructor. If using custom HttpClient, then set Timeout value
     *         in HttpClient instead of QnAMakerOptions.Timeout.
     */
    public Double getTimeout() {
        return timeout;
    }

    /**
     * Sets the time in milliseconds to wait before the request times out.
     *
     * @param withTimeout The time in milliseconds to wait before the request times
     *                    out. Default is 100000 milliseconds. This property allows
     *                    users to set Timeout without having to pass in a custom
     *                    HttpClient to QnAMaker class constructor. If using custom
     *                    HttpClient, then set Timeout value in HttpClient instead
     *                    of QnAMakerOptions.Timeout.
     */
    public void setTimeout(Double withTimeout) {
        this.timeout = withTimeout;
    }

    /**
     * Gets the number of ranked results you want in the output.
     *
     * @return The number of ranked results you want in the output.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Sets the number of ranked results you want in the output.
     *
     * @param withTop The number of ranked results you want in the output.
     */
    public void setTop(Integer withTop) {
        this.top = withTop;
    }

    /**
     * Gets context of the previous turn.
     *
     * @return The context of previous turn.
     */
    public QnARequestContext getContext() {
        return context;
    }

    /**
     * Sets context of the previous turn.
     *
     * @param withContext The context of previous turn.
     */
    public void setContext(QnARequestContext withContext) {
        this.context = withContext;
    }

    /**
     * Gets QnA Id of the current question asked (if availble).
     *
     * @return Id of the current question asked.
     */
    public Integer getQnAId() {
        return qnAId;
    }

    /**
     * Sets QnA Id of the current question asked (if availble).
     *
     * @param withQnAId Id of the current question asked.
     */
    public void setQnAId(Integer withQnAId) {
        this.qnAId = withQnAId;
    }

    /**
     * Gets the {@link Metadata} collection to be sent when calling QnA Maker to
     * filter results.
     *
     * @return An array of {@link Metadata}
     */
    public Metadata[] getStrictFilters() {
        return strictFilters;
    }

    /**
     * Sets the {@link Metadata} collection to be sent when calling QnA Maker to
     * filter results.
     *
     * @param withStrictFilters An array of {@link Metadata}
     */
    public void setStrictFilters(Metadata[] withStrictFilters) {
        this.strictFilters = withStrictFilters;
    }

    /**
     * Gets a value indicating whether to call test or prod environment of knowledge
     * base to be called.
     *
     * @return A value indicating whether to call test or prod environment of
     *         knowledge base.
     */
    public Boolean getIsTest() {
        return isTest;
    }

    /**
     * Sets a value indicating whether to call test or prod environment of knowledge
     * base to be called.
     *
     * @param withIsTest A value indicating whether to call test or prod environment
     *                   of knowledge base.
     */
    public void setIsTest(Boolean withIsTest) {
        isTest = withIsTest;
    }

    /**
     * Gets the QnA Maker ranker type to use.
     *
     * @return The QnA Maker ranker type to use.
     */
    public String getRankerType() {
        return rankerType;
    }

    /**
     * Sets the QnA Maker ranker type to use.
     *
     * @param withRankerType The QnA Maker ranker type to use.
     */
    public void setRankerType(String withRankerType) {
        this.rankerType = withRankerType;
    }

    /**
     * Gets Strict Filters join operator.
     *
     * @return A value indicating choice for Strict Filters Join Operation.
     */
    public JoinOperator getStrictFiltersJoinOperator() {
        return strictFiltersJoinOperator;
    }

    /**
     * Sets Strict Filters join operator.
     *
     * @param withStrictFiltersJoinOperator A value indicating choice for Strict
     *                                      Filters Join Operation.
     */
    public void setStrictFiltersJoinOperator(JoinOperator withStrictFiltersJoinOperator) {
        this.strictFiltersJoinOperator = withStrictFiltersJoinOperator;
    }

    /**
     * Initializes a new instance of the {@link QnAMakerOptions} class.
     */
    public QnAMakerOptions() {
        this.scoreThreshold = SCORE_THRESHOLD;
    }
}
