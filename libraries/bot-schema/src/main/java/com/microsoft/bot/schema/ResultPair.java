package com.microsoft.bot.schema;

public class ResultPair<X, Y> {
    public final X x;
    public final Y y;
    public ResultPair(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}
