// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.auth0.jwt.algorithms.Algorithm;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ChannelValidation;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.EmulatorValidation;
import com.microsoft.bot.connector.authentication.GovernmentChannelValidation;
import com.microsoft.bot.connector.authentication.JwtTokenExtractor;
import com.microsoft.bot.connector.authentication.OpenIdMetadata;
import com.microsoft.bot.connector.authentication.OpenIdMetadataKey;
import com.microsoft.bot.connector.authentication.TokenValidationParameters;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Notes:
 *
 * The PKCS12 certificates were created using these steps:
 * https://kb.globalscape.com/Knowledgebase/11039/Generating-a-PKCS12-Private-Key-and-Public-Certificate
 *
 * For the expired cert, just specify a negative number of days in step #4.
 *
 * For both valid and expired certs, these unit tests expect the alias for both to be "bot-connector-pkcs12"
 * and the password to be "botframework"
 */
public class JwtTokenExtractorTests {
    private CertInfo valid;
    private CertInfo expired;

    @Before
    public void setup() throws GeneralSecurityException, IOException {
        ChannelValidation.getTokenValidationParameters().validateLifetime = false;
        EmulatorValidation.getTokenValidationParameters().validateLifetime = false;
        GovernmentChannelValidation.getTokenValidationParameters().validateLifetime = false;

        valid = loadCert("bot-connector.pkcs12");
        expired = loadCert("bot-connector-expired.pkcs12");
    }

    @Test(expected = CompletionException.class)
    public void JwtTokenExtractor_WithExpiredCert_ShouldNotAllowCertSigningKey() {
        // this should throw a CompletionException (which contains an AuthenticationException)
        buildExtractorAndValidateToken(
            expired.cert, expired.keypair.getPrivate()
        ).join();
    }

    @Test
    public void JwtTokenExtractor_WithValidCert_ShouldAllowCertSigningKey() {
        // this should not throw
        buildExtractorAndValidateToken(
            valid.cert, valid.keypair.getPrivate()
        ).join();
    }

    @Test(expected = CompletionException.class)
    public void JwtTokenExtractor_WithExpiredToken_ShouldNotAllow() {
        // this should throw a CompletionException (which contains an AuthenticationException)
        Date now = new Date();
        Date issuedAt = new Date(now.getTime() - 86400000L);

        buildExtractorAndValidateToken(
            expired.cert, expired.keypair.getPrivate(), issuedAt
        ).join();
    }

    private CompletableFuture<ClaimsIdentity> buildExtractorAndValidateToken(
        X509Certificate cert,
        PrivateKey privateKey
    ) {
        return buildExtractorAndValidateToken(cert, privateKey, new Date());
    }

    private CompletableFuture<ClaimsIdentity> buildExtractorAndValidateToken(
        X509Certificate cert,
        PrivateKey privateKey,
        Date issuedAt
    ) {
        TokenValidationParameters tokenValidationParameters = createTokenValidationParameters(cert);

        JwtTokenExtractor tokenExtractor = new JwtTokenExtractor(
            tokenValidationParameters,
            "https://login.botframework.com/v1/.well-known/openidconfiguration",
            AuthenticationConstants.ALLOWED_SIGNING_ALGORITHMS
        );

        String token = createTokenForCertificate(cert, privateKey, issuedAt);

        return tokenExtractor.getIdentity("Bearer " + token, "test");
    }

    private static String createTokenForCertificate(X509Certificate cert, PrivateKey privateKey) {
        return createTokenForCertificate(cert, privateKey, new Date());
    }

    // creates a token that expires 5 minutes from the 'issuedAt' value.
    private static String createTokenForCertificate(X509Certificate cert, PrivateKey privateKey, Date issuedAt) {
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
        Algorithm algorithm = Algorithm.RSA256(publicKey, (RSAPrivateKey) privateKey);
        return com.auth0.jwt.JWT.create()
            .withIssuer("https://api.botframework.com")
            .withIssuedAt(issuedAt)
            .withNotBefore(issuedAt)
            .withExpiresAt(new Date(issuedAt.getTime() + 300000L))
            .sign(algorithm);
    }

    private static TokenValidationParameters createTokenValidationParameters(X509Certificate cert)
    {
        TokenValidationParameters parameters = new TokenValidationParameters();
        parameters.validateIssuer = false;
        parameters.validIssuers = Collections.singletonList(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER);

        // Audience validation takes place in JwtTokenExtractor
        parameters.validateAudience = false;
        parameters.validateLifetime = true;
        parameters.clockSkew = Duration.ofMinutes(5);
        parameters.requireSignedTokens = true;

        // provide a custom resolver so that calls to openid won't happen (which wouldn't
        // work for these tests).
        parameters.issuerSigningKeyResolver = key -> (OpenIdMetadata) keyId -> {
            // return our certificate data
            OpenIdMetadataKey key1 = new OpenIdMetadataKey();
            key1.key = (RSAPublicKey) cert.getPublicKey();
            key1.certificateChain = Collections.singletonList(encodeCertificate(cert));
            return key1;
        };
        return parameters;
    }

    private static class CertInfo {
        public X509Certificate cert;
        public KeyPair keypair;
    }

    private static CertInfo loadCert(String pkcs12File)
        throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
        UnrecoverableKeyException {
        InputStream fis = ClassLoader.getSystemResourceAsStream(pkcs12File);
        KeyStore p12 = KeyStore.getInstance("pkcs12");
        p12.load(fis, "botframework".toCharArray());

        CertInfo certInfo = new CertInfo();
        certInfo.cert = (X509Certificate) p12.getCertificate("bot-connector-pkcs12");
        certInfo.keypair = new KeyPair(certInfo.cert.getPublicKey(),
            (PrivateKey) p12.getKey("bot-connector-pkcs12", "botframework".toCharArray())
        );
        return certInfo;
    }

    private static String encodeCertificate(Certificate certificate) {
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] rawCrtText = certificate.getEncoded();
            return new String(encoder.encode(rawCrtText));
        } catch(CertificateEncodingException e) {
            return null;
        }
    }
}
