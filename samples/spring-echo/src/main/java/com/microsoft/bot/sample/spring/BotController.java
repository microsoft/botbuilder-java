// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.spring;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.connector.authentication.*;
import com.microsoft.bot.schema.Activity;
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
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.schema.ActivityTypes;

import java.util.concurrent.CompletionException;

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
        _credentialProvider = new SimpleCredentialProvider(appId, appPassword);
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
            JwtTokenValidation.authenticateRequest(activity, authHeader, _credentialProvider, new SimpleChannelProvider())
                .thenRunAsync(() -> {
                    if (activity.getType().equals(ActivityTypes.MESSAGE)) {
                        logger.info("Received: " + activity.getText());

                        // reply activity with the same text
                        ConnectorClient connector = new RestConnectorClient(activity.getServiceUrl(), _credentials);
                        connector.conversations().sendToConversation(
                            activity.getConversation().getId(),
                            activity.createReply("Echo: " + activity.getText()));
                    }
                }, ExecutorFactory.getExecutor()).join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof AuthenticationException) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // send ack to user activity
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
