// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope for cards for a Tab request.
 */
public class TabResponseCard {
    @JsonProperty(value = "card")
    private Object card;

    /**
     * Initializes a new instance of the class.
     */
    public TabResponseCard() {

    }

    /**
     * Gets adaptive card for this card tab response.
     * @return Cards for this TabResponse.
     */
    public Object getCard() {
        return card;
    }

    /**
     * Sets adaptive card for this card tab response.
     * @param withCard Cards for this TabResponse.
     */
    public void setCard(Object withCard) {
        card = withCard;
    }
}
