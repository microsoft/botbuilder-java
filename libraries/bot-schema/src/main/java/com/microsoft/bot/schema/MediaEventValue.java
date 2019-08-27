/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Supplementary parameter for media events.
 */
public class MediaEventValue {
    /**
     * Callback parameter specified in the Value field of the MediaCard that
     * originated this event.
     */
    @JsonProperty(value = "cardValue")
    private Object cardValue;

    public MediaEventValue(Object withCardValue) {
        this.cardValue = withCardValue;
    }

    /**
     * Get the cardValue value.
     *
     * @return the cardValue value
     */
    public Object getCardValue() {
        return this.cardValue;
    }

    /**
     * Set the cardValue value.
     *
     * @param withCardValue the cardValue value to set
     */
    public void setCardValue(Object withCardValue) {
        this.cardValue = withCardValue;
    }
}
