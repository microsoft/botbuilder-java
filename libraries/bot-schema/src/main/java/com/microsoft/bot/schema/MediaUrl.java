// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Media URL.
 */
public class MediaUrl {
    /**
     * Url for the media.
     */
    @JsonProperty(value = "url")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String url;

    /**
     * Optional profile hint to the client to differentiate multiple MediaUrl
     * objects from each other.
     */
    @JsonProperty(value = "profile")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String profile;

    /**
     * Creates a new MediaUrl.
     */
    public MediaUrl() {
    }

    /**
     * Creates a new MediaUrl.
     *
     * @param withUrl The url value.
     */
    public MediaUrl(String withUrl) {
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
     * Get the profile value.
     *
     * @return the profile value
     */
    public String getProfile() {
        return this.profile;
    }

    /**
     * Set the profile value.
     *
     * @param withProfile the profile value to set
     */
    public void setProfile(String withProfile) {
        this.profile = withProfile;
    }
}
