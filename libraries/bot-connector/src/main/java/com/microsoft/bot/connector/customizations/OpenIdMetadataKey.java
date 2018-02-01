package com.microsoft.bot.connector.customizations;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

class OpenIdMetadataKey {
    RSAPublicKey key;
    List<String> endorsements;
}