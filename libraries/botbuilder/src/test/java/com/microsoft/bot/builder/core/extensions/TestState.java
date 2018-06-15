package com.microsoft.bot.builder.core.extensions;

public class TestState implements StoreItem {
    private String etag;

    @Override
    public String geteTag() {
        return this.etag;
    }

    @Override
    public void seteTag(String etag) {
        this.etag = etag;
    }

    private String value;

    public String value() {
        return this.value;
    }

    public void withValue(String value) {
        this.value = value;
    }
}

