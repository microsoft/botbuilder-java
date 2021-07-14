// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * Specific details of a Teams meeting.
 */
public class MeetingDetails extends MeetingDetailsBase {
    @JsonProperty(value = "msGraphResourceId")
    private String msGraphResourceId;

    @JsonProperty(value = "scheduledStartTime")
    private OffsetDateTime scheduledStartTime;

    @JsonProperty(value = "scheduledEndTime")
    private OffsetDateTime scheduledEndTime;

    @JsonProperty(value = "type")
    private String type;

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
    public OffsetDateTime getScheduledStartTime() {
        return scheduledStartTime;
    }

    /**
     * Sets the meeting's scheduled start time, in UTC.
     *
     * @param withScheduledStartTime The meeting's scheduled start time, in UTC.
     */
    public void setScheduledStartTime(OffsetDateTime withScheduledStartTime) {
        scheduledStartTime = withScheduledStartTime;
    }

    /**
     * Gets the meeting's scheduled end time, in UTC.
     *
     * @return The meeting's scheduled end time, in UTC.
     */
    public OffsetDateTime getScheduledEndTime() {
        return scheduledEndTime;
    }

    /**
     * Sets the meeting's scheduled end time, in UTC.
     *
     * @param withScheduledEndTime The meeting's scheduled end time, in UTC.
     */
    public void setScheduledEndTime(OffsetDateTime withScheduledEndTime) {
        scheduledEndTime = withScheduledEndTime;
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
