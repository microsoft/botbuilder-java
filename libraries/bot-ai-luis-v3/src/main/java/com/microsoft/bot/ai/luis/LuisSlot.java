// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

/**
 * Utility class to set the Luis endpoint Slot.
 *
 */
public final class LuisSlot {

    // Not Called
    private LuisSlot() {

    }

    /**
     * Production slot on LUIS.
     */
    public static final String PRODUCTION = "production";

    /**
     * Staging slot on LUIS.
     */
    public static final String STAGING = "staging";
}
