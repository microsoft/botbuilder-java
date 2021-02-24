// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

/**
 * Utility class to set the telemetry values for the Luis Recognizer.
 *
 */
public final class LuisTelemetryConstants {

    private LuisTelemetryConstants() {

    }

    /**
     * The Key used when storing a LUIS Result in a custom event within telemetry.
     */
    public static final String LUIS_RESULT = "LuisResult"; // Event name

    /**
     * The Key used when storing a LUIS app ID in a custom event within telemetry.
     */
    public static final String APPLICATION_ID_PROPERTY = "applicationId";

    /**
     * The Key used when storing a LUIS intent in a custom event within telemetry.
     */
    public static final String INTENT_PROPERTY = "intent";

    /**
     * The Key used when storing a LUIS intent score in a custom event within
     * telemetry.
     */
    public static final String INTENT_SCORE_PROPERTY = "intentScore";

    /**
     * The Key used when storing a LUIS intent in a custom event within telemetry.
     */
    public static final String INTENT_2_PROPERTY = "intent2";

    /**
     * The Key used when storing a LUIS intent score in a custom event within
     * telemetry.
     */
    public static final String INTENT_SCORE_2_PROPERTY = "intentScore2";

    /**
     * The Key used when storing LUIS entities in a custom event within telemetry.
     */
    public static final String ENTITIES_PROPERTY = "entities";

    /**
     * The Key used when storing the LUIS query in a custom event within telemetry.
     */
    public static final String QUESTION_PROPERTY = "question";

    /**
     * The Key used when storing an Activity ID in a custom event within telemetry.
     */
    public static final String ACTIVITY_ID_PROPERTY = "activityId";

    /**
     * The Key used when storing a sentiment label in a custom event within
     * telemetry.
     */
    public static final String SENTIMENT_LABEL_PROPERTY = "sentimentLabel";

    /**
     * The Key used when storing a LUIS sentiment score in a custom event within
     * telemetry.
     */
    public static final String SENTIMENT_SCORE_PROPERTY = "sentimentScore";

    /**
     * The Key used when storing the FromId in a custom event within telemetry.
     */
    public static final String FROM_ID_PROPERTY = "fromId";
}
