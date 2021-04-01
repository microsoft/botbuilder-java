// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * A receipt card.
 */
public class ReceiptCard {
    @JsonIgnore
    public static final String CONTENTTYPE = "application/vnd.microsoft.card.receipt";

    /**
     * Title of the card.
     */
    @JsonProperty(value = "title")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String title;

    /**
     * Array of Fact objects.
     */
    @JsonProperty(value = "facts")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Fact> facts;

    /**
     * Array of Receipt Items.
     */
    @JsonProperty(value = "items")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ReceiptItem> items;

    /**
     * This action will be activated when user taps on the card.
     */
    @JsonProperty(value = "tap")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CardAction tap;

    /**
     * Total amount of money paid (or to be paid).
     */
    @JsonProperty(value = "total")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String total;

    /**
     * Total amount of tax paid (or to be paid).
     */
    @JsonProperty(value = "tax")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String tax;

    /**
     * Total amount of VAT paid (or to be paid).
     */
    @JsonProperty(value = "vat")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String vat;

    /**
     * Set of actions applicable to the current card.
     */
    @JsonProperty(value = "buttons")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardAction> buttons;

    /**
     * Get the title value.
     *
     * @return the title value
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title value.
     *
     * @param withTitle the title value to set
     */
    public void setTitle(String withTitle) {
        this.title = withTitle;
    }

    /**
     * Get the facts value.
     *
     * @return the facts value
     */
    public List<Fact> getFacts() {
        return this.facts;
    }

    /**
     * Set the facts value.
     *
     * @param withFacts the facts value to set
     */
    public void setFacts(List<Fact> withFacts) {
        this.facts = withFacts;
    }

    /**
     * Set the facts value.
     *
     * @param withFacts the facts value to set
     */
    public void setFacts(Fact... withFacts) {
        this.facts = Arrays.asList(withFacts);
    }

    /**
     * Get the items value.
     *
     * @return the items value
     */
    public List<ReceiptItem> getItems() {
        return this.items;
    }

    /**
     * Set the items value.
     *
     * @param withItems the items value to set
     */
    public void setItems(List<ReceiptItem> withItems) {
        this.items = withItems;
    }

    /**
     * Set the items value.
     *
     * @param withItems the items value to set
     */
    public void setItems(ReceiptItem... withItems) {
        this.items = Arrays.asList(withItems);
    }

    /**
     * Get the tap value.
     *
     * @return the tap value
     */
    public CardAction getTap() {
        return this.tap;
    }

    /**
     * Set the tap value.
     *
     * @param withTap the tap value to set
     */
    public void setTap(CardAction withTap) {
        this.tap = withTap;
    }

    /**
     * Get the total value.
     *
     * @return the total value
     */
    public String getTotal() {
        return this.total;
    }

    /**
     * Set the total value.
     *
     * @param withTotal the total value to set
     */
    public void setTotal(String withTotal) {
        this.total = withTotal;
    }

    /**
     * Get the tax value.
     *
     * @return the tax value
     */
    public String geTax() {
        return this.tax;
    }

    /**
     * Set the tax value.
     *
     * @param withTax the tax value to set
     */
    public void setTax(String withTax) {
        this.tax = withTax;
    }

    /**
     * Get the vat value.
     *
     * @return the vat value
     */
    public String getVat() {
        return this.vat;
    }

    /**
     * Set the vat value.
     *
     * @param withVat the vat value to set
     */
    public void setVat(String withVat) {
        this.vat = withVat;
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
}
