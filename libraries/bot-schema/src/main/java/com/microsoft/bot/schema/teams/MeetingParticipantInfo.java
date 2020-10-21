package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Teams meeting participant details.
 */
public class MeetingParticipantInfo {
    @JsonProperty(value = "role")
    private String role;

    @JsonProperty(value = "inMeeting")
    private boolean inMeeting;

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
    public boolean isInMeeting() {
        return inMeeting;
    }

    /**
     * Sets a value indicating whether the participant is in the meeting or not.
     * @param withInMeeting The value indicating if the participant is in the meeting.
     */
    public void setInMeeting(boolean withInMeeting) {
        inMeeting = withInMeeting;
    }
}
