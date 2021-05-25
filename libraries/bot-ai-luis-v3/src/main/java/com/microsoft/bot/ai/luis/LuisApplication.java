// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import okhttp3.HttpUrl;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Luis Application representation with information necessary to query the
 * specific Luis Application. Data describing a LUIS application.
 *
 */
public class LuisApplication {

    /**
     * LUIS application ID.
     */
    private String applicationId;

    /**
     * LUIS subscription or endpoint key.
     */
    private String endpointKey;

    /**
     * LUIS endpoint like https://westus.api.cognitive.microsoft.com.
     */
    private String endpoint;

    /**
     * Initializes a new instance of the Luis Application class.
     */
    public LuisApplication() {
    }

    /**
     * Initializes a new instance of the Luis Application class.
     *
     * @param applicationId Luis Application ID to query
     * @param endpointKey   LUIS subscription or endpoint key.
     * @param endpoint      LUIS endpoint to use like
     *                      https://westus.api.cognitive.microsoft.com
     */
    public LuisApplication(String applicationId, String endpointKey, String endpoint) {
        setLuisApplication(applicationId, endpointKey, endpoint);
    }

    /**
     * Initializes a new instance of the Luis Application class.
     *
     * @param applicationEndpoint LUIS application query endpoint containing
     *                            subscription key and application id as part of the
     *                            url.
     */
    public LuisApplication(String applicationEndpoint) {
        parse(applicationEndpoint);
    }

    /**
     * Sets Luis application ID to query.
     *
     * @param applicationId Luis application ID to query.
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Gets LUIS application ID.
     *
     * @return LUIS application ID.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the LUIS subscription or endpoint key.
     *
     * @param endpointKey LUIS subscription or endpoint key.
     */
    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }

    /**
     * Gets the LUIS subscription or endpoint key.
     *
     * @return LUIS subscription or endpoint key.
     */
    public String getEndpointKey() {
        return endpointKey;
    }

    /**
     * Sets LUIS endpoint like https://westus.api.cognitive.microsoft.com.
     *
     * @param endpoint LUIS endpoint where application is hosted.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the LUIS endpoint where application is hosted.
     *
     * @return LUIS endpoint where application is hosted.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Helper method to set and validate Luis arguments passed.
     */
    private void setLuisApplication(String applicationId, String endpointKey, String endpoint) {

        if (!isValidUUID(applicationId)) {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS application id.", applicationId));
        }

        if (!isValidUUID(endpointKey)) {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS subscription key.", endpointKey));
        }

        if (StringUtils.isBlank(endpoint)) {
            endpoint = "https://westus.api.cognitive.microsoft.com";
        }

        if (!isValidURL(endpoint)) {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS endpoint.", endpoint));
        }

        this.applicationId = applicationId;
        this.endpointKey = endpointKey;
        this.endpoint = endpoint;
    }

    /**
     * Helper method to parse validate and set Luis application members from the
     * full application full endpoint.
     */
    private void parse(String applicationEndpoint) {
        String appId = "";
        try {
            String[] segments = new URL(applicationEndpoint).getPath().split("/");
            for (int segment = 0; segment < segments.length - 1; segment++) {
                if (segments[segment].equals("apps")) {
                    appId = segments[segment + 1].trim();
                    break;
                }
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to create the LUIS endpoint with the given %s.", applicationEndpoint));
        }

        if (appId.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Could not find application Id in %s", applicationEndpoint));
        }

        try {
            String endpointKeyParsed = HttpUrl.parse(applicationEndpoint).queryParameterValues("subscription-key")
                    .stream().findFirst().orElse("");

            String endpointPared = String.format("%s://%s", new URL(applicationEndpoint).getProtocol(),
                    new URL(applicationEndpoint).toURI().getHost());

            setLuisApplication(appId, endpointKeyParsed, endpointPared);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to create the LUIS endpoint with the given %s.", applicationEndpoint));
        }

    }

    private boolean isValidUUID(String uuid) {
        try {
            if (!uuid.contains("-")) {
                uuid = uuid.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5");
            }

            return UUID.fromString(uuid).toString().equals(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidURL(String uri) {
        try {
            return new URL(uri).toURI().isAbsolute();
        } catch (URISyntaxException | MalformedURLException exception) {
            return false;
        }
    }
}
