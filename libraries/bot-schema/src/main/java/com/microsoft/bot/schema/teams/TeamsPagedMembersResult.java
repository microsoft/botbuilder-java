// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.schema.PagedMembersResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * Converts a PagedMembersResult to a TeamsPagedMembersResult.
     * 
     * @param pagedMembersResult The PagedMembersResult value.
     */
    public TeamsPagedMembersResult(PagedMembersResult pagedMembersResult) {
        continuationToken = pagedMembersResult.getContinuationToken();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        members = pagedMembersResult.getMembers().stream().map(channelAccount -> {
            try {
                // convert fro ChannelAccount to TeamsChannelAccount by going to JSON then back
                // to TeamsChannelAccount.
                // Is this really the most efficient way to handle this?
                JsonNode node = objectMapper.valueToTree(channelAccount);
                return objectMapper.treeToValue(node, TeamsChannelAccount.class);
            } catch (JsonProcessingException jpe) {
                // this would be a conversion error. for now, return null and filter the results
                // below. there is probably a more elegant way to handle this.
                return null;
            }
        }).collect(Collectors.toCollection(ArrayList::new));

        members.removeIf(Objects::isNull);
    }

    /**
     * Gets paging token.
     * 
     * @return The continuation token to be used in the next call.
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Sets paging token.
     * 
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
