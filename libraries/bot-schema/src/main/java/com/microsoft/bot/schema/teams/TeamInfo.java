// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a team.
 */
public class TeamInfo {
    /**
     * Unique identifier representing a team.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * Name of a team.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Azure Active Directory (AAD) Group Id for the team.
     * <p>
     * We don't see this C#, but Teams definitely sends this to the bot.
     */
    @JsonProperty(value = "aadGroupId")
    private String aadGroupId;

    /**
     * Get unique identifier representing a team.
     *
     * @return Unique identifier representing a team.
     */
    public String getId() {
        return id;
    }

    /**
     * Set unique identifier representing a team.
     *
     * @param withId unique identifier representing a team.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Get the name of the team.
     *
     * @return get the name of the team.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the team.
     *
     * @param withName name of the team.
     */
    public void setName(String withName) {
        name = withName;
    }

    /**
     * Get Azure Active Directory (AAD) Group Id for the team.
     *
     * @return Azure Active Directory (AAD) Group Id for the team.
     */
    public String getAadGroupId() {
        return aadGroupId;
    }

    /**
     * Set Azure Active Directory (AAD) Group Id for the team.
     *
     * @param withAadGroupId Azure Active Directory (AAD) Group Id for the team
     */
    public void setAadGroupId(String withAadGroupId) {
        this.aadGroupId = withAadGroupId;
    }

    /**
     * A new instance of TeamInfo.
     *
     * @param withId         unique identifier representing a team.
     * @param withName       Set the name of the team.
     * @param withAadGroupId Azure Active Directory (AAD) Group Id for the team
     */
    public TeamInfo(String withId, String withName, String withAadGroupId) {
        this.id = withId;
        this.name = withName;
        this.aadGroupId = withAadGroupId;
    }

    /**
     * A new empty instance of TeamInfo.
     */
    public TeamInfo() {
    }

    /**
     * A new instance of TeamInfo with ID.
     * 
     * @param withId The id of the team.
     */
    public TeamInfo(String withId) {
        this(withId, null, null);
    }
}
