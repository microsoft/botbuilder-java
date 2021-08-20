// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.PasswordServiceClientCredentialFactory;

/**
 * Credential provider which uses {@link Configuration} to lookup appId and password.
 * This will populate the {@link PasswordServiceClientCredentialFactory#getAppId()} from an configuration entry with the
 * key of {@link MicrosoftAppCredentials#MICROSOFTAPPID} and the
 * {@link PasswordServiceClientCredentialFactory#getPassword()}
 * from a configuration entry with the key of {@link MicrosoftAppCredentials#MICROSOFTAPPPASSWORD}.
 *
 * NOTE: if the keys are not present, a null value will be used.
 */
public class ConfigurationServiceClientCredentialFactory extends PasswordServiceClientCredentialFactory {

    /**
     * Initializes a new instance of the {@link ConfigurationServiceClientCredentialFactory} class.
     * @param configuration An instance of {@link Configuration}.
     */
    public ConfigurationServiceClientCredentialFactory(Configuration configuration) {
        super(configuration.getProperty(MicrosoftAppCredentials.MICROSOFTAPPID),
              configuration.getProperty(MicrosoftAppCredentials.MICROSOFTAPPPASSWORD));
    }
}
