/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representing inner http error.
 */
public class InnerHttpError {
    /**
     * HttpStatusCode from failed request.
     */
    @JsonProperty(value = "statusCode")
    private int statusCode;

    /**
     * Body from failed request.
     */
    @JsonProperty(value = "body")
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
     * @param withStatusCode
     */
    public void setStatusCode(int withStatusCode) {
        this.statusCode = withStatusCode;
    }

    /**
     * Gets Body from failed request.
     */
    public Object getBody() {
        return this.body;
    }

    /**
     * Sets Body from failed request.
     */
    public void setBody(Object withBody) {
        this.body = withBody;
    }
}
