// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

public  class LuisTelemetryConstants {
    /**
     * The Key used when storing a LUIS Result in a custom event within telemetry.
     */
    public static String LuisResult = "LuisResult";  // Event name

    /**
     * The Key used when storing a LUIS app ID in a custom event within telemetry.
     */
    public static String ApplicationIdProperty = "applicationId";

    /**
     * The Key used when storing a LUIS intent in a custom event within telemetry.
     */
    public static String IntentProperty = "intent";

    /**
     * The Key used when storing a LUIS intent score in a custom event within telemetry.
     */
    public static String IntentScoreProperty = "intentScore";

    /**
     * The Key used when storing a LUIS intent in a custom event within telemetry.
     */
    public static  String Intent2Property = "intent2";

    /**
     * The Key used when storing a LUIS intent score in a custom event within telemetry.
     */
    public static  String IntentScore2Property = "intentScore2";

    /**
     * The Key used when storing LUIS entities in a custom event within telemetry.
     */
    public static  String EntitiesProperty = "entities";

    /**
     * The Key used when storing the LUIS query in a custom event within telemetry.
     */
    public static  String QuestionProperty = "question";

    /**
     * The Key used when storing an Activity ID in a custom event within telemetry.
     */
    public static String ActivityIdProperty = "activityId";

    /**
     * The Key used when storing a sentiment label in a custom event within telemetry.
     */
    public static String SentimentLabelProperty = "sentimentLabel";

    /**
     * The Key used when storing a LUIS sentiment score in a custom event within telemetry.
     */
    public static String SentimentScoreProperty = "sentimentScore";

    /**
     * The Key used when storing the FromId in a custom event within telemetry.
     */
    public static String FromIdProperty = "fromId";
}
