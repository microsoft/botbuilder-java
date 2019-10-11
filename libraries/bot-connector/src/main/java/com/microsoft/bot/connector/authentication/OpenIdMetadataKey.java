// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Wrapper to hold Jwk key data.
 */
class OpenIdMetadataKey {
    @SuppressWarnings("checkstyle:VisibilityModifier")
    RSAPublicKey key;
    @SuppressWarnings("checkstyle:VisibilityModifier")
    List<String> endorsements;
}
