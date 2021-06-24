// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specific details of a Teams meeting end event.
 */
public class MeetingEndEventDetails extends MeetingDetails {
    @JsonProperty(value = "EndTime")
    private String endTime;

    /**
     * Gets the meeting's end time, in UTC.
     * @return The meeting's end time, in UTC.
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Sets the meeting's end time, in UTC.
     * @param withEndTime The meeting's end time, in UTC.
     */
    public void setEndTime(String withEndTime) {
        endTime = withEndTime;
    }
}
