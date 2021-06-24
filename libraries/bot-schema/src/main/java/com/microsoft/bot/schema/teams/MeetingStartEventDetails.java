// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specific details of a Teams meeting start event.
 */
public class MeetingStartEventDetails extends MeetingEventDetails {
    @JsonProperty(value = "StartTime")
    private String startTime;

    /**
     * Gets the meeting's start time, in UTC.
     * @return The meeting's start time, in UTC.
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the meeting's start time, in UTC.
     * @param withStartTime The meeting's start time, in UTC.
     */
    public void setStartTime(String withStartTime) {
        startTime = withStartTime;
    }
}
