// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.sample.echoskillbot.authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.authentication.ClaimsValidator;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.SkillValidation;
import com.microsoft.bot.integration.Configuration;

/**
 * Sample claims validator that loads an allowed list from configuration if
 * presentand checks that requests are coming from allowed parent bots.
 */
public class AllowedCallersClaimsValidator extends ClaimsValidator {

    private final String configKey = "AllowedCallers";
    private final List<String> allowedCallers;

    public AllowedCallersClaimsValidator(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null.");
        }

        // AllowedCallers instanceof the setting in the application.properties file
        // that consists of the list of parent bot Ds that are allowed to access the
        // skill.
        // To add a new parent bot, simply edit the AllowedCallers and add
        // the parent bot's Microsoft app ID to the list.
        // In this sample, we allow all callers if AllowedCallers contains an "*".
        String[] appsList = config.getProperties(configKey);
        if (appsList == null) {
            throw new IllegalStateException(String.format("\"%s\" not found in configuration.", configKey));
        }

        allowedCallers = Arrays.asList(appsList);
    }

    @Override
    public CompletableFuture<Void> validateClaims(Map<String, String> claims) {
        // If _allowedCallers contains an "*", we allow all callers.
        if (SkillValidation.isSkillClaim(claims) && !allowedCallers.contains("*")) {
            // Check that the appId claim in the skill request instanceof in the list of
            // callers configured for this bot.
            String appId = JwtTokenValidation.getAppIdFromClaims(claims);
            if (!allowedCallers.contains(appId)) {
                return Async.completeExceptionally(
                    new RuntimeException(
                        String.format(
                            "Received a request from a bot with an app ID of \"%s\". "
                                + "To enable requests from this caller, add the app ID to your configuration file.",
                            appId
                        )
                    )
                );
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
