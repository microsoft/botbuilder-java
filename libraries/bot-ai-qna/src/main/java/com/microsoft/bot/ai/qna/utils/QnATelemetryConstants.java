// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

/**
 * Default QnA event and property names logged using IBotTelemetryClient.
 */
public final class QnATelemetryConstants {

    private QnATelemetryConstants() {
    }

    /**
     * The Key used for the custom event type within telemetry.
     */
    public static final String QNA_MSG_EVENT = "QnaMessage"; // Event name

    /**
     * The Key used when storing a QnA Knowledge Base ID in a custom event within
     * telemetry.
     */
    public static final String KNOWLEDGE_BASE_ID_PROPERTY = "knowledgeBaseId";

    /**
     * The Key used when storing a QnA Answer in a custom event within telemetry.
     */
    public static final String ANSWER_PROPERTY = "answer";

    /**
     * The Key used when storing a flag indicating if a QnA article was found in a
     * custom event within telemetry.
     */
    public static final String ARTICLE_FOUND_PROPERTY = "articleFound";

    /**
     * The Key used when storing the Channel ID in a custom event within telemetry.
     */
    public static final String CHANNEL_ID_PROPERTY = "channelId";

    /**
     * The Key used when storing a matched question ID in a custom event within
     * telemetry.
     */
    public static final String MATCHED_QUESTION_PROPERTY = "matchedQuestion";

    /**
     * The Key used when storing the identified question text in a custom event
     * within telemetry.
     */
    public static final String QUESTION_PROPERTY = "question";

    /**
     * The Key used when storing the identified question ID in a custom event within
     * telemetry.
     */
    public static final String QUESTION_ID_PROPERTY = "questionId";

    /**
     * The Key used when storing a QnA Maker result score in a custom event within
     * telemetry.
     */
    public static final String SCORE_PROPERTY = "score";

    /**
     * The Key used when storing a username in a custom event within telemetry.
     */
    public static final String USERNAME_PROPERTY = "username";
}
