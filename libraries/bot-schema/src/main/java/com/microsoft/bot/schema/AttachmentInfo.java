// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    /**
     * ContentType of the attachment.
     */
    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    /**
     * attachment views.
     */
    @JsonProperty(value = "views")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
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
