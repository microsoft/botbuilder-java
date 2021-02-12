// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder;

import java.time.Duration;

/**
 * Constants used in TurnState.
 */
public final class TurnStateConstants {

    private TurnStateConstants() {

    }

    /**
     * TurnState key for the OAuth login timeout.
     */
    public static final String OAUTH_LOGIN_TIMEOUT_KEY = "loginTimeout";

    /**
     * Name of the token polling settings key.
     */
    public static final String TOKEN_POLLING_SETTINGS_KEY = "tokenPollingSettings";

    /**
     * Default amount of time an OAuthCard will remain active (clickable and
     * actively waiting for a token). After this time: (1) the OAuthCard will not
     * allow the user to click on it. (2) any polling triggered by the OAuthCard
     * will stop.
     */
    public static final Duration OAUTH_LOGIN_TIMEOUT_VALUE = Duration.ofMinutes(15);
}
