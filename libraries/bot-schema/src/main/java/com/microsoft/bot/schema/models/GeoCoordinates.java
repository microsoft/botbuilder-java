/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.EntityImpl;

/**
 * GeoCoordinates (entity type: "https://schema.org/GeoCoordinates").
 */
public class GeoCoordinates extends EntityImpl {
    /**
     * Elevation of the location [WGS
     * 84](https://en.wikipedia.org/wiki/World_Geodetic_System).
     */
    @JsonProperty(value = "elevation")
    private Double elevation;

    /**
     * Latitude of the location [WGS
     * 84](https://en.wikipedia.org/wiki/World_Geodetic_System).
     */
    @JsonProperty(value = "latitude")
    private Double latitude;

    /**
     * Longitude of the location [WGS
     * 84](https://en.wikipedia.org/wiki/World_Geodetic_System).
     */
    @JsonProperty(value = "longitude")
    private Double longitude;

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
     * Get the elevation value.
     *
     * @return the elevation value
     */
    public Double elevation() {
        return this.elevation;
    }

    /**
     * Set the elevation value.
     *
     * @param elevation the elevation value to set
     * @return the GeoCoordinates object itself.
     */
    public GeoCoordinates withElevation(Double elevation) {
        this.elevation = elevation;
        return this;
    }

    /**
     * Get the latitude value.
     *
     * @return the latitude value
     */
    public Double latitude() {
        return this.latitude;
    }

    /**
     * Set the latitude value.
     *
     * @param latitude the latitude value to set
     * @return the GeoCoordinates object itself.
     */
    public GeoCoordinates withLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    /**
     * Get the longitude value.
     *
     * @return the longitude value
     */
    public Double longitude() {
        return this.longitude;
    }

    /**
     * Set the longitude value.
     *
     * @param longitude the longitude value to set
     * @return the GeoCoordinates object itself.
     */
    public GeoCoordinates withLongitude(Double longitude) {
        this.longitude = longitude;
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
     * @return the GeoCoordinates object itself.
     */
    public GeoCoordinates withType(String type) {
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
     * @return the GeoCoordinates object itself.
     */
    public GeoCoordinates withName(String name) {
        this.name = name;
        return this;
    }

}
