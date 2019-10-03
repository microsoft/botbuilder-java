package com.microsoft.bot.connector.authentication;

import java.util.ArrayList;
import java.util.List;

public class RetryException extends RuntimeException {
    private List<Throwable> exceptions = new ArrayList<>();

    public RetryException() {
        super();
    }

    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, List<Throwable> withExceptions) {
        exceptions = withExceptions;
    }

    public RetryException(Throwable cause) {
        super(cause);
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }
}
