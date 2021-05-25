// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.bot.ai.qna.models.FeedbackRecords;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.ai.qna.models.QueryResults;
import com.microsoft.bot.ai.qna.utils.ActiveLearningUtils;
import com.microsoft.bot.ai.qna.utils.GenerateAnswerUtils;
import com.microsoft.bot.ai.qna.utils.QnATelemetryConstants;
import com.microsoft.bot.ai.qna.utils.TrainUtils;

import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * Provides access to a QnA Maker knowledge base.
 */
public class QnAMaker implements QnAMakerClient, TelemetryQnAMaker {

    private QnAMakerEndpoint endpoint;

    private GenerateAnswerUtils generateAnswerHelper;
    private TrainUtils activeLearningTrainHelper;
    private Boolean logPersonalInformation;
    @JsonIgnore
    private BotTelemetryClient telemetryClient;

    /**
     * The name of the QnAMaker class.
     */
    public static final String QNA_MAKER_NAME = "QnAMaker";
    /**
     * The type used when logging QnA Maker trace.
     */
    public static final String QNA_MAKER_TRACE_TYPE = "https://www.qnamaker.ai/schemas/trace";
    /**
     * The label used when logging QnA Maker trace.
     */
    public static final String QNA_MAKER_TRACE_LABEL = "QnAMaker Trace";

    /**
     * Initializes a new instance of the QnAMaker class.
     *
     * @param withEndpoint               The endpoint of the knowledge base to
     *                                   query.
     * @param options                    The options for the QnA Maker knowledge
     *                                   base.
     * @param withTelemetryClient        The IBotTelemetryClient used for logging
     *                                   telemetry events.
     * @param withLogPersonalInformation Set to true to include personally
     *                                   identifiable information in telemetry
     *                                   events.
     */
    public QnAMaker(
        QnAMakerEndpoint withEndpoint,
        QnAMakerOptions options,
        BotTelemetryClient withTelemetryClient,
        Boolean withLogPersonalInformation
    ) {
        if (withLogPersonalInformation == null) {
            withLogPersonalInformation = false;
        }

        if (withEndpoint == null) {
            throw new IllegalArgumentException("endpoint");
        }
        this.endpoint = withEndpoint;

        if (StringUtils.isBlank(this.endpoint.getKnowledgeBaseId())) {
            throw new IllegalArgumentException("knowledgeBaseId");
        }

        if (StringUtils.isBlank(this.endpoint.getHost())) {
            throw new IllegalArgumentException("host");
        }

        if (StringUtils.isBlank(this.endpoint.getEndpointKey())) {
            throw new IllegalArgumentException("endpointKey");
        }

        if (this.endpoint.getHost().endsWith("v2.0") || this.endpoint.getHost().endsWith("v3.0")) {
            throw new UnsupportedOperationException(
                "v2.0 and v3.0 of QnA Maker service" + " is no longer supported in the QnA Maker."
            );
        }

        this.telemetryClient = withTelemetryClient != null ? withTelemetryClient : new NullBotTelemetryClient();
        this.logPersonalInformation = withLogPersonalInformation;

        this.generateAnswerHelper = new GenerateAnswerUtils(this.endpoint, options);
        this.activeLearningTrainHelper = new TrainUtils(this.endpoint);
    }

    /**
     * Initializes a new instance of the {@link QnAMaker} class.
     *
     * @param withEndpoint The endpoint of the knowledge base to query.
     * @param options      The options for the QnA Maker knowledge base.
     */
    public QnAMaker(QnAMakerEndpoint withEndpoint, @Nullable QnAMakerOptions options) {
        this(withEndpoint, options, null, null);
    }

    /**
     * Gets a value indicating whether determines whether to log personal
     * information that came from the user.
     *
     * @return If true, will log personal information into the
     *         IBotTelemetryClient.TrackEvent method; otherwise the properties will
     *         be filtered.
     */
    public Boolean getLogPersonalInformation() {
        return this.logPersonalInformation;
    }

    /**
     * Gets the currently configured {@link BotTelemetryClient}.
     *
     * @return {@link BotTelemetryClient} being used to log events.
     */
    public BotTelemetryClient getTelemetryClient() {
        return this.telemetryClient;
    }

    /**
     * Generates an answer from the knowledge base.
     *
     * @param turnContext The Turn Context that contains the user question to be
     *                    queried against your knowledge base.
     * @param options     The options for the QnA Maker knowledge base. If null,
     *                    constructor option is used for this instance.
     * @return A list of answers for the user query, sorted in decreasing order of
     *         ranking score.
     */
    public CompletableFuture<QueryResult[]> getAnswers(TurnContext turnContext, @Nullable QnAMakerOptions options) {
        return this.getAnswers(turnContext, options, null, null);
    }

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
    public CompletableFuture<QueryResult[]> getAnswers(
        TurnContext turnContext,
        QnAMakerOptions options,
        Map<String, String> telemetryProperties,
        @Nullable Map<String, Double> telemetryMetrics
    ) {
        return this.getAnswersRaw(turnContext, options, telemetryProperties, telemetryMetrics)
            .thenApply(result -> result.getAnswers());
    }

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
    public CompletableFuture<QueryResults> getAnswersRaw(
        TurnContext turnContext,
        QnAMakerOptions options,
        @Nullable Map<String, String> telemetryProperties,
        @Nullable Map<String, Double> telemetryMetrics
    ) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException("turnContext"));
        }
        if (turnContext.getActivity() == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException(
                    String.format("The %1$s property for %2$s can't be null.", "Activity", "turnContext")
                )
            );
        }
        Activity messageActivity = turnContext.getActivity();
        if (messageActivity == null || !messageActivity.isType(ActivityTypes.MESSAGE)) {
            return Async.completeExceptionally(new IllegalArgumentException("Activity type is not a message"));
        }

        if (StringUtils.isBlank(turnContext.getActivity().getText())) {
            return Async.completeExceptionally(new IllegalArgumentException("Null or empty text"));
        }

        return this.generateAnswerHelper.getAnswersRaw(turnContext, messageActivity, options).thenCompose(result -> {
            try {
                this.onQnaResults(result.getAnswers(), turnContext, telemetryProperties, telemetryMetrics);
            } catch (IOException e) {
                LoggerFactory.getLogger(QnAMaker.class).error("getAnswersRaw");
            }
            return CompletableFuture.completedFuture(result);
        });
    }

    /**
     * Filters the ambiguous question for active learning.
     *
     * @param queryResult User query output.
     * @return Filtered array of ambiguous question.
     */
    public QueryResult[] getLowScoreVariation(QueryResult[] queryResult) {
        List<QueryResult> queryResults = ActiveLearningUtils.getLowScoreVariation(Arrays.asList(queryResult));
        return queryResults.toArray(new QueryResult[queryResults.size()]);
    }

    /**
     * Send feedback to the knowledge base.
     *
     * @param feedbackRecords Feedback records.
     * @return Representing the asynchronous operation.
     * @throws IOException Throws an IOException if there is any.
     */
    public CompletableFuture<Void> callTrain(FeedbackRecords feedbackRecords) throws IOException {
        return this.activeLearningTrainHelper.callTrain(feedbackRecords);
    }

    /**
     * Executed when a result is returned from QnA Maker.
     *
     * @param queryResults        An array of {@link QueryResult}
     * @param turnContext         The {@link TurnContext}
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @return A Task representing the work to be executed.
     * @throws IOException Throws an IOException if there is any.
     */
    protected CompletableFuture<Void> onQnaResults(
        QueryResult[] queryResults,
        TurnContext turnContext,
        @Nullable Map<String, String> telemetryProperties,
        @Nullable Map<String, Double> telemetryMetrics
    ) throws IOException {
        return fillQnAEvent(queryResults, turnContext, telemetryProperties, telemetryMetrics).thenAccept(
            eventData -> {
                // Track the event
                this.telemetryClient
                    .trackEvent(QnATelemetryConstants.QNA_MSG_EVENT, eventData.getLeft(), eventData.getRight());
            }
        );
    }

    /**
     * Fills the event properties and metrics for the QnaMessage event for
     * telemetry. These properties are logged when the QnA GetAnswers method is
     * called.
     *
     * @param queryResults        QnA service results.
     * @param turnContext         Context object containing information for a single
     *                            turn of conversation with a user.
     * @param telemetryProperties Properties to add/override for the event.
     * @param telemetryMetrics    Metrics to add/override for the event.
     * @return A tuple of Properties and Metrics that will be sent to the
     *         IBotTelemetryClient. TrackEvent method for the QnAMessage event. The
     *         properties and metrics returned the standard properties logged with
     *         any properties passed from the GetAnswersAsync method.
     * @throws IOException Throws an IOException if there is any.
     */
    protected CompletableFuture<Pair<Map<String, String>, Map<String, Double>>> fillQnAEvent(
        QueryResult[] queryResults,
        TurnContext turnContext,
        @Nullable Map<String, String> telemetryProperties,
        @Nullable Map<String, Double> telemetryMetrics
    ) throws IOException {
        Map<String, String> properties = new HashMap<String, String>();
        Map<String, Double> metrics = new HashMap<String, Double>();

        properties.put(QnATelemetryConstants.KNOWLEDGE_BASE_ID_PROPERTY, this.endpoint.getKnowledgeBaseId());

        String text = turnContext.getActivity().getText();
        String userName =
            turnContext.getActivity().getFrom() != null ? turnContext.getActivity().getFrom().getName() : null;

        // Use the LogPersonalInformation flag to toggle logging PII data, text and user
        // name are common examples
        if (this.logPersonalInformation) {
            if (!StringUtils.isBlank(text)) {
                properties.put(QnATelemetryConstants.QUESTION_PROPERTY, text);
            }

            if (!StringUtils.isBlank(userName)) {
                properties.put(QnATelemetryConstants.USERNAME_PROPERTY, userName);
            }
        }

        // Fill in QnA Results (found or not)
        if (queryResults.length > 0) {
            JacksonAdapter jacksonAdapter = new JacksonAdapter();
            QueryResult queryResult = queryResults[0];
            properties.put(
                QnATelemetryConstants.MATCHED_QUESTION_PROPERTY,
                jacksonAdapter.serialize(queryResult.getQuestions())
            );
            properties.put(
                QnATelemetryConstants.QUESTION_ID_PROPERTY,
                queryResult.getId() != null ? queryResult.getId().toString() : ""
            );
            properties.put(QnATelemetryConstants.ANSWER_PROPERTY, queryResult.getAnswer());
            metrics.put(QnATelemetryConstants.SCORE_PROPERTY, queryResult.getScore().doubleValue());
            properties.put(QnATelemetryConstants.ARTICLE_FOUND_PROPERTY, "true");
        } else {
            properties.put(QnATelemetryConstants.MATCHED_QUESTION_PROPERTY, "No Qna Question matched");
            properties.put(QnATelemetryConstants.QUESTION_ID_PROPERTY, "No QnA Question Id matched");
            properties.put(QnATelemetryConstants.ANSWER_PROPERTY, "No Qna Answer matched");
            properties.put(QnATelemetryConstants.ARTICLE_FOUND_PROPERTY, "false");
        }

        // Additional Properties can override "stock" properties.
        if (telemetryProperties != null) {
            Multimap<String, String> multiMapTelemetryProperties = LinkedListMultimap.create();
            for (Entry<String, String> entry : telemetryProperties.entrySet()) {
                multiMapTelemetryProperties.put(entry.getKey(), entry.getValue());
            }
            for (Entry<String, String> entry : properties.entrySet()) {
                multiMapTelemetryProperties.put(entry.getKey(), entry.getValue());
            }
            for (Entry<String, Collection<String>> entry : multiMapTelemetryProperties.asMap().entrySet()) {
                telemetryProperties.put(entry.getKey(), entry.getValue().iterator().next());
            }
        }

        // Additional Metrics can override "stock" metrics.
        if (telemetryMetrics != null) {
            Multimap<String, Double> multiMapTelemetryMetrics = LinkedListMultimap.create();
            for (Entry<String, Double> entry : telemetryMetrics.entrySet()) {
                multiMapTelemetryMetrics.put(entry.getKey(), entry.getValue());
            }
            for (Entry<String, Double> entry : metrics.entrySet()) {
                multiMapTelemetryMetrics.put(entry.getKey(), entry.getValue());
            }
            for (Entry<String, Collection<Double>> entry : multiMapTelemetryMetrics.asMap().entrySet()) {
                telemetryMetrics.put(entry.getKey(), entry.getValue().iterator().next());
            }
        }

        Map<String, String> telemetryPropertiesResult = telemetryProperties != null ? telemetryProperties : properties;
        Map<String, Double> telemetryMetricsResult = telemetryMetrics != null ? telemetryMetrics : metrics;
        return CompletableFuture.completedFuture(new Pair<>(telemetryPropertiesResult, telemetryMetricsResult));
    }
}
