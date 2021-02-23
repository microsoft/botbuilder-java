package com.microsoft.bot.connector.authentication;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract class used to validate identity.
 */
public abstract class ClaimsValidator {

    /**
     * Validates a Map of claims and should throw an exception if the
     * validation fails.
     *
     * @param claims  The Map of claims to validate.
     *
     * @return   true if the validation is successful, false if not.
     */
    public abstract CompletableFuture<Void> validateClaims(Map<String, String> claims);
}
