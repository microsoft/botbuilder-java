// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

/**
 * The Telemetry Logger Event names.
 */
public final class TelemetryLoggerConstants {
    private TelemetryLoggerConstants() {

    }

    /**
     * The name of the event when a new message is received from the user.
     */
    public static final String BOTMSGRECEIVEEVENT = "BotMessageReceived";

    /**
     * The name of the event when logged when a message is sent from the bot to the
     * user.
     */
    public static final String BOTMSGSENDEVENT = "BotMessageSend";

    /**
     * The name of the event when a message is updated by the bot.
     */
    public static final String BOTMSGUPDATEEVENT = "BotMessageUpdate";

    /**
     * The name of the event when a message is deleted by the bot.
     */
    public static final String BOTMSGDELETEEVENT = "BotMessageDelete";
}
