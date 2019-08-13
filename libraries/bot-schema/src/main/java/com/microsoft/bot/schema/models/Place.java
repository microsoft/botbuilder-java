/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 */

package com.microsoft.bot.schema.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.EntityImpl;

/**
 * Place (entity type: "https://schema.org/Place").
 */
public class Place extends EntityImpl {
    /**
     * Address of the place (may be `string` or complex object of type
     * `PostalAddress`).
     */
    @JsonProperty(value = "address")
    private Object address;

    /**
     * Geo coordinates of the place (may be complex object of type
     * `GeoCoordinates` or `GeoShape`).
     */
    @JsonProperty(value = "geo")
    private Object geo;

    /**
     * Map to the place (may be `string` (URL) or complex object of type
     * `Map`).
     */
    @JsonProperty(value = "hasMap")
    private Object hasMap;

    /**
     * The type of the thing.
     */
    @JsonProperty(value = "type")
    private String type;

    /**
     * The name of the thing.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Get the address value.
     *
     * @return the address value
     */
    public Object address() {
        return this.address;
    }

    /**
     * Set the address value.
     *
     * @param address the address value to set
     * @return the Place object itself.
     */
    public Place withAddress(Object address) {
        this.address = address;
        return this;
    }

    /**
     * Get the geo value.
     *
     * @return the geo value
     */
    public Object geo() {
        return this.geo;
    }

    /**
     * Set the geo value.
     *
     * @param geo the geo value to set
     * @return the Place object itself.
     */
    public Place withGeo(Object geo) {
        this.geo = geo;
        return this;
    }

    /**
     * Get the hasMap value.
     *
     * @return the hasMap value
     */
    public Object hasMap() {
        return this.hasMap;
    }

    /**
     * Set the hasMap value.
     *
     * @param hasMap the hasMap value to set
     * @return the Place object itself.
     */
    public Place withHasMap(Object hasMap) {
        this.hasMap = hasMap;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the Place object itself.
     */
    public Place withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the Place object itself.
     */
    public Place withName(String name) {
        this.name = name;
        return this;
    }

}
