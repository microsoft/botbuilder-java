package com.microsoft.bot.builder.core.extensions;

public interface StoreItem
{
    /// <summary>
    /// eTag for concurrency
    /// </summary>

    String geteTag();
    void seteTag(String eTag);
}
