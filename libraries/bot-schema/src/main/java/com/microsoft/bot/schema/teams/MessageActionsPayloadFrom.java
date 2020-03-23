// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageActionsPayloadFrom {
    @JsonProperty(value = "user")
    public MessageActionsPayloadUser user;

    @JsonProperty(value = "application")
    public MessageActionsPayloadApp application;

    @JsonProperty(value = "conversation")
    public MessageActionsPayloadConversation conversation;

    public MessageActionsPayloadUser getUser() {
        return user;
    }

    public void setUser(MessageActionsPayloadUser withUser) {
        user = withUser;
    }

    public MessageActionsPayloadApp getApplication() {
        return application;
    }

    public void setApplication(MessageActionsPayloadApp withApplication) {
        application = withApplication;
    }

    public MessageActionsPayloadConversation getConversation() {
        return conversation;
    }

    public void setConversation(MessageActionsPayloadConversation withConversation) {
        conversation = withConversation;
    }
}
