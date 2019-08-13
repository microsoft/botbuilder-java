// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.Map;

public interface ClaimsIdentity {
    boolean isAuthenticated();
    Map<String, String> claims();
    String getIssuer();
}
