// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Messaging extension query.
 */
public class MessagingExtensionQuery {
    @JsonProperty(value = "commandId")
    private String commandId;

    @JsonProperty(value = "parameters")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MessagingExtensionParameter> parameters;

    @JsonProperty(value = "queryOptions")
    private MessagingExtensionQueryOptions queryOptions;

    @JsonProperty(value = "state")
    private String state;

    /**
     * Gets id of the command assigned by Bot.
     * 
     * @return The command id.
     */
    public String getCommandId() {
        return commandId;
    }

    /**
     * Sets id of the command assigned by Bot.
     * 
     * @param withCommandId The command id.
     */
    public void setCommandId(String withCommandId) {
        commandId = withCommandId;
    }

    /**
     * Gets parameters for the query.
     * 
     * @return The query parameters.
     */
    public List<MessagingExtensionParameter> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters for the query.
     * 
     * @param withParameters The query parameters.
     */
    public void setParameters(List<MessagingExtensionParameter> withParameters) {
        parameters = withParameters;
    }

    /**
     * Gets the query options.
     * 
     * @return The query options.
     */
    public MessagingExtensionQueryOptions getQueryOptions() {
        return queryOptions;
    }

    /**
     * Sets the query options.
     * 
     * @param withQueryOptions The query options.
     */
    public void setQueryOptions(MessagingExtensionQueryOptions withQueryOptions) {
        queryOptions = withQueryOptions;
    }

    /**
     * Gets state parameter passed back to the bot after
     * authentication/configuration flow.
     * 
     * @return The state parameter.
     */
    public String getState() {
        return state;
    }

    /**
     * Sets state parameter passed back to the bot after
     * authentication/configuration flow.
     * 
     * @param withState The state parameter.
     */
    public void setState(String withState) {
        state = withState;
    }
}
