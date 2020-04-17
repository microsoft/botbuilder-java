// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details related to a team.
 */
public class TeamDetails {
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "aadGroupId")
    private String aadGroupId;

    @JsonProperty(value = "channelCount")
    private int channelCount;

    @JsonProperty(value = "memberCount")
    private int memberCount;

    /**
     * Gets unique identifier representing a team.
     * 
     * @return The teams id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique identifier representing a team.
     * 
     * @param withId The teams id.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Gets name of team.
     * 
     * @return The team name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of team.
     * 
     * @param withName The team name.
     */
    public void setName(String withName) {
        name = withName;
    }

    /**
     * Gets Azure Active Directory (AAD) Group Id for the team.
     * 
     * @return The Azure group id.
     */
    public String getAadGroupId() {
        return aadGroupId;
    }

    /**
     * Sets Azure Active Directory (AAD) Group Id for the team.
     * 
     * @param withAadGroupId The Azure group id.
     */
    public void setAadGroupId(String withAadGroupId) {
        aadGroupId = withAadGroupId;
    }

    /**
     * Gets the number of channels in the team.
     * 
     * @return The number of channels.
     */
    public int getChannelCount() {
        return channelCount;
    }

    /**
     * Sets the number of channels in the team.
     * 
     * @param withChannelCount The number of channels.
     */
    public void setChannelCount(int withChannelCount) {
        channelCount = withChannelCount;
    }

    /**
     * Gets the number of members in the team.
     * 
     * @return The number of memebers.
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * Sets the number of members in the team.
     * 
     * @param withMemberCount The number of members.
     */
    public void setMemberCount(int withMemberCount) {
        memberCount = withMemberCount;
    }
}
