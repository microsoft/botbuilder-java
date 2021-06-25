// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

/**
 * Values and constants used for Conversation specific info.
 */
public final class ConversationConstants {
    private ConversationConstants() {
    }

    /**
     * The name of Http Request Header to add Conversation Id to skills requests.
     */
    public static final String CONVERSATION_ID_HTTP_HEADERNAME = "x-ms-conversation-id";
}
