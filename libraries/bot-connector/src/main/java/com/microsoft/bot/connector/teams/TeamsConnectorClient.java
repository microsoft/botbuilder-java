package com.microsoft.bot.connector.teams;

import com.microsoft.bot.rest.RestClient;

/**
 * Teams operations.
 */
public interface TeamsConnectorClient extends AutoCloseable {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
     */
    RestClient getRestClient();

    /**
     * Gets the User-Agent header for the client.
     * @return the user agent string.
     */
    String getUserAgent();

    /**
     * Gets the preferred language for the response..
     * @return the acceptLanguage value.
     */
    String getAcceptLanguage();

    /**
     * Sets the preferred language for the response..
     * @param acceptLanguage the acceptLanguage value.
     */
    void setAcceptLanguage(String acceptLanguage);

    /**
     * Gets the retry timeout in seconds for Long Running Operations. Default value is 30..
     * @return the timeout value.
     */
    int getLongRunningOperationRetryTimeout();

    /**
     * Sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     * @param timeout the longRunningOperationRetryTimeout value.
     */
    void setLongRunningOperationRetryTimeout(int timeout);

    /**
     * When set to true a unique x-ms-client-request-id value is generated and included in each request.
     * is true.
     * @return the generateClientRequestId value.
     */
    boolean getGenerateClientRequestId();

    /**
     * When set to true a unique x-ms-client-request-id value is generated and included in each request.
     * Default is true.
     * @param generateClientRequestId the generateClientRequestId value.
     */
    void setGenerateClientRequestId(boolean generateClientRequestId);

    /**
     * Gets TeamsOperations.
     * @return A TeamsOperations object.
     */
    TeamsOperations getTeams();
}
