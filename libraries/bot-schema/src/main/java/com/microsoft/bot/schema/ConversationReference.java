/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object relating to a particular point in a conversation.
 */
public class ConversationReference {
    /**
     * (Optional) ID of the activity to refer to.
     */
    @JsonProperty(value = "activityId")
    private String activityId;

    /**
     * (Optional) User participating in this conversation.
     */
    @JsonProperty(value = "user")
    private ChannelAccount user;

    /**
     * Bot participating in this conversation.
     */
    @JsonProperty(value = "bot")
    private ChannelAccount bot;

    /**
     * Conversation reference.
     */
    @JsonProperty(value = "conversation")
    private ConversationAccount conversation;

    /**
     * Channel ID.
     */
    @JsonProperty(value = "channelId")
    private String channelId;

    /**
     * Service endpoint where operations concerning the referenced conversation
     * may be performed.
     */
    @JsonProperty(value = "serviceUrl")
    private String serviceUrl;

    public static ConversationReference clone(ConversationReference conversationReference) {
        if (conversationReference == null) {
            return null;
        }

        return new ConversationReference() {{
            setActivityId(conversationReference.getActivityId());
            setBot(ChannelAccount.clone(conversationReference.getBot()));
            setUser(ChannelAccount.clone(conversationReference.getUser()));
            setConversation(ConversationAccount.clone(conversationReference.getConversation()));
            setServiceUrl(conversationReference.getServiceUrl());
            setChannelId(conversationReference.getChannelId());
        }};
    }

    /**
     * Get the activityId value.
     *
     * @return the activityId value
     */
    public String getActivityId() {
        return this.activityId;
    }

    /**
     * Set the activityId value.
     *
     * @param withActivityId the activityId value to set
     */
    public void setActivityId(String withActivityId) {
        this.activityId = withActivityId;
    }

    /**
     * Get the user value.
     *
     * @return the user value
     */
    public ChannelAccount getUser() {
        return this.user;
    }

    /**
     * Set the user value.
     *
     * @param withUser the user value to set
     */
    public void setUser(ChannelAccount withUser) {
        this.user = withUser;
    }

    /**
     * Get the bot value.
     *
     * @return the bot value
     */
    public ChannelAccount getBot() {
        return this.bot;
    }

    /**
     * Set the bot value.
     *
     * @param withBot the bot value to set
     */
    public void setBot(ChannelAccount withBot) {
        this.bot = withBot;
    }

    /**
     * Get the conversation value.
     *
     * @return the conversation value
     */
    public ConversationAccount getConversation() {
        return this.conversation;
    }

    /**
     * Set the conversation value.
     *
     * @param withConversation the conversation value to set
     */
    public void setConversation(ConversationAccount withConversation) {
        this.conversation = withConversation;
    }

    /**
     * Get the channelId value.
     *
     * @return the channelId value
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * Set the channelId value.
     *
     * @param withChannelId the channelId value to set
     */
    public void setChannelId(String withChannelId) {
        this.channelId = withChannelId;
    }

    /**
     * Get the serviceUrl value.
     *
     * @return the serviceUrl value
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * Set the serviceUrl value.
     *
     * @param withServiceUrl the serviceUrl value to set
     */
    public void setServiceUrl(String withServiceUrl) {
        this.serviceUrl = withServiceUrl;
    }
}
