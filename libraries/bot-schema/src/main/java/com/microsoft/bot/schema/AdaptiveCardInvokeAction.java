// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the structure that arrives in the Activity.getValue().Action for
 * Invoke activity with Name of 'adaptiveCard/action'.
 */
public class AdaptiveCardInvokeAction {

    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    @JsonProperty(value = "verb")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String verb;

    @JsonProperty(value = "data")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object data;

    /**
     * Gets the Type of this adaptive card action invoke.
     * @return the Type value as a String.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the Type of this adaptive card action invoke.
     * @param withType The Type value.
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * Gets the Id of this adaptive card action invoke.
     * @return the Id value as a String.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the Id of this adaptive card action invoke.
     * @param withId The Id value.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Gets the Verb of this adaptive card action invoke.
     * @return the Verb value as a String.
     */
    public String getVerb() {
        return this.verb;
    }

    /**
     * Sets the Verb of this adaptive card action invoke.
     * @param withVerb The Verb value.
     */
    public void setVerb(String withVerb) {
        this.verb = withVerb;
    }

    /**
     * Gets the Data of this adaptive card action invoke.
     * @return the Data value as a Object.
     */
    public Object getData() {
        return this.data;
    }

    /**
     * Sets the Data of this adaptive card action invoke.
     * @param withData The Data value.
     */
    public void setData(Object withData) {
        this.data = withData;
    }

}
