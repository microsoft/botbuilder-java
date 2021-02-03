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
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
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
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class JwtTokenExtractorTests {
    private X509Certificate validCertificate;
    private X509Certificate expiredCertificate;
    private KeyPair keyPair;

    @Before
    public void setup() throws GeneralSecurityException, IOException {
        ChannelValidation.TOKENVALIDATIONPARAMETERS.validateLifetime = false;
        EmulatorValidation.TOKENVALIDATIONPARAMETERS.validateLifetime = false;
        GovernmentChannelValidation.TOKENVALIDATIONPARAMETERS.validateLifetime = false;

        // create keys
        keyPair = createKeyPair();
        Date now = new Date();
        Date from = new Date(now.getTime() - (10 * 86400000L));

        // create expired certificate
        Date to = new Date(now.getTime() - (9 * 86400000L));
        expiredCertificate = createSelfSignedCertificate(keyPair, from, to);

        // create valid certificate
        to = new Date(now.getTime() + (9 * 86400000L));
        validCertificate = createSelfSignedCertificate(keyPair, from, to);
    }

    @Test(expected = CompletionException.class)
    public void JwtTokenExtractor_WithExpiredCert_ShouldNotAllowCertSigningKey() {
        // this should throw a CompletionException (which contains an AuthenticationException)
        buildExtractorAndValidateToken(
            expiredCertificate, keyPair.getPrivate()
        ).join();
    }

    @Test
    public void JwtTokenExtractor_WithValidCert_ShouldAllowCertSigningKey() {
        // this should not throw
        buildExtractorAndValidateToken(
            validCertificate, keyPair.getPrivate()
        ).join();
    }

    @Test(expected = CompletionException.class)
    public void JwtTokenExtractor_WithExpiredToken_ShouldNotAllow() {
        // this should throw a CompletionException (which contains an AuthenticationException)
        Date now = new Date();
        Date issuedAt = new Date(now.getTime() - 86400000L);

        buildExtractorAndValidateToken(
            expiredCertificate, keyPair.getPrivate(), issuedAt
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
        return new TokenValidationParameters() {{
            validateIssuer = false;
            validIssuers = Collections.singletonList(AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER);

            // Audience validation takes place in JwtTokenExtractor
            validateAudience = false;
            validateLifetime = true;
            clockSkew = Duration.ofMinutes(5);
            requireSignedTokens = true;

            // provide a custom resolver so that calls to openid won't happen (which wouldn't
            // work for these tests).
            issuerSigningKeyResolver = key -> (OpenIdMetadata) keyId -> {
                // return our certificate data
                OpenIdMetadataKey key1 = new OpenIdMetadataKey();
                key1.key = (RSAPublicKey) cert.getPublicKey();
                key1.certificateChain = Collections.singletonList(encodeCertificate(cert));
                return key1;
            };
        }};
    }

    private KeyPair createKeyPair() throws NoSuchAlgorithmException {
        // note that this isn't allowing for a "kid" value
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static X509Certificate createSelfSignedCertificate(
        KeyPair pair, Date from, Date to
    ) throws GeneralSecurityException, IOException {
        String dn = "CN=Bot, OU=BotFramework, O=Microsoft, C=US";
        String algorithm = "SHA256withRSA";

        PrivateKey privateKey = pair.getPrivate();
        X509CertInfo info = new X509CertInfo();

        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(dn);

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, owner);
        info.set(X509CertInfo.ISSUER, owner);
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privateKey, algorithm);

        // Update the algorithm, and resign.
        algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privateKey, algorithm);
        return cert;
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
