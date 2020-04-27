// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;

/**
 * Credential provider which uses Configuration to lookup appId and password.
 */
public class ConfigurationCredentialProvider extends SimpleCredentialProvider {
    /**
     * Initializes a new instance using a {@link Configuration}.
     * 
     * @param configuration The Configuration to use.
     */
    public ConfigurationCredentialProvider(Configuration configuration) {
        setAppId(configuration.getProperty(MicrosoftAppCredentials.MICROSOFTAPPID));
        setPassword(configuration.getProperty(MicrosoftAppCredentials.MICROSOFTAPPPASSWORD));
    }
}
