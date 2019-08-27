/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Metdata for an attachment.
 */
public class AttachmentInfo {
    /**
     * Name of the attachment.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * ContentType of the attachment.
     */
    @JsonProperty(value = "type")
    private String type;

    /**
     * attachment views.
     */
    @JsonProperty(value = "views")
    private List<AttachmentView> views;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param withName the name value to set
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param withType the type value to set
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * Get the views value.
     *
     * @return the views value
     */
    public List<AttachmentView> getViews() {
        return this.views;
    }

    /**
     * Set the views value.
     *
     * @param withViews the views value to set
     */
    public void setViews(List<AttachmentView> withViews) {
        this.views = withViews;
    }
}
