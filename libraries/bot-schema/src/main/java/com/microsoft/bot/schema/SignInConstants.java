// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

/**
 * Names for signin invoke operations in the token protocol.
 */
public final class SignInConstants {
    private SignInConstants() {

    }

    /**
     * Name for the signin invoke to verify the 6-digit authentication code as part
     * of sign-in. This invoke operation includes a value containing a state
     * property for the magic code.
     */
    public static final String VERIFY_STATE_OPERATION_NAME = "signin/verifyState";

    /**
     * Name for signin invoke to perform a token exchange. This invoke operation
     * includes a value of the token exchange class.
     */
    public static final String TOKEN_EXCHANGE_OPERATION_NAME = "signin/tokenExchange";

    /**
     * The EventActivity name when a token is sent to the bot.
     */
    public static final String TOKEN_RESPONSE_EVENT_NAME = "tokens/response";
}
