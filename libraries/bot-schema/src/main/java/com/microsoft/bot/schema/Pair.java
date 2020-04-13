// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * A simple 2 Tuple-like class since Java doesn't natively support them. This is
 * an immutable object.
 * 
 * @param <L> The type of the left tuple value.
 * @param <R> The type of the right tuple value.
 */
public class Pair<L, R> {
    private L left;
    private R right;

    /**
     * Creates a new Pair.
     * 
     * @param withLeft  The left value.
     * @param withRight The right value.
     */
    public Pair(L withLeft, R withRight) {
        left = withLeft;
        right = withRight;
    }

    /**
     * Gets the left value.
     * 
     * @return The left vale of type L.
     */
    public L getLeft() {
        return left;
    }

    /**
     * Gets the right value.
     * 
     * @return The right value of type R.
     */
    public R getRight() {
        return right;
    }
}
