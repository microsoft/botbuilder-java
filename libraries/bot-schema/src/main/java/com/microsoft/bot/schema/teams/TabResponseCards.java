// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Envelope for cards for a TabResponse.
 */
public class TabResponseCards {
    @JsonProperty(value = "cards")
    private List<Object> cards;

    /**
     * Initializes a new instance of the class.
     */
    public TabResponseCards() {

    }

    /**
     * Gets adaptive cards for this card tab response.
     * @return Cards for the TabResponse.
     */
    public List<Object> getCards() {
        return cards;
    }

    /**
     * Sets adaptive cards for this card tab response.
     * @param withCards Cards for the TabResponse.
     */
    public void setCards(List<Object> withCards) {
        cards = withCards;
    }
}
