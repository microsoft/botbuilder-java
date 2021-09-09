// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.dialogs;

import com.microsoft.bot.connector.Async;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.ai.qna.QnADialogResponseOptions;
import com.microsoft.bot.ai.qna.QnAMaker;
import com.microsoft.bot.ai.qna.QnAMakerClient;
import com.microsoft.bot.ai.qna.QnAMakerEndpoint;
import com.microsoft.bot.ai.qna.QnAMakerOptions;
import com.microsoft.bot.ai.qna.models.FeedbackRecord;
import com.microsoft.bot.ai.qna.models.FeedbackRecords;
import com.microsoft.bot.ai.qna.models.Metadata;
import com.microsoft.bot.ai.qna.models.QnARequestContext;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.ai.qna.models.QueryResults;
import com.microsoft.bot.ai.qna.models.RankerTypes;
import com.microsoft.bot.ai.qna.utils.ActiveLearningUtils;
import com.microsoft.bot.ai.qna.utils.BindToActivity;
import com.microsoft.bot.ai.qna.utils.QnACardBuilder;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogEvent;
import com.microsoft.bot.dialogs.DialogReason;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.DialogTurnStatus;
import com.microsoft.bot.dialogs.ObjectPath;
import com.microsoft.bot.dialogs.TurnPath;
import com.microsoft.bot.dialogs.WaterfallDialog;
import com.microsoft.bot.dialogs.WaterfallStepContext;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

import okhttp3.OkHttpClient;
import org.slf4j.LoggerFactory;

/**
 * A dialog that supports multi-step and adaptive-learning QnA Maker services.
 * An instance of this class targets a specific QnA Maker knowledge base. It
 * supports knowledge bases that include follow-up prompt and active learning
 * features.
 */
public class QnAMakerDialog extends WaterfallDialog {

    @JsonIgnore
    private OkHttpClient httpClient;

    @JsonProperty("knowledgeBaseId")
    private String knowledgeBaseId;

    @JsonProperty("hostName")
    private String hostName;

    @JsonProperty("endpointKey")
    private String endpointKey;

    @JsonProperty("threshold")
    private Float threshold = DEFAULT_THRESHOLD;

    @JsonProperty("top")
    private Integer top = DEFAULT_TOP_N;

    @JsonProperty("noAnswer")
    private BindToActivity noAnswer = new BindToActivity(MessageFactory.text(DEFAULT_NO_ANSWER));

    @JsonProperty("activeLearningCardTitle")
    private String activeLearningCardTitle;

    @JsonProperty("cardNoMatchText")
    private String cardNoMatchText;

    @JsonProperty("cardNoMatchResponse")
    private BindToActivity cardNoMatchResponse =
        new BindToActivity(MessageFactory.text(DEFAULT_CARD_NO_MATCH_RESPONSE));

    @JsonProperty("strictFilters")
    private Metadata[] strictFilters;

    @JsonProperty("logPersonalInformation")
    private Boolean logPersonalInformation = false;

    @JsonProperty("isTest")
    private Boolean isTest;

    @JsonProperty("rankerType")
    private String rankerType = RankerTypes.DEFAULT_RANKER_TYPE;

    /**
     * The path for storing and retrieving QnA Maker context data. This represents
     * context about the current or previous call to QnA Maker. It is stored within
     * the current step's {@link WaterfallStepContext}. It supports QnA Maker's
     * follow-up prompt and active learning features.
     */
    protected static final String QNA_CONTEXT_DATA = "qnaContextData";

    /**
     * The path for storing and retrieving the previous question ID. This represents
     * the QnA question ID from the previous turn. It is stored within the current
     * step's {@link WaterfallStepContext}. It supports QnA Maker's follow-up prompt
     * and active learning features.
     */
    protected static final String PREVIOUS_QNA_ID = "prevQnAId";

    /**
     * The path for storing and retrieving the options for this instance of the
     * dialog. This includes the options with which the dialog was started and
     * options expected by the QnA Maker service. It is stored within the current
     * step's {@link WaterfallStepContext}. It supports QnA Maker and the dialog
     * system.
     */
    protected static final String OPTIONS = "options";

    // Dialog Options parameters

    /**
     * The default threshold for answers returned, based on score.
     */
    protected static final Float DEFAULT_THRESHOLD = 0.3F;

    /**
     * The default maximum number of answers to be returned for the question.
     */
    protected static final Integer DEFAULT_TOP_N = 3;

    private static final String DEFAULT_NO_ANSWER = "No QnAMaker answers found.";

    // Card parameters
    private static final String DEFAULT_CARD_TITLE = "Did you mean:";
    private static final String DEFAULT_CARD_NO_MATCH_TEXT = "None of the above.";
    private static final String DEFAULT_CARD_NO_MATCH_RESPONSE = "Thanks for the feedback.";
    private static final Integer PERCENTAGE_DIVISOR = 100;

    /**
     * Gets the OkHttpClient instance to use for requests to the QnA Maker service.
     *
     * @return The HTTP client.
     */
    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Gets the OkHttpClient instance to use for requests to the QnA Maker service.
     *
     * @param withHttpClient The HTTP client.
     */
    public void setHttpClient(OkHttpClient withHttpClient) {
        this.httpClient = withHttpClient;
    }

    /**
     * Gets the QnA Maker knowledge base ID to query.
     *
     * @return The knowledge base ID or an expression which evaluates to the
     *         knowledge base ID.
     */
    public String getKnowledgeBaseId() {
        return this.knowledgeBaseId;
    }

    /**
     * Sets the QnA Maker knowledge base ID to query.
     *
     * @param withKnowledgeBaseId The knowledge base ID or an expression which
     *                            evaluates to the knowledge base ID.
     */
    public void setKnowledgeBaseId(String withKnowledgeBaseId) {
        this.knowledgeBaseId = withKnowledgeBaseId;
    }

    /**
     * Gets the QnA Maker host URL for the knowledge base.
     *
     * @return The QnA Maker host URL or an expression which evaluates to the host
     *         URL.
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Sets the QnA Maker host URL for the knowledge base.
     *
     * @param withHostName The QnA Maker host URL or an expression which evaluates
     *                     to the host URL.
     */
    public void setHostName(String withHostName) {
        this.hostName = withHostName;
    }

    /**
     * Gets the QnA Maker endpoint key to use to query the knowledge base.
     *
     * @return The QnA Maker endpoint key to use or an expression which evaluates to
     *         the endpoint key.
     */
    public String getEndpointKey() {
        return this.endpointKey;
    }

    /**
     * Sets the QnA Maker endpoint key to use to query the knowledge base.
     *
     * @param withEndpointKey The QnA Maker endpoint key to use or an expression
     *                        which evaluates to the endpoint key.
     */
    public void setEndpointKey(String withEndpointKey) {
        this.endpointKey = withEndpointKey;
    }

    /**
     * Gets the threshold for answers returned, based on score.
     *
     * @return The threshold for answers returned or an expression which evaluates
     *         to the threshold.
     */
    public Float getThreshold() {
        return this.threshold;
    }

    /**
     * Sets the threshold for answers returned, based on score.
     *
     * @param withThreshold The threshold for answers returned or an expression
     *                      which evaluates to the threshold.
     */
    public void setThreshold(Float withThreshold) {
        this.threshold = withThreshold;
    }

    /**
     * Gets the maximum number of answers to return from the knowledge base.
     *
     * @return The maximum number of answers to return from the knowledge base or an
     *         expression which evaluates to the maximum number to return.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Sets the maximum number of answers to return from the knowledge base.
     *
     * @param withTop The maximum number of answers to return from the knowledge
     *                base or an expression which evaluates to the maximum number to
     *                return.
     */
    public void setTop(Integer withTop) {
        this.top = withTop;
    }

    /**
     * Gets the template to send the user when QnA Maker does not find an answer.
     *
     * @return The template to send the user when QnA Maker does not find an answer.
     */
    public BindToActivity getNoAnswer() {
        return this.noAnswer;
    }

    /**
     * Sets the template to send the user when QnA Maker does not find an answer.
     *
     * @param withNoAnswer The template to send the user when QnA Maker does not
     *                     find an answer.
     */
    public void setNoAnswer(BindToActivity withNoAnswer) {
        this.noAnswer = withNoAnswer;
    }

    /**
     * Gets the card title to use when showing active learning options to the user,
     * if active learning is enabled.
     *
     * @return The path card title to use when showing active learning options to
     *         the user or an expression which evaluates to the card title.
     */
    public String getActiveLearningCardTitle() {
        return this.activeLearningCardTitle;
    }

    /**
     * Sets the card title to use when showing active learning options to the user,
     * if active learning is enabled.
     *
     * @param withActiveLearningCardTitle The path card title to use when showing
     *                                    active learning options to the user or an
     *                                    expression which evaluates to the card
     *                                    title.
     */
    public void setActiveLearningCardTitle(String withActiveLearningCardTitle) {
        this.activeLearningCardTitle = withActiveLearningCardTitle;
    }

    /**
     * Gets the button text to use with active learning options, allowing a user to
     * indicate none of the options are applicable.
     *
     * @return The button text to use with active learning options or an expression
     *         which evaluates to the button text.
     */
    public String getCardNoMatchText() {
        return this.cardNoMatchText;
    }

    /**
     * Sets the button text to use with active learning options, allowing a user to
     * indicate none of the options are applicable.
     *
     * @param withCardNoMatchText The button text to use with active learning
     *                            options or an expression which evaluates to the
     *                            button text.
     */
    public void setCardNoMatchText(String withCardNoMatchText) {
        this.cardNoMatchText = withCardNoMatchText;
    }

    /**
     * Gets the template to send the user if they select the no match option on an
     * active learning card.
     *
     * @return The template to send the user if they select the no match option on
     *         an active learning card.
     */
    public BindToActivity getCardNoMatchResponse() {
        return this.cardNoMatchResponse;
    }

    /**
     * Sets the template to send the user if they select the no match option on an
     * active learning card.
     *
     * @param withCardNoMatchResponse The template to send the user if they select
     *                                the no match option on an active learning
     *                                card.
     */
    public void setCardNoMatchResponse(BindToActivity withCardNoMatchResponse) {
        this.cardNoMatchResponse = withCardNoMatchResponse;
    }

    /**
     * Gets the QnA Maker metadata with which to filter or boost queries to the
     * knowledge base; or null to apply none.
     *
     * @return The QnA Maker metadata with which to filter or boost queries to the
     *         knowledge base or an expression which evaluates to the QnA Maker
     *         metadata.
     */
    public Metadata[] getStrictFilters() {
        return this.strictFilters;
    }

    /**
     * Sets the QnA Maker metadata with which to filter or boost queries to the
     * knowledge base; or null to apply none.
     *
     * @param withStrictFilters The QnA Maker metadata with which to filter or boost
     *                          queries to the knowledge base or an expression which
     *                          evaluates to the QnA Maker metadata.
     */
    public void setStrictFilters(Metadata[] withStrictFilters) {
        this.strictFilters = withStrictFilters;
    }

    /**
     * Gets the flag to determine if personal information should be logged in
     * telemetry.
     *
     * @return The flag to indicate in personal information should be logged in
     *         telemetry.
     */
    public Boolean getLogPersonalInformation() {
        return this.logPersonalInformation;
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
     * Gets a value indicating whether gets or sets environment of knowledgebase to
     * be called.
     *
     * @return A value indicating whether to call test or prod environment of
     *         knowledge base.
     */
    public Boolean getIsTest() {
        return this.isTest;
    }

    /**
     * Sets a value indicating whether gets or sets environment of knowledgebase to
     * be called.
     *
     * @param withIsTest A value indicating whether to call test or prod environment
     *                   of knowledge base.
     */
    public void setIsTest(Boolean withIsTest) {
        this.isTest = withIsTest;
    }

    /**
     * Gets the QnA Maker ranker type to use.
     *
     * @return The QnA Maker ranker type to use or an expression which evaluates to
     *         the ranker type.
     */
    public String getRankerType() {
        return this.rankerType;
    }

    /**
     * Sets the QnA Maker ranker type to use.
     *
     * @param withRankerType The QnA Maker ranker type to use or an expression which
     *                       evaluates to the ranker type.
     */
    public void setRankerType(String withRankerType) {
        this.rankerType = withRankerType;
    }

    /**
     * Initializes a new instance of the @{link QnAMakerDialog} class.
     */
    public QnAMakerDialog() {
        super("QnAMakerDialog", null);

        // add waterfall steps
        this.addStep(this::callGenerateAnswer);
        this.addStep(this::callTrain);
        this.addStep(this::checkForMultiTurnPrompt);
        this.addStep(this::displayQnAResult);
    }

    /**
     * Initializes a new instance of the @{link QnAMakerDialog} class.
     *
     * @param dialogId                    The ID of the @{link Dialog}.
     * @param withKnowledgeBaseId         The ID of the QnA Maker knowledge base to
     *                                    query.
     * @param withEndpointKey             The QnA Maker endpoint key to use to query
     *                                    the knowledge base.
     * @param withHostName                The QnA Maker host URL for the knowledge
     *                                    base, starting with "https://" and ending
     *                                    with "/qnamaker".
     * @param withNoAnswer                The activity to send the user when QnA
     *                                    Maker does not find an answer.
     * @param withThreshold               The threshold for answers returned, based
     *                                    on score.
     * @param withActiveLearningCardTitle The card title to use when showing active
     *                                    learning options to the user, if active
     *                                    learning is enabled.
     * @param withCardNoMatchText         The button text to use with active
     *                                    learning options, allowing a user to
     *                                    indicate none of the options are
     *                                    applicable.
     * @param withTop                     The maximum number of answers to return
     *                                    from the knowledge base.
     * @param withCardNoMatchResponse     The activity to send the user if they
     *                                    select the no match option on an active
     *                                    learning card.
     * @param withStrictFilters           QnA Maker metadata with which to filter or
     *                                    boost queries to the knowledge base; or
     *                                    null to apply none.
     * @param withHttpClient              An HTTP client to use for requests to the
     *                                    QnA Maker Service; or `null` to use a
     *                                    default client.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public QnAMakerDialog(
        String dialogId,
        String withKnowledgeBaseId,
        String withEndpointKey,
        String withHostName,
        @Nullable Activity withNoAnswer,
        Float withThreshold,
        String withActiveLearningCardTitle,
        String withCardNoMatchText,
        Integer withTop,
        @Nullable Activity withCardNoMatchResponse,
        @Nullable Metadata[] withStrictFilters,
        @Nullable OkHttpClient withHttpClient
    ) {
        super(dialogId, null);
        if (knowledgeBaseId == null) {
            throw new IllegalArgumentException("knowledgeBaseId");
        }
        this.knowledgeBaseId = withKnowledgeBaseId;
        if (hostName == null) {
            throw new IllegalArgumentException("hostName");
        }
        this.hostName = withHostName;
        if (withEndpointKey == null) {
            throw new IllegalArgumentException("endpointKey");
        }
        this.endpointKey = withEndpointKey;
        this.threshold = withThreshold != null ? withThreshold : DEFAULT_THRESHOLD;
        this.top = withTop != null ? withTop : DEFAULT_TOP_N;
        this.activeLearningCardTitle =
            withActiveLearningCardTitle != null ? withActiveLearningCardTitle : DEFAULT_CARD_TITLE;
        this.cardNoMatchText = withCardNoMatchText != null ? withCardNoMatchText : DEFAULT_CARD_NO_MATCH_TEXT;
        this.strictFilters = withStrictFilters;
        this.noAnswer =
            new BindToActivity(withNoAnswer != null ? withNoAnswer : MessageFactory.text(DEFAULT_NO_ANSWER));
        this.cardNoMatchResponse = new BindToActivity(
            withCardNoMatchResponse != null
                ? withCardNoMatchResponse
                : MessageFactory.text(DEFAULT_CARD_NO_MATCH_RESPONSE)
        );
        this.httpClient = withHttpClient;

        // add waterfall steps
        this.addStep(this::callGenerateAnswer);
        this.addStep(this::callTrain);
        this.addStep(this::checkForMultiTurnPrompt);
        this.addStep(this::displayQnAResult);
    }

    /**
     * Initializes a new instance of the {@link QnAMakerDialog} class.
     *
     * @param withKnowledgeBaseId         The ID of the QnA Maker knowledge base to
     *                                    query.
     * @param withEndpointKey             The QnA Maker endpoint key to use to query
     *                                    the knowledge base.
     * @param withHostName                The QnA Maker host URL for the knowledge
     *                                    base, starting with "https://" and ending
     *                                    with "/qnamaker".
     * @param withNoAnswer                The activity to send the user when QnA
     *                                    Maker does not find an answer.
     * @param withThreshold               The threshold for answers returned, based
     *                                    on score.
     * @param withActiveLearningCardTitle The card title to use when showing active
     *                                    learning options to the user, if active
     *                                    learning is enabled.
     * @param withCardNoMatchText         The button text to use with active
     *                                    learning options, allowing a user to
     *                                    indicate none of the options are
     *                                    applicable.
     * @param withTop                     The maximum number of answers to return
     *                                    from the knowledge base.
     * @param withCardNoMatchResponse     The activity to send the user if they
     *                                    select the no match option on an active
     *                                    learning card.
     * @param withStrictFilters           QnA Maker metadata with which to filter or
     *                                    boost queries to the knowledge base; or
     *                                    null to apply none.
     * @param withHttpClient              An HTTP client to use for requests to the
     *                                    QnA Maker Service; or `null` to use a
     *                                    default client.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public QnAMakerDialog(
        String withKnowledgeBaseId,
        String withEndpointKey,
        String withHostName,
        @Nullable Activity withNoAnswer,
        Float withThreshold,
        String withActiveLearningCardTitle,
        String withCardNoMatchText,
        Integer withTop,
        @Nullable Activity withCardNoMatchResponse,
        @Nullable Metadata[] withStrictFilters,
        @Nullable OkHttpClient withHttpClient
    ) {
        this(
            "QnAMakerDialog",
            withKnowledgeBaseId,
            withEndpointKey,
            withHostName,
            withNoAnswer,
            withThreshold,
            withActiveLearningCardTitle,
            withCardNoMatchText,
            withTop,
            withCardNoMatchResponse,
            withStrictFilters,
            withHttpClient
        );
    }

    /**
     * Called when the dialog is started and pushed onto the dialog stack.
     *
     * @param dc      The @{link DialogContext} for the current turn of
     *                conversation.
     * @param options Optional, initial information to pass to the dialog.
     * @return A Task representing the asynchronous operation. If the task is
     *         successful, the result indicates whether the dialog is still active
     *         after the turn has been processed by the dialog.
     *
     *         You can use the @{link options} parameter to include the QnA Maker
     *         context data, which represents context from the previous query. To do
     *         so, the value should include a `context` property of type @{link
     *         QnaResponseContext}.
     */
    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, @Nullable Object options) {
        if (dc == null) {
            return Async.completeExceptionally(new IllegalArgumentException("dc"));
        }

        if (!dc.getContext().getActivity().isType(ActivityTypes.MESSAGE)) {
            return CompletableFuture.completedFuture(END_OF_TURN);
        }

        QnAMakerOptions qnAMakerOptions = this.getQnAMakerOptions(dc).join();
        QnADialogResponseOptions qnADialogResponseOptions = this.getQnAResponseOptions(dc).join();

        QnAMakerDialogOptions dialogOptions = new QnAMakerDialogOptions();
        dialogOptions.setQnAMakerOptions(qnAMakerOptions);
        dialogOptions.setResponseOptions(qnADialogResponseOptions);

        if (options != null) {
            dialogOptions = ObjectPath.assign(dialogOptions, options);
        }

        ObjectPath.setPathValue(dc.getActiveDialog().getState(), OPTIONS, dialogOptions);

        return super.beginDialog(dc, dialogOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
        Boolean interrupted = dc.getState().getValue(TurnPath.INTERRUPTED, false, Boolean.class);
        if (interrupted) {
            // if qnamaker was interrupted then end the qnamaker dialog
            return dc.endDialog();
        }

        return super.continueDialog(dc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompletableFuture<Boolean> onPreBubbleEvent(DialogContext dc, DialogEvent e) {
        if (dc.getContext().getActivity().isType(ActivityTypes.MESSAGE)) {
            // decide whether we want to allow interruption or not.
            // if we don't get a response from QnA which signifies we expected it,
            // then we allow interruption.

            String reply = dc.getContext().getActivity().getText();
            QnAMakerDialogOptions dialogOptions = (QnAMakerDialogOptions) ObjectPath
                .getPathValue(dc.getActiveDialog().getState(), OPTIONS, QnAMakerDialogOptions.class);

            if (reply.equalsIgnoreCase(dialogOptions.getResponseOptions().getCardNoMatchText())) {
                // it matches nomatch text, we like that.
                return CompletableFuture.completedFuture(true);
            }

            List<String> suggestedQuestions = (List<String>) dc.getState().get("this.suggestedQuestions");
            if (
                suggestedQuestions != null
                    && suggestedQuestions.stream().anyMatch(question -> question.compareToIgnoreCase(reply.trim()) == 0)
            ) {
                // it matches one of the suggested actions, we like that.
                return CompletableFuture.completedFuture(true);
            }

            // Calling QnAMaker to get response.
            return this.getQnAMakerClient(dc).thenCompose(qnaClient -> {
                QnAMakerDialog.resetOptions(dc, dialogOptions);

                return qnaClient.getAnswersRaw(dc.getContext(), dialogOptions.getQnAMakerOptions(), null, null)
                    .thenApply(
                        response -> {
                            // cache result so step doesn't have to do it again, this is a turn cache and we
                            // use hashcode so we don't conflict with any other qnamakerdialogs out there.
                            dc.getState().setValue(String.format("turn.qnaresult%s", this.hashCode()), response);

                            // disable interruption if we have answers.
                            return !(response.getAnswers().length == 0);
                        }
                    );
            });
        }
        // call base for default behavior.
        return this.onPostBubbleEvent(dc, e);
    }

    /**
     * Gets an {@link QnAMakerClient} to use to access the QnA Maker knowledge base.
     *
     * @param dc The {@link DialogContext} for the current turn of conversation.
     * @return A Task representing the asynchronous operation. If the task is
     *         successful, the result contains the QnA Maker client to use.
     */
    protected CompletableFuture<QnAMakerClient> getQnAMakerClient(DialogContext dc) {
        QnAMakerClient qnaClient = (QnAMakerClient) dc.getContext().getTurnState();
        if (qnaClient != null) {
            // return mock client
            return CompletableFuture.completedFuture(qnaClient);
        }

        QnAMakerEndpoint endpoint = new QnAMakerEndpoint();
        endpoint.setEndpointKey(endpointKey);
        endpoint.setHost(hostName);
        endpoint.setKnowledgeBaseId(knowledgeBaseId);

        return this.getQnAMakerOptions(
            dc
        ).thenApply(options -> new QnAMaker(endpoint, options, this.getTelemetryClient(), this.logPersonalInformation));
    }

    /**
     * Gets the options for the QnA Maker client that the dialog will use to query
     * the knowledge base.
     *
     * @param dc The {@link "DialogContext"} for the current turn of
     *           conversation.
     * @return A {@link "Task"} representing the asynchronous operation. If the
     *         task is successful, the result contains the QnA Maker options to use.
     */
    protected CompletableFuture<QnAMakerOptions> getQnAMakerOptions(DialogContext dc) {
        QnAMakerOptions options = new QnAMakerOptions();
        options.setScoreThreshold(threshold);
        options.setStrictFilters(strictFilters);
        options.setTop(top);
        options.setContext(new QnARequestContext());
        options.setQnAId(0);
        options.setRankerType(rankerType);
        options.setIsTest(isTest);
        return CompletableFuture.completedFuture(options);
    }

    /**
     * Gets the options the dialog will use to display query results to the user.
     *
     * @param dc The {@link DialogContext} for the current turn of conversation.
     * @return A Task representing the asynchronous operation. If the task is
     *         successful, the result contains the response options to use.
     */
    protected CompletableFuture<QnADialogResponseOptions> getQnAResponseOptions(DialogContext dc) {
        QnADialogResponseOptions options = new QnADialogResponseOptions();
        options.setNoAnswer(noAnswer.bind(dc, dc.getState()).join());
        options.setActiveLearningCardTitle(
            activeLearningCardTitle != null ? activeLearningCardTitle : DEFAULT_CARD_TITLE
        );
        options.setCardNoMatchText(cardNoMatchText != null ? cardNoMatchText : DEFAULT_CARD_NO_MATCH_TEXT);
        options.setCardNoMatchResponse(cardNoMatchResponse.bind(dc, null).join());

        return CompletableFuture.completedFuture(options);
    }

    /**
     * Displays QnA Result from stepContext through Activity - with first answer
     * from QnA Maker response.
     *
     * @param stepContext stepContext.
     * @return An object of Task of type {@link DialogTurnResult}.
     */
    protected CompletableFuture<DialogTurnResult> displayQnAResult(WaterfallStepContext stepContext) {
        QnAMakerDialogOptions dialogOptions =
            ObjectPath.getPathValue(stepContext.getActiveDialog().getState(), OPTIONS, QnAMakerDialogOptions.class);
        String reply = stepContext.getContext().getActivity().getText();
        if (reply.compareToIgnoreCase(dialogOptions.getResponseOptions().getCardNoMatchText()) == 0) {
            Activity activity = dialogOptions.getResponseOptions().getCardNoMatchResponse();
            if (activity == null) {
                stepContext.getContext().sendActivity(DEFAULT_CARD_NO_MATCH_RESPONSE).join();
            } else {
                stepContext.getContext().sendActivity(activity).join();
            }

            return stepContext.endDialog();
        }

        // If previous QnAId is present, replace the dialog
        Integer previousQnAId =
            ObjectPath.getPathValue(stepContext.getActiveDialog().getState(), PREVIOUS_QNA_ID, Integer.class, 0);
        if (previousQnAId > 0) {
            // restart the waterfall to step 0
            return this.runStep(stepContext, 0, DialogReason.BEGIN_CALLED, null);
        }

        // If response is present then show that response, else default answer.
        List<QueryResult> response = (List<QueryResult>) stepContext.getResult();
        if (response != null && response.size() > 0) {
            stepContext.getContext().sendActivity(response.get(0).getAnswer()).join();
        } else {
            Activity activity = dialogOptions.getResponseOptions().getNoAnswer();
            if (activity == null) {
                stepContext.getContext().sendActivity(DEFAULT_NO_ANSWER).join();
            } else {
                stepContext.getContext().sendActivity(activity).join();
            }
        }

        return stepContext.endDialog();
    }

    private static void resetOptions(DialogContext dc, QnAMakerDialogOptions dialogOptions) {
        // Resetting context and QnAId
        dialogOptions.getQnAMakerOptions().setQnAId(0);
        dialogOptions.getQnAMakerOptions().setContext(new QnARequestContext());

        // -Check if previous context is present, if yes then put it with the query
        // -Check for id if query is present in reverse index.
        Map<String, Integer> previousContextData =
            ObjectPath.getPathValue(
                dc.getActiveDialog().getState(),
                QNA_CONTEXT_DATA,
                Map.class,
                new HashMap<String, Integer>());
        Integer previousQnAId =
            ObjectPath.getPathValue(dc.getActiveDialog().getState(), PREVIOUS_QNA_ID, Integer.class, 0);

        if (previousQnAId > 0) {
            QnARequestContext context = new QnARequestContext();
            context.setPreviousQnAId(previousQnAId);
            dialogOptions.getQnAMakerOptions().setContext(context);

            Integer currentQnAId = previousContextData.get(dc.getContext().getActivity().getText());
            if (currentQnAId != null) {
                dialogOptions.getQnAMakerOptions().setQnAId(currentQnAId);
            }
        }
    }

    private CompletableFuture<DialogTurnResult> callGenerateAnswer(WaterfallStepContext stepContext) {
        // clear suggestedQuestions between turns.
        stepContext.getState().removeValue("this.suggestedQuestions");

        QnAMakerDialogOptions dialogOptions =
            ObjectPath.getPathValue(stepContext.getActiveDialog().getState(), OPTIONS, QnAMakerDialogOptions.class);
        QnAMakerDialog.resetOptions(stepContext, dialogOptions);

        // Storing the context info
        stepContext.getValues().put(ValueProperty.CURRENT_QUERY, stepContext.getContext().getActivity().getText());

        // Calling QnAMaker to get response.
        return this.getQnAMakerClient(stepContext).thenApply(qnaMakerClient -> {
            QueryResults response = qnaMakerClient
                    .getAnswersRaw(stepContext.getContext(), dialogOptions.getQnAMakerOptions(), null, null)
                    .join();

            // Resetting previous query.
            Integer previousQnAId = -1;
            ObjectPath.setPathValue(stepContext.getActiveDialog().getState(), PREVIOUS_QNA_ID, previousQnAId);

            // Take this value from GetAnswerResponse
            Boolean isActiveLearningEnabled = response.getActiveLearningEnabled();

            stepContext.getValues().put(ValueProperty.QNA_DATA, Arrays.asList(response.getAnswers()));

            // Check if active learning is enabled.
            // MaximumScoreForLowScoreVariation is the score above which no need to check
            // for feedback.
            if (
                response.getAnswers().length > 0 && response.getAnswers()[0]
                    .getScore() <= (ActiveLearningUtils.getMaximumScoreForLowScoreVariation() / PERCENTAGE_DIVISOR)
            ) {
                // Get filtered list of the response that support low score variation criteria.
                response.setAnswers(qnaMakerClient.getLowScoreVariation(response.getAnswers()));

                if (response.getAnswers().length > 1 && isActiveLearningEnabled) {
                    List<String> suggestedQuestions = new ArrayList<String>();
                    for (QueryResult qna : response.getAnswers()) {
                        suggestedQuestions.add(qna.getQuestions()[0]);
                    }

                    // Get active learning suggestion card activity.
                    Activity message = QnACardBuilder.getSuggestionsCard(
                        suggestedQuestions,
                        dialogOptions.getResponseOptions().getActiveLearningCardTitle(),
                        dialogOptions.getResponseOptions().getCardNoMatchText()
                    );
                    stepContext.getContext().sendActivity(message).join();

                    ObjectPath.setPathValue(stepContext.getActiveDialog().getState(), OPTIONS, dialogOptions);
                    stepContext.getState().setValue("this.suggestedQuestions", suggestedQuestions);
                    return new DialogTurnResult(DialogTurnStatus.WAITING);
                }
            }

            List<QueryResult> result = new ArrayList<QueryResult>();
            if (!(response.getAnswers().length == 0)) {
                result.add(response.getAnswers()[0]);
            }

            stepContext.getValues().put(ValueProperty.QNA_DATA, result);
            ObjectPath.setPathValue(stepContext.getActiveDialog().getState(), OPTIONS, dialogOptions);

            // If card is not shown, move to next step with top QnA response.
            return stepContext.next(result).join();
        });
    }

    private CompletableFuture<DialogTurnResult> callTrain(WaterfallStepContext stepContext) {
        QnAMakerDialogOptions dialogOptions =
            ObjectPath.getPathValue(stepContext.getActiveDialog().getState(), OPTIONS, QnAMakerDialogOptions.class);
        List<QueryResult> trainResponses = (List<QueryResult>) stepContext.getValues().get(ValueProperty.QNA_DATA);
        String currentQuery = (String) stepContext.getValues().get(ValueProperty.CURRENT_QUERY);

        String reply = stepContext.getContext().getActivity().getText();

        if (trainResponses.size() > 1) {
            QueryResult qnaResult =
                trainResponses.stream().filter(kvp -> kvp.getQuestions()[0].equals(reply)).findFirst().orElse(null);
            if (qnaResult != null) {
                List<QueryResult> queryResultArr = new ArrayList<QueryResult>();
                stepContext.getValues().put(ValueProperty.QNA_DATA, queryResultArr.add(qnaResult));
                FeedbackRecord record = new FeedbackRecord();
                record.setUserId(stepContext.getContext().getActivity().getId());
                record.setUserQuestion(currentQuery);
                record.setQnaId(qnaResult.getId());
                FeedbackRecord[] records = {record};
                FeedbackRecords feedbackRecords = new FeedbackRecords();
                feedbackRecords.setRecords(records);
                // Call Active Learning Train API
                return this.getQnAMakerClient(stepContext).thenCompose(qnaClient -> {
                    try {
                        return qnaClient.callTrain(feedbackRecords)
                            .thenCompose(task -> stepContext.next(new ArrayList<QueryResult>().add(qnaResult)));
                    } catch (IOException e) {
                        LoggerFactory.getLogger(QnAMakerDialog.class).error("callTrain");
                    }
                    return CompletableFuture.completedFuture(null);
                });
            } else if (reply.compareToIgnoreCase(dialogOptions.getResponseOptions().getCardNoMatchText()) == 0) {
                Activity activity = dialogOptions.getResponseOptions().getCardNoMatchResponse();
                if (activity == null) {
                    stepContext.getContext().sendActivity(DEFAULT_CARD_NO_MATCH_RESPONSE).join();
                } else {
                    stepContext.getContext().sendActivity(activity).join();
                }

                return stepContext.endDialog();
            } else {
                // restart the waterfall to step 0
                return runStep(stepContext, 0, DialogReason.BEGIN_CALLED, null);
            }
        }

        return stepContext.next(stepContext.getResult());
    }

    private CompletableFuture<DialogTurnResult> checkForMultiTurnPrompt(WaterfallStepContext stepContext) {
        QnAMakerDialogOptions dialogOptions =
            ObjectPath.getPathValue(stepContext.getActiveDialog().getState(), OPTIONS, QnAMakerDialogOptions.class);
        List<QueryResult> response = (List<QueryResult>) stepContext.getResult();
        if (response != null && response.size() > 0) {
            // -Check if context is present and prompt exists
            // -If yes: Add reverse index of prompt display name and its corresponding QnA
            // ID
            // -Set PreviousQnAId as answer.Id
            // -Display card for the prompt
            // -Wait for the reply
            // -If no: Skip to next step

            QueryResult answer = response.get(0);

            if (answer.getContext() != null && answer.getContext().getPrompts().length > 0) {
                Map<String, Integer> previousContextData =
                    ObjectPath.getPathValue(
                        stepContext.getActiveDialog().getState(),
                        QNA_CONTEXT_DATA,
                        Map.class,
                        new HashMap<>());
                for (QnAMakerPrompt prompt : answer.getContext().getPrompts()) {
                    previousContextData.put(prompt.getDisplayText(), prompt.getQnaId());
                }

                ObjectPath
                    .setPathValue(stepContext.getActiveDialog().getState(), QNA_CONTEXT_DATA, previousContextData);
                ObjectPath.setPathValue(stepContext.getActiveDialog().getState(), PREVIOUS_QNA_ID, answer.getId());
                ObjectPath.setPathValue(stepContext.getActiveDialog().getState(), OPTIONS, dialogOptions);

                // Get multi-turn prompts card activity.
                Activity message =
                    QnACardBuilder.getQnAPromptsCard(answer, dialogOptions.getResponseOptions().getCardNoMatchText());
                stepContext.getContext().sendActivity(message).join();

                return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.WAITING));
            }
        }

        return stepContext.next(stepContext.getResult());
    }

    /**
     * Helper class.
     */
    final class ValueProperty {
        public static final String CURRENT_QUERY = "currentQuery";
        public static final String QNA_DATA = "qnaData";

        private ValueProperty() {
        }
    }
}
