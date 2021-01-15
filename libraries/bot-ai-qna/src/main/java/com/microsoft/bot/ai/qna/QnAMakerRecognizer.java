// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.microsoft.bot.ai.qna.models.RankerTypes;
import com.microsoft.bot.ai.qna.models.Metadata;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.Recognizer;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Pair;
import net.minidev.json.JSONObject;
import okhttp3.OkHttpClient;

/**
 * IRecognizer implementation which uses QnAMaker KB to identify intents.
 */
// TODO: missing StringExpression, IntExpression, BoolExpression,
// ArrayExpression, ObjectExpression,
// DialogContext classes
public class QnAMakerRecognizer implements Recognizer {
    private static final String INTENT_PREFIX = "intent=";

    @JsonProperty("knowledgeBaseId")
    private StringExpression knowledgeBaseId;

    @JsonProperty("hostname")
    private StringExpression hostName;

    @JsonProperty("endpointKey")
    private StringExpression endpointKey;

    @JsonProperty("top")
    private IntegerExpression top = 3;

    @JsonProperty("threshold")
    private NumberExpresion threshold = 0.3f;

    @JsonProperty("isTest")
    private Boolean isTest;

    @JsonProperty("rankerType")
    private StringExpression rankerType = RankerTypes.DEFAULT_RANKER_TYPE;

    @JsonProperty("strictFiltersJoinOperator")
    private JoinOperator strictFiltersJoinOperator;

    @JsonProperty("includeDialogNameInMetadata")
    private BoolExpression includeDialogNameInMetadata = true;

    @JsonProperty("metadata")
    private ArrayExpression<Metadata> metadata;

    @JsonProperty("context")
    private ObjectExpression<QnARequestContext> context;

    @JsonProperty("qnaId")
    private IntExpression qnaId = 0;

    @JsonIgnore
    private OkHttpClient httpClient;

    @JsonProperty("logPersonalInformation")
    private BoolExpression logPersonalInformation = "=settings.telemetry.logPersonalInformation";

    /**
     * Gets the KnowledgeBase Id of your QnA Maker KnowledgeBase.
     *
     * @return The knowledgebase Id.
     */
    public StringExpression getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    /**
     * Sets the KnowledgeBase Id of your QnA Maker KnowledgeBase.
     *
     * @param withKnowledgeBaseId The knowledgebase Id.
     */
    public void setKnowledgeBaseId(StringExpression withKnowledgeBaseId) {
        this.knowledgeBaseId = withKnowledgeBaseId;
    }

    /**
     * Gets the Hostname for your QnA Maker service.
     *
     * @return The host name of the QnA Maker knowledgebase.
     */
    public StringExpression getHostName() {
        return hostName;
    }

    /**
     * Sets the Hostname for your QnA Maker service.
     *
     * @param withHostName The host name of the QnA Maker knowledgebase.
     */
    public void setHostName(StringExpression withHostName) {
        this.hostName = withHostName;
    }

    /**
     * Gets the Endpoint key for the QnA Maker KB.
     *
     * @return The endpoint key for the QnA service.
     */
    public StringExpression getEndpointKey() {
        return endpointKey;
    }

    /**
     * Sets the Endpoint key for the QnA Maker KB.
     *
     * @param withEndpointKey The endpoint key for the QnA service.
     */
    public void setEndpointKey(StringExpression withEndpointKey) {
        this.endpointKey = withEndpointKey;
    }

    /**
     * Gets the number of results you want.
     *
     * @return The number of results you want.
     */
    public IntegerExpression getTop() {
        return top;
    }

    /**
     * Sets the number of results you want.
     *
     * @param withTop The number of results you want.
     */
    public void setTop(IntegerExpression withTop) {
        this.top = withTop;
    }

    /**
     * Gets the threshold score to filter results.
     *
     * @return The threshold for the results.
     */
    public NumberExpresion getThreshold() {
        return threshold;
    }

    /**
     * Sets the threshold score to filter results.
     *
     * @param withThreshold The threshold for the results.
     */
    public void setThreshold(NumberExpresion withThreshold) {
        this.threshold = withThreshold;
    }

    /**
     * Gets a value indicating whether gets or sets environment of knowledgebase to
     * be called.
     *
     * @return A value indicating whether to call test or prod environment of
     *         knowledgebase.
     */
    public Boolean getTest() {
        return isTest;
    }

    /**
     * Sets a value indicating whether gets or sets environment of knowledgebase to
     * be called.
     *
     * @param withIstest A value indicating whether to call test or prod environment
     *                   of knowledgebase.
     */
    public void setTest(Boolean withIstest) {
        isTest = withIstest;
    }

    /**
     * Gets ranker Type.
     *
     * @return The desired RankerType.
     */
    public StringExpression getRankerType() {
        return rankerType;
    }

    /**
     * Sets ranker Type.
     *
     * @param withRankerType The desired RankerType.
     */
    public void setRankerType(StringExpression withRankerType) {
        this.rankerType = withRankerType;
    }

    /**
     * Gets {@link Metadata} join operator.
     *
     * @return A value used for Join operation of Metadata {@link Metadata}.
     */
    public JoinOperator getStrictFiltersJoinOperator() {
        return strictFiltersJoinOperator;
    }

    /**
     * Sets {@link Metadata} join operator.
     *
     * @param withStrictFiltersJoinOperator A value used for Join operation of
     *                                      Metadata {@link Metadata}.
     */
    public void setStrictFiltersJoinOperator(JoinOperator withStrictFiltersJoinOperator) {
        this.strictFiltersJoinOperator = withStrictFiltersJoinOperator;
    }

    /**
     * Gets the whether to include the dialog name metadata for QnA context.
     *
     * @return A bool or boolean expression.
     */
    public BoolExpression getIncludeDialogNameInMetadata() {
        return includeDialogNameInMetadata;
    }

    /**
     * Sets the whether to include the dialog name metadata for QnA context.
     *
     * @param withIncludeDialogNameInMetadata A bool or boolean expression.
     */
    public void setIncludeDialogNameInMetadata(BoolExpression withIncludeDialogNameInMetadata) {
        this.includeDialogNameInMetadata = withIncludeDialogNameInMetadata;
    }

    /**
     * Gets an expression to evaluate to set additional metadata name value pairs.
     *
     * @return An expression to evaluate for pairs of metadata.
     */
    public ArrayExpression<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Sets an expression to evaluate to set additional metadata name value pairs.
     *
     * @param withMetadata An expression to evaluate for pairs of metadata.
     */
    public void setMetadata(ArrayExpression<Metadata> withMetadata) {
        this.metadata = withMetadata;
    }

    /**
     * Gets an expression to evaluate to set the context.
     *
     * @return An expression to evaluate to QnARequestContext to pass as context.
     */
    public ObjectExpression<QnARequestContext> getContext() {
        return context;
    }

    /**
     * Sets an expression to evaluate to set the context.
     *
     * @param withContext An expression to evaluate to QnARequestContext to pass as
     *                    context.
     */
    public void setContext(ObjectExpression<QnARequestContext> withContext) {
        this.context = withContext;
    }

    /**
     * Gets an expression or number to use for the QnAId parameter.
     *
     * @return The expression or number.
     */
    public IntExpression getQnaId() {
        return qnaId;
    }

    /**
     * Sets an expression or number to use for the QnAId parameter.
     *
     * @param withQnaId The expression or number.
     */
    public void setQnaId(IntExpression withQnaId) {
        this.qnaId = withQnaId;
    }

    /**
     * Gets the {@link OkHttpClient} to be used when calling the QnA Maker API.
     *
     * @return An instance of {@link OkHttpClient}.
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Sets the {@link OkHttpClient} to be used when calling the QnA Maker API.
     *
     * @param withHttpClient An instance of {@link OkHttpClient}.
     */
    public void setHttpClient(OkHttpClient withHttpClient) {
        this.httpClient = withHttpClient;
    }

    /**
     * Gets the flag to determine if personal information should be logged in
     * telemetry.
     *
     * @return The flag to indicate in personal information should be logged in
     *         telemetry.
     */
    public BoolExpression getLogPersonalInformation() {
        return logPersonalInformation;
    }

    /**
     * Sets the flag to determine if personal information should be logged in
     * telemetry.
     *
     * @param withLogPersonalInformation The flag to indicate in personal
     *                                   information should be logged in telemetry.
     */
    public void setLogPersonalInformation(BoolExpression withLogPersonalInformation) {
        this.logPersonalInformation = withLogPersonalInformation;
    }

    /**
     * The declarative type for this recognizer.
     */
    @JsonProperty("kind")
    public static final String kind = "Microsoft.QnAMakerRecognizer";

    /**
     * Key used when adding the intent to the {@link RecognizerResult} intents
     * collection.
     */
    public static final String qnaMatchIntent = "QnAMatch";

    /**
     * Initializes a new instance of the {@link QnAMakerRecognizer} class.
     */
    public QnAMakerRecognizer() {

    }

    /**
     * Return results of the call to QnA Maker.
     *
     * @param dialogContext       Context object containing information for a single
     *                            turn of conversation with a user.
     * @param activity            The incoming activity received from the user. The
     *                            Text property value is used as the query text for
     *                            QnA Maker.
     * @param telemetryProperties Additional properties to be logged to telemetry
     *                            with the LuisResult event.
     * @param telemetryMetrics    Additional metrics to be logged to telemetry with
     *                            the LuisResult event.
     * @return A {@link RecognizerResult} containing the QnA Maker result.
     */
    @Override
    public CompletableFuture<RecognizerResult> recognize(DialogContext dialogContext, Activity activity,
            @Nullable Map<String, String> telemetryProperties, @Nullable Map<String, Double> telemetryMetrics) {
        // Identify matched intents
        RecognizerResult recognizerResult = new RecognizerResult() {
            {
                setText(activity.getText());
                setIntents(new HashMap<String, IntentScore>());
            }
        };

        if (Strings.isNullOrEmpty(activity.getText())) {
            Map<String, IntentScore> intents = recognizerResult.getIntents();
            intents.put("None", new IntentScore());
            recognizerResult.setIntents(intents);
            return CompletableFuture.completedFuture(recognizerResult);
        }

        List<Metadata> filters = new ArrayList<Metadata>();
        if (this.includeDialogNameInMetadata.getValue(dialogContext.getState())) {
            filters.add(new Metadata() {
                {
                    setName("dialogName");
                    setValue(dialogContext.getActiveDialog.getId());
                }
            });
        }

        // if there is $qna.metadata set add to filters
        List<Metadata> externalMetadata = this.metadata.getValue(dialogContext.getState());
        if (externalMetadata != null) {
            filters.addAll(externalMetadata);
        }

        // Calling QnAMaker to get response.
        return this.getQnAMakerClient(dialogContext).thenCompose(qnaClient -> {
            return qnaClient.getAnswers(dialogContext.getContext(), new QnAMakerOptions() {
                {
                    setContext(context.getValue(dialogContext.getState()));
                    setScoreThreshold(threshold.getValue(dialogContext.getState()));
                    setStrictFilters((Metadata[]) filters.toArray());
                    setTop(top.getValue(dialogContext.getState()));
                    setQnAId(qnaId.getValue(dialogContext.getState()));
                    setRankerType(rankerType.getValue(dialogContext.getState()));
                    setIsTest(isTest);
                    setStrictFiltersJoinOperator(strictFiltersJoinOperator);
                }
            }, null).thenApply(answers -> {
                if (answers.length > 0) {
                    QueryResult topAnswer = null;
                    for (QueryResult answer : answers) {
                        if (topAnswer == null || answer.getScore() > topAnswer.getScore()) {
                            topAnswer = answer;
                        }
                    }

                    if (topAnswer.getAnswer().trim().toUpperCase(Locale.ROOT)
                            .startsWith(QnAMakerRecognizer.INTENT_PREFIX.toUpperCase(Locale.ROOT))) {
                        Map<String, IntentScore> intents = recognizerResult.getIntents();
                        QueryResult finalTopAnswer = topAnswer;
                        intents.put(topAnswer.getAnswer().trim().substring(QnAMakerRecognizer.INTENT_PREFIX.length())
                                .trim(), new IntentScore() {
                                    {
                                        setScore(finalTopAnswer.getScore());
                                    }
                                });
                        recognizerResult.setIntents(intents);
                    } else {
                        Map<String, IntentScore> intents = recognizerResult.getIntents();
                        QueryResult finalTopAnswer = topAnswer;
                        intents.put(this.qnaMatchIntent, new IntentScore() {
                            {
                                setScore(finalTopAnswer.getScore());
                            }
                        });
                    }

                    List<String> answerArray = new ArrayList<String>();
                    answerArray.add(topAnswer.getAnswer());
                    ObjectPath.setPathValue(recognizerResult, "entities.answer", answerArray);

                    List instance = new ArrayList();
                    JSONObject data = new JSONObject() {
                        {
                            put("startIndex", 0);
                            put("endIndex", activity.getText().length());
                        }
                    };
                    instance.add(data);
                    ObjectPath.setPathValue(recognizerResult, "entities.$instance.answer", instance);
                    recognizerResult.setProperties("answers", answers);
                } else {
                    Map<String, IntentScore> intents = recognizerResult.getIntents();
                    intents.put("None", new IntentScore() {
                        {
                            setScore(1.0f);
                        }
                    });
                    recognizerResult.setIntents(intents);
                }

                this.trackRecognizerResult(dialogContext, "QnAMakerRecognizerResult",
                        this.fillRecognizerResultTelemetryProperties(recognizerResult, telemetryProperties),
                        telemetryMetrics);
                return recognizerResult;
            });
        });
    }

    /**
     * Gets an instance of {@link IQnAMakerClient}.
     *
     * @param dc The {@link DialogContext} used to access state.
     * @return An instance of {@link IQnAMakerClient}.
     */
    protected CompletableFuture<IQnAMakerClient> getQnAMakerClient(DialogContext dc) {
        IQnAMakerClient qnaClient = (IQnAMakerClient) dc.getContext().getTurnState().get();
        if (qnaClient != null) {
            // return mock client
            return CompletableFuture.completedFuture(qnaClient);
        }

        OkHttpClient httpClient = (OkHttpClient) dc.getContext().getTurnState().get();
        if (httpClient == null) {
            httpClient = this.httpClient;
        }

        Pair endpointPair = this.endpointKey.tryGetValue(dc.getState());
        Pair hostNamePair = this.hostName.tryGetValue(dc.getState());
        Pair knowledgeBaseIdPair = this.knowledgeBaseId.tryGetValue(dc.getState());
        Pair logPersonalInformationPair = this.logPersonalInformation.tryGetValue(dc.getState());

        if (endpointPair.getLeft() == null) {
            throw new IllegalStateException(String.format("Unable to get a value for %1$s from state. %2$s",
                    "endpointKey", endpointPair.getRight()));
        }

        if (hostNamePair.getLeft() == null) {
            throw new IllegalStateException(String.format("Unable to get a value for %1$s from state. %2$s", "hostName",
                    hostNamePair.getRight()));
        }

        if (knowledgeBaseIdPair.getLeft() == null) {
            throw new IllegalStateException(String.format("Unable to get a value for %1$s from state. %2$s",
                    "knowledgeBaseId", knowledgeBaseIdPair.getRight()));
        }

        QnAMakerEndpoint endpoint = new QnAMakerEndpoint() {
            {
                setEndpointKey((String) endpointPair.getLeft());
                setHost((String) hostNamePair.getLeft());
                setKnowledgeBaseId((String) knowledgeBaseIdPair.getLeft());
            }
        };

        return CompletableFuture.completedFuture(new QnAMaker(endpoint, new QnAMakerOptions(), httpClient,
                this.telemetryClient, logPersonalInformationPair.getLeft()));
    }
}
