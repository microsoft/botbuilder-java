// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Invoke request body type for app-based link query.
 */
public class AppBasedLinkQuery {
    @JsonProperty(value = "url")
    private String url;

    @JsonProperty(value = "state")
    private String state;

    /**
     * Initializes a new empty instance of the AppBasedLinkQuery class.
     */
    public AppBasedLinkQuery() {

    }

    /**
     * Initializes a new instance of the AppBasedLinkQuery class.
     * 
     * @param withUrl The query url.
     */
    public AppBasedLinkQuery(String withUrl) {
        url = withUrl;
    }

    /**
     * Gets url queried by user.
     * 
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets url queried by user.
     * 
     * @param withUrl The url.
     */
    public void setUrl(String withUrl) {
        url = withUrl;
    }

    /**
     * Gets the magic code for OAuth Flow.
     *
     * @return The state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the magic code for OAuth Flow.
     *
     * @param withState The state.
     */
    public void setState(String withState) {
        state = withState;
    }
}
