// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Verify that the specified endorsement exists on the JWT token.
 */
public abstract class EndorsementsValidator {
    /**
     * Verify that the specified endorsement exists on the JWT token. Call this
     * method multiple times to validate multiple endorsements.
     *
     * <p>
     * For example, if an {@link com.microsoft.bot.schema.Activity} comes from
     * WebChat, that activity's
     * {@link com.microsoft.bot.schema.Activity#getChannelId()} property is set to
     * "webchat" and the signing party of the JWT token must have a corresponding
     * endorsement of “Webchat”.
     * </p>
     *
     * @param expectedEndorsement The expected endorsement. Generally the ID of the
     *                            channel to validate, typically extracted from the
     *                            activity's
     *                            {@link com.microsoft.bot.schema.Activity#getChannelId()}
     *                            property, that to which the Activity is
     *                            affinitized. Alternatively, it could represent a
     *                            compliance certification that is required.
     * @param endorsements        The JWT token’s signing party is permitted to send
     *                            activities only for specific channels. That list,
     *                            the set of channels the service can sign for, is
     *                            called the endorsement list. The activity’s
     *                            Schema.Activity.ChannelId MUST be found in the
     *                            endorsement list, or the incoming activity is not
     *                            considered valid.
     * @return True is the expected endorsement is found in the Endorsement set.
     * @throws IllegalArgumentException Missing endorsements
     */
    public static boolean validate(String expectedEndorsement, List<String> endorsements)
        throws IllegalArgumentException {

        // If the Activity came in and doesn't have a Channel ID then it's making no
        // assertions as to who endorses it. This means it should pass.
        if (StringUtils.isEmpty(expectedEndorsement)) {
            return true;
        }

        if (endorsements == null) {
            throw new IllegalArgumentException("endorsements must be present.");
        }

        // The Call path to get here is:
        // JwtTokenValidation.authenticateRequest
        // ->
        // JwtTokenValidation.validateAuthHeader
        // ->
        // ChannelValidation.authenticateToken
        // ->
        // JwtTokenExtractor

        // Does the set of endorsements match the expected endorsement that was passed
        // in?
        return endorsements.contains(expectedEndorsement);
    }
}
