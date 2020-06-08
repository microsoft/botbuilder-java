// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Wrapper to hold Jwk key data.
 */
public class OpenIdMetadataKey {
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public RSAPublicKey key;
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public List<String> endorsements;
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public List<String> certificateChain;
}
