// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * State object passed to the bot token service.
 */
public class TokenExchangeState {
    @JsonProperty(value = "msAppId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String msAppId;

    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    @JsonProperty(value = "conversation")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ConversationReference conversation;

    @JsonProperty(value = "botUrl")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String botUrl;

    @JsonProperty(value = "relatesTo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ConversationReference relatesTo;

    /**
     * The connection name that was used.
     *
     * @return The connection name.
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * The connection name that was used.
     *
     * @param withConnectionName The connection name.
     */
    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }

    /**
     * A reference to the conversation.
     *
     * @return The conversation reference.
     */
    public ConversationReference getConversation() {
        return this.conversation;
    }

    /**
     * A reference to the conversation.
     *
     * @param withConversation The conversation reference.
     */
    public void setConversation(ConversationReference withConversation) {
        this.conversation = withConversation;
    }

    /**
     * The URL of the bot messaging endpoint.
     *
     * @return The messaging endpoint.
     */
    public String getBotUrl() {
        return this.botUrl;
    }

    /**
     * The URL of the bot messaging endpoint.
     *
     * @param withBotUrl The messaging endpoint.
     */
    public void setBotUrl(String withBotUrl) {
        this.botUrl = withBotUrl;
    }

    /**
     * The bot's registered application ID.
     *
     * @return The app id.
     */
    public String getMsAppId() {
        return this.msAppId;
    }

    /**
     * The bot's registered application ID.
     *
     * @param withMsAppId The app id.
     */
    public void setMsAppId(String withMsAppId) {
        this.msAppId = withMsAppId;
    }

    /**
     * Gets the reference to a related parent conversation for this token exchange.
     *
     * @return A reference to a related parent conversation.
     */
    public ConversationReference getRelatesTo() {
        return relatesTo;
    }

    /**
     * Sets the reference to a related parent conversation for this token exchange.
     *
     * @param withRelatesTo A reference to a related parent conversation.
     */
    public void setRelatesTo(ConversationReference withRelatesTo) {
        relatesTo = withRelatesTo;
    }
}
