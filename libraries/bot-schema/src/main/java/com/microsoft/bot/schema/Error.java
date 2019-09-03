/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representing error information.
 */
public class Error {
    /**
     * Error code.
     */
    @JsonProperty(value = "code")
    private String code;

    /**
     * Error message.
     */
    @JsonProperty(value = "message")
    private String message;

    /**
     * Error from inner http call
     */
    @JsonProperty(value = "innerHttpError")
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
     */
    public InnerHttpError getInnerHttpError() {
        return this.innerHttpError;
    }

    /**
     * Sets error from inner http call.
     */
    public void setInnerHttpError(InnerHttpError withInnerHttpError) {
        this.innerHttpError = withInnerHttpError;
    }
}
