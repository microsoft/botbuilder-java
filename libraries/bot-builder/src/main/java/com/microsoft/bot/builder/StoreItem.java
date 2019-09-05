package com.microsoft.bot.builder;

public interface StoreItem {
    /**
     * eTag for concurrency
     */
    String getETag();
    void setETag(String eTag);
}
