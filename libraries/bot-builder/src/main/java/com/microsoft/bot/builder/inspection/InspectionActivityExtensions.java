// CHECKSTYLE:OFF
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;

/**
 * Helper class for the inspection middleware.
 */
final class InspectionActivityExtensions {
    private InspectionActivityExtensions() {

    }

    static Activity makeCommandActivity(String command) {
        return Activity.createTraceActivity(
            "Command",
            "https://www.botframework.com/schemas/command",
            command,
            "Command"
        );
    }

    static Activity traceActivity(JsonNode state) {
        return Activity.createTraceActivity(
            "BotState",
            "https://www.botframework.com/schemas/botState",
            state,
            "Bot State"
        );
    }

    static Activity traceActivity(Activity activity, String name, String label) {
        return Activity.createTraceActivity(
            name,
            "https://www.botframework.com/schemas/activity",
            activity,
            label
        );
    }

    static Activity traceActivity(ConversationReference conversationReference) {
        return Activity.createTraceActivity(
            "MessageDelete",
            "https://www.botframework.com/schemas/conversationReference",
            conversationReference,
            "Deleted Message"
        );
    }

    static Activity traceActivity(Throwable exception) {
        return Activity.createTraceActivity(
            "TurnError",
            "https://www.botframework.com/schemas/error",
            exception.getMessage(),
            "Turn Error"
        );
    }
}
