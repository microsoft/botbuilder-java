// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the structure that is returned as the result of a health check on the bot.
 * The health check is sent to the bot as an InvokeActivity and this class along with {@link HealthCheckResponse}
 * defines the structure of the body of the response.
 */
public class HealthResults {
    /**
     * A value indicating whether the health check has succeeded and the bot is healthy.
     */
    @JsonProperty(value = "success")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Boolean success;

    /**
     * A value that is exactly the same as the Authorization header that would have been added to an HTTP POST back.
     */
    @JsonProperty(value = "authorization")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String authorization;

    /**
     * A value that is exactly the same as the User-Agent header that would have been added to an HTTP POST back.
     */
    @JsonProperty(value = "user-agent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String userAgent;

    /**
     * Informational messages that can be optionally included in the health check response.
     */
    @JsonProperty(value = "messages")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String[] messages;

    /**
     * Diagnostic data that can be optionally included in the health check response.
     */
    @JsonProperty(value = "diagnostics")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object diagnostics;

    /**
     * Gets the success value.
     *
     * @return The success value.
     */
    public Boolean getSuccess() {
        return this.success;
    }

    /**
     * Sets the success value.
     *
     * @param withSuccess The success value to set.
     */
    public void setSuccess(Boolean withSuccess) {
        this.success = withSuccess;
    }

    /**
     * Get the authorization value.
     *
     * @return the authorization value
     */
    public String getAuthorization() {
        return this.authorization;
    }

    /**
     * Set the authorization value.
     *
     * @param withAuthorization the authization value to set
     */
    public void setAuthorization(String withAuthorization) {
        this.authorization = withAuthorization;
    }

    /**
     * Get the userAgent value.
     *
     * @return the userAgent value
     */
    public String getUserAgent() {
        return this.userAgent;
    }

    /**
     * Set the userAgent value.
     *
     * @param withUserAgent the userAgent value to set
     */
    public void setUserAgent(String withUserAgent) {
        this.userAgent = withUserAgent;
    }

    /**
     * Get the messages value.
     *
     * @return the messages value
     */
    public String[] getMessages() {
        return this.messages;
    }

    /**
     * Set the messages value.
     *
     * @param withMessages the messages value to set
     */
    public void setMessages(String[] withMessages) {
        this.messages = withMessages;
    }

    /**
     * Get the diagnostics value.
     *
     * @return the diagnostics value
     */
    public Object getDiagnostics() {
        return this.diagnostics;
    }

    /**
     * Set the diagnostics value.
     *
     * @param withDiagnostics the diagnostics value to set
     */
    public void setDiagnostics(Object withDiagnostics) {
        this.diagnostics = withDiagnostics;
    }
}
