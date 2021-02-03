// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

/**
 * Contains recognition result information.
 *
 * @param <T> The type of object recognized.
 */
public class ModelResult<T> {
    private String text;
    private int start;
    private int end;
    private String typeName;
    private T resolution;

    /**
     * Gets the substring of the input that was recognized.
     *
     * @return The substring of the input that was recognized.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the substring of the input that was recognized.
     *
     * @param withText The substring of the input that was recognized.
     */
    public void setText(String withText) {
        text = withText;
    }

    /**
     * Gets the start character position of the recognized substring.
     * @return The start character position of the recognized substring.
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the start character position of the recognized substring.
     * @param withStart The start character position of the recognized substring.
     */
    public void setStart(int withStart) {
        start = withStart;
    }

    /**
     * Gets the end character position of the recognized substring.
     * @return The end character position of the recognized substring.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Starts the end character position of the recognized substring.
     * @param withEnd The end character position of the recognized substring.
     */
    public void setEnd(int withEnd) {
        end = withEnd;
    }

    /**
     * Gets the type of entity that was recognized.
     * @return The type of entity that was recognized.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Sets the type of entity that was recognized.
     * @param withTypeName The type of entity that was recognized.
     */
    public void setTypeName(String withTypeName) {
        typeName = withTypeName;
    }

    /**
     * Gets the recognized object.
     * @return The recognized object.
     */
    public T getResolution() {
        return resolution;
    }

    /**
     * Sets the recognized object.
     * @param withResolution The recognized object.
     */
    public void setResolution(T withResolution) {
        resolution = withResolution;
    }
}
