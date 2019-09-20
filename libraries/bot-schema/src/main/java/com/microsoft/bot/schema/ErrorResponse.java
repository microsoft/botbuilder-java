/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An HTTP API response.
 */
public class ErrorResponse {
    public ErrorResponse() {

    }

    /**
     * Error message.
     */
    @JsonProperty(value = "error")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Error error;

    public ErrorResponse(Error withError) {
        this.error = withError;
    }

    /**
     * Get the error value.
     *
     * @return the error value
     */
    public Error getError() {
        return this.error;
    }

    /**
     * Set the error value.
     *
     * @param withError the error value to set
     */
    public void setError(Error withError) {
        this.error = withError;
    }
}
