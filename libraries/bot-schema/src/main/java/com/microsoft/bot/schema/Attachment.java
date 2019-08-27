/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * An attachment within an activity.
 */
public class Attachment {
    /**
     * mimetype/Contenttype for the file.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /**
     * Content Url.
     */
    @JsonProperty(value = "contentUrl")
    private String contentUrl;

    /**
     * Embedded content.
     */
    @JsonProperty(value = "content")
    private Object content;

    /**
     * (OPTIONAL) The name of the attachment.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * (OPTIONAL) Thumbnail associated with attachment.
     */
    @JsonProperty(value = "thumbnailUrl")
    private String thumbnailUrl;
    /**
     * Holds the overflow properties that aren't first class
     * properties in the object.  This allows extensibility
     * while maintaining the object.
     */
    private HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param withContentType the contentType value to set
     */
    public void setContentType(String withContentType) {
        this.contentType = withContentType;
    }

    /**
     * Get the contentUrl value.
     *
     * @return the contentUrl value
     */
    public String getContentUrl() {
        return this.contentUrl;
    }

    /**
     * Set the contentUrl value.
     *
     * @param withContentUrl the contentUrl value to set
     */
    public void setContentUrl(String withContentUrl) {
        this.contentUrl = withContentUrl;
    }

    /**
     * Get the content value.
     *
     * @return the content value
     */
    public Object getContent() {
        return this.content;
    }

    /**
     * Set the content value.
     *
     * @param withContent the content value to set
     */
    public void setContent(Object withContent) {
        this.content = withContent;
    }

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
     * Get the thumbnailUrl value.
     *
     * @return the thumbnailUrl value
     */
    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    /**
     * Set the thumbnailUrl value.
     *
     * @param withThumbnailUrl the thumbnailUrl value to set
     */
    public void setThumbnailUrl(String withThumbnailUrl) {
        this.thumbnailUrl = withThumbnailUrl;
    }

    /**
     * Overflow properties.
     * Properties that are not modelled as first class properties in the object are accessible here.
     * Note: A property value can be be nested.
     *
     * @return A Key-Value map of the properties
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * Set overflow properties.
     *
     * @param key   Key for the property
     * @param value JsonNode of value (can be nested)
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }
}
