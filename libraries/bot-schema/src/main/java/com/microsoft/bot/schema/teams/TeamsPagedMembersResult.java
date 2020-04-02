// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Teams page of members.
 */
public class TeamsPagedMembersResult {
    @JsonProperty(value = "continuationToken")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String continuationToken;

    @JsonProperty(value = "members")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TeamsChannelAccount> members;

    /**
     * Gets paging token.
     * @return The continuation token to be used in the next call.
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Sets paging token.
     * @param withContinuationToken The continuation token.
     */
    public void setContinuationToken(String withContinuationToken) {
        continuationToken = withContinuationToken;
    }

    /**
     * Gets the Channel Accounts.
     *
     * @return the members value
     */
    public List<TeamsChannelAccount> getMembers() {
        return members;
    }

    /**
     * Sets the Channel Accounts.
     *
     * @param withMembers the members value to set
     */
    public void setMembers(List<TeamsChannelAccount> withMembers) {
        members = withMembers;
    }
}
