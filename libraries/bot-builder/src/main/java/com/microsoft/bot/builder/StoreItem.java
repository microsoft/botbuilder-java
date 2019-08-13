package com.microsoft.bot.builder;

public interface StoreItem
{
    /**
     * eTag for concurrency
     */

    String geteTag();
    void seteTag(String eTag);
}
