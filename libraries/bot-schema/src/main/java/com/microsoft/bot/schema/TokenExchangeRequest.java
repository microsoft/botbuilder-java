// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload to be sent to the Bot Framework Token Service for Single Sign
 * On.If the URI is set to a custom scope, then Token Service will exchange the
 * token in its cache for a token targeting the custom scope and return it in
 * the response.If a Token is sent in the payload, then Token Service will
 * exchange the token for a token targetting the scopes specified in the
 * corresponding OAauth connection.
 */
public class TokenExchangeRequest {

    @JsonProperty(value = "uri")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String uri;

    @JsonProperty(value = "token")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String token;


    /**
     * Initializes a new instance of the {@link TokenExchangeRequest} class.
     */
    public TokenExchangeRequest() {
        customInit();
    }

    /**
     * Initializes a new instance of the TokenExchangeRequest class.
     * @param uri The uri to intialize this instance with.
     * @param token The token to initialize this instance with.
     */
    public TokenExchangeRequest(String uri, String token) {
        this.uri = uri;
        this.token = token;
        customInit();
    }

    /**
     * An initialization method that performs custom operations like setting.
     * defaults
     */
    void customInit() {

    }

    /**
     * Gets a URI String.
     * @return the Uri value as a String.
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Sets a URI String.
     * @param withUri The Uri value.
     */
    public void setUri(String withUri) {
        this.uri = withUri;
    }
    /**
     * Gets a token String.
     * @return the Token value as a String.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Sets a token String.
     * @param withToken The Token value.
     */
    public void setToken(String withToken) {
        this.token = withToken;
    }
}

