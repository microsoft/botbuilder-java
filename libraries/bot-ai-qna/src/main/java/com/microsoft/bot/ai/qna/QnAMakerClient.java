// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.microsoft.bot.ai.qna.models.FeedbackRecords;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.ai.qna.models.QueryResults;
import com.microsoft.bot.builder.TurnContext;

/**
 * Client to access a QnA Maker knowledge base.
 */
public interface QnAMakerClient {
    /**
     * Generates an answer from the knowledge base.
     *
     * @param turnContext         The Turn Context that contains the user question
     *                            to be queried against your knowledge base.
     * @param options             The options for the QnA Maker knowledge base. If
     *                            null, constructor option is used for this
     *                            instance.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the QnaMessage event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the QnaMessage event.
     * @return A list of answers for the user query, sorted in decreasing order of
     *         ranking score.
     */
    CompletableFuture<QueryResult[]> getAnswers(
        TurnContext turnContext,
        QnAMakerOptions options,
        Map<String, String> telemetryProperties,
        @Nullable Map<String, Double> telemetryMetrics
    );

    /**
     * Generates an answer from the knowledge base.
     *
     * @param turnContext         The Turn Context that contains the user question
     *                            to be queried against your knowledge base.
     * @param options             The options for the QnA Maker knowledge base. If
     *                            null, constructor option is used for this
     *                            instance.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the QnaMessage event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the QnaMessage event.
     * @return A list of answers for the user query, sorted in decreasing order of
     *         ranking score.
     */
    CompletableFuture<QueryResults> getAnswersRaw(
        TurnContext turnContext,
        QnAMakerOptions options,
        @Nullable Map<String, String> telemetryProperties,
        @Nullable Map<String, Double> telemetryMetrics
    );

    /**
     * Filters the ambiguous question for active learning.
     *
     * @param queryResults User query output.
     * @return Filtered array of ambiguous question.
     */
    QueryResult[] getLowScoreVariation(QueryResult[] queryResults);

    /**
     * Send feedback to the knowledge base.
     *
     * @param feedbackRecords Feedback records.
     * @return A Task representing the asynchronous operation.
     * @throws IOException Throws an IOException if there is any.
     */
    CompletableFuture<Void> callTrain(FeedbackRecords feedbackRecords) throws IOException;
}
