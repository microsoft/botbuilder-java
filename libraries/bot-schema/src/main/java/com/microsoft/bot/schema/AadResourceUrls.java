/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The AadResourceUrls model.
 */
public class AadResourceUrls {
    /**
     * The resourceUrls property.
     */
    @JsonProperty(value = "resourceUrls")
    private List<String> resourceUrls;

    /**
     * Get the resourceUrls value.
     *
     * @return the resourceUrls value
     */
    public List<String> resourceUrls() {
        return this.resourceUrls;
    }

    /**
     * Set the resourceUrls value.
     *
     * @param resourceUrls the resourceUrls value to set
     * @return the AadResourceUrls object itself.
     */
    public AadResourceUrls withResourceUrls(List<String> resourceUrls) {
        this.resourceUrls = resourceUrls;
        return this;
    }

}
