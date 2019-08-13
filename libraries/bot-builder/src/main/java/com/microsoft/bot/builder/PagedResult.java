package com.microsoft.bot.builder;


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 Page of results from an enumeration.

 <typeparam name="T"></typeparam>
 */
public class PagedResult<T>
{
    /**
     Page of items.
     */

//C# TO JAVA CONVERTER WARNING: Java does not allow direct instantiation of arrays of generic type parameters:
//ORIGINAL LINE: private T[] Items = new T[0];
    private T[] items = (T[])new Object[0];
    public final T[] getItems()
    {
        return this.items;
    }
    public final void items(T[] value)
    {
        this.items = value;
    }

    /**
     Token used to page through multiple pages.
     */
    private String continuationToken;
    public final String continuationToken()
    {
        return this.continuationToken;
    }
    public final PagedResult<T> withContinuationToken(String value)
    {
        this.continuationToken = value;
        return this;
    }
}
