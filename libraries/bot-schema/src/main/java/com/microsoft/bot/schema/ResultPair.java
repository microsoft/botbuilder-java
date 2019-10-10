// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * Result pair.
 * @param <X> Type of x.
 * @param <Y> Type of y.
 */
public class ResultPair<X, Y> {
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public final X x;
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public final Y y;

    /**
     * ResultPair with values.
     * @param withX The X.
     * @param withY The Y.
     */
    public ResultPair(X withX, Y withY) {
        this.x = withX;
        this.y = withY;
    }
}
