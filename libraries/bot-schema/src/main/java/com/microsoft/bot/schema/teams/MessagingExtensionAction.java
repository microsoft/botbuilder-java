// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Activity;

import java.util.List;

/**
 * Messaging extension action.
 */
public class MessagingExtensionAction extends TaskModuleRequest {
    @JsonProperty(value = "commandId")
    private String commandId;

    @JsonProperty(value = "commandContext")
    private String commandContext;

    @JsonProperty(value = "botMessagePreviewAction")
    private String botMessagePreviewAction;

    @JsonProperty(value = "botActivityPreview")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Activity> botActivityPreview;

    @JsonProperty(value = "messagePayload")
    private MessageActionsPayload messagePayload;

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
     * Gets the context from which the command originates. Possible values include:
     * 'message', 'compose', 'commandbox'
     * 
     * @return The command context.
     */
    public String getCommandContext() {
        return commandContext;
    }

    /**
     * Sets the context from which the command originates. Possible values include:
     * 'message', 'compose', 'commandbox'
     * 
     * @param withCommandContext The command context.
     */
    public void setCommandContext(String withCommandContext) {
        commandContext = withCommandContext;
    }

    /**
     * Gets bot message preview action taken by user. Possible values include:
     * 'edit', 'send'
     * 
     * @return The preview action.
     */
    public String getBotMessagePreviewAction() {
        return botMessagePreviewAction;
    }

    /**
     * Sets bot message preview action taken by user. Possible values include:
     * 'edit', 'send'
     * 
     * @param withBotMessagePreviewAction The preview action.
     */
    public void setBotMessagePreviewAction(String withBotMessagePreviewAction) {
        botMessagePreviewAction = withBotMessagePreviewAction;
    }

    /**
     * Gets the list of preview Activities.
     * 
     * @return The preview activities.
     */
    public List<Activity> getBotActivityPreview() {
        return botActivityPreview;
    }

    /**
     * Sets the list of preview Activities.
     * 
     * @param withBotActivityPreview The preview activities.
     */
    public void setBotActivityPreview(List<Activity> withBotActivityPreview) {
        botActivityPreview = withBotActivityPreview;
    }

    /**
     * Gets message content sent as part of the command request.
     * 
     * @return The message payload.
     */
    public MessageActionsPayload getMessagePayload() {
        return messagePayload;
    }

    /**
     * Sets message content sent as part of the command request.
     * 
     * @param withMessagePayload The message payload.
     */
    public void setMessagePayload(MessageActionsPayload withMessagePayload) {
        messagePayload = withMessagePayload;
    }
}
