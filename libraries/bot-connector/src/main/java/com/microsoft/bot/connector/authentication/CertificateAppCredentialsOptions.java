// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import java.io.InputStream;

/**
 * CertificateAppCredentials Options.
 */
public class CertificateAppCredentialsOptions {
    private String appId;
    private String channelAuthTenant;
    private String oAuthScope;
    private InputStream pkcs12Certificate;
    private String pkcs12Password;
    private boolean sendX5c = true;

    /**
     * Initializes the CertificateAppCredentialsOptions with the required arguments.
     *
     * @param withAppId             The Microsoft app ID.
     * @param withPkcs12Certificate The InputStream to the pkcs certificate.
     * @param withPkcs12Password    The pkcs certificate password.
     */
    public CertificateAppCredentialsOptions(
        String withAppId,
        InputStream withPkcs12Certificate,
        String withPkcs12Password
    ) {
        this(withAppId, withPkcs12Certificate, withPkcs12Password, null, null, true);
    }

    /**
     * Initializes the CertificateAppCredentialsOptions.
     *
     * @param withAppId             The Microsoft app ID.
     * @param withPkcs12Certificate The InputStream to the pkcs certificate.
     * @param withPkcs12Password    The pkcs certificate password.
     * @param withChannelAuthTenant Optional. The oauth token tenant.
     * @param withOAuthScope        Optional. The scope for the token.
     * @param withSendX5c           Specifies if the x5c claim (public key of the
     *                              certificate) should be sent to the STS.
     */
    public CertificateAppCredentialsOptions(
        String withAppId,
        InputStream withPkcs12Certificate,
        String withPkcs12Password,
        String withChannelAuthTenant,
        String withOAuthScope,
        boolean withSendX5c
    ) {
        appId = withAppId;
        channelAuthTenant = withChannelAuthTenant;
        oAuthScope = withOAuthScope;
        pkcs12Certificate = withPkcs12Certificate;
        pkcs12Password = withPkcs12Password;
        sendX5c = withSendX5c;
    }

    /**
     * Gets the Microsfot AppId.
     *
     * @return The app id.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the Microsfot AppId.
     * 
     * @param withAppId The app id.
     */
    public void setAppId(String withAppId) {
        appId = withAppId;
    }

    /**
     * Gets the Channel Auth Tenant.
     *
     * @return The OAuth Channel Auth Tenant.
     */
    public String getChannelAuthTenant() {
        return channelAuthTenant;
    }

    /**
     * Sets the Channel Auth Tenant.
     *
     * @param withChannelAuthTenant The OAuth Channel Auth Tenant.
     */
    public void setChannelAuthTenant(String withChannelAuthTenant) {
        channelAuthTenant = withChannelAuthTenant;
    }

    /**
     * Gets the OAuth scope.
     *
     * @return The OAuthScope.
     */
    public String getoAuthScope() {
        return oAuthScope;
    }

    /**
     * Sets the OAuth scope.
     *
     * @param withOAuthScope The OAuthScope.
     */
    public void setoAuthScope(String withOAuthScope) {
        oAuthScope = withOAuthScope;
    }

    /**
     * Gets the InputStream to the PKCS12 certificate.
     *
     * @return The InputStream to the certificate.
     */
    public InputStream getPkcs12Certificate() {
        return pkcs12Certificate;
    }

    /**
     * Sets the InputStream to the PKCS12 certificate.
     *
     * @param withPkcs12Certificate The InputStream to the certificate.
     */
    public void setPkcs12Certificate(InputStream withPkcs12Certificate) {
        pkcs12Certificate = withPkcs12Certificate;
    }

    /**
     * Gets the pkcs12 certiciate password.
     *
     * @return The password for the certificate.
     */
    public String getPkcs12Password() {
        return pkcs12Password;
    }

    /**
     * Sets the pkcs12 certiciate password.
     *
     * @param withPkcs12Password The password for the certificate.
     */
    public void setPkcs12Password(String withPkcs12Password) {
        pkcs12Password = withPkcs12Password;
    }

    /**
     * Gets if the x5c claim (public key of the certificate) should be sent to the
     * STS.
     *
     * @return true to send x5c.
     */
    public boolean getSendX5c() {
        return sendX5c;
    }

    /**
     * Sets if the x5c claim (public key of the certificate) should be sent to the
     * STS.
     *
     * @param withSendX5c true to send x5c.
     */
    public void setSendX5c(boolean withSendX5c) {
        sendX5c = withSendX5c;
    }
}
