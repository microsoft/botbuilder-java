// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.TurnContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * Interface for adding telemetry logging capabilities to {@link QnAMaker}/>.
 */
public interface TelemetryQnAMaker {

    /**
     * Gets a value indicating whether determines whether to log personal
     * information that came from the user.
     *
     * @return If true, will log personal information into the
     *         IBotTelemetryClient.TrackEvent method; otherwise the properties will
     *         be filtered.
     */
    Boolean getLogPersonalInformation();

    /**
     * Gets the currently configured {@link BotTelemetryClient} that logs the
     * QnaMessage event.
     *
     * @return The {@link BotTelemetryClient} being used to log events.
     */
    BotTelemetryClient getTelemetryClient();

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
}
