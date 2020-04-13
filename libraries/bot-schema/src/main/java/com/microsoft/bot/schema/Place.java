// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Place (entity type: "https://schema.org/Place").
 */
public class Place implements EntitySerialization {
    /**
     * Address of the place (may be `string` or complex object of type
     * `PostalAddress`).
     */
    @JsonProperty(value = "address")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object address;

    /**
     * Geo coordinates of the place (may be complex object of type `GeoCoordinates`
     * or `GeoShape`).
     */
    @JsonProperty(value = "geo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object geo;

    /**
     * Map to the place (may be `string` (URL) or complex object of type `Map`).
     */
    @JsonProperty(value = "hasMap")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object hasMap;

    /**
     * The type of the thing.
     */
    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    /**
     * The name of the thing.
     */
    @JsonProperty(value = "name")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    /**
     * Place of type "Place".
     */
    public Place() {
        this.type = "Place";
    }

    /**
     * Get the address value.
     *
     * @return the address value
     */
    public Object getAddress() {
        return this.address;
    }

    /**
     * Set the address value.
     *
     * @param withAddress the address value to set
     */
    public void setAddress(Object withAddress) {
        this.address = withAddress;
    }

    /**
     * Get the geo value.
     *
     * @return the geo value
     */
    public Object getGeo() {
        return this.geo;
    }

    /**
     * Set the geo value.
     *
     * @param withGeo the geo value to set
     */
    public void setGeo(Object withGeo) {
        this.geo = withGeo;
    }

    /**
     * Get the hasMap value.
     *
     * @return the hasMap value
     */
    public Object getHasMap() {
        return this.hasMap;
    }

    /**
     * Set the hasMap value.
     *
     * @param withHasMap the hasMap value to set
     */
    public void setHasMap(Object withHasMap) {
        this.hasMap = withHasMap;
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
}
