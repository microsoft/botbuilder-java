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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.microsoft.bot.ai.qna.models.Metadata;
import com.microsoft.bot.ai.qna.models.QnARequestContext;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.ai.qna.models.RankerTypes;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.ObjectPath;
import com.microsoft.bot.dialogs.Recognizer;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Serialization;

/**
 * IRecognizer implementation which uses QnAMaker KB to identify intents.
 */
public class QnAMakerRecognizer extends Recognizer {

    @JsonProperty("$kind")
    private final String kind = "Microsoft.QnAMakerRecognizer";

    private final String qnAMatchIntent = "QnAMatch";

    private final String intentPrefix = "intent=";

    @JsonProperty("knowledgeBaseId")
    private String knowledgeBaseId;

    @JsonProperty("hostname")
    private String hostName;

    @JsonProperty("endpointKey")
    private String endpointKey;

    @JsonProperty("top")
    private Integer top = 3;

    @JsonProperty("threshold")
    private Float threshold = 0.3f;

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
     * Gets the declarative type for this recognizer.
     *
     * @return The declarative type for this recognizer.
     */
    public String getKind() {
        return kind;
    }

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
     * @param knowledgeBaseId The knowledgebase Id.
     */
    public void setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
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
     * @param hostName The host name of the QnA Maker knowledgebase.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
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
     * @param endpointKey The endpoint key for the QnA service.
     */
    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
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
     * @param top The number of results you want.
     */
    public void setTop(Integer top) {
        this.top = top;
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
     * @param threshold The threshold for the results.
     */
    public void setThreshold(Float threshold) {
        this.threshold = threshold;
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
     * @param isTest A value indicating whether to call test or prod environment of
     *               knowledgebase.
     */
    public void setIsTest(Boolean isTest) {
        this.isTest = isTest;
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
     * @param rankerType The desired RankerType.
     */
    public void setRankerType(String rankerType) {
        this.rankerType = rankerType;
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
     * @param strictFiltersJoinOperator A value used for Join operation of Metadata
     *                                  {@link Metadata}.
     */
    public void setStrictFiltersJoinOperator(JoinOperator strictFiltersJoinOperator) {
        this.strictFiltersJoinOperator = strictFiltersJoinOperator;
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
     * @param includeDialogNameInMetadata A bool or boolean expression.
     */
    public void setIncludeDialogNameInMetadata(Boolean includeDialogNameInMetadata) {
        this.includeDialogNameInMetadata = includeDialogNameInMetadata;
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
     * @param metadata An expression to evaluate for pairs of metadata.
     */
    public void setMetadata(Metadata[] metadata) {
        this.metadata = metadata;
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
     * @param context An expression to evaluate to QnARequestContext to pass as
     *                context.
     */
    public void setContext(QnARequestContext context) {
        this.context = context;
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
     * @param qnAId The expression or number.
     */
    public void setQnAId(Integer qnAId) {
        this.qnAId = qnAId;
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
     * @param logPersonalInformation The flag to indicate in personal information
     *                               should be logged in telemetry.
     */
    public void setLogPersonalInformation(Boolean logPersonalInformation) {
        this.logPersonalInformation = logPersonalInformation;
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
            Map<String, String> telemetryProperties, Map<String, Double> telemetryMetrics) {
        // Identify matched intents
        RecognizerResult recognizerResult = new RecognizerResult() {
            {
                setText(activity.getText());
                setIntents(new HashMap<String, IntentScore>());
            }
        };

        if (Strings.isNullOrEmpty(activity.getText())) {
            recognizerResult.getIntents().put("None", new IntentScore());
            return CompletableFuture.completedFuture(recognizerResult);
        }

        List<Metadata> filters = new ArrayList<Metadata>();
        // TODO: this should be uncommented as soon as Expression is added in Java
        /* if (this.includeDialogNameInMetadata.getValue(dialogContext.getState())) {
            filters.add(new Metadata() {
                {
                    setName("dialogName");
                    setValue(dialogContext.getActiveDialog().getId());
                }
            });
        } */

        // if there is $qna.metadata set add to filters
        Metadata[] externalMetadata = this.metadata;
        if (externalMetadata != null) {
            filters.addAll(Arrays.asList(externalMetadata));
        }

        QnAMakerOptions options = new QnAMakerOptions() {
            {
                setContext(context);
                setThreshold(threshold);
                setStrictFilters(filters.toArray(new Metadata[filters.size()]));
                setTop(top);
                setQnAId(qnAId);
                setIsTest(isTest);
                setStrictFiltersJoinOperator(strictFiltersJoinOperator);
            }
        };
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
                        recognizerResult.getIntents().put(
                                topAnswer.getAnswer().trim().substring(intentPrefix.length()).trim(),
                                new IntentScore() {
                                    {
                                        setScore(internalTopAnswer);
                                    }
                                });
                    } else {
                        recognizerResult.getIntents().put(this.qnAMatchIntent, new IntentScore() {
                            {
                                setScore(internalTopAnswer);
                            }
                        });
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> answerArray = new ArrayList<String>();
                    answerArray.add(topAnswer.getAnswer());
                    ObjectPath.setPathValue(recognizerResult, "entities.answer", answerArray);

                    ObjectNode instance = mapper.createObjectNode();
                    instance.put("startIndex", 0);
                    instance.put("endIndex", activity.getText().length());
                    ObjectPath.setPathValue(recognizerResult, "entities.$instance.answer", instance);

                    recognizerResult.getProperties().put("answers", mapper.valueToTree(answerArray));
                } else {
                    recognizerResult.getIntents().put("None", new IntentScore() {
                        {
                            setScore(1.0f);
                        }
                    });
                }

                this.trackRecognizerResult(dialogContext, "QnAMakerRecognizerResult", this
                        .fillRecognizerResultTelemetryProperties(recognizerResult, telemetryProperties, dialogContext),
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
        IQnAMakerClient qnaClient = dc.getContext().getTurnState().get(IQnAMakerClient.class);
        if (qnaClient != null) {
            // return mock client
            return CompletableFuture.completedFuture(qnaClient);
        }

        String epKey = this.endpointKey;
        String hn = this.hostName;
        String kbId = this.knowledgeBaseId;
        Boolean logPersonalInfo = this.logPersonalInformation;
        QnAMakerEndpoint endpoint = new QnAMakerEndpoint() {
            {
                setEndpointKey(epKey);
                setHost(hn);
                setKnowledgeBaseId(kbId);
            }
        };

        return CompletableFuture.completedFuture(
                new QnAMaker(endpoint, new QnAMakerOptions(), this.getTelemetryClient(), logPersonalInfo));
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
    protected Map<String, String> fillRecognizerResultTelemetryProperties(RecognizerResult recognizerResult,
            Map<String, String> telemetryProperties, @Nullable DialogContext dialogContext) {
        if (dialogContext == null) {
            throw new IllegalArgumentException(
                    "dialogContext: DialogContext needed for state in AdaptiveRecognizer.FillRecognizerResultTelemetryProperties method.");
        }

        Map<String, String> properties = new HashMap<String, String>() {
            {
                put("TopIntent",
                        !recognizerResult.getIntents().isEmpty()
                                ? (String) recognizerResult.getIntents().keySet().toArray()[0]
                                : null);
                put("TopIntentScore",
                        !recognizerResult.getIntents().isEmpty()
                                ? Double.toString(
                                        ((IntentScore) recognizerResult.getIntents().values().toArray()[0]).getScore())
                                : null);
                put("Intents",
                        !recognizerResult.getIntents().isEmpty()
                                ? Serialization.toStringSilent(recognizerResult.getIntents())
                                : null);
                put("Entities",
                        recognizerResult.getEntities() != null
                                ? Serialization.toStringSilent(recognizerResult.getEntities())
                                : null);
                put("AdditionalProperties",
                        !recognizerResult.getProperties().isEmpty()
                                ? Serialization.toStringSilent(recognizerResult.getProperties())
                                : null);
            }
        };

        if (logPersonalInformation && !Strings.isNullOrEmpty(recognizerResult.getText())) {
            properties.put("Text", recognizerResult.getText());
            properties.put("AlteredText", recognizerResult.getAlteredText());
        }

        // Additional Properties can override "stock properties".
        if (telemetryProperties != null) {
            telemetryProperties.putAll(properties);
            Map<String, List<String>> telemetryPropertiesMap = telemetryProperties.entrySet().stream().collect(
                    Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
            return telemetryPropertiesMap.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get(0)));
        }

        return properties;
    }
}
