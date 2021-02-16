package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A request to exchange a token.
 */
public class TokenExchangeInvokeRequest {
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    @JsonProperty(value = "token")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String token;

    /**
     * Gets the id from the TokenExchangeInvokeRequest.
     * @return the Id value as a String.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id from the TokenExchangeInvokeRequest.
     * @param withId The Id value.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Gets the connection name.
     * @return the ConnectionName value as a String.
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * Sets the connection name.
     * @param withConnectionName The ConnectionName value.
     */
    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }

    /**
     * Gets the details of why the token exchange failed.
     * @return the Token value as a String.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Sets the details of why the token exchange failed.
     * @param withToken The FailureDetail value.
     */
    public void setToken(String withToken) {
        this.token = withToken;
    }

}
