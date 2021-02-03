// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.CardAction;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a choice for a choice prompt.
 */
public class Choice {
    @JsonProperty(value = "value")
    private String value;

    @JsonProperty(value = "action")
    private CardAction action;

    @JsonProperty(value = "synonyms")
    private List<String> synonyms;

    /**
     * Creates a Choice.
     */
    public Choice() {
        this(null);
    }

    /**
     * Creates a Choice.
     * @param withValue The value.
     */
    public Choice(String withValue) {
        value = withValue;
    }

    /**
     * Creates a Choice.
     * @param withValue The value.
     * @param withSynonyms The list of synonyms to recognize in addition to the value.
     */
    public Choice(String withValue, List<String> withSynonyms) {
        value = withValue;
        synonyms = withSynonyms;
    }

    /**
     * Creates a Choice.
     * @param withValue The value.
     * @param withSynonyms The list of synonyms to recognize in addition to the value.
     */
    public Choice(String withValue, String... withSynonyms) {
        value = withValue;
        synonyms = Arrays.asList(withSynonyms);
    }

    /**
     * Gets the value to return when selected.
     * @return The value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value to return when selected.
     * @param withValue The value to return.
     */
    public void setValue(String withValue) {
        value = withValue;
    }

    /**
     * Gets the action to use when rendering the choice as a suggested action or hero card.
     * @return The action to use.
     */
    public CardAction getAction() {
        return action;
    }

    /**
     * Sets the action to use when rendering the choice as a suggested action or hero card.
     * @param withAction The action to use.
     */
    public void setAction(CardAction withAction) {
        action = withAction;
    }

    /**
     * Gets the list of synonyms to recognize in addition to the value. This is optional.
     * @return The list of synonyms.
     */
    public List<String> getSynonyms() {
        return synonyms;
    }

    /**
     * Sets the list of synonyms to recognize in addition to the value. This is optional.
     * @param withSynonyms The list of synonyms.
     */
    public void setSynonyms(List<String> withSynonyms) {
        synonyms = withSynonyms;
    }
}
