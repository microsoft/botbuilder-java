/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.azure;

/**
 * Type representing LRO meta-data present in the x-ms-long-running-operation-options autorest extension.
 */
public final class LongRunningOperationOptions {
    /**
     * Default instance of this type.
     */
    public static final LongRunningOperationOptions DEFAULT = new LongRunningOperationOptions().withFinalStateVia(LongRunningFinalState.DEFAULT);

    /**
     * Describes how to retrieve the final state of the LRO.
     */
    private LongRunningFinalState finalStateVia;

    /**
     * @return indicates how to retrieve the final state of LRO.
     */
    public LongRunningFinalState finalStateVia() {
        return this.finalStateVia;
    }

    /**
     * Sets LongRunningFinalState value.
     *
     * @param finalStateVia indicates how to retrieve the final state of LRO.
     * @return LongRunningOperationOptions
     */
    public LongRunningOperationOptions withFinalStateVia(LongRunningFinalState finalStateVia) {
        this.finalStateVia = finalStateVia;
        return this;
    }
}
