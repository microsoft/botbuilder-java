// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Teams meeting participant details.
 */
public class MeetingParticipantInfo {
    @JsonProperty(value = "role")
    private String role;

    @JsonProperty(value = "inMeeting")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Boolean inMeeting;

    /**
     * Gets the participant's role in the meeting.
     * @return The participant's role in the meeting.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the participant's role in the meeting.
     * @param withRole The participant's role in the meeting.
     */
    public void setRole(String withRole) {
        role = withRole;
    }

    /**
     * Gets a value indicating whether the participant is in the meeting or not.
     * @return The value indicating if the participant is in the meeting.
     */
    public Boolean isInMeeting() {
        return inMeeting;
    }

    /**
     * Sets a value indicating whether the participant is in the meeting or not.
     * @param withInMeeting The value indicating if the participant is in the meeting.
     */
    public void setInMeeting(Boolean withInMeeting) {
        inMeeting = withInMeeting;
    }
}
