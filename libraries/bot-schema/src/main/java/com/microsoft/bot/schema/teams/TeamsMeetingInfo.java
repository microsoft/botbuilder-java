// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a Teams Meeting.
 */
public class TeamsMeetingInfo {
    @JsonProperty(value = "id")
    private String id;

    /**
     * New Teams meeting.
     */
    public TeamsMeetingInfo() {

    }

    /**
     * New Teams meeting with ID.
     * @param withId Unique identifier representing a teams meeting.
     */
    public TeamsMeetingInfo(String withId) {
        id = withId;
    }

    /**
     * Gets the unique identifier representing a meeting.
     * @return The meeting id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier representing a meeting.
     * @param withId The meeting id
     */
    public void setId(String withId) {
        id = withId;
    }
}
