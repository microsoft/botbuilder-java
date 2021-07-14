// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.ConversationAccount;

/**
 * General information about a Teams meeting.
 */
public class MeetingInfo {
    @JsonProperty(value = "details")
    private MeetingDetails details;

    @JsonProperty(value = "conversation")
    private ConversationAccount conversation;

    @JsonProperty(value = "organizer")
    private TeamsChannelAccount organizer;

    /**
     * Initializes a new instance.
     */
    public MeetingInfo() {
    }

    /**
     * Gets the specific details of a Teams meeting.
     * 
     * @return The specific details of a Teams meeting.
     */
    public MeetingDetails getDetails() {
        return details;
    }

    /**
     * Sets the specific details of a Teams meeting.
     * 
     * @param withDetails The specific details of a Teams meeting.
     */
    public void setDetails(MeetingDetails withDetails) {
        details = withDetails;
    }

    /**
     * Gets the Conversation Account for the meeting.
     * 
     * @return The Conversation Account for the meeting.
     */
    public ConversationAccount getConversation() {
        return conversation;
    }

    /**
     * Sets the Conversation Account for the meeting.
     * 
     * @param withConversation The Conversation Account for the meeting.
     */
    public void setConversation(ConversationAccount withConversation) {
        conversation = withConversation;
    }

    /**
     * Gets the meeting organizer's user information.
     * 
     * @return The organizer's user information.
     */
    public TeamsChannelAccount getOrganizer() {
        return organizer;
    }

    /**
     * Sets the meeting organizer's user information.
     * 
     * @param withOrganizer The organizer's user information.
     */
    public void setOrganizer(TeamsChannelAccount withOrganizer) {
        organizer = withOrganizer;
    }
}
