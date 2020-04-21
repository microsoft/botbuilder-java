// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A response containing a resource ID.
 */
public class ResourceResponse {
    /**
     * Id of the resource.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Empty ResourceResponse.
     */
    public ResourceResponse() {

    }

    /**
     * ResourceResponse with ID.
     * 
     * @param withId The id.
     */
    public ResourceResponse(String withId) {
        this.id = withId;
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param withId the id value to set
     */
    public void setId(String withId) {
        this.id = withId;
    }
}
