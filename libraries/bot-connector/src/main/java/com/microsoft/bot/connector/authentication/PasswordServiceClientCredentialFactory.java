// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * A simple implementation of the {@link ServiceClientCredentialsFactory}
 * interface.
 */
public class PasswordServiceClientCredentialFactory extends ServiceClientCredentialsFactory {

    private String appId;
    private String password;

    /**
     * Gets the app ID for this credential.
     *
     * @return The app ID for this credential.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the app ID for this credential.
     *
     * @param appId The app ID for this credential.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Gets the app password for this credential.
     *
     * @return The app password for this credential.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the app password for this credential.
     *
     * @param password The app password for this credential.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Initializes a new instance of the
     * {@link PasswordServiceClientCredentialFactory} class. with empty credentials.
     */
    public PasswordServiceClientCredentialFactory() {

    }

    /**
     * Initializes a new instance of the
     * {@link PasswordServiceClientCredentialFactory} class. with the provided
     * credentials.
     *
     * @param withAppId    The app ID.
     * @param withPassword The app password.
     */
    public PasswordServiceClientCredentialFactory(String withAppId, String withPassword) {
        this.appId = withAppId;
        this.password = withPassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> isValidAppId(String appId) {
        return CompletableFuture.completedFuture(appId == this.appId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> isAuthenticationDisabled() {
        return CompletableFuture.completedFuture(StringUtils.isBlank(this.appId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ServiceClientCredentials> createCredentials(String appId, String oauthScope,
            String loginEndpoint, Boolean validateAuthority) {
        if (this.isAuthenticationDisabled().join()) {
            return CompletableFuture.completedFuture(MicrosoftAppCredentials.empty());
        }

        if (!this.isValidAppId(appId).join()) {
            throw new IllegalArgumentException("Invalid appId.");
        }

        if (loginEndpoint.toLowerCase()
                .startsWith(AuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL_TEMPLATE.toLowerCase())) {
            // TODO : Unpack necessity of these empty credentials based on the loginEndpoint
            //  as no tokens are fetched when auth is disabled.
            ServiceClientCredentials credentials = appId == null ? MicrosoftAppCredentials.empty()
                    : new MicrosoftAppCredentials(appId, this.password);
            return CompletableFuture.completedFuture(credentials);
        } else if (loginEndpoint
                .equalsIgnoreCase(GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL)) {
            ServiceClientCredentials credentials = appId == null ? MicrosoftGovernmentAppCredentials.empty()
                    : new MicrosoftGovernmentAppCredentials(appId, this.password);
            return CompletableFuture.completedFuture(credentials);
        } else {
            ServiceClientCredentials credentials = appId == null
                    ? new PrivateCloudAppCredentials(null, null, null, loginEndpoint, validateAuthority)
                    : new PrivateCloudAppCredentials(appId, this.password, oauthScope, loginEndpoint,
                            validateAuthority);
            return CompletableFuture.completedFuture(credentials);
        }
    }

    public class PrivateCloudAppCredentials extends MicrosoftAppCredentials {
        private final String oauthEndpoint;
        private final Boolean validateAuthority;

        /**
         * Gets the OAuth endpoint to use.
         *
         * @return The OAuthEndpoint to use.
         */
        public String oAuthEndpoint() {
            return oauthEndpoint;
        }

        /**
         * Gets a value indicating whether to validate the Authority.
         *
         * @return The ValidateAuthority value to use.
         */
        public Boolean getValidateAuthority() {
            return validateAuthority;
        }

        public PrivateCloudAppCredentials(String appId, String password, String oAuthScope, String withOauthEndpoint,
                Boolean withValidateAuthority) {
            super(appId, password, null, oAuthScope);
            this.oauthEndpoint = withOauthEndpoint;
            this.validateAuthority = withValidateAuthority;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean validateAuthority() {
            return validateAuthority;
        }
    }
}
