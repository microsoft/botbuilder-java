package com.microsoft.bot.builder;

public class TestState implements StoreItem {
    private String etag;
    private String value;

    @Override
    public String getETag() {
        return this.etag;
    }

    @Override
    public void setETag(String etag) {
        this.etag = etag;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

