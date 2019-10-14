// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.suggestedactions;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.connector.authentication.AuthenticationException;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.schema.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * This is the controller that will receive incoming Channel Activity messages.
 *
 * <p>This class provides a route for the "/api/messages" path.  Additional routes
 * could be supplied.  For example, to handle proactive messages.</p>
 */
@RestController
public class BotController {
    /**
     * The slf4j Logger to use.  Note that slf4j is configured by providing
     * Log4j dependencies in the POM, and corresponding Log4j configuration in
     * the 'resources' folder.
     */
    private Logger logger = LoggerFactory.getLogger(BotController.class);

    /**
     * The BotFrameworkHttpAdapter to use.  Note is is provided by dependency
     * injection via the constructor.
     *
     * See DefaultDependencyConfiguration#getBotFrameworkHttpAdaptor(Configuration).
     */
    private final BotFrameworkHttpAdapter adapter;

    /**
     * The BotFrameworkHttpAdapter to use.  Note is is provided by dependency
     * injection via the constructor.
     *
     * See DefaultDependencyConfiguration#getBot.
     */
    private final Bot bot;

    /**
     * Autowires Spring to use this constructor for creation.
     *
     * See DefaultDependencyConfiguration#getBotFrameworkHttpAdaptor(Configuration).
     * See DefaultDependencyConfiguration#getBot.
     *
     * @param withAdapter  The BotFrameworkHttpAdapter to use.
     * @param withBot The Bot to use.
     */
    @Autowired
    public BotController(BotFrameworkHttpAdapter withAdapter, Bot withBot) {
        adapter = withAdapter;
        bot = withBot;
    }

    /**
     * This will receive incoming Channel Activities.
     *
     * @param activity The incoming Activity.
     * @param authHeader The incoming Authorization header.
     * @return The request response.
     */
    @PostMapping("/api/messages")
    public CompletableFuture<ResponseEntity<Object>> incoming(
        @RequestBody Activity activity,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader) {

        return adapter.processIncomingActivity(authHeader, activity, bot)

            .handle((result, exception) -> {
                if (exception == null) {
                    return new ResponseEntity<>(HttpStatus.ACCEPTED);
                }

                logger.error("Exception handling message", exception);

                if (exception instanceof CompletionException) {
                    if (exception.getCause() instanceof AuthenticationException) {
                        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                    } else {
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
    }
}
