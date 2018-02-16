package com.microsoft.bot.connector.customizations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.microsoft.aad.adal4j.AuthenticationException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

public class JwtTokenExtractor {
    private static final ConcurrentMap<String, OpenIdMetadata> openIdMetadataCache = new ConcurrentHashMap<>();

    private TokenValidationParameters tokenValidationParameters;
    private List<String> allowedSigningAlgorithms;
    private Function<List<String>, Boolean> validator;
    private OpenIdMetadata openIdMetadata;

    public JwtTokenExtractor(TokenValidationParameters tokenValidationParameters, String metadataUrl, List<String> allowedSigningAlgorithms, Function<List<String>, Boolean> validator) {
        this.tokenValidationParameters = new TokenValidationParameters(tokenValidationParameters);
        this.tokenValidationParameters.requireSignedTokens = true;
        this.allowedSigningAlgorithms = allowedSigningAlgorithms;
        if (validator != null) {
            this.validator = validator;
        } else {
            this.validator = (endorsements) -> true;
        }
        this.openIdMetadata = openIdMetadataCache.computeIfAbsent(metadataUrl, key -> new OpenIdMetadata(metadataUrl));
    }

    public CompletableFuture<ClaimsIdentity> getIdentityAsync(String authorizationHeader) {
        if (authorizationHeader == null) {
            return CompletableFuture.completedFuture(null);
        }

        String[] parts = authorizationHeader.split(" ");
        if (parts.length != 2) {
            return CompletableFuture.completedFuture(null);
        }

        return getIdentityAsync(parts[0], parts[1]);
    }

    public CompletableFuture<ClaimsIdentity> getIdentityAsync(String schema, String token) {
        // No header in correct scheme or no token
        if (!schema.equalsIgnoreCase("bearer") || token == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Issuer isn't allowed? No need to check signature
        if (!this.hasAllowedIssuer(token)) {
            return CompletableFuture.completedFuture(null);
        }

        return this.validateTokenAsync(token);
    }

    private boolean hasAllowedIssuer(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return this.tokenValidationParameters.validIssuers != null && this.tokenValidationParameters.validIssuers.contains(decodedJWT.getIssuer());
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<ClaimsIdentity> validateTokenAsync(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        OpenIdMetadataKey key = openIdMetadata.getKey(decodedJWT.getKeyId());
        if (key != null) {
            Verification verification = JWT.require(Algorithm.RSA256(key.key, null));
            if (!tokenValidationParameters.validateLifetime) {
                verification = verification
                        .acceptExpiresAt(System.currentTimeMillis() + 500)
                        .acceptNotBefore(0);
            }
            try {
                verification.build().verify(token);
                if (!validator.apply(key.endorsements)) {
                    throw new AuthenticationException(String.format("Could not validate endorsement for key: %s with endorsements: %s", decodedJWT.getKeyId(), StringUtils.join(key.endorsements)));
                }
                if(!allowedSigningAlgorithms.contains(decodedJWT.getAlgorithm())) {
                    throw new AuthenticationException(String.format("Could not validate algorithm for key: %s with algorithms: %s", decodedJWT.getAlgorithm(), StringUtils.join(allowedSigningAlgorithms)));
                }
                Map<String, String> claims = new HashMap<>();
                if (decodedJWT.getClaims() != null) {
                    decodedJWT.getClaims().forEach((k,v) -> claims.put(k, v.asString()));
                }
                return CompletableFuture.completedFuture(new ClaimsIdentityImpl(decodedJWT.getIssuer(), claims));

            } catch (JWTVerificationException ex) {
                String errorDescription = ex.getMessage();
                LOGGER.log(Level.WARNING, errorDescription);
                return CompletableFuture.completedFuture(null);
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
