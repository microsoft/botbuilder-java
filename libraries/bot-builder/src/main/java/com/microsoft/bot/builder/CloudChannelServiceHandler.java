// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import java.util.concurrent.CompletableFuture;

/**
 * A class to help with the implementation of the Bot Framework protocol using BotFrameworkAuthentication.
 */
public class CloudChannelServiceHandler extends ChannelServiceHandlerBase {
    private final BotFrameworkAuthentication auth;

    /**
     * Initializes a new instance of the {@link CloudChannelServiceHandler} class, using Bot Framework Authentication.
     * @param withAuth The Bot Framework Authentication object.
     */
    public CloudChannelServiceHandler(BotFrameworkAuthentication withAuth) {
        if (withAuth == null) {
            throw new IllegalArgumentException("withAuth cannot be null");
        }

        auth = withAuth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompletableFuture<ClaimsIdentity> authenticate(String authHeader) {
        return auth.authenticateChannelRequest(authHeader);
    }
}
