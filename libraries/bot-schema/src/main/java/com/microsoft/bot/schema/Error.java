// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representing error information.
 */
public class Error {
    @JsonProperty(value = "code")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String code;

    @JsonProperty(value = "message")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String message;

    @JsonProperty(value = "innerHttpError")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private InnerHttpError innerHttpError;

    /**
     * Get the code value.
     * 
     * @return the code value
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Set the code value.
     * 
     * @param withCode the code value to set
     */
    public void setCode(String withCode) {
        this.code = withCode;
    }

    /**
     * Get the message value.
     * 
     * @return the message value
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message value.
     * 
     * @param withMessage the message value to set
     */
    public void setMessage(String withMessage) {
        this.message = withMessage;
    }

    /**
     * Gets error from inner http call.
     * 
     * @return The InnerHttpError.
     */
    public InnerHttpError getInnerHttpError() {
        return this.innerHttpError;
    }

    /**
     * Sets error from inner http call.
     * 
     * @param withInnerHttpError The InnerHttpError.
     */
    public void setInnerHttpError(InnerHttpError withInnerHttpError) {
        this.innerHttpError = withInnerHttpError;
    }
}
