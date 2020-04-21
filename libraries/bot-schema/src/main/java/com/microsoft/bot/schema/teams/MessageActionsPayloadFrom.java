// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a user, application, or conversation type that either sent or was
 * referenced in a message.
 */
public class MessageActionsPayloadFrom {
    @JsonProperty(value = "user")
    private MessageActionsPayloadUser user;

    @JsonProperty(value = "application")
    private MessageActionsPayloadApp application;

    @JsonProperty(value = "conversation")
    private MessageActionsPayloadConversation conversation;

    /**
     * Gets details of the user.
     * 
     * @return The payload user.
     */
    public MessageActionsPayloadUser getUser() {
        return user;
    }

    /**
     * Sets details of the user.
     * 
     * @param withUser The payload user.
     */
    public void setUser(MessageActionsPayloadUser withUser) {
        user = withUser;
    }

    /**
     * Gets details of the app.
     * 
     * @return The application details.
     */
    public MessageActionsPayloadApp getApplication() {
        return application;
    }

    /**
     * Sets details of the app.
     * 
     * @param withApplication The application details.
     */
    public void setApplication(MessageActionsPayloadApp withApplication) {
        application = withApplication;
    }

    /**
     * Gets details of the conversation.
     * 
     * @return The conversation details.
     */
    public MessageActionsPayloadConversation getConversation() {
        return conversation;
    }

    /**
     * Sets details of the conversation.
     * 
     * @param withConversation The conversation details.
     */
    public void setConversation(MessageActionsPayloadConversation withConversation) {
        conversation = withConversation;
    }
}
