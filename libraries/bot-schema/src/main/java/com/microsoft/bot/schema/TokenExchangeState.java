/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * State object passed to the bot token service.
 */
public class TokenExchangeState {
    /**
     * The bot's registered application ID
     */
    @JsonProperty("msAppId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String msAppId;

    /**
     * The connection name that was used
     */
    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    /**
     * A reference to the conversation
     */
    @JsonProperty(value = "conversation")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ConversationReference conversation;

    /**
     * The URL of the bot messaging endpoint
     */
    @JsonProperty("botUrl")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String botUrl;

    public String getConnectionName() {
        return this.connectionName;
    }

    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }

    public ConversationReference getConversation() {
        return this.conversation;
    }

    public void setConversation(ConversationReference withConversation) {
        this.conversation = withConversation;
    }

    public String getBotUrl() {
        return this.botUrl;
    }

    public void setBotUrl(String withBotUrl) {
        this.botUrl = withBotUrl;
    }

    public String getMsAppId() {
        return this.msAppId;
    }

    public void setMsAppId(String withMsAppId) {
        this.msAppId = withMsAppId;
    }
}
