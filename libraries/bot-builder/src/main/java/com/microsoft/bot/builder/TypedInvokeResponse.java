// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

/**
 * Tuple class containing an HTTP Status Code and a JSON Serializable object.
 * The HTTP Status code is, in the invoke activity scenario, what will be set in
 * the resulting POST. The Body of the resulting POST will be the JSON
 * Serialized content from the Body property.
 * @param <T> The type for the body of the TypedInvokeResponse.
 */
@Deprecated // Use the one of bot-schema
public class TypedInvokeResponse<T> extends com.microsoft.bot.schema.TypedInvokeResponse {

    /**
     * Initializes new instance of InvokeResponse.
     *
     * @param withStatus The invoke response status.
     * @param withBody   The invoke response body.
     */
    public TypedInvokeResponse(int withStatus, T withBody) {
        super(withStatus, withBody);
    }
}
