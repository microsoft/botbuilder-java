/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

/**
 * Describes how to retrieve the final state of a long running operation.
 */
public enum LongRunningFinalState {
    /**
     * Indicate that no specific action required to retrieve the final state.
     */
    DEFAULT,
    /**
     * Indicate that use azure async operation uri to retrieve the final state.
     */
    AZURE_ASYNC_OPERATION,
    /**
     * Indicate that use location uri to retrieve the final state.
     */
    LOCATION,
    /**
     * Indicate that use original uri to retrieve the final state.
     */
    ORIGINAL_URI
}
