// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.ai.qna.QnAMaker;
import com.microsoft.bot.ai.qna.QnAMakerEndpoint;
import com.microsoft.bot.ai.qna.QnAMakerOptions;
import com.microsoft.bot.ai.qna.models.Metadata;
import com.microsoft.bot.ai.qna.models.QnAMakerTraceInfo;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.ai.qna.models.QueryResults;
import com.microsoft.bot.ai.qna.models.RankerTypes;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.schema.Activity;

import net.minidev.json.JSONObject;
import okhttp3.Response;
import org.slf4j.LoggerFactory;

/**
 * Helper class for Generate Answer API.
 */
public class GenerateAnswerUtils {
    private QnAMakerEndpoint endpoint;
    private QnAMakerOptions options;

    private static final Integer PERCENTAGE_DIVISOR = 100;
    private static final Float SCORE_THRESHOLD = 0.3f;
    private static final Double TIMEOUT = 100000d;

    /**
     * Initializes a new instance of the {@link GenerateAnswerUtils} class.
     *
     * @param withEndpoint        QnA Maker endpoint details.
     * @param withOptions         QnA Maker options.
     */
    public GenerateAnswerUtils(QnAMakerEndpoint withEndpoint,
            QnAMakerOptions withOptions) {
        this.endpoint = withEndpoint;

        this.options = withOptions != null ? withOptions : new QnAMakerOptions();
        GenerateAnswerUtils.validateOptions(this.options);
    }

    /**
     * Gets qnA Maker options.
     *
     * @return The options for QnAMaker.
     */
    public QnAMakerOptions getOptions() {
        return this.options;
    }

    /**
     * Sets qnA Maker options.
     *
     * @param withOptions The options for QnAMaker.
     */
    public void setOptions(QnAMakerOptions withOptions) {
        this.options = withOptions;
    }

    /**
     * Generates an answer from the knowledge base.
     *
     * @param turnContext     The Turn Context that contains the user question to be
     *                        queried against your knowledge base.
     * @param messageActivity Message activity of the turn context.
     * @param withOptions         The options for the QnA Maker knowledge base. If null,
     *                        constructor option is used for this instance.
     * @return A list of answers for the user query, sorted in decreasing order of
     *         ranking score.
     * @throws IOException IOException
     */
    @Deprecated
    public CompletableFuture<QueryResult[]> getAnswers(TurnContext turnContext, Activity messageActivity,
            QnAMakerOptions withOptions) throws IOException {
        return this.getAnswersRaw(turnContext, messageActivity, withOptions).thenApply(result -> result.getAnswers());
    }

    /**
     * Generates an answer from the knowledge base.
     *
     * @param turnContext     The Turn Context that contains the user question to be
     *                        queried against your knowledge base.
     * @param messageActivity Message activity of the turn context.
     * @param withOptions         The options for the QnA Maker knowledge base. If null,
     *                        constructor option is used for this instance.
     * @return A list of answers for the user query, sorted in decreasing order of
     *         ranking score.
     */
    public CompletableFuture<QueryResults> getAnswersRaw(TurnContext turnContext, Activity messageActivity,
            QnAMakerOptions withOptions) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext");
        }

        if (turnContext.getActivity() == null) {
            throw new IllegalArgumentException(String.format("The %1$s property for %2$s can't be null: turnContext",
                    turnContext.getActivity(), "turnContext"));
        }

        if (messageActivity == null) {
            throw new IllegalArgumentException("Activity type is not a message");
        }

        QnAMakerOptions hydratedOptions = this.hydrateOptions(withOptions);
        GenerateAnswerUtils.validateOptions(hydratedOptions);

        try {
            return this.queryQnaService(messageActivity, hydratedOptions).thenCompose(result -> {
                this.emitTraceInfo(turnContext, messageActivity, result.getAnswers(), hydratedOptions);
                return CompletableFuture.completedFuture(result);
            });
        } catch (IOException e) {
            LoggerFactory.getLogger(GenerateAnswerUtils.class).error("getANswersRaw");
            return CompletableFuture.completedFuture(null);
        }
    }

    private static CompletableFuture<QueryResults> formatQnAResult(Response response, QnAMakerOptions options)
            throws IOException {
        String jsonResponse = null;
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        QueryResults results = null;

        jsonResponse = response.body().string();
        results = jacksonAdapter.deserialize(jsonResponse, QueryResults.class);
        for (QueryResult answer : results.getAnswers()) {
            answer.setScore(answer.getScore() / PERCENTAGE_DIVISOR);
        }
        results.setAnswers((QueryResult[]) Arrays.asList(results.getAnswers()).stream()
                .filter(answer -> answer.getScore() > options.getScoreThreshold()).toArray());

        return CompletableFuture.completedFuture(results);
    }

    private static void validateOptions(QnAMakerOptions options) {
        if (options.getScoreThreshold() == 0) {
            options.setScoreThreshold(SCORE_THRESHOLD);
        }

        if (options.getTop() == 0) {
            options.setTop(1);
        }

        if (options.getScoreThreshold() < 0 || options.getScoreThreshold() > 1) {
            throw new IllegalArgumentException(String
                    .format("options: The %s property should be a value between 0 and 1", options.getScoreThreshold()));
        }

        if (options.getTimeout() == 0.0d) {
            options.setTimeout(TIMEOUT);
        }

        if (options.getTop() < 1) {
            throw new IllegalArgumentException("options: The top property should be an integer greater than 0");
        }

        if (options.getStrictFilters() == null) {
            options.setStrictFilters(new Metadata[0]);
        }

        if (options.getRankerType() == null) {
            options.setRankerType(RankerTypes.DEFAULT_RANKER_TYPE);
        }
    }

    /**
     * Combines QnAMakerOptions passed into the QnAMaker constructor with the
     * options passed as arguments into GetAnswersAsync().
     *
     * @param queryOptions The options for the QnA Maker knowledge base.
     * @return Return modified options for the QnA Maker knowledge base.
     */
    private QnAMakerOptions hydrateOptions(QnAMakerOptions queryOptions) {
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        QnAMakerOptions hydratedOptions = null;

        try {
            hydratedOptions = jacksonAdapter.<QnAMakerOptions>deserialize(jacksonAdapter.serialize(queryOptions),
                    QnAMakerOptions.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(GenerateAnswerUtils.class).error("hydrateOptions");
        }

        if (queryOptions != null) {
            if (queryOptions.getScoreThreshold() != hydratedOptions.getScoreThreshold()
                    && queryOptions.getScoreThreshold() != 0) {
                hydratedOptions.setScoreThreshold(queryOptions.getScoreThreshold());
            }

            if (queryOptions.getTop() != hydratedOptions.getTop() && queryOptions.getTop() != 0) {
                hydratedOptions.setTop(queryOptions.getTop());
            }

            if (queryOptions.getStrictFilters().length > 0) {
                hydratedOptions.setStrictFilters(queryOptions.getStrictFilters());
            }

            hydratedOptions.setContext(queryOptions.getContext());
            hydratedOptions.setQnAId(queryOptions.getQnAId());
            hydratedOptions.setIsTest(queryOptions.getIsTest());
            hydratedOptions.setRankerType(queryOptions.getRankerType() != null ? queryOptions.getRankerType()
                    : RankerTypes.DEFAULT_RANKER_TYPE);
            hydratedOptions.setStrictFiltersJoinOperator(queryOptions.getStrictFiltersJoinOperator());
        }

        return hydratedOptions;
    }

    private CompletableFuture<QueryResults> queryQnaService(Activity messageActivity, QnAMakerOptions withOptions)
            throws IOException {
        String requestUrl = String.format("%1$s/knowledgebases/%2$s/generateanswer", this.endpoint.getHost(),
                this.endpoint.getKnowledgeBaseId());
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        String jsonRequest = null;

        jsonRequest = jacksonAdapter.serialize(new JSONObject() {
            {
                put("question", messageActivity.getText());
                put("top", withOptions.getTop());
                put("strictFilters", withOptions.getStrictFilters());
                put("scoreThreshold", withOptions.getScoreThreshold());
                put("context", withOptions.getContext());
                put("qnaId", withOptions.getQnAId());
                put("isTest", withOptions.getIsTest());
                put("rankerType", withOptions.getRankerType());
                put("StrictFiltersCompoundOperationType", withOptions.getStrictFiltersJoinOperator());
            }
        });

        HttpRequestUtils httpRequestHelper = new HttpRequestUtils();
        return httpRequestHelper.executeHttpRequest(requestUrl, jsonRequest, this.endpoint).thenCompose(response -> {
            try {
                return GenerateAnswerUtils.formatQnAResult(response, withOptions);
            } catch (IOException e) {
                LoggerFactory.getLogger(GenerateAnswerUtils.class).error("QueryQnAService", e);
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    private CompletableFuture<Void> emitTraceInfo(TurnContext turnContext, Activity messageActivity,
            QueryResult[] result, QnAMakerOptions withOptions) {
        String knowledgeBaseId = this.endpoint.getKnowledgeBaseId();
        QnAMakerTraceInfo traceInfo = new QnAMakerTraceInfo() {
            {
                setMessage(messageActivity);
                setQueryResults(result);
                setKnowledgeBaseId(knowledgeBaseId);
                setScoreThreshold(withOptions.getScoreThreshold());
                setTop(withOptions.getTop());
                setStrictFilters(withOptions.getStrictFilters());
                setContext(withOptions.getContext());
                setQnAId(withOptions.getQnAId());
                setIsTest(withOptions.getIsTest());
                setRankerType(withOptions.getRankerType());
            }
        };
        Activity traceActivity = Activity.createTraceActivity(QnAMaker.QNA_MAKER_NAME, QnAMaker.QNA_MAKER_TRACE_TYPE,
                traceInfo, QnAMaker.QNA_MAKER_TRACE_LABEL);
        return turnContext.sendActivity(traceActivity).thenApply(response -> null);
    }
}
