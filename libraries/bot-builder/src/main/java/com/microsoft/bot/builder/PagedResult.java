// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;


import java.util.ArrayList;
import java.util.List;

/**
 * Page of results from an enumeration.
 *
 * @param <T> The type of items in the results.
 */
public class PagedResult<T> {
    /**
     * Page of items.
     */
    private List<T> items = new ArrayList<>();

    /**
     * Token used to page through multiple pages.
     */
    private String continuationToken;

    public List<T> getItems() {
        return this.items;
    }

    public void setItems(List<T> value) {
        this.items = value;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String withValue) {
        continuationToken = withValue;
    }
}
