// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A value that can be sorted and still refer to its original position with a source array.
 */
public class SortedValue {
    @JsonProperty(value = "value")
    private String value;

    @JsonProperty(value = "index")
    private int index;

    /**
     * Creates a sort value.
     * @param withValue The value that will be sorted.
     * @param withIndex The values original position within its unsorted array.
     */
    public SortedValue(String withValue, int withIndex) {
        value = withValue;
        index = withIndex;
    }

    /**
     * Gets the value that will be sorted.
     * @return The value that will be sorted.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value that will be sorted.
     * @param withValue The value that will be sorted.
     */
    public void setValue(String withValue) {
        value = withValue;
    }

    /**
     * Gets the values original position within its unsorted array.
     * @return The values original position within its unsorted array.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the values original position within its unsorted array.
     * @param withIndex The values original position within its unsorted array.
     */
    public void setIndex(int withIndex) {
        index = withIndex;
    }
}
