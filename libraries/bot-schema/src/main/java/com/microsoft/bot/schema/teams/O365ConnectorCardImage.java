// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card image.
 */
public class O365ConnectorCardImage {
    @JsonProperty(value = "image")
    private String image;

    @JsonProperty(value = "title")
    private String title;

    /**
     * Gets the URL for the image.
     * 
     * @return The image url.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the URL for the image.
     * 
     * @param withImage The image url.
     */
    public void setImage(String withImage) {
        image = withImage;
    }

    /**
     * Gets the alternative text for the image.
     * 
     * @return The image alt text.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the alternative text for the image.
     * 
     * @param withTitle The image alt text.
     */
    public void setTitle(String withTitle) {
        title = withTitle;
    }
}
