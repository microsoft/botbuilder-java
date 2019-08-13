package com.microsoft.bot.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TypedObject {
    @JsonProperty
    private String name;

    public String name() {
        return this.name;
    }

    public TypedObject withName(String name) {
        this.name = name;
        return this;
    }
}
