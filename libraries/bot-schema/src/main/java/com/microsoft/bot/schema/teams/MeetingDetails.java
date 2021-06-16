// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specific details of a Teams meeting.
 */
public class MeetingDetails {
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "msGraphResourceId")
    private String msGraphResourceId;

    @JsonProperty(value = "scheduledStartTime")
    private String scheduledStartTime;

    @JsonProperty(value = "scheduledEndTime")
    private String scheduledEndTime;

    @JsonProperty(value = "joinUrl")
    private String joinUrl;

    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "type")
    private String type;

    /**
     * Initializes a new instance.
     */
    public MeetingDetails() {
    }

    /**
     * Gets the meeting's Id, encoded as a BASE64 String.
     * 
     * @return The meeting's Id, encoded as a BASE64 String.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the meeting's Id, encoded as a BASE64 String.
     * 
     * @param withId The meeting's Id, encoded as a BASE64 String.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the MsGraphResourceId, used specifically for MS Graph API calls.
     * 
     * @return The MsGraphResourceId, used specifically for MS Graph API calls.
     */
    public String getMsGraphResourceId() {
        return msGraphResourceId;
    }

    /**
     * Sets the MsGraphResourceId, used specifically for MS Graph API calls.
     * 
     * @param withMsGraphResourceId The MsGraphResourceId, used specifically for MS
     *                              Graph API calls.
     */
    public void setMsGraphResourceId(String withMsGraphResourceId) {
        msGraphResourceId = withMsGraphResourceId;
    }

    /**
     * Gets the meeting's scheduled start time, in UTC.
     * 
     * @return The meeting's scheduled start time, in UTC.
     */
    public String getScheduledStartTime() {
        return scheduledStartTime;
    }

    /**
     * Sets the meeting's scheduled start time, in UTC.
     * 
     * @param withScheduledStartTime The meeting's scheduled start time, in UTC.
     */
    public void setScheduledStartTime(String withScheduledStartTime) {
        scheduledStartTime = withScheduledStartTime;
    }

    /**
     * Gets the meeting's scheduled end time, in UTC.
     * 
     * @return The meeting's scheduled end time, in UTC.
     */
    public String getScheduledEndTime() {
        return scheduledEndTime;
    }

    /**
     * Sets the meeting's scheduled end time, in UTC.
     * 
     * @param withScheduledEndTime The meeting's scheduled end time, in UTC.
     */
    public void setScheduledEndTime(String withScheduledEndTime) {
        scheduledEndTime = withScheduledEndTime;
    }

    /**
     * Gets the URL used to join the meeting.
     * 
     * @return The URL used to join the meeting.
     */
    public String getJoinUrl() {
        return joinUrl;
    }

    /**
     * Sets the URL used to join the meeting.
     * 
     * @param withJoinUrl The URL used to join the meeting.
     */
    public void setJoinUrl(String withJoinUrl) {
        joinUrl = withJoinUrl;
    }

    /**
     * Gets the title of the meeting.
     * 
     * @return The title of the meeting.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the meeting.
     * 
     * @param withTitle The title of the meeting.
     */
    public void setTitle(String withTitle) {
        title = withTitle;
    }

    /**
     * Gets the meeting's type.
     * 
     * @return The meeting's type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the meeting's type.
     * 
     * @param withType The meeting's type.
     */
    public void setType(String withType) {
        type = withType;
    }
}
