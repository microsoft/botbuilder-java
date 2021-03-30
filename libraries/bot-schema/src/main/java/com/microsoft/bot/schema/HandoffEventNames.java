
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * Defines values for handoff event names.
 */
public final class HandoffEventNames {

    private HandoffEventNames() {

    }

    /**
     * The value of handoff events for initiate handoff.
     */
    public static final String  INITIATEHANDOFF = "handoff.initiate";

    /**
     * The value of handoff events for handoff status.
     */
    public static final String  HANDOFFSTATUS = "handoff.status";
}
