// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.microsoft.bot.ai.qna.models.Metadata;
import com.microsoft.bot.ai.qna.models.QnARequestContext;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.ai.qna.models.RankerTypes;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.Recognizer;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Serialization;

/**
 * IRecognizer implementation which uses QnAMaker KB to identify intents.
 */
public class QnAMakerRecognizer extends Recognizer {

    private static final Integer TOP_DEFAULT_VALUE = 3;
    private static final Float THRESHOLD_DEFAULT_VALUE = 0.3f;

    private final String qnAMatchIntent = "QnAMatch";

    private final String intentPrefix = "intent=";

    @JsonProperty("knowledgeBaseId")
    private String knowledgeBaseId;

    @JsonProperty("hostname")
    private String hostName;

    @JsonProperty("endpointKey")
    private String endpointKey;

    @JsonProperty("top")
    private Integer top = TOP_DEFAULT_VALUE;

    @JsonProperty("threshold")
    private Float threshold = THRESHOLD_DEFAULT_VALUE;

    @JsonProperty("isTest")
    private Boolean isTest;

    @JsonProperty("rankerType")
    private String rankerType = RankerTypes.DEFAULT_RANKER_TYPE;

    @JsonProperty("strictFiltersJoinOperator")
    private JoinOperator strictFiltersJoinOperator;

    @JsonProperty("includeDialogNameInMetadata")
    private Boolean includeDialogNameInMetadata = true;

    @JsonProperty("metadata")
    private Metadata[] metadata;

    @JsonProperty("context")
    private QnARequestContext context;

    @JsonProperty("qnaId")
    private Integer qnAId = 0;

    @JsonProperty("logPersonalInformation")
    private Boolean logPersonalInformation = false;

    /**
     * Gets key used when adding the intent to the {@link RecognizerResult} intents
     * collection.
     *
     * @return Key used when adding the intent to the {@link RecognizerResult}
     *         intents collection.
     */
    public String getQnAMatchIntent() {
        return qnAMatchIntent;
    }

    /**
     * Gets the KnowledgeBase Id of your QnA Maker KnowledgeBase.
     *
     * @return The knowledgebase Id.
     */
    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    /**
     * Sets the KnowledgeBase Id of your QnA Maker KnowledgeBase.
     *
     * @param withKnowledgeBaseId The knowledgebase Id.
     */
    public void setKnowledgeBaseId(String withKnowledgeBaseId) {
        this.knowledgeBaseId = withKnowledgeBaseId;
    }

    /**
     * Gets the Hostname for your QnA Maker service.
     *
     * @return The host name of the QnA Maker knowledgebase.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the Hostname for your QnA Maker service.
     *
     * @param withHostName The host name of the QnA Maker knowledgebase.
     */
    public void setHostName(String withHostName) {
        this.hostName = withHostName;
    }

    /**
     * Gets the Endpoint key for the QnA Maker KB.
     *
     * @return The endpoint key for the QnA service.
     */
    public String getEndpointKey() {
        return endpointKey;
    }

    /**
     * Sets the Endpoint key for the QnA Maker KB.
     *
     * @param withEndpointKey The endpoint key for the QnA service.
     */
    public void setEndpointKey(String withEndpointKey) {
        this.endpointKey = withEndpointKey;
    }

    /**
     * Gets the number of results you want.
     *
     * @return The number of results you want.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Sets the number of results you want.
     *
     * @param withTop The number of results you want.
     */
    public void setTop(Integer withTop) {
        this.top = withTop;
    }

    /**
     * Gets the threshold score to filter results.
     *
     * @return The threshold for the results.
     */
    public Float getThreshold() {
        return threshold;
    }

    /**
     * Sets the threshold score to filter results.
     *
     * @param withThreshold The threshold for the results.
     */
    public void setThreshold(Float withThreshold) {
        this.threshold = withThreshold;
    }

    /**
     * Gets a value indicating whether gets or sets environment of knowledgebase to
     * be called.
     *
     * @return A value indicating whether to call test or prod environment of
     *         knowledgebase.
     */
    public Boolean getIsTest() {
        return isTest;
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
     * Gets ranker Type.
     *
     * @return The desired RankerType.
     */
    public String getRankerType() {
        return rankerType;
    }

    /**
     * Sets ranker Type.
     *
     * @param withRankerType The desired RankerType.
     */
    public void setRankerType(String withRankerType) {
        this.rankerType = withRankerType;
    }

    /**
     * Gets {@link Metadata} join operator.
     *
     * @return A value used for Join operation of Metadata <see cref="Metadata"/>.
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
    public Boolean getIncludeDialogNameInMetadata() {
        return includeDialogNameInMetadata;
    }

    /**
     * Sets the whether to include the dialog name metadata for QnA context.
     *
     * @param withIncludeDialogNameInMetadata A bool or boolean expression.
     */
    public void setIncludeDialogNameInMetadata(Boolean withIncludeDialogNameInMetadata) {
        this.includeDialogNameInMetadata = withIncludeDialogNameInMetadata;
    }

    /**
     * Gets an expression to evaluate to set additional metadata name value pairs.
     *
     * @return An expression to evaluate for pairs of metadata.
     */
    public Metadata[] getMetadata() {
        return metadata;
    }

    /**
     * Sets an expression to evaluate to set additional metadata name value pairs.
     *
     * @param withMetadata An expression to evaluate for pairs of metadata.
     */
    public void setMetadata(Metadata[] withMetadata) {
        this.metadata = withMetadata;
    }

    /**
     * Gets an expression to evaluate to set the context.
     *
     * @return An expression to evaluate to QnARequestContext to pass as context.
     */
    public QnARequestContext getContext() {
        return context;
    }

    /**
     * Sets an expression to evaluate to set the context.
     *
     * @param withContext An expression to evaluate to QnARequestContext to pass as
     *                    context.
     */
    public void setContext(QnARequestContext withContext) {
        this.context = withContext;
    }

    /**
     * Gets an expression or numberto use for the QnAId paratemer.
     *
     * @return The expression or number.
     */
    public Integer getQnAId() {
        return qnAId;
    }

    /**
     * Sets an expression or numberto use for the QnAId paratemer.
     *
     * @param withQnAId The expression or number.
     */
    public void setQnAId(Integer withQnAId) {
        this.qnAId = withQnAId;
    }

    /**
     * Gets the flag to determine if personal information should be logged in
     * telemetry.
     *
     * @return The flag to indicate in personal information should be logged in
     *         telemetry.
     */
    public Boolean getLogPersonalInformation() {
        return logPersonalInformation;
    }

    /**
     * Sets the flag to determine if personal information should be logged in
     * telemetry.
     *
     * @param withLogPersonalInformation The flag to indicate in personal
     *                                   information should be logged in telemetry.
     */
    public void setLogPersonalInformation(Boolean withLogPersonalInformation) {
        this.logPersonalInformation = withLogPersonalInformation;
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
    public CompletableFuture<RecognizerResult> recognize(
        DialogContext dialogContext,
        Activity activity,
        Map<String, String> telemetryProperties,
        Map<String, Double> telemetryMetrics
    ) {
        // Identify matched intents
        RecognizerResult recognizerResult = new RecognizerResult();
        recognizerResult.setText(activity.getText());
        recognizerResult.setIntents(new HashMap<String, IntentScore>());
        if (Strings.isNullOrEmpty(activity.getText())) {
            recognizerResult.getIntents().put("None", new IntentScore());
            return CompletableFuture.completedFuture(recognizerResult);
        }

        List<Metadata> filters = new ArrayList<Metadata>();
        // TODO this should be uncommented as soon as Expression is added in Java
        /*
         * if (this.includeDialogNameInMetadata.getValue(dialogContext.getState())) {
         * filters.add(new Metadata() { { setName("dialogName");
         * setValue(dialogContext.getActiveDialog().getId()); } }); }
         */

        // if there is $qna.metadata set add to filters
        Metadata[] externalMetadata = this.metadata;
        if (externalMetadata != null) {
            filters.addAll(Arrays.asList(externalMetadata));
        }

        QnAMakerOptions options = new QnAMakerOptions();
        options.setContext(context);
        options.setScoreThreshold(threshold);
        options.setStrictFilters(filters.toArray(new Metadata[filters.size()]));
        options.setTop(top);
        options.setQnAId(qnAId);
        options.setIsTest(isTest);
        options.setStrictFiltersJoinOperator(strictFiltersJoinOperator);

        // Calling QnAMaker to get response.
        return this.getQnAMakerClient(dialogContext).thenCompose(qnaClient -> {
            return qnaClient.getAnswers(dialogContext.getContext(), options, null, null).thenApply(answers -> {
                if (answers.length > 0) {
                    QueryResult topAnswer = null;
                    for (QueryResult answer : answers) {
                        if (topAnswer == null || answer.getScore() > topAnswer.getScore()) {
                            topAnswer = answer;
                        }
                    }
                    Float internalTopAnswer = topAnswer.getScore();
                    if (topAnswer.getAnswer().trim().toUpperCase().startsWith(intentPrefix.toUpperCase())) {
                        IntentScore intentScore = new IntentScore();
                        intentScore.setScore(internalTopAnswer);
                        recognizerResult.getIntents()
                            .put(topAnswer.getAnswer().trim().substring(
                                intentPrefix.length()).trim(),
                                intentScore
                            );
                    } else {
                        IntentScore intentScore = new IntentScore();
                        intentScore.setScore(internalTopAnswer);
                        recognizerResult.getIntents().put(this.qnAMatchIntent, intentScore);
                    }
                    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
                    ObjectNode entitiesNode = mapper.createObjectNode();
                    List<String> answerArray = new ArrayList<String>();
                    answerArray.add(topAnswer.getAnswer());
                    ArrayNode entitiesArrayNode = entitiesNode.putArray("answer");
                    entitiesArrayNode.add(topAnswer.getAnswer());

                    ObjectNode instance = entitiesNode.putObject("$instance");
                    ArrayNode instanceArrayNode = instance.putArray("answer");
                    ObjectNode data = instanceArrayNode.addObject();
                    data.setAll((ObjectNode) mapper.valueToTree(topAnswer));
                    data.put("startIndex", 0);
                    data.put("endIndex", activity.getText().length());

                    recognizerResult.setEntities(entitiesNode);
                    recognizerResult.getProperties().put("answers", mapper.valueToTree(answers));
                } else {
                    IntentScore intentScore = new IntentScore();
                    intentScore.setScore(1.0f);
                    recognizerResult.getIntents().put("None", intentScore);
                }

                this.trackRecognizerResult(
                    dialogContext,
                    "QnAMakerRecognizerResult",
                    this.fillRecognizerResultTelemetryProperties(recognizerResult, telemetryProperties, dialogContext),
                    telemetryMetrics
                );
                return recognizerResult;
            });
        });
    }

    /**
     * Gets an instance of {@link QnAMakerClient}.
     *
     * @param dc The {@link DialogContext} used to access state.
     * @return An instance of {@link QnAMakerClient}.
     */
    protected CompletableFuture<QnAMakerClient> getQnAMakerClient(DialogContext dc) {
        QnAMakerClient qnaClient = dc.getContext().getTurnState().get(QnAMakerClient.class);
        if (qnaClient != null) {
            // return mock client
            return CompletableFuture.completedFuture(qnaClient);
        }

        String epKey = this.endpointKey;
        String hn = this.hostName;
        String kbId = this.knowledgeBaseId;
        Boolean logPersonalInfo = this.logPersonalInformation;
        QnAMakerEndpoint endpoint = new QnAMakerEndpoint();
        endpoint.setEndpointKey(epKey);
        endpoint.setHost(hn);
        endpoint.setKnowledgeBaseId(kbId);

        return CompletableFuture
            .completedFuture(new QnAMaker(endpoint, new QnAMakerOptions(), this.getTelemetryClient(), logPersonalInfo));
    }

    /**
     * Uses the RecognizerResult to create a list of properties to be included when
     * tracking the result in telemetry.
     *
     * @param recognizerResult    Recognizer Result.
     * @param telemetryProperties A list of properties to append or override the
     *                            properties created using the RecognizerResult.
     * @param dialogContext       Dialog Context.
     * @return A dictionary that can be included when calling the TrackEvent method
     *         on the TelemetryClient.
     */
    @Override
    protected Map<String, String> fillRecognizerResultTelemetryProperties(
        RecognizerResult recognizerResult,
        Map<String, String> telemetryProperties,
        @Nullable DialogContext dialogContext
    ) {
        if (dialogContext == null) {
            throw new IllegalArgumentException(
                "dialogContext: DialogContext needed for state in "
                    + "AdaptiveRecognizer.FillRecognizerResultTelemetryProperties method."
            );
        }

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(
            "TopIntent",
            !recognizerResult.getIntents().isEmpty()
                ? (String) recognizerResult.getIntents().keySet().toArray()[0]
                : null
        );
        properties.put(
            "TopIntentScore",
            !recognizerResult.getIntents()
                .isEmpty()
                    ? Double.toString(
                        ((IntentScore) recognizerResult.getIntents().values().toArray()[0]).getScore()
                    )
                    : null
        );
        properties.put(
            "Intents",
            !recognizerResult.getIntents().isEmpty()
                ? Serialization.toStringSilent(recognizerResult.getIntents())
                : null
        );
        properties.put(
            "Entities",
            recognizerResult.getEntities() != null
                ? Serialization.toStringSilent(recognizerResult.getEntities())
                : null
        );
        properties.put(
            "AdditionalProperties",
            !recognizerResult.getProperties().isEmpty()
                ? Serialization.toStringSilent(recognizerResult.getProperties())
                : null
        );
        if (logPersonalInformation && !Strings.isNullOrEmpty(recognizerResult.getText())) {
            properties.put("Text", recognizerResult.getText());
            properties.put("AlteredText", recognizerResult.getAlteredText());
        }

        // Additional Properties can override "stock properties".
        if (telemetryProperties != null) {
            telemetryProperties.putAll(properties);
            Map<String, List<String>> telemetryPropertiesMap =
                telemetryProperties.entrySet()
                    .stream()
                    .collect(
                        Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList()))
                    );
            return telemetryPropertiesMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get(0)));
        }

        return properties;
    }
}
