/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.schema.SignInResource;

/**
 * An instance of this class provides access to all the operations defined in
 * BotSignIns.
 */
public interface BotSignIn {
    /**
     *
     * @param state the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    CompletableFuture<String> getSignInUrl(String state);

    /**
     *
     * @param state         the String value
     * @param codeChallenge the String value
     * @param emulatorUrl   the String value
     * @param finalRedirect the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    CompletableFuture<String> getSignInUrl(
        String state,
        String codeChallenge,
        String emulatorUrl,
        String finalRedirect
    );

    /**
     *
     * @param state the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    CompletableFuture<SignInResource> getSignInResource(String state);
    /**
     *
     * @param state         the String value
     * @param codeChallenge the String value
     * @param emulatorUrl   the String value
     * @param finalRedirect the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    CompletableFuture<SignInResource> getSignInResource(
        String state,
        String codeChallenge,
        String emulatorUrl,
        String finalRedirect
    );
}
