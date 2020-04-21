// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * General configuration settings for authentication.
 */
public class AuthenticationConfiguration {
    /**
     * Required endorsements for auth.
     * 
     * @return A List of endorsements.
     */
    public List<String> requiredEndorsements() {
        return new ArrayList<String>();
    }
}
