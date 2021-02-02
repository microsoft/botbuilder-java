// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;

/**
 * Replies in response to DeliveryModes.EXPECT_REPLIES.
 */
public class ExpectedReplies {
    @JsonProperty(value = "activities")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Activity> activities;

    /**
     * Create an instance of ExpectReplies.
     */
    public ExpectedReplies() {

    }

    /**
     * Create an instance of ExpectReplies.
     * @param withActivities The collection of activities that conforms to the
     *                       ExpectedREplies schema.
     */
    public ExpectedReplies(List<Activity> withActivities) {
        activities = withActivities;
    }

    /**
     * Create an instance of ExpectReplies.
     * @param withActivities The array of activities that conforms to the
     *                       ExpectedREplies schema.
     */
    public ExpectedReplies(Activity... withActivities) {
        this(Arrays.asList(withActivities));
    }

    /**
     * Gets collection of Activities that conforms to the ExpectedReplies schema.
     * @return The collection of activities that conforms to the ExpectedREplies schema.
     */
    public List<Activity> getActivities() {
        return activities;
    }

    /**
     * Sets collection of Activities that conforms to the ExpectedReplies schema.
     * @param withActivities The collection of activities that conforms to the
     *                       ExpectedREplies schema.
     */
    public void setActivities(List<Activity> withActivities) {
        activities = withActivities;
    }
}
