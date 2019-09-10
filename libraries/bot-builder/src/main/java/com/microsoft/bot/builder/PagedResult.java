// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;


/**
 * Page of results from an enumeration.
 *
 * @param <T> The type of items in the results.
 */
public class PagedResult<T> {
    /**
     * Page of items.
     */
    private T[] items = (T[]) new Object[0];

    /**
     * Token used to page through multiple pages.
     */
    private String continuationToken;

    public T[] getItems() {
        return this.items;
    }

    public void setItems(T[] value) {
        this.items = value;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String withValue) {
        continuationToken = withValue;
    }
}
