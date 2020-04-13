// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card OpenUri target.
 */
public class O365ConnectorCardOpenUriTarget {
    @JsonProperty(value = "os")
    private String os;

    @JsonProperty(value = "uri")
    private String uri;

    /**
     * Gets target operating system. Possible values include: 'default', 'iOS',
     * 'android', 'windows'
     * 
     * @return The target os.
     */
    public String getOs() {
        return os;
    }

    /**
     * Sets target operating system. Possible values include: 'default', 'iOS',
     * 'android', 'windows'
     * 
     * @param withOs The target os.
     */
    public void setOs(String withOs) {
        os = withOs;
    }

    /**
     * Gets the target uri.
     * 
     * @return The target uri.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the target uri.
     * 
     * @param withUri The target uri.
     */
    public void setUri(String withUri) {
        uri = withUri;
    }
}
