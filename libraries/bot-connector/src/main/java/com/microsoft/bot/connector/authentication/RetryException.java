// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * Retry exception when the Retry task fails to execute successfully.
 */
public class RetryException extends RuntimeException {
    private List<Throwable> exceptions = new ArrayList<>();

    /**
     * A RetryException with description and list of exceptions.
     * 
     * @param message        The message.
     * @param withExceptions The list of exceptions collected by {@link Retry}.
     */
    public RetryException(String message, List<Throwable> withExceptions) {
        super(message);
        exceptions = withExceptions;
    }

    /**
     * A Retry failure caused by an unexpected failure.
     * 
     * @param cause The caught exception.
     */
    public RetryException(Throwable cause) {
        super(cause);
    }

    /**
     * A List of exceptions encountered when executing the Retry task.
     * 
     * @return The List of exceptions.
     */
    public List<Throwable> getExceptions() {
        return exceptions;
    }
}
