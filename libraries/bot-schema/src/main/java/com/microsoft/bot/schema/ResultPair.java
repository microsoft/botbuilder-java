// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * Result pair.
 * 
 * @param <OUT_VALUE> Type of 'Right' value.
 */
public class ResultPair<OUT_VALUE> extends Pair<Boolean, OUT_VALUE> {
    /**
     * Creates a new immutable instance of a ResultPair.
     * 
     * @param withResult The result of the ResultPair value.
     * @param withValue  The value.
     */
    public ResultPair(Boolean withResult, OUT_VALUE withValue) {
        super(withResult, withValue);
    }

    /**
     * Gets the result.
     * 
     * @return True if successful.
     */
    public Boolean result() {
        return getLeft();
    }

    /**
     * Gets the value.
     * 
     * @return The value of type OUT_VALUE.
     */
    public OUT_VALUE value() {
        return getRight();
    }
}
