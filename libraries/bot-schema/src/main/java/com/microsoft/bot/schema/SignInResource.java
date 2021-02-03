// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A type containing information for single sign-on.
 */
public class SignInResource {

    @JsonProperty(value = "signInLink")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String signInLink;

    @JsonProperty(value = "tokenExchangeResource")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private TokenExchangeResource tokenExchangeResource;

    /**
     * Initializes a new instance of the SignInUrlResponse class.
     */
    public SignInResource() {
        customInit();
    }

    /**
     * Initializes a new instance of the SignInUrlResponse class.
     * @param signInLink the sign in link to initialize this instance to.
     * @param tokenExchangeResource the tokenExchangeResource to initialize this instance to.
     */
    public SignInResource(String signInLink, TokenExchangeResource tokenExchangeResource) {
        this.signInLink = signInLink;
        this.tokenExchangeResource = tokenExchangeResource;
        customInit();
    }

    /**
     * An initialization method that performs custom operations like setting.
     * defaults
     */
    void customInit() {
    }

    /**
     * The sign-in link.
     * @return the SignInLink value as a String.
     */
    public String getSignInLink() {
        return this.signInLink;
    }

    /**
     * The sign-in link.
     * @param withSignInLink The SignInLink value.
     */
    public void setSignInLink(String withSignInLink) {
        this.signInLink = withSignInLink;
    }
    /**
     * Additional properties that cna be used for token exchange operations.
     * @return the TokenExchangeResource value as a TokenExchangeResource.
     */
    public TokenExchangeResource getTokenExchangeResource() {
        return this.tokenExchangeResource;
    }

    /**
     * Additional properties that cna be used for token exchange operations.
     * @param withTokenExchangeResource The TokenExchangeResource value.
     */
    public void setTokenExchangeResource(TokenExchangeResource withTokenExchangeResource) {
        this.tokenExchangeResource = withTokenExchangeResource;
    }
}
