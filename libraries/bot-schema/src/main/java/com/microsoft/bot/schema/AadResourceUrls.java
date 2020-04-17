// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import java.util.Arrays;
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
     * Construct with var args or String[].
     * 
     * @param withResourceUrl Array of urls.
     */
    public AadResourceUrls(String... withResourceUrl) {
        resourceUrls = Arrays.asList(withResourceUrl);
    }

    /**
     * Construct with List of urls.
     * 
     * @param withResourceUrls List of urls.
     */
    public AadResourceUrls(List<String> withResourceUrls) {
        resourceUrls = withResourceUrls;
    }

    /**
     * Get the resourceUrls value.
     *
     * @return the resourceUrls value
     */
    public List<String> getResourceUrls() {
        return resourceUrls;
    }

    /**
     * Set the resourceUrls value.
     *
     * @param withResourceUrls the resourceUrls value to set
     */
    public void setResourceUrls(List<String> withResourceUrls) {
        resourceUrls = withResourceUrls;
    }
}
