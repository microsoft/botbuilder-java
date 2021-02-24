// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is internal and should not be used.
 * Please use FoundChoice instead.
 */
class FoundValue {
    @JsonProperty(value = "value")
    private String value;

    @JsonProperty(value = "index")
    private int index;

    @JsonProperty(value = "score")
    private float score;

    /**
     * Gets the value that was matched.
     * @return The value that was matched.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value that was matched.
     * @param withValue The value that was matched.
     */
    public void setValue(String withValue) {
        value = withValue;
    }

    /**
     * Gets the index of the value that was matched.
     * @return The index of the value that was matched.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of the value that was matched.
     * @param withIndex The index of the value that was matched.
     */
    public void setIndex(int withIndex) {
        index = withIndex;
    }

    /**
     * Gets the accuracy with which the value matched the specified portion of the utterance. A
     * value of 1.0 would indicate a perfect match.
     * @return The accuracy with which the value matched the specified portion of the utterance.
     * A value of 1.0 would indicate a perfect match.
     */
    public float getScore() {
        return score;
    }

    /**
     * Sets the accuracy with which the value matched the specified portion of the utterance. A
     * value of 1.0 would indicate a perfect match.
     * @param withScore The accuracy with which the value matched the specified portion of the
     *                  utterance. A value of 1.0 would indicate a perfect match.
     */
    public void setScore(float withScore) {
        score = withScore;
    }
}
