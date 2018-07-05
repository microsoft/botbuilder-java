package com.microsoft.bot.builder;

public class TypedObject {
    private String name;

    public String name() {
        return this.name;
    }

    public TypedObject withName(String name) {
        this.name = name;
        return this;
    }
}
