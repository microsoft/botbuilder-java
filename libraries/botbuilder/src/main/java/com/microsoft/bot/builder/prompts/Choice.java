// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder.prompts;

import com.microsoft.bot.schema.models.CardAction;

import java.util.ArrayList;

public class Choice
{
    /**
     * Value to return when selected.
     */
    String _value;
    public void setValue(String value) {
        this._value = value;
    }
    public String getValue() {
        return this._value;
    }

    /**
     * (Optional) action to use when rendering the choice as a suggested action.
     */
    CardAction _action;
    public CardAction getAction() {
        return this._action;
    }
    public void setAction(CardAction action) {
        this._action = action;
    }

    /**
     * (Optional) list of synonyms to recognize in addition to the value.
     */
    ArrayList<String> _synonyms;
    public ArrayList<String> getSynonyms() {
        return _synonyms;
    }
    public void setSynonyms(ArrayList<String> synonyms) {
        this._synonyms = synonyms;
    }
}
