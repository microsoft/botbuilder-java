// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specifies if a notification is to be sent for the mentions.
 */
public class NotificationInfo {
    /**
     * Gets or sets true if notification is to be sent to the user, false.
     */
    @JsonProperty(value = "alert")
    private Boolean alert;

    /**
     * Initialize new NotificationInfo.
     * @param withAlert initial alert value.
     */
    public NotificationInfo(boolean withAlert) {
        setAlert(withAlert);
    }

    /**
     * Getter for alert.
     *
     * @return return the alter value.
     */
    public Boolean getAlert() {
        return alert;
    }

    /**
     * Setter for alert.
     *
     * @param withAlert the value to set.
     */
    public void setAlert(Boolean withAlert) {
        alert = withAlert;
    }

    /**
     * A new instance of NotificationInfo.
     *
     * @param withAlert alert value to set.
     */
    public NotificationInfo(Boolean withAlert) {
        alert = withAlert;
    }

    /**
     * A new instance of NotificationInfo.
     */
    public NotificationInfo() {
    }
}
