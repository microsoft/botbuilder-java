/**
 * Copyright  = c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ActivityTypes.
 */
public final class ActivityTypes {
    /**
     * Enum value message.
     */
    public static final String MESSAGE = "message";

    /**
     * Enum value contactRelationUpdate.
     */
    public static final String CONTACT_RELATION_UPDATE = "contactRelationUpdate";

    /**
     * Enum value conversationUpdate.
     */
    public static final String CONVERSATION_UPDATE = "conversationUpdate";

    /**
     * Enum value typing.
     */
    public static final String TYPING = "typing";

    /**
     * Enum value endOfConversation.
     */
    public static final String END_OF_CONVERSATION = "endOfConversation";

    /**
     * Enum value event.
     */
    public static final String EVENT = "event";

    /**
     * Enum value invoke.
     */
    public static final String INVOKE = "invoke";

    /**
     * Enum value deleteUserData.
     */
    public static final String DELETE_USER_DATA = "deleteUserData";

    /**
     * Enum value messageUpdate.
     */
    public static final String MESSAGE_UPDATE = "messageUpdate";

    /**
     * Enum value messageDelete.
     */
    public static final String MESSAGE_DELETE = "messageDelete";

    /**
     * Enum value installationUpdate.
     */
    public static final String INSTALLATION_UPDATE = "installationUpdate";

    /**
     * Enum value messageReaction.
     */
    public static final String MESSAGE_REACTION = "messageReaction";

    /**
     * Enum value suggestion.
     */
    public static final String SUGGESTION = "suggestion";

    /**
     * Enum value trace.
     */
    public static final String TRACE = "trace";

    /**
     * Enum value handoff.
     */
    public static final String HANDOFF = "handoff";
}
