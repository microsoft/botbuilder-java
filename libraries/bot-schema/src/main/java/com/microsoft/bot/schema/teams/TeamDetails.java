// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getId() {
        return id;
    }

    public void setId(String withId) {
        this.id = withId;
    }

    public String getName() {
        return name;
    }

    public void setName(String withName) {
        name = withName;
    }

    public String getAadGroupId() {
        return aadGroupId;
    }

    public void setAadGroupId(String withAadGroupId) {
        aadGroupId = withAadGroupId;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int withChannelCount) {
        channelCount = withChannelCount;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int withMemberCount) {
        memberCount = withMemberCount;
    }
}
