// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representing inner http error.
 */
public class InnerHttpError {
    @JsonProperty(value = "statusCode")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private int statusCode;

    @JsonProperty(value = "body")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object body;

    /**
     * Gets HttpStatusCode from failed request.
     *
     * @return the statusCode value
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Sets HttpStatusCode from failed request.
     *
     * @param withStatusCode The HTTP status code.
     */
    public void setStatusCode(int withStatusCode) {
        this.statusCode = withStatusCode;
    }

    /**
     * Gets Body from failed request.
     * 
     * @return the body of the error.
     */
    public Object getBody() {
        return this.body;
    }

    /**
     * Sets Body from failed request.
     * 
     * @param withBody The body of the error.
     */
    public void setBody(Object withBody) {
        this.body = withBody;
    }
}
