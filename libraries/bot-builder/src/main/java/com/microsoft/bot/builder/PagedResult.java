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

    /**
     * Gets the page of items.
     * 
     * @return The List of items.
     */
    public List<T> getItems() {
        return this.items;
    }

    /**
     * Sets the page of items.
     * 
     * @param value The List of items.
     */
    public void setItems(List<T> value) {
        this.items = value;
    }

    /**
     * Gets the token for retrieving the next page of results.
     * 
     * @return The Continuation Token to pass to get the next page of results.
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Sets the token for retrieving the next page of results.
     * 
     * @param withValue The Continuation Token to pass to get the next page of
     *                  results.
     */
    public void setContinuationToken(String withValue) {
        continuationToken = withValue;
    }
}
