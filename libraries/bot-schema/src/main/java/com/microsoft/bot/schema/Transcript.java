// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A collection of Activities that conforms to the Transcript schema.
 */
public class Transcript {
    @JsonProperty(value = "activities")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Activity> activities;

    /**
     * Gets collection of Activities that conforms to the Transcript schema.
     *
     * @return the activities value
     */
    public List<Activity> getActivities() {
        return this.activities;
    }

    /**
     * Sets collection of Activities that conforms to the Transcript schema.
     *
     * @param withActivities the activities value to set
     */
    public void setActivities(List<Activity> withActivities) {
        this.activities = withActivities;
    }
}
