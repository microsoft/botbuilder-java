// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Defines path for avaiable turns.
 */
public final class TurnPath {
    private TurnPath() { }

    /// The result from the last dialog that was called.
    public static final String LAST_RESULT = "turn.lastresult";

    /// The current activity for the turn.
    public static final String ACTIVITY = "turn.activity";

    /// The recognized result for the current turn.
    public static final String RECOGNIZED = "turn.recognized";

    /// Path to the top intent.
    public static final String TOP_INTENT = "turn.recognized.intent";

    /// Path to the top score.
    public static final String TOP_SCORE = "turn.recognized.score";

    /// Original text.
    public static final String TEXT = "turn.recognized.text";

    /// Original utterance split into unrecognized strings.
    public static final String UNRECOGNIZED_TEXT = "turn.unrecognizedText";

    /// Entities that were recognized from text.
    public static final String RECOGNIZED_ENTITIES = "turn.recognizedEntities";

    /// If true an interruption has occured.
    public static final String INTERRUPTED = "turn.interrupted";

    /// The current dialog event (set during event processing).
    public static final String DIALOG_EVENT = "turn.dialogEvent";

    /// Used to track that we don't end up in infinite loop of RepeatDialogs().
    public static final String REPEATED_IDS = "turn.repeatedIds";

    /// This is a bool which if set means that the turncontext.activity has been consumed by
    // some component in the system.
    public static final String ACTIVITY_PROCESSED = "turn.activityProcessed";

    /**
     * Utility function to get just the property name without the memory scope prefix.
     * @param property memory scope property path.
     * @return name of the property without the prefix.
     */
    public static String getPropertyName(String property) {
        return property.replace("turn.", "");
    }
}
