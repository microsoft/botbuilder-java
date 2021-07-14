// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * Specific details of a Teams meeting end event.
 */
public class MeetingEndEventDetails extends MeetingEventDetails {
    @JsonProperty(value = "EndTime")
    private OffsetDateTime endTime;

    /**
     * Gets the meeting's end time, in UTC.
     * @return The meeting's end time, in UTC.
     */
    public OffsetDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the meeting's end time, in UTC.
     * @param withEndTime The meeting's end time, in UTC.
     */
    public void setEndTime(OffsetDateTime withEndTime) {
        endTime = withEndTime;
    }
}
