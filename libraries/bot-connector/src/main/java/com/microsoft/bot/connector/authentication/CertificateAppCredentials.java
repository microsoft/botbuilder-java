// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * AppCredentials using a certificate.
 */
public class CertificateAppCredentials extends AppCredentials {
    private Authenticator authenticator;

    /**
     * Initializes a new instance of the AppCredentials class.
     *
     * @param withOptions The options for CertificateAppCredentials.
     * @throws CertificateException      During Authenticator creation.
     * @throws UnrecoverableKeyException During Authenticator creation.
     * @throws NoSuchAlgorithmException  During Authenticator creation.
     * @throws KeyStoreException         During Authenticator creation.
     * @throws NoSuchProviderException   During Authenticator creation.
     * @throws IOException               During Authenticator creation.
     */
    public CertificateAppCredentials(CertificateAppCredentialsOptions withOptions)
        throws CertificateException,
            UnrecoverableKeyException,
            NoSuchAlgorithmException,
            KeyStoreException,
            NoSuchProviderException,
            IOException {

        super(withOptions.getChannelAuthTenant(), withOptions.getoAuthScope());

        // going to create this now instead of lazy loading so we don't have some
        // awkward InputStream hanging around.
        authenticator =
            new CertificateAuthenticator(withOptions, new OAuthConfiguration(oAuthEndpoint(), oAuthScope()));
    }

    /**
     * Returns a CertificateAuthenticator.
     *
     * @return An Authenticator object.
     */
    @Override
    protected Authenticator buildAuthenticator() {
        return authenticator;
    }
}
