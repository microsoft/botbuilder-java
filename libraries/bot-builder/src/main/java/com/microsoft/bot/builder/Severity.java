// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

public enum Severity {
    VERBOSE(0),
    INFORMATION(1),
    WARNING(2),
    ERROR(3),
    CRITICAL(4);

    private int value;

    Severity(int witValue) {
        value = witValue;
    }

    public int getSeverity() {
        return value;
    }
}
