// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

/**
 * A date-time value, as recognized by the {@link DateTimePrompt} .
 *
 * A value can represent a date, a time, a date and time, or a range of any of
 * these. The representation of the value is determined by the locale used to
 * parse the input.
 */

public class DateTimeResolution {

    private String value;
    private String start;
    private String end;
    private String timex;

    /**
     * Gets a human-readable representation of the value, for a non-range result.
     *
     * @return A human-readable representation of the value, for a non-range result.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets a human-readable representation of the value, for a non-range result.
     *
     * @param value A human-readable representation of the value, for a non-range
     *              result.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets a human-readable representation of the start value, for a range result.
     *
     * @return A human-readable representation of the start value, for a range
     *         result.
     */
    public String getStart() {
        return this.start;
    }

    /**
     * Sets a human-readable representation of the start value, for a range result.
     *
     * @param start A human-readable representation of the start value, for a range
     *              result.
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * Gets a human-readable represntation of the end value, for a range result.
     *
     * @return A human-readable representation of the end value, for a range result.
     */
    public String getEnd() {
        return this.end;
    }

    /**
     * Sets a human-readable represntation of the end value, for a range result.
     *
     * @param end A human-readable representation of the end value, for a range
     *            result.
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * Gets the value in TIMEX format. The TIMEX format that follows the ISO 8601
     * standard.
     *
     * @return A TIMEX representation of the value.
     */
    public String getTimex() {
        return this.timex;
    }

    /**
     * Sets the value in TIMEX format. The TIMEX format that follows the ISO 8601
     * standard.
     *
     * @param timex A TIMEX representation of the value.
     */
    public void setTimex(String timex) {
        this.timex = timex;
    }

}
