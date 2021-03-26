// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * A card representing a request to peform a sign in via OAuth.
 */
public class OAuthCard {
    @JsonIgnore
    public static final String CONTENTTYPE = "application/vnd.microsoft.card.oauth";

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
     * The resource to try to perform token exchange with.
     */
    private TokenExchangeResource tokenExchangeResource;

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

    /**
     * Set the buttons value.
     *
     * @param withButtons the buttons value to set
     */
    public void setButtons(CardAction... withButtons) {
        this.buttons = Arrays.asList(withButtons);
    }

    /**
     * Creates an @{link Attachment} for this card.
     *
     * @return An Attachment object containing the card.
     */
    public Attachment toAttachment() {
        Attachment attachment = new Attachment();
        attachment.setContent(this);
        attachment.setContentType(CONTENTTYPE);

        return attachment;
    }

    /**
     * Gets the resource to try to perform token exchange with.
     * @return The tokenExchangeResource value.
     */
    public TokenExchangeResource getTokenExchangeResource() {
        return tokenExchangeResource;
    }

    /**
     * Sets the resource to try to perform token exchange with.
     * @param withExchangeResource The tokenExchangeResource value.
     */
    public void setTokenExchangeResource(TokenExchangeResource withExchangeResource) {
        tokenExchangeResource = withExchangeResource;
    }
}
