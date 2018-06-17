package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.models.ConversationReference;

/// <summary>
/// State object passed to the bot token service.
/// </summary>
public class TokenExchangeState
{
    /// <summary>
    /// The connection name that was used
    /// </summary>
    @JsonProperty(value = "connectionName")
    private String connectionName;
    public String connectionName() {
        return this.connectionName;
    }
    public TokenExchangeState withConnectionName(String connectionName) {
        this.connectionName = connectionName;
        return this;
    }

    /// <summary>
    /// A reference to the conversation
    /// </summary>
    @JsonProperty(value = "conversation")
    private ConversationReference conversation;
    public ConversationReference conversation() {
        return this.conversation;
    }
    public TokenExchangeState withConversation(ConversationReference conversation) {
        this.conversation = conversation;
        return this;
    }

    /// <summary>
    /// The URL of the bot messaging endpoint
    /// </summary>
    @JsonProperty("botUrl")
    private String botUrl;
    public String botUrl() {
        return this.botUrl;
    }
    public TokenExchangeState withBotUrl(String botUrl) {
        this.botUrl = botUrl;
        return this;
    }

    /// <summary>
    /// The bot's registered application ID
    /// </summary>
    @JsonProperty("msAppId")
    String msAppId;
    public String msAppId() {
        return this.msAppId;
    }
    public TokenExchangeState withMsAppId(String msAppId) {
        this.msAppId = msAppId;
        return this;
    }
}
