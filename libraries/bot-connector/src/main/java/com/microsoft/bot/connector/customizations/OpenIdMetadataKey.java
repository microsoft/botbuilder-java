// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.customizations;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

class OpenIdMetadataKey {
    RSAPublicKey key;
    List<String> endorsements;
}