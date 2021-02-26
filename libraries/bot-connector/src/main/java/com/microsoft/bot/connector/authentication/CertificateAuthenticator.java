// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * A provider of tokens for CertificateAppCredentials.
 */
public class CertificateAuthenticator implements Authenticator {
    private final ConfidentialClientApplication app;
    private final ClientCredentialParameters parameters;

    /**
     * Constructs an Authenticator using appId and pkcs certificate.
     *
     * @param withOptions       The options for CertificateAppCredentials.
     * @param withConfiguration The OAuthConfiguration.
     * @throws CertificateException      During MSAL app creation.
     * @throws UnrecoverableKeyException During MSAL app creation.
     * @throws NoSuchAlgorithmException  During MSAL app creation.
     * @throws KeyStoreException         During MSAL app creation.
     * @throws NoSuchProviderException   During MSAL app creation.
     * @throws IOException               During MSAL app creation.
     */
    public CertificateAuthenticator(CertificateAppCredentialsOptions withOptions, OAuthConfiguration withConfiguration)
        throws CertificateException,
            UnrecoverableKeyException,
            NoSuchAlgorithmException,
            KeyStoreException,
            NoSuchProviderException,
            IOException {

        app = ConfidentialClientApplication.builder(
            withOptions.getAppId(),
            ClientCredentialFactory.createFromCertificate(
                withOptions.getPkcs12Certificate(),
                withOptions.getPkcs12Password())
        )
            .authority(withConfiguration.getAuthority()).sendX5c(withOptions.getSendX5c()).build();

        parameters = ClientCredentialParameters.builder(Collections.singleton(withConfiguration.getScope())).build();
    }

    /**
     * Returns a token.
     *
     * @return The MSAL token result.
     */
    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken() {
        return app.acquireToken(parameters)
            .exceptionally(
                exception -> {
                    // wrapping whatever msal throws into our own exception
                    throw new AuthenticationException(exception);
                }
            );
    }
}
