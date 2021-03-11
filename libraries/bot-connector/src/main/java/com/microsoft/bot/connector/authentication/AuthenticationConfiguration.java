// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * General configuration settings for authentication.
 */
public class AuthenticationConfiguration {

    private ClaimsValidator claimsValidator = null;

    /**
     * Required endorsements for auth.
     *
     * @return A List of endorsements.
     */
    public List<String> requiredEndorsements() {
        return new ArrayList<String>();
    }

    /**
     * Access to the ClaimsValidator used to validate the identity claims.
     * @return the ClaimsValidator value if set.
     */
    public ClaimsValidator getClaimsValidator() {
        return claimsValidator;
    }

    /**
     * Access to the ClaimsValidator used to validate the identity claims.
     * @param withClaimsValidator the value to set the ClaimsValidator to.
     */
    public void setClaimsValidator(ClaimsValidator withClaimsValidator) {
        claimsValidator = withClaimsValidator;
    }
}
