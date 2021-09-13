// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.connector.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.Async;

/**
 * Sample claims validator that loads an allowed list from configuration if
 * presentand checks that requests are coming from allowed parent bots.
 */
public class AllowedCallersClaimsValidator extends ClaimsValidator {

    private List<String> allowedCallers;

    /**
     * Creates an instance of an {@link AllowedCallersClaimsValidator}.
     * @param withAllowedCallers    A {@link List<String>} that contains the list of allowed callers.
     */
    public AllowedCallersClaimsValidator(List<String> withAllowedCallers) {
        this.allowedCallers = withAllowedCallers != null ? withAllowedCallers : new ArrayList<String>();
    }

    /**
     * Validates a Map of claims and should throw an exception if the
     * validation fails.
     *
     * @param claims  The Map of claims to validate.
     *
     * @return   true if the validation is successful, false if not.
     */
    @Override
    public CompletableFuture<Void> validateClaims(Map<String, String> claims) {
        if (claims == null) {
            return Async.completeExceptionally(new IllegalArgumentException("Claims cannot be null"));
        }

        // If _allowedCallers contains an "*", we allow all callers.
        if (SkillValidation.isSkillClaim(claims) && !allowedCallers.contains("*")) {
            // Check that the appId claim in the skill request instanceof in the list of
            // callers configured for this bot.
            String appId = JwtTokenValidation.getAppIdFromClaims(claims);
            if (!allowedCallers.contains(appId)) {
                return Async.completeExceptionally(
                    new RuntimeException(
                        String.format(
                            "Received a request from a bot with an app ID of \"%s\". To enable requests from this "
                            + "caller, add the app ID to the configured set of allowedCallers.",
                            appId
                        )
                    )
                );
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
