// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.spring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.CredentialProviderImpl;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ActivityTypes;

import java.util.concurrent.CompletableFuture;

/**
 * This is the controller that will receive incoming Channel Activity messages.
 */
@RestController
public class BotController {
    /** The appId from application.properties. */
    @Value("${MicrosoftAppId}")
    private String appId;

    /** The app secret from application.properties. */
    @Value("${MicrosoftAppPassword}")
    private String appPassword;

    private CredentialProvider _credentialProvider;
    private MicrosoftAppCredentials _credentials;

    private Logger logger = LoggerFactory.getLogger(BotController.class);

    /**
     * Performs post construction initialization for this controller.
     * This must be done post construction so that application.properties
     * have been loaded.
     */
    @PostConstruct
    public void init() {
        _credentialProvider = new CredentialProviderImpl(appId, appPassword);
        _credentials = new MicrosoftAppCredentials(appId, appPassword);
    }

    /**
     * This will receive incoming Channel Activities.
     *
     * @param activity
     * @param authHeader
     * @return
     */
    @PostMapping("/api/messages")
    public ResponseEntity<Object> incoming(@RequestBody Activity activity,
            @RequestHeader(value = "Authorization", defaultValue = "") String authHeader) {
        try {
            CompletableFuture<ClaimsIdentity> authenticateRequest = JwtTokenValidation.authenticateRequest(activity, authHeader, _credentialProvider);
            authenticateRequest.thenRunAsync(() -> {
                if (activity.type().equals(ActivityTypes.MESSAGE)) {
                    logger.info("Received: " + activity.text());

                    // reply activity with the same text
                    ConnectorClientImpl connector = new ConnectorClientImpl(activity.serviceUrl(), _credentials);
                    connector.conversations().sendToConversation(activity.conversation().id(),
                            new Activity().withType(ActivityTypes.MESSAGE).withText("Echo: " + activity.text())
                                    .withRecipient(activity.from()).withFrom(activity.recipient()));
                }
            });
        } catch (AuthenticationException ex) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // send ack to user activity
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
