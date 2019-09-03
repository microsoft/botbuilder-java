/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

public class ResultPair<X, Y> {
    public final X x;
    public final Y y;

    public ResultPair(X withX, Y withY) {
        this.x = withX;
        this.y = withY;
    }
}
