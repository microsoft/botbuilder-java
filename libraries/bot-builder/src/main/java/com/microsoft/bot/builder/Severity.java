// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

/**
 * This enumeration is used by TrackTrace to identify severity level.
 */
public enum Severity {
    /**
     * Verbose severity level.
     */
    VERBOSE(0),

    /**
     * Information severity level.
     */
    INFORMATION(1),

    /**
     * Warning severity level.
     */
    WARNING(2),

    /**
     * Error severity level.
     */
    ERROR(3),

    /**
     * Critical severity level.
     */
    CRITICAL(4);

    private int value;

    /**
     * Constructs with an in value.
     * 
     * @param witValue Severity level.
     */
    Severity(int witValue) {
        value = witValue;
    }

    /**
     * For conversion to int.
     * 
     * @return The int value of this enum.
     */
    public int getSeverity() {
        return value;
    }
}
