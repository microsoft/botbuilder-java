// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * Defines values for ActivityTypes.
 */
public final class ActivityTypes {
    private ActivityTypes() {

    }

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
     * Enum value for Command Activities.
     */
    public static final String COMMAND = "command";

    /**
     * Enum value for Command Result Activities.
     */
    public static final String COMMAND_RESULT = "commandResult";

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

    /**
     * The type value for delay activities.
     *
     * As an outgoing activity type, causes the adapter to pause for
     * {@link Activity#getValue} milliseconds. The activity's
     * {@link Activity#getValue} should be an integer value.
     */
    public static final String DELAY = "delay";

    /**
     * The type value for invoke response activities.
     *
     * This is used for a return payload in response to an invoke activity. Invoke
     * activities communicate programmatic information from a client or channel to a
     * bot, and have a corresponding return payload for use within the channel. The
     * meaning of an invoke activity is defined by the {@link Activity#getName}
     * property, which is meaningful within the scope of a channel.
     */
    public static final String INVOKE_RESPONSE = "invokeResponse";
}
