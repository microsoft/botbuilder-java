// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

/**
 * Represents an individual token, such as a word in an input string.
 */
public class Token {
    private String text;
    private int start;
    private int end;
    private String normalized;

    /**
     * Gets the original text of the token.
     *
     * @return The original text of the token.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the original text of the token.
     *
     * @param withText The original text of the token.
     */
    public void setText(String withText) {
        text = withText;
    }

    /**
     * Appends a string to the text value.
     * @param withText The text to append.
     */
    public void appendText(String withText) {
        if (text != null) {
            text += withText;
        } else {
            text = withText;
        }
    }

    /**
     * Gets the index of the first character of the token within the input.
     * @return The index of the first character of the token.
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the index of the first character of the token within the input.
     * @param withStart The index of the first character of the token.
     */
    public void setStart(int withStart) {
        start = withStart;
    }

    /**
     * Gets the index of the last character of the token within the input.
     * @return The index of the last character of the token.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Starts the index of the last character of the token within the input.
     * @param withEnd The index of the last character of the token.
     */
    public void setEnd(int withEnd) {
        end = withEnd;
    }

    /**
     * Gets the normalized version of the token.
     * @return A normalized version of the token.
     */
    public String getNormalized() {
        return normalized;
    }

    /**
     * Sets the normalized version of the token.
     * @param withNormalized A normalized version of the token.
     */
    public void setNormalized(String withNormalized) {
        normalized = withNormalized;
    }
}
