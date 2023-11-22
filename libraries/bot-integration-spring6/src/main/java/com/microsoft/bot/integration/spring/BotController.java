// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration.spring;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.connector.authentication.AuthenticationException;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.schema.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * This is the default controller that will receive incoming Channel Activity
 * messages.
 *
 * <p>
 * This controller is suitable in most cases. Bots that want to use this
 * controller should do so by using the @Import({BotController.class})
 * annotation. See any of the samples Application class for an example.
 * </p>
 */
@RestController
public class BotController {
    /**
     * The slf4j Logger to use. Note that slf4j is configured by providing Log4j
     * dependencies in the POM, and corresponding Log4j configuration in the
     * 'resources' folder.
     */
    private Logger logger = LoggerFactory.getLogger(BotController.class);

    /**
     * The BotFrameworkHttpAdapter to use. Note it is provided by dependency
     * injection via the constructor.
     */
    private final BotFrameworkHttpAdapter adapter;

    /**
     * The Bot to use. Note it is provided by dependency
     * injection via the constructor.
     */
    private final Bot bot;

    /**
     * Spring will use this constructor for creation.
     *
     * <p>
     * The Bot application should define class that implements {@link Bot} and
     * annotate it with @Component.
     * </p>
     *
     * @see BotDependencyConfiguration
     *
     * @param withAdapter The BotFrameworkHttpAdapter to use.
     * @param withBot     The Bot to use.
     */
    public BotController(BotFrameworkHttpAdapter withAdapter, Bot withBot) {
        adapter = withAdapter;
        bot = withBot;
    }

    /**
     * This will receive incoming Channel Activities.
     *
     * @param activity   The incoming Activity.
     * @param authHeader The incoming Authorization header.
     * @return The request response.
     */
    @PostMapping("/api/messages")
    public CompletableFuture<ResponseEntity<Object>> incoming(
        @RequestBody Activity activity,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {

        return adapter.processIncomingActivity(authHeader, activity, bot)

            .handle((result, exception) -> {
                if (exception == null) {
                    if (result != null) {
                        return new ResponseEntity<>(
                            result.getBody(),
                            HttpStatus.valueOf(result.getStatus())
                        );
                    }
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
