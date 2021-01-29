// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

/**
 * OAuth client interface.
 */
public interface OAuthClient {
    /**
     * Gets the BotSignIns object to access its operations.
     * 
     * @return the BotSignIns object.
     */
    BotSignIn getBotSignIn();

    /**
     * Gets the UserTokens object to access its operations.
     * 
     * @return the UserTokens object.
     */
    UserToken getUserToken();
}
