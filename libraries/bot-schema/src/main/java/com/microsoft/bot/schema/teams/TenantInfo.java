// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a tenant.
 */
public class TenantInfo {
    /**
     * Unique identifier representing a tenant.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * Get Unique identifier representing a tenant.
     *
     * @return Unique identifier representing a tenant.
     */
    public String getId() {
        return id;
    }

    /**
     * Set Unique identifier representing a tenant.
     *
     * @param withId Unique identifier representing a tenant.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * New instance of TenantInfo.
     *
     * @param withId Unique identifier representing a tenant.
     */
    public TenantInfo(String withId) {
        this.id = withId;
    }

    /**
     * New instance of TenantInfo.
     */
    public TenantInfo() {
    }
}
