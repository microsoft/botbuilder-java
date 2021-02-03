package com.microsoft.bot.ai.luis;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class LuisApplication {
    /// <summary>
    /// Initializes a new instance of the <see cref="LuisApplication"/> class.
    /// </summary>
    public LuisApplication()
    {
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="LuisApplication"/> class.
    /// </summary>
    /// <param name="applicationId">LUIS application ID.</param>
    /// <param name="endpointKey">LUIS subscription or endpoint key.</param>
    /// <param name="endpoint">LUIS endpoint to use like https://westus.api.cognitive.microsoft.com.</param>
    public LuisApplication(String applicationId, String endpointKey, String endpoint) {
        setLuisApplication(applicationId, endpointKey, endpoint);
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="LuisApplication"/> class.
    /// </summary>
    /// <param name="applicationEndpoint">LUIS application endpoint.</param>
    public LuisApplication(String applicationEndpoint) {
        parse(applicationEndpoint);
    }

    private void setLuisApplication (String applicationId, String endpointKey, String endpoint) {

        if (!isValidUUID(applicationId)) {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS application id.", applicationId));
        }


        if (!isValidUUID(endpointKey)) {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS subscription key.", endpointKey));
        }

        if (endpoint == null || endpoint.isEmpty())
        {
            endpoint = "https://westus.api.cognitive.microsoft.com";
        }

        if (!isValidURL(endpoint))
        {
            throw new IllegalArgumentException(String.format("%s is not a valid LUIS endpoint.", endpoint));
        }

        this.applicationId = applicationId;
        this.endpointKey = endpointKey;
        this.endpoint = endpoint;
    }

    /// <summary>
    /// Gets or sets lUIS application ID.
    /// </summary>
    /// <value>
    /// LUIS application ID.
    /// </value>
    public String applicationId;

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    /// <summary>
    /// Gets or sets lUIS subscription or endpoint key.
    /// </summary>
    /// <value>
    /// LUIS subscription or endpoint key.
    /// </value>
    public String endpointKey;

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    /// <summary>
    /// Gets or sets lUIS endpoint like https://westus.api.cognitive.microsoft.com.
    /// </summary>
    /// <value>
    /// LUIS endpoint where application is hosted.
    /// </value>
    public String endpoint;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    private void parse(String applicationEndpoint) {
        String applicationId = "";
        try {
            String [] segments = new URL(applicationEndpoint).getPath().split("/");
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


        if (applicationId.isEmpty())
        {
            throw new IllegalArgumentException(
                String.format(
                    "Could not find application Id in %s",
                    applicationEndpoint
                )
            );
        }

        try {

            String endpointKey = new URIBuilder(applicationEndpoint).getQueryParams()
                .stream()
                .filter(param -> param.getName().equalsIgnoreCase("subscription-key"))
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
            e.printStackTrace();
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
