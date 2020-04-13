// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Parameters for creating a new conversation.
 */
public class ConversationParameters {
    /**
     * IsGroup.
     */
    @JsonProperty(value = "isGroup")
    private boolean isGroup;

    /**
     * The bot address for this conversation.
     */
    @JsonProperty(value = "bot")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ChannelAccount bot;

    /**
     * Members to add to the conversation.
     */
    @JsonProperty(value = "members")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ChannelAccount> members;

    /**
     * (Optional) Topic of the conversation (if supported by the channel).
     */
    @JsonProperty(value = "topicName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String topicName;

    /**
     * (Optional) The tenant ID in which the conversation should be created.
     */
    @JsonProperty(value = "tenantId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String tenantId;

    /**
     * (Optional) When creating a new conversation, use this activity as the intial
     * message to the conversation.
     */
    @JsonProperty(value = "activity")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Activity activity;

    /**
     * Channel specific payload for creating the conversation.
     */
    @JsonProperty(value = "channelData")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object channelData;

    /**
     * Get the {@link #isGroup} value.
     * 
     * @return The isGroup value.
     */
    public boolean isGroup() {
        return this.isGroup;
    }

    /**
     * Set the {@link #isGroup} value.
     * 
     * @param withIsGroup the isGroup value to set
     */
    public void setIsGroup(boolean withIsGroup) {
        this.isGroup = withIsGroup;
    }

    /**
     * Get the {@link #bot} value.
     * 
     * @return The bot value.
     */
    public ChannelAccount getBot() {
        return this.bot;
    }

    /**
     * Set the {@link #bot} value.
     * 
     * @param withBot the bot value to set
     */
    public void setBot(ChannelAccount withBot) {
        this.bot = withBot;
    }

    /**
     * Get the {@link #members} value.
     * 
     * @return The members value.
     */
    public List<ChannelAccount> getMembers() {
        return this.members;
    }

    /**
     * Set the {@link #members} value.
     * 
     * @param withMembers the members value to set
     */
    public void setMembers(List<ChannelAccount> withMembers) {
        this.members = withMembers;
    }

    /**
     * Get the {@link #topicName} value.
     * 
     * @return The topicname value.
     */
    public String getTopicName() {
        return this.topicName;
    }

    /**
     * Set the {@link #topicName} value.
     * 
     * @param withTopicName the topicName value to set
     */
    public void setTopicName(String withTopicName) {
        this.topicName = withTopicName;
    }

    /**
     * Get the {@link Activity} value.
     * 
     * @return The Activity value.
     */
    public Activity getActivity() {
        return this.activity;
    }

    /**
     * Set the {@link Activity} value.
     * 
     * @param withActivity the activity value to set.
     */
    public void setActivity(Activity withActivity) {
        this.activity = withActivity;
    }

    /**
     * Get the {@link #channelData} value.
     * 
     * @return the channelData value.
     */
    public Object getChannelData() {
        return this.channelData;
    }

    /**
     * Set the {@link #channelData} value.
     * 
     * @param withChannelData the channelData value to set
     */
    public void setChannelData(Object withChannelData) {
        this.channelData = withChannelData;
    }

    /**
     * Gets {@link #tenantId}.
     * 
     * @return The tenantId value.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Sets {@link #tenantId} value.
     * 
     * @param withTenantId The tenantId value to set.
     */
    public void setTenantId(String withTenantId) {
        this.tenantId = withTenantId;
    }
}
