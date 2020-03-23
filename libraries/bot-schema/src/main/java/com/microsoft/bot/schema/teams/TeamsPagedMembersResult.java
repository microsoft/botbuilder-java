// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TeamsPagedMembersResult {
    @JsonProperty(value = "continuationToken")
    private String continuationToken;

    @JsonProperty(value = "members")
    private List<TeamsChannelAccount> members;

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String withContinuationToken) {
        continuationToken = withContinuationToken;
    }

    public List<TeamsChannelAccount> getMembers() {
        return members;
    }

    public void setMembers(List<TeamsChannelAccount> withMembers) {
        members = withMembers;
    }
}
