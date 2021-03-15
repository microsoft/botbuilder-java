// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * Define values for common event names used by activities of type
 * ActivityTypes.Event.
 */
public final class ActivityEventNames {
    private ActivityEventNames() {

    }

    /**
     * The event name for continuing a conversation.
     */
    public static final String CONTINUE_CONVERSATION = "ContinueConversation";

    /**
     * The event name for creating a conversation.
     */
    public static final String CREATE_CONVERSATION = "CreateConversation";
}
