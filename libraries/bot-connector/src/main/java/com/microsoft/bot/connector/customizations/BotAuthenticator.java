package com.microsoft.bot.connector.customizations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import org.apache.commons.lang3.StringUtils;

public class BotAuthenticator {
    private class BotConnectorEndpoint {
        private String refreshEndpoint;
        private String refreshScope;
        private String botConnectorOpenIdMetadata;
        private String botConnectorIssuer;
        private String botConnectorAudience;
        private String emulatorOpenIdMetadata;
        private String emulatorAuthV31IssuerV1;
        private String emulatorAuthV31IssuerV2;
        private String emulatorAuthV32IssuerV1;
        private String emulatorAuthV32IssuerV2;
        private String emulatorAudience;
    }

    private class BotAuthenticatorSettings {
        private String appId;
        private String appPassword;
        private BotConnectorEndpoint endpoint;
        private String openIdMetadata;
    }

    private class JwtVerifyOptions {
        Algorithm algorithm;
        String issuer;
        String audience;
        int clockTolerance;
    }

    private BotAuthenticatorSettings settings;
    private OpenIdMetadata botConnectorOpenIdMetadata;
    private OpenIdMetadata emulatorOpenIdMetadata;

    public BotAuthenticator(BotCredentials credentials) {
        this.settings = new BotAuthenticatorSettings();
        this.settings.appId = credentials.appId();
        this.settings.appPassword = credentials.appPassword();
        if (this.settings.endpoint == null) {
            BotConnectorEndpoint endpoint = new BotConnectorEndpoint();
            endpoint.refreshEndpoint = AuthSettings.REFRESH_ENDPOINT;
            endpoint.refreshScope = AuthSettings.REFRESH_SCOPE;
            endpoint.botConnectorOpenIdMetadata = AuthSettings.BOT_CONNECTOR_OPEN_ID_METADATA;
            endpoint.botConnectorIssuer = AuthSettings.BOT_CONNECTOR_ISSUER;
            endpoint.emulatorOpenIdMetadata = AuthSettings.EMULATOR_OPEN_ID_METADATA;
            endpoint.emulatorAuthV31IssuerV1 = AuthSettings.EMULATOR_AUTH_V31_ISSUER_V1;
            endpoint.emulatorAuthV31IssuerV2 = AuthSettings.EMULATOR_AUTH_V31_ISSUER_V2;
            endpoint.emulatorAuthV32IssuerV1 = AuthSettings.EMULATOR_AUTH_V32_ISSUER_V1;
            endpoint.emulatorAuthV32IssuerV2 = AuthSettings.EMULATOR_AUTH_V32_ISSUER_V2;

            if (this.settings.openIdMetadata != null) {
                endpoint.botConnectorOpenIdMetadata = this.settings.openIdMetadata;
            }

            if (this.settings.appId != null) {
                endpoint.botConnectorAudience = this.settings.appId;
                endpoint.emulatorAudience = this.settings.appId;
            }
            this.settings.endpoint = endpoint;
        }
        this.botConnectorOpenIdMetadata = new OpenIdMetadata(this.settings.endpoint.botConnectorOpenIdMetadata);
        this.emulatorOpenIdMetadata = new OpenIdMetadata(this.settings.endpoint.emulatorOpenIdMetadata);
    }

    public boolean authenticate(Headers headers, String channelId, String serviceUrl) {
        boolean isEmulator = channelId.equalsIgnoreCase("emulator");

        String authHeader = headers.getFirst("authorization");
        if (authHeader == null) {
            authHeader = headers.getFirst("Authorization");
        }

        String token = "";
        if (authHeader != null) {
            String[] auth = authHeader.trim().split(" ");
            if (auth.length == 2 && auth[0].equalsIgnoreCase("bearer")) {
                token = auth[1];
            }
        }

        if (!token.isEmpty()) {
            DecodedJWT decodedJWT = JWT.decode(token);

            JwtVerifyOptions verifyOptions = null;
            OpenIdMetadata openIdMetadata = null;

            String decodedServiceUrl = decodedJWT.getClaim("serviceurl").asString();
            decodedServiceUrl = (decodedServiceUrl == null) ? "" : decodedServiceUrl;

            if (isEmulator) {
                String ver = decodedJWT.getClaim("ver").asString();
                String azp = decodedJWT.getClaim("azp").asString();
                String appid = decodedJWT.getClaim("appid").asString();
                String iss = decodedJWT.getClaim("iss").asString();

                // validate the claims from the emulator
                if ((ver.equalsIgnoreCase("2.0") && !azp.equalsIgnoreCase(this.settings.appId)) ||
                        (!ver.equalsIgnoreCase("2.0") && !appid.equalsIgnoreCase(this.settings.appId))) {
                    // TODO: record error : ChatConnector: receive - invalid token. Requested by unexpected app ID.
                    return false;
                }

                // the token came from the emulator, so ensure the correct issuer is used
                String issuer = "";
                if (ver.equalsIgnoreCase("1.0") && iss.equalsIgnoreCase(this.settings.endpoint.emulatorAuthV31IssuerV1)) {
                    // This token came from the emulator as a v1 token using the Auth v3.1 issuer
                    issuer = this.settings.endpoint.emulatorAuthV31IssuerV1;
                } else if (ver.equalsIgnoreCase("2.0") && iss.equalsIgnoreCase(this.settings.endpoint.emulatorAuthV31IssuerV2)) {
                    // This token came from the emulator as a v2 token using the Auth v3.1 issuer
                    issuer = this.settings.endpoint.emulatorAuthV31IssuerV2;
                } else if (ver.equalsIgnoreCase("1.0") && iss.equalsIgnoreCase(this.settings.endpoint.emulatorAuthV32IssuerV1)) {
                    // This token came from the emulator as a v1 token using the Auth v3.2 issuer
                    issuer = this.settings.endpoint.emulatorAuthV32IssuerV1;
                } else if (ver.equalsIgnoreCase("2.0") && iss.equalsIgnoreCase(this.settings.endpoint.emulatorAuthV32IssuerV2)) {
                    // This token came from the emulator as a v2 token using the Auth v3.2 issuer
                    issuer = this.settings.endpoint.emulatorAuthV32IssuerV2;
                }

                if (!issuer.isEmpty()) {
                    openIdMetadata = this.emulatorOpenIdMetadata;
                    verifyOptions = new JwtVerifyOptions();
                    verifyOptions.algorithm = null; // TODO: Add algorithm
                    verifyOptions.issuer = issuer;
                    verifyOptions.audience = this.settings.endpoint.emulatorAudience;
                    verifyOptions.clockTolerance = 300;
                }
            }

            if (verifyOptions == null) {
                // This is a normal token, so use our Bot Connector verification
                openIdMetadata = this.botConnectorOpenIdMetadata;
                verifyOptions = new JwtVerifyOptions();
                verifyOptions.issuer = this.settings.endpoint.botConnectorIssuer;
                verifyOptions.audience = this.settings.endpoint.emulatorAudience;
                verifyOptions.clockTolerance = 300;
            }

            if (openIdMetadata != null) {
                OpenIdMetadataKey key = openIdMetadata.getKey(decodedJWT.getKeyId());
                if (key != null) {
                    JWTVerifier verifier = JWT.require(Algorithm.RSA256(key.key, null))
                            .withIssuer(verifyOptions.issuer)
                            .withAudience(verifyOptions.audience)
                            .build();
                    try {
                        verifier.verify(token);

                        // enforce endorsements in openIdMetadadata if there is any endorsements associated with the key
                        if (!channelId.isEmpty() && key.endorsements != null && !key.endorsements.contains(channelId)) {
                            String errorDescription = String.format("channelId in req.body: %s didn't match the endorsements: %s}.", channelId, StringUtils.join(key.endorsements));
                            // TODO: record error : BotAuthenticator: receive - endorsements validation failure.
                            return false;
                        }

                        if (!decodedServiceUrl.isEmpty() && !serviceUrl.isEmpty() && !serviceUrl.equalsIgnoreCase(decodedServiceUrl)) {
                            String errorDescription = String.format("ServiceUrl in payload of token: %s didn't match the request's serviceurl: %s.", decodedServiceUrl, serviceUrl);
                            // TODO: record error : BotAuthenticator: receive - serviceurl mismatch.
                            return false;
                        }
                    } catch (JWTVerificationException ex) {
                        String errorDescription = ex.getMessage();
                        // TODO: record error : BotAuthenticator: receive - invalid token. Check bot's app ID & Password.
                        return false;
                    }

                    return true;
                }
            }
        } else if (isEmulator && !this.settings.appId.isEmpty() && !this.settings.appPassword.isEmpty()) {
            // Emulator running without auth enabled
            // TODO: record warning
            return true;
        } else {
            // Token not provided so
            String errorDescription = "BotAuthenticator: receive - no security token sent.";
            return  false;
        }
        return true;
    }
}
