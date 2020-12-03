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

    @JsonProperty(value = "alertInMeeting")
    private Boolean alertInMeeting;

    @JsonProperty(value = "externalResourceUrl")
    private String externalResourceUrl;

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
     * Indicates if this is a meeting alert.
     * @return True if this is a meeting alert.
     */
    public Boolean getAlertInMeeting() {
        return alertInMeeting;
    }

    /**
     * Indicates if this is a meeting alert.
     * @param withAlertInMeeting True if this is a meeting alert.
     */
    public void setAlertInMeeting(Boolean withAlertInMeeting) {
        alertInMeeting = withAlertInMeeting;
    }

    /**
     * Gets the resource Url of a meeting alert.
     * @return The external resource url.
     */
    public String getExternalResourceUrl() {
        return externalResourceUrl;
    }

    /**
     * The resource Url of a meeting alert.
     * @param withExternalResourceUrl The external resource Url.
     */
    public void setExternalResourceUrl(String withExternalResourceUrl) {
        externalResourceUrl = withExternalResourceUrl;
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
     * A new instance of a meeting alert.
     * @param withAlertInMeeting True if this is a meeting alert.
     * @param withExternalResourceUrl  The external resource Url.
     */
    public NotificationInfo(boolean withAlertInMeeting, String withExternalResourceUrl) {
        setAlert(true);
        setAlertInMeeting(withAlertInMeeting);
        setExternalResourceUrl(withExternalResourceUrl);
    }

    /**
     * A new instance of NotificationInfo.
     */
    public NotificationInfo() {
    }
}
