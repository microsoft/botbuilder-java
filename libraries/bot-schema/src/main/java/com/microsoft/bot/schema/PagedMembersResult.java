// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Page of members.
 */
public class PagedMembersResult {

    @JsonProperty(value = "continuationToken")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String continuationToken;

    /**
     * List of members in this conversation.
     */
    @JsonProperty(value = "members")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ChannelAccount> members;

    /**
     * Gets paging token.
     * 
     * @return The continuation token to be used in the next call.
     */
    public String getContinuationToken() {
        return this.continuationToken;
    }

    /**
     * Sets paging token.
     * 
     * @param withContinuationToken The continuation token.
     */
    public void setContinuationToken(String withContinuationToken) {
        this.continuationToken = withContinuationToken;
    }

    /**
     * Gets the Channel Accounts.
     *
     * @return the members value
     */
    public List<ChannelAccount> getMembers() {
        return this.members;
    }

    /**
     * Sets the Channel Accounts.
     *
     * @param withMembers the members value to set
     */
    public void setMembers(List<ChannelAccount> withMembers) {
        this.members = withMembers;
    }
}
