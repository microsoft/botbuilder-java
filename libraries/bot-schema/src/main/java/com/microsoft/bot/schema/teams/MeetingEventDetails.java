// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specific details of a Teams meeting.
 */
public class MeetingEventDetails extends MeetingDetailsBase {
    @JsonProperty(value = "MeetingType")
    private String meetingType;

    /**
     * Gets the meeting's type.
     * @return The meeting's type.
     */
    public String getMeetingType() {
        return meetingType;
    }

    /**
     * Sets the meeting's type.
     * @param withMeetingType The meeting's type.
     */
    public void setMeetingType(String withMeetingType) {
        meetingType = withMeetingType;
    }
}
