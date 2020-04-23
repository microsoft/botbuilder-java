// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * Constants for CallerId values.
 */
public final class CallerIdConstants {
    private CallerIdConstants() {

    }

    /**
     * The caller ID for any Bot Framework channel.
     */
    public static final String PUBLIC_AZURE_CHANNEL = "urn:botframework:azure";

    /**
     * The caller ID for any Bot Framework US Government cloud channel.
     */
    public static final String US_GOV_CHANNEL = "urn:botframework:azureusgov";

    /**
     * The caller ID prefix when a bot initiates a request to another bot. This
     * prefix will be followed by the Azure Active Directory App ID of the bot that
     * initiated the call.
     */
    public static final String BOT_TO_BOT_PREFIX = "urn:botframework:aadappid:";
}
