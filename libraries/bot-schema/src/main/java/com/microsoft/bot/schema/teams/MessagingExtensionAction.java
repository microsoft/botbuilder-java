// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Activity;

import java.util.List;

public class MessagingExtensionAction extends TaskModuleRequest {
    @JsonProperty(value = "commandId")
    private String commandId;

    @JsonProperty(value = "commandContext")
    private String commandContext;

    @JsonProperty(value = "botMessagePreviewAction")
    private String botMessagePreviewAction;

    @JsonProperty(value = "botActivityPreview")
    public List<Activity> botActivityPreview;

    @JsonProperty(value = "messagePayload")
    public MessageActionsPayload messagePayload;

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String withCommandId) {
        commandId = withCommandId;
    }

    public String getCommandContext() {
        return commandContext;
    }

    public void setCommandContext(String withCommandContext) {
        commandContext = withCommandContext;
    }

    public String getBotMessagePreviewAction() {
        return botMessagePreviewAction;
    }

    public void setBotMessagePreviewAction(String withBotMessagePreviewAction) {
        botMessagePreviewAction = withBotMessagePreviewAction;
    }

    public List<Activity> getBotActivityPreview() {
        return botActivityPreview;
    }

    public void setBotActivityPreview(List<Activity> withBotActivityPreview) {
        botActivityPreview = withBotActivityPreview;
    }

    public MessageActionsPayload getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(MessageActionsPayload withMessagePayload) {
        messagePayload = withMessagePayload;
    }
}
