/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A card representing a request to peform a sign in via OAuth.
 */
public class OAuthCard {
    /**
     * Text for signin request.
     */
    @JsonProperty(value = "text")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    /**
     * The name of the registered connection.
     */
    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    /**
     * Action to use to perform signin.
     */
    @JsonProperty(value = "buttons")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardAction> buttons;

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param withText the text value to set
     */
    public void setText(String withText) {
        this.text = withText;
    }

    /**
     * Get the connectionName value.
     *
     * @return the connectionName value
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * Set the connectionName value.
     *
     * @param withConnectionName the connectionName value to set
     */
    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }

    /**
     * Get the buttons value.
     *
     * @return the buttons value
     */
    public List<CardAction> getButtons() {
        return this.buttons;
    }

    /**
     * Set the buttons value.
     *
     * @param withButtons the buttons value to set
     */
    public void setButtons(List<CardAction> withButtons) {
        this.buttons = withButtons;
    }
}
