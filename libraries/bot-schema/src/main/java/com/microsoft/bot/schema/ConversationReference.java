// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object relating to a particular point in a conversation.
 */
public class ConversationReference {
    @JsonProperty(value = "activityId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String activityId;

    @JsonProperty(value = "user")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ChannelAccount user;

    @JsonProperty(value = "bot")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ChannelAccount bot;

    @JsonProperty(value = "conversation")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ConversationAccount conversation;

    @JsonProperty(value = "channelId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String channelId;

    @JsonProperty(value = "locale")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String locale;

    @JsonProperty(value = "serviceUrl")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String serviceUrl;

    /**
     * Performs a deep copy of a ConversationReference.
     * 
     * @param conversationReference The ConversationReference to copy.
     * @return A clone of the ConversationReference.
     */
    public static ConversationReference clone(ConversationReference conversationReference) {
        if (conversationReference == null) {
            return null;
        }

        ConversationReference cloned = new ConversationReference();
        cloned.setActivityId(conversationReference.getActivityId());
        cloned.setBot(ChannelAccount.clone(conversationReference.getBot()));
        cloned.setUser(ChannelAccount.clone(conversationReference.getUser()));
        cloned.setConversation(ConversationAccount.clone(conversationReference.getConversation()));
        cloned.setServiceUrl(conversationReference.getServiceUrl());
        cloned.setLocale(conversationReference.getLocale());
        cloned.setChannelId(conversationReference.getChannelId());

        return cloned;
    }

    /**
     * Creates {@link Activity} from conversation reference as it is posted to bot.
     * 
     * @return A continuation activity.
     */
    @JsonIgnore
    public Activity getContinuationActivity() {
        Activity activity = Activity.createEventActivity();
        activity.setName(ActivityEventNames.CONTINUE_CONVERSATION);
        activity.setId(UUID.randomUUID().toString());
        activity.setChannelId(getChannelId());
        activity.setConversation(getConversation());
        activity.setRecipient(getBot());
        activity.setLocale(getLocale());
        activity.setServiceUrl(getServiceUrl());
        activity.setFrom(getUser());
        activity.setRelatesTo(this);
        return activity;
    }

    /**
     * (Optional) ID of the activity to refer to.
     *
     * @return the activityId value
     */
    public String getActivityId() {
        return this.activityId;
    }

    /**
     * (Optional) ID of the activity to refer to.
     *
     * @param withActivityId the activityId value to set
     */
    public void setActivityId(String withActivityId) {
        this.activityId = withActivityId;
    }

    /**
     * (Optional) User participating in this conversation.
     *
     * @return the user value
     */
    public ChannelAccount getUser() {
        return this.user;
    }

    /**
     * (Optional) User participating in this conversation.
     *
     * @param withUser the user value to set
     */
    public void setUser(ChannelAccount withUser) {
        this.user = withUser;
    }

    /**
     * Bot participating in this conversation.
     *
     * @return the bot value
     */
    public ChannelAccount getBot() {
        return this.bot;
    }

    /**
     * Bot participating in this conversation.
     *
     * @param withBot the bot value to set
     */
    public void setBot(ChannelAccount withBot) {
        this.bot = withBot;
    }

    /**
     * Conversation reference.
     *
     * @return the conversation value
     */
    public ConversationAccount getConversation() {
        return this.conversation;
    }

    /**
     * Conversation reference.
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
     * Get the locale value.
     *
     * @return the locale value
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Set the locale value.
     *
     * @param withLocale the locale value to set
     */
    public void setLocale(String withLocale) {
        this.locale = withLocale;
    }

    /**
     * Service endpoint where operations concerning the referenced conversation may
     * be performed.
     *
     * @return the serviceUrl value
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * Service endpoint where operations concerning the referenced conversation may
     * be performed.
     *
     * @param withServiceUrl the serviceUrl value to set
     */
    public void setServiceUrl(String withServiceUrl) {
        this.serviceUrl = withServiceUrl;
    }
}
