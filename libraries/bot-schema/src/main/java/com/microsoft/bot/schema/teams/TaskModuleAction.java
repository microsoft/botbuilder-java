// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.CardAction;
import com.sun.org.slf4j.internal.LoggerFactory;

public class TaskModuleAction extends CardAction {
    public TaskModuleAction(String withTitle, Object withValue) {
        super.setType(ActionTypes.INVOKE);
        super.setTitle(withTitle);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        ObjectNode data = objectMapper.valueToTree(withValue);
        data.put("type", "task/fetch");

        try {
            super.setValue(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            LoggerFactory.getLogger(TaskModuleAction.class).error("TaskModuleAction", e);
        }
    }
}
