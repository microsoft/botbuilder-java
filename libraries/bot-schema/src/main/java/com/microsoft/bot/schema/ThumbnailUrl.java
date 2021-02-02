// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Thumbnail URL.
 */
public class ThumbnailUrl {
    /**
     * URL pointing to the thumbnail to use for media content.
     */
    @JsonProperty(value = "url")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String url;

    /**
     * HTML alt text to include on this thumbnail image.
     */
    @JsonProperty(value = "alt")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String alt;

    /**
     * Creates a new ThumbnailUrl.
     */
    public ThumbnailUrl() {
    }

    /**
     * Creates a new ThumbnailUrl.
     * @param withUrl The url value to set.
     */
    public ThumbnailUrl(String withUrl) {
        url = withUrl;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param withUrl the url value to set
     */
    public void setUrl(String withUrl) {
        this.url = withUrl;
    }

    /**
     * Get the alt value.
     *
     * @return the alt value
     */
    public String getAlt() {
        return this.alt;
    }

    /**
     * Set the alt value.
     *
     * @param withAlt the alt value to set
     */
    public void setAlt(String withAlt) {
        this.alt = withAlt;
    }
}
