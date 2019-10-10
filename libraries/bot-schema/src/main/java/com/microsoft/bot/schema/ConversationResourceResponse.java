// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A response containing a resource.
 */
public class ConversationResourceResponse {
    /**
     * ID of the Activity (if sent).
     */
    @JsonProperty(value = "activityId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String activityId;

    /**
     * Service endpoint where operations concerning the conversation may be
     * performed.
     */
    @JsonProperty(value = "serviceUrl")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String serviceUrl;

    /**
     * Id of the resource.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Get the activityId value.
     *
     * @return the activityId value
     */
    public String getActivityId() {
        return this.activityId;
    }

    /**
     * Set the activityId value.
     *
     * @param withActivityId the activityId value to set
     */
    public void setActivityId(String withActivityId) {
        this.activityId = withActivityId;
    }

    /**
     * Get the serviceUrl value.
     *
     * @return the serviceUrl value
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * Set the serviceUrl value.
     *
     * @param withServiceUrl the serviceUrl value to set
     */
    public void setServiceUrl(String withServiceUrl) {
        this.serviceUrl = withServiceUrl;
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param withId the id value to set
     */
    public void setId(String withId) {
        this.id = withId;
    }
}
