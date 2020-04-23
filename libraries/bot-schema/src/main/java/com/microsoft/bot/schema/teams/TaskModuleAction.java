// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.CardAction;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Adapter class to represent BotBuilder card action as adaptive card action (in
 * type of Action.Submit).
 */
public class TaskModuleAction extends CardAction {
    /**
     * Initializes a new instance.
     * 
     * @param withTitle Button title.
     * @param withValue Free hidden value binding with button. The value will be
     *                  sent out with "task/fetch" invoke event.
     */
    public TaskModuleAction(String withTitle, Object withValue) {
        super.setType(ActionTypes.INVOKE);
        super.setTitle(withTitle);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        ObjectNode data = null;
        if (withValue instanceof String) {

            try {
                data = objectMapper.readValue((String) withValue, ObjectNode.class);
            } catch (IOException e) {
                LoggerFactory.getLogger(TaskModuleAction.class).error("TaskModuleAction", e);
            }
        } else {
            data = objectMapper.valueToTree(withValue);
        }

        data.put("type", "task/fetch");

        try {
            super.setValue(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            LoggerFactory.getLogger(TaskModuleAction.class).error("TaskModuleAction", e);
        }
    }
}
