/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Attachment View name and size.
 */
public class AttachmentView {
    /**
     * Id of the attachment.
     */
    @JsonProperty(value = "viewId")
    private String viewId;

    /**
     * Size of the attachment.
     */
    @JsonProperty(value = "size")
    private Integer size;

    /**
     * Get the viewId value.
     *
     * @return the viewId value
     */
    public String getViewId() {
        return this.viewId;
    }

    /**
     * Set the viewId value.
     *
     * @param withViewId the viewId value to set
     */
    public void setViewId(String withViewId) {
        this.viewId = withViewId;
    }

    /**
     * Get the size value.
     *
     * @return the size value
     */
    public Integer getSize() {
        return this.size;
    }

    /**
     * Set the size value.
     *
     * @param withSize the size value to set
     */
    public void setSize(Integer withSize) {
        this.size = withSize;
    }
}
