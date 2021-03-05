// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Activity;

/**
 * This class represents all the trace info that we collect from the QnAMaker
 * Middleware.
 */
public class QnAMakerTraceInfo {
    @JsonProperty("message")
    private Activity message;

    @JsonProperty("queryResults")
    private QueryResult[] queryResults;

    @JsonProperty("knowledgeBaseId")
    private String knowledgeBaseId;

    @JsonProperty("scoreThreshold")
    private Float scoreThreshold;

    @JsonProperty("top")
    private Integer top;

    @JsonProperty("strictFilters")
    private Metadata[] strictFilters;

    @JsonProperty("context")
    private QnARequestContext context;

    @JsonProperty("qnaId")
    private Integer qnaId;

    @JsonProperty("isTest")
    private Boolean isTest;

    @JsonProperty("rankerType")
    private String rankerType;

    @Deprecated
    @JsonIgnore
    private Metadata[] metadataBoost;

    /**
     * Gets message which instigated the query to QnAMaker.
     *
     * @return Message which instigated the query to QnAMaker.
     */
    public Activity getMessage() {
        return this.message;
    }

    /**
     * Sets message which instigated the query to QnAMaker.
     *
     * @param withMessage Message which instigated the query to QnAMaker.
     */
    public void setMessage(Activity withMessage) {
        this.message = withMessage;
    }

    /**
     * Gets results that QnAMaker returned.
     *
     * @return Results that QnAMaker returned.
     */
    public QueryResult[] getQueryResults() {
        return this.queryResults;
    }

    /**
     * Sets results that QnAMaker returned.
     *
     * @param withQueryResult Results that QnAMaker returned.
     */
    public void setQueryResults(QueryResult[] withQueryResult) {
        this.queryResults = withQueryResult;
    }

    /**
     * Gets iD of the Knowledgebase that is being used.
     *
     * @return ID of the Knowledgebase that is being used.
     */
    public String getKnowledgeBaseId() {
        return this.knowledgeBaseId;
    }

    /**
     * Sets iD of the Knowledgebase that is being used.
     *
     * @param withKnowledgeBaseId ID of the Knowledgebase that is being used.
     */
    public void setKnowledgeBaseId(String withKnowledgeBaseId) {
        this.knowledgeBaseId = withKnowledgeBaseId;
    }

    /**
     * Gets the minimum score threshold, used to filter returned results. Scores are
     * normalized to the range of 0.0 to 1.0 before filtering.
     *
     * @return The minimum score threshold, used to filter returned results.
     */
    public Float getScoreThreshold() {
        return this.scoreThreshold;
    }

    /**
     * Sets the minimum score threshold, used to filter returned results. Scores are
     * normalized to the range of 0.0 to 1.0 before filtering
     *
     * @param withScoreThreshold The minimum score threshold, used to filter
     *                           returned results.
     */
    public void setScoreThreshold(Float withScoreThreshold) {
        this.scoreThreshold = withScoreThreshold;
    }

    /**
     * Gets number of ranked results that are asked to be returned.
     *
     * @return Number of ranked results that are asked to be returned.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Sets number of ranked results that are asked to be returned.
     *
     * @param withTop Number of ranked results that are asked to be returned.
     */
    public void setTop(Integer withTop) {
        this.top = withTop;
    }

    /**
     * Gets the filters used to return answers that have the specified metadata.
     *
     * @return The filters used to return answers that have the specified metadata.
     */
    public Metadata[] getStrictFilters() {
        return this.strictFilters;
    }

    /**
     * Sets the filters used to return answers that have the specified metadata.
     *
     * @param withStrictFilters The filters used to return answers that have the
     *                          specified metadata.
     */
    public void setStrictFilters(Metadata[] withStrictFilters) {
        this.strictFilters = withStrictFilters;
    }

    /**
     * Gets context for multi-turn responses.
     *
     * @return The context from which the QnA was extracted.
     */
    public QnARequestContext getContext() {
        return this.context;
    }

    /**
     * Sets context for multi-turn responses.
     *
     * @param withContext The context from which the QnA was extracted.
     */
    public void setContext(QnARequestContext withContext) {
        this.context = withContext;
    }

    /**
     * Gets QnA Id of the current question asked.
     *
     * @return Id of the current question asked.
     */
    public Integer getQnAId() {
        return this.qnaId;
    }

    /**
     * Sets QnA Id of the current question asked.
     *
     * @param withQnAId Id of the current question asked.
     */
    public void setQnAId(Integer withQnAId) {
        this.qnaId = withQnAId;
    }

    /**
     * Gets a value indicating whether gets or sets environment of knowledgebase to
     * be called.
     *
     * @return A value indicating whether to call test or prod environment of
     *         knowledgebase.
     */
    public Boolean getIsTest() {
        return this.isTest;
    }

    /**
     * Sets a value indicating whether gets or sets environment of knowledgebase to
     * be called.
     *
     * @param withIsTest A value indicating whether to call test or prod environment
     *                   of knowledgebase.
     */
    public void setIsTest(Boolean withIsTest) {
        this.isTest = withIsTest;
    }

    /**
     * Gets ranker Types.
     *
     * @return Ranker Types.
     */
    public String getRankerType() {
        return this.rankerType;
    }

    /**
     * Sets ranker Types.
     *
     * @param withRankerType Ranker Types.
     */
    public void setRankerType(String withRankerType) {
        this.rankerType = withRankerType;
    }

    /**
     * Gets the {@link Metadata} collection to be sent when calling QnA Maker to
     * boost results.
     *
     * @return An array of {@link Metadata}.
     */
    public Metadata[] getMetadataBoost() {
        return this.metadataBoost;
    }

    /**
     * Sets the {@link Metadata} collection to be sent when calling QnA Maker to
     * boost results.
     *
     * @param withMetadataBoost An array of {@link Metadata}.
     */
    public void setMetadataBoost(Metadata[] withMetadataBoost) {
        this.metadataBoost = withMetadataBoost;
    }
}
