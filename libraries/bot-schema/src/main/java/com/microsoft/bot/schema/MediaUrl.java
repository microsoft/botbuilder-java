/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Media URL.
 */
public class MediaUrl {
    /**
     * Url for the media.
     */
    @JsonProperty(value = "url")
    private String url;

    /**
     * Optional profile hint to the client to differentiate multiple MediaUrl
     * objects from each other.
     */
    @JsonProperty(value = "profile")
    private String profile;

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
