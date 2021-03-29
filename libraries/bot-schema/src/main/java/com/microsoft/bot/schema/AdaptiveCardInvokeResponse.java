// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the structure that is returned as the result of an Invoke activity
 * with Name of 'adaptiveCard/action'.
 */
public class AdaptiveCardInvokeResponse {

    @JsonProperty(value = "statusCode")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private int statusCode;

    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object value;

    /**
     * Gets the Card Action response getStatusCode().
     * @return the StatusCode value as a int.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Sets the Card Action response getStatusCode().
     * @param withStatusCode The StatusCode value.
     */
    public void setStatusCode(int withStatusCode) {
        this.statusCode = withStatusCode;
    }

    /**
     * Gets the Type of this {@link AdaptiveCardInvokeResponse} .
     * @return the Type value as a String.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the Type of this {@link AdaptiveCardInvokeResponse} .
     * @param withType The Type value.
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * Gets the json response Object.
     * @return the Value value as a Object.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the json response Object.
     * @param withValue The Value value.
     */
    public void setValue(Object withValue) {
        this.value = withValue;
    }

}
