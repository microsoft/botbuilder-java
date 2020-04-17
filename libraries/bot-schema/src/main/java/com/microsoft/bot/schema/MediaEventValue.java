// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Supplementary parameter for media events.
 */
public class MediaEventValue {
    @JsonProperty(value = "cardValue")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object cardValue;

    /**
     * MediaEventValue with card value.
     * 
     * @param withCardValue The card value.
     */
    public MediaEventValue(Object withCardValue) {
        this.cardValue = withCardValue;
    }

    /**
     * Callback parameter specified in the Value field of the MediaCard that
     * originated this event.
     *
     * @return the cardValue value
     */
    public Object getCardValue() {
        return this.cardValue;
    }

    /**
     * Callback parameter specified in the Value field of the MediaCard that
     * originated this event.
     *
     * @param withCardValue the cardValue value to set
     */
    public void setCardValue(Object withCardValue) {
        this.cardValue = withCardValue;
    }
}
