// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

/**
 * Catchall exception for auth failures.
 */
public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Construct with exception.
     * 
     * @param t The cause.
     */
    public AuthenticationException(Throwable t) {
        super(t);
    }

    /**
     * Construct with message.
     * 
     * @param message The exception message.
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Construct with caught exception and message.
     * 
     * @param message The message.
     * @param t       The caught exception.
     */
    public AuthenticationException(String message, Throwable t) {
        super(message, t);
    }
}
