// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.microsoft.bot.connector.ExecutorFactory;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Extracts relevant data from JWT Tokens.
 */
public class JwtTokenExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingOpenIdMetadata.class);

    private TokenValidationParameters tokenValidationParameters;
    private List<String> allowedSigningAlgorithms;
    private OpenIdMetadataResolver openIdMetadataResolver;
    private OpenIdMetadata openIdMetadata;

    /**
     * Initializes a new instance of the JwtTokenExtractor class.
     *
     * @param withTokenValidationParameters tokenValidationParameters.
     * @param withMetadataUrl               metadataUrl.
     * @param withAllowedSigningAlgorithms  allowedSigningAlgorithms.
     */
    public JwtTokenExtractor(
        TokenValidationParameters withTokenValidationParameters,
        String withMetadataUrl,
        List<String> withAllowedSigningAlgorithms
    ) {
        this.tokenValidationParameters =
            new TokenValidationParameters(withTokenValidationParameters);
        this.tokenValidationParameters.requireSignedTokens = true;
        this.allowedSigningAlgorithms = withAllowedSigningAlgorithms;

        if (tokenValidationParameters.issuerSigningKeyResolver == null) {
            this.openIdMetadataResolver = new CachingOpenIdMetadataResolver();
        } else {
            this.openIdMetadataResolver = tokenValidationParameters.issuerSigningKeyResolver;
        }

        this.openIdMetadata = this.openIdMetadataResolver.get(withMetadataUrl);
    }

    /**
     * Get a ClaimsIdentity from an auth header and channel id.
     *
     * @param authorizationHeader The Authorization header value.
     * @param channelId           The channel id.
     * @return A ClaimsIdentity if successful.
     */
    public CompletableFuture<ClaimsIdentity> getIdentity(
        String authorizationHeader,
        String channelId
    ) {
        return getIdentity(authorizationHeader, channelId, new ArrayList<>());
    }

    /**
     * Get a ClaimsIdentity from an auth header and channel id.
     *
     * @param authorizationHeader  The Authorization header value.
     * @param channelId            The channel id.
     * @param requiredEndorsements A list of endorsements that are required.
     * @return A ClaimsIdentity if successful.
     */
    public CompletableFuture<ClaimsIdentity> getIdentity(
        String authorizationHeader,
        String channelId,
        List<String> requiredEndorsements
    ) {
        if (authorizationHeader == null) {
            return CompletableFuture.completedFuture(null);
        }

        String[] parts = authorizationHeader.split(" ");
        if (parts.length == 2) {
            return getIdentity(parts[0], parts[1], channelId, requiredEndorsements);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Get a ClaimsIdentity from a schema, token and channel id.
     *
     * @param schema               The schema.
     * @param token                The token.
     * @param channelId            The channel id.
     * @param requiredEndorsements A list of endorsements that are required.
     * @return A ClaimsIdentity if successful.
     */
    public CompletableFuture<ClaimsIdentity> getIdentity(
        String schema,
        String token,
        String channelId,
        List<String> requiredEndorsements
    ) {
        // No header in correct scheme or no token
        if (!schema.equalsIgnoreCase("bearer") || token == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Issuer isn't allowed? No need to check signature
        if (!hasAllowedIssuer(token)) {
            return CompletableFuture.completedFuture(null);
        }

        return validateToken(token, channelId, requiredEndorsements);
    }

    private boolean hasAllowedIssuer(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return this.tokenValidationParameters.validIssuers != null
            && this.tokenValidationParameters.validIssuers.contains(decodedJWT.getIssuer());
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<ClaimsIdentity> validateToken(
        String token,
        String channelId,
        List<String> requiredEndorsements
    ) {
        return CompletableFuture.supplyAsync(() -> {
            DecodedJWT decodedJWT = JWT.decode(token);
            OpenIdMetadataKey key = this.openIdMetadata.getKey(decodedJWT.getKeyId());
            if (key == null) {
                return null;
            }

            Verification verification = JWT.require(Algorithm.RSA256(key.key, null))
                .acceptLeeway(tokenValidationParameters.clockSkew.getSeconds());
            try {
                verification.build().verify(token);

                // If specified, validate the signing certificate.
                if (
                    tokenValidationParameters.validateIssuerSigningKey
                    && key.certificateChain != null
                    && key.certificateChain.size() > 0
                ) {
                    X509Certificate cert = decodeCertificate(key.certificateChain.get(0));
                    if (!isCertValid(cert)) {
                        throw new JWTVerificationException("Signing certificate is not valid");
                    }
                }

                // Note: On the Emulator Code Path, the endorsements collection is null so the
                // validation code below won't run. This is normal.
                if (key.endorsements != null) {
                    // Validate Channel / Token Endorsements. For this, the channelID present on the
                    // Activity needs to be matched by an endorsement.
                    boolean isEndorsed =
                        EndorsementsValidator.validate(channelId, key.endorsements);
                    if (!isEndorsed) {
                        throw new AuthenticationException(
                            String.format(
                                "Could not validate endorsement for key: %s with endorsements: %s",
                                key.key.toString(), StringUtils.join(key.endorsements)
                            )
                        );
                    }

                    // Verify that additional endorsements are satisfied. If no additional
                    // endorsements are expected, the requirement is satisfied as well
                    boolean additionalEndorsementsSatisfied = requiredEndorsements.stream()
                        .allMatch(
                            (endorsement) -> EndorsementsValidator
                                .validate(endorsement, key.endorsements)
                        );
                    if (!additionalEndorsementsSatisfied) {
                        throw new AuthenticationException(
                            String.format(
                                "Could not validate additional endorsement for key: %s with endorsements: %s",
                                key.key.toString(), StringUtils.join(requiredEndorsements)
                            )
                        );
                    }
                }

                if (!this.allowedSigningAlgorithms.contains(decodedJWT.getAlgorithm())) {
                    throw new AuthenticationException(
                        String.format(
                            "Could not validate algorithm for key: %s with algorithms: %s",
                            decodedJWT.getAlgorithm(), StringUtils.join(allowedSigningAlgorithms)
                        )
                    );
                }

                return new ClaimsIdentity(decodedJWT);
            } catch (JWTVerificationException | CertificateException ex) {
                LOGGER.warn(ex.getMessage());
                throw new AuthenticationException(ex);
            }
        }, ExecutorFactory.getExecutor());
    }

    private X509Certificate decodeCertificate(String certStr) throws CertificateException {
        byte[] decoded = Base64.getDecoder().decode(certStr);
        return (X509Certificate) CertificateFactory
            .getInstance("X.509").generateCertificate(new ByteArrayInputStream(decoded));
    }

    private boolean isCertValid(X509Certificate cert) {
        if (cert == null) {
            return false;
        }

        long now = new Date().getTime();
        long clockskew = tokenValidationParameters.clockSkew.toMillis();
        long startValid = cert.getNotBefore().getTime() - clockskew;
        long endValid = cert.getNotAfter().getTime() + clockskew;
        return now >= startValid && now <= endValid;
    }
}
