// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.connector.authentication.AuthenticateRequestResult;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthenticationFactory;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ConnectorFactory;
import com.microsoft.bot.connector.authentication.ServiceClientCredentialsFactory;
import com.microsoft.bot.connector.authentication.UserTokenClient;
import com.microsoft.bot.connector.skills.BotFrameworkClient;
import com.microsoft.bot.schema.Activity;
import okhttp3.OkHttpClient;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * Creates a BotFrameworkAuthentication instance from configuration.
 */
public class ConfigurationBotFrameworkAuthentication extends BotFrameworkAuthentication {
    private final BotFrameworkAuthentication inner;

    /**
     * Initializes a new instance of the ConfigurationBotFrameworkAuthentication
     * class.
     *
     * @param configuration      A {@link Configuration} instance.
     * @param credentialsFactory A {@link ServiceClientCredentialsFactory} instance.
     * @param authConfiguration  An {@link AuthenticationConfiguration} instance.
     */
    public ConfigurationBotFrameworkAuthentication(Configuration configuration,
                                                   @Nullable ServiceClientCredentialsFactory credentialsFactory,
                                                   @Nullable AuthenticationConfiguration authConfiguration) {
        String channelService = configuration.getProperty("ChannelService");
        String validateAuthority = configuration.getProperty("ValidateAuthority");
        String toChannelFromBotLoginUrl = configuration.getProperty("ToChannelFromBotLoginUrl");
        String toChannelFromBotOAuthScope = configuration.getProperty("ToChannelFromBotOAuthScope");
        String toBotFromChannelTokenIssuer = configuration.getProperty("ToBotFromChannelTokenIssuer");
        String oAuthUrl = configuration.getProperty("OAuthUrl") != null ? configuration.getProperty("OAuthUrl")
                : configuration.getProperty(AuthenticationConstants.OAUTH_URL_KEY);
        String toBotFromChannelOpenIdMetadataUrl = configuration
                .getProperty("ToBotFromChannelOpenIdMetadataUrl") != null
                        ? configuration.getProperty("ToBotFromChannelOpenIdMetadataUrl")
                        : configuration.getProperty(AuthenticationConstants.BOT_OPENID_METADATA_KEY);
        String toBotFromEmulatorOpenIdMetadataUrl = configuration.getProperty("ToBotFromEmulatorOpenIdMetadataUrl");
        String callerId = configuration.getProperty("CallerId");

        inner = BotFrameworkAuthenticationFactory.create(
            channelService,
            Boolean.parseBoolean(validateAuthority != null ? validateAuthority : "true"),
            toChannelFromBotLoginUrl,
            toChannelFromBotOAuthScope,
            toBotFromChannelTokenIssuer,
            oAuthUrl,
            toBotFromChannelOpenIdMetadataUrl,
            toBotFromEmulatorOpenIdMetadataUrl,
            callerId,
            credentialsFactory != null ? credentialsFactory
                    : new ConfigurationServiceClientCredentialFactory(configuration),
            authConfiguration != null ? authConfiguration : new AuthenticationConfiguration(),
            new OkHttpClient());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOriginatingAudience() {
        return inner.getOriginatingAudience();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ClaimsIdentity> authenticateChannelRequest(String authHeader) {
        return inner.authenticateChannelRequest(authHeader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<AuthenticateRequestResult> authenticateRequest(Activity activity, String authHeader) {
        return inner.authenticateRequest(activity, authHeader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<AuthenticateRequestResult> authenticateStreamingRequest(String authHeader,
            String channelIdHeader) {
        return inner.authenticateStreamingRequest(authHeader, channelIdHeader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorFactory createConnectorFactory(ClaimsIdentity claimsIdentity) {
        return inner.createConnectorFactory(claimsIdentity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<UserTokenClient> createUserTokenClient(ClaimsIdentity claimsIdentity) {
        return inner.createUserTokenClient(claimsIdentity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotFrameworkClient createBotFrameworkClient() {
        return inner.createBotFrameworkClient();
    }
}
