// CHECKSTYLE:OFF
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.inspection;

import com.microsoft.bot.schema.ConversationReference;

import java.util.HashMap;
import java.util.Map;

public class InspectionSessionsByStatus {
    private Map<String, ConversationReference> openedSessions = new HashMap<>();
    private Map<String, ConversationReference> attachedSessions = new HashMap<>();

    public Map<String, ConversationReference> getAttachedSessions() {
        return attachedSessions;
    }

    public void setAttachedSessions(Map<String, ConversationReference> attachedSessions) {
        this.attachedSessions = attachedSessions;
    }

    public Map<String, ConversationReference> getOpenedSessions() {
        return openedSessions;
    }

    public void setOpenedSessions(Map<String, ConversationReference> openedSessions) {
        this.openedSessions = openedSessions;
    }
}
