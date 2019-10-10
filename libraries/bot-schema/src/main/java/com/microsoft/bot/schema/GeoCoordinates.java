// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeoCoordinates (entity type: "https://schema.org/GeoCoordinates").
 */
public class GeoCoordinates implements EntitySerialization {
    /**
     * Elevation of the location [WGS
     * 84](https://en.wikipedia.org/wiki/World_Geodetic_System).
     */
    @JsonProperty(value = "elevation")
    private double elevation;

    /**
     * Latitude of the location [WGS
     * 84](https://en.wikipedia.org/wiki/World_Geodetic_System).
     */
    @JsonProperty(value = "latitude")
    private double latitude;

    /**
     * Longitude of the location [WGS
     * 84](https://en.wikipedia.org/wiki/World_Geodetic_System).
     */
    @JsonProperty(value = "longitude")
    private double longitude;

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
     * GeoCoordinates of type "GeoCoordinates".
     */
    public GeoCoordinates() {
        this.type = "GeoCoordinates";
    }

    /**
     * Get the elevation value.
     *
     * @return the elevation value
     */
    public Double getElevation() {
        return this.elevation;
    }

    /**
     * Set the elevation value.
     *
     * @param withElevation the elevation value to set
     */
    public void setElevation(double withElevation) {
        this.elevation = withElevation;
    }

    /**
     * Get the latitude value.
     *
     * @return the latitude value
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Set the latitude value.
     *
     * @param withLatitude the latitude value to set
     */
    public void setLatitude(double withLatitude) {
        this.latitude = withLatitude;
    }

    /**
     * Get the longitude value.
     *
     * @return the longitude value
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Set the longitude value.
     *
     * @param withLongitude the longitude value to set
     */
    public void setLongitude(double withLongitude) {
        this.longitude = withLongitude;
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
