// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.ConversationAccount;

/**
 * Teams participant channel account detailing user Azure Active Directory and meeting
 * participant details.
 */
public class TeamsMeetingParticipant {
    @JsonProperty(value = "user")
    private TeamsChannelAccount user;

    @JsonProperty(value = "meeting")
    private MeetingParticipantInfo meeting;

    @JsonProperty(value = "conversation")
    private ConversationAccount conversation;

    /**
     * Create TeamsParticipantChannelAccount.
     */
    public TeamsMeetingParticipant() {

    }

    /**
     * Gets the participant's user information.
     * @return  The participant's user information.
     */
    public TeamsChannelAccount getUser() {
        return user;
    }

    /**
     * Sets the participant's user information.
     * @param withUser The participant's user information.
     */
    public void setUser(TeamsChannelAccount withUser) {
        user = withUser;
    }

    /**
     * Gets the participant's meeting information.
     * @return The participant's role in the meeting.
     */
    public MeetingParticipantInfo getMeeting() {
        return meeting;
    }

    /**
     * Sets the participant's meeting information.
     * @param withMeeting The participant's role in the meeting.
     */
    public void setMeeting(MeetingParticipantInfo withMeeting) {
        meeting = withMeeting;
    }

    /**
     * Gets the Conversation Account for the meeting.
     * @return The Conversation Account for the meeting.
     */
    public ConversationAccount getConversation() {
        return conversation;
    }

    /**
     * Sets the Conversation Account for the meeting.
     * @param withConversation The Conversation Account for the meeting.
     */
    public void setConversation(ConversationAccount withConversation) {
        conversation = withConversation;
    }
}
