// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the structure that is returned as the result of a health check on the bot.
 * The health check is sent to the bot as an {@link Activity} of type "invoke" and this class along
 * with {@link HealthResults} defines the structure of the body of the response.
 * The name of the invoke Activity is "healthCheck".
 */
public class HealthCheckResponse {
    /**
     * The health check results.
     */
    @JsonProperty(value = "healthResults")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private HealthResults healthResults;

    /**
     * Gets the healthResults value.
     *
     * @return The healthResults value.
     */
    public HealthResults getHealthResults() {
        return this.healthResults;
    }

    /**
     * Sets the healthResults value.
     *
     * @param withHealthResults The healthResults value to set.
     */
    public void setHealthResults(HealthResults withHealthResults) {
        this.healthResults = withHealthResults;
    }
}
