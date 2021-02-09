// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class LuisApplication {

    /**
     * Luis application ID.
     */
    private String applicationId;

    /**
     * Luis subscription or endpoint key.
     */
    private String endpointKey;

    /**
     * Luis subscription or endpoint key.
     */
    private String endpoint;

    /**
     *  Luis endpoint like https://westus.api.cognitive.microsoft.com.
     */
    public LuisApplication() {
    }

    /**
     * Initializes a new instance of the Luis Application class.
     * @param applicationId Luis Application ID to query
     * @param endpointKey LUIS subscription or endpoint key.
     * @param endpoint LUIS endpoint to use like https://westus.api.cognitive.microsoft.com
     */
    public LuisApplication(
        String applicationId,
        String endpointKey,
        String endpoint) {
        setLuisApplication(
            applicationId,
            endpointKey,
            endpoint);
    }

    /**
     * Initializes a new instance of the Luis Application class.
     * @param applicationEndpoint LUIS application query endpoint containing subscription key
     *                            and application id as part of the url.
     */
    public LuisApplication(
        String applicationEndpoint) {
        parse(applicationEndpoint);
    }

    /**
     * Sets Luis application ID to query.
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Gets Luis application ID.
     * @return applicationId.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the LUIS subscription or endpoint key.
     */
    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }

    /**
     * Gets the LUIS subscription or endpoint key.
     * @return endpointKey.
     */
    public String getEndpointKey() {
        return endpointKey;
    }

    /**
     * Sets Luis endpoint like https://westus.api.cognitive.microsoft.com.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the LUIS endpoint where application is hosted.
     * @return endpoint.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Helper method to set and validate Luis arguments passed.
     */
    private void setLuisApplication (
        String applicationId,
        String endpointKey,
        String endpoint) {

        if (!isValidUUID(applicationId)) {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS application id.", applicationId));
        }


        if (!isValidUUID(endpointKey)) {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS subscription key.", endpointKey));
        }

        if (endpoint == null || endpoint.isEmpty()) {
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
     * Helper method to parse validate and set Luis application members from the full application full endpoint.
     */
    private void parse(String applicationEndpoint) {
        String applicationId = "";
        try {
            String [] segments = new URL(applicationEndpoint)
                .getPath()
                .split("/");
            for (int segment = 0; segment < segments.length - 1; segment++) {
                if (segments[segment].equals("apps")) {
                    applicationId = segments[segment + 1].trim();
                    break;
                }
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                String.format(
                    "Unable to create the LUIS endpoint with the given %s.",
                    applicationEndpoint
                )
            );
        }


        if (applicationId.isEmpty()) {
            throw new IllegalArgumentException(
                String.format(
                    "Could not find application Id in %s",
                    applicationEndpoint
                )
            );
        }

        try {

            String endpointKey = new URIBuilder(applicationEndpoint)
                .getQueryParams()
                .stream()
                .filter(param -> param.getName()
                    .equalsIgnoreCase("subscription-key"))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse("");

            String endpoint = String.format(
                "%s://%s",
                new URL(applicationEndpoint).getProtocol(),
                new URL(applicationEndpoint).toURI().getHost()
            );

            setLuisApplication(applicationId, endpointKey, endpoint);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException(
                String.format(
                "Unable to create the LUIS endpoint with the given %s.",
                applicationEndpoint
            ));
        }

    }

    private boolean isValidUUID (String uuid) {
        try {
            if (!uuid.contains("-")) {
                uuid = uuid.replaceAll(
                    "(.{8})(.{4})(.{4})(.{4})(.+)",
                    "$1-$2-$3-$4-$5"
                );
            }

            return UUID.fromString(uuid).toString().equals(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidURL (String uri) {
        try {
            return new URL(uri).toURI().isAbsolute();
        } catch (URISyntaxException | MalformedURLException exception) {
            return false;
        }
    }
}
