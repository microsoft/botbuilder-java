// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * Middleware for adding an object to or registering a service with the current
 * turn context.
 *
 * @param <T> The typeof service to add.
 */
public class RegisterClassMiddleware<T> implements Middleware {
    private String key;

    /**
     * Initializes a new instance of the RegisterClassMiddleware class.
     *
     * @param service The Service to register.
     */
    public RegisterClassMiddleware(T service) {
        this.service = service;
    }

    /**
     * Initializes a new instance of the RegisterClassMiddleware class.
     *
     * @param service The Service to register.
     * @param key     optional key for service object in turn state. Default is name
     *                of service.
     */
    public RegisterClassMiddleware(T service, String key) {
        this.service = service;
        this.key = key;
    }

    private T service;

    /**
     * Gets the Service.
     *
     * @return The Service.
     */
    public T getService() {
        return service;
    }

    /**
     * Sets the Service.
     *
     * @param withService The value to set the Service to.
     */
    public void setService(T withService) {
        this.service = withService;
    }

    /**
     * Adds the associated object or service to the current turn context.
     * @param turnContext The context object for this turn.
     * @param next The delegate to call to continue the bot middleware pipeline.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        if (!StringUtils.isBlank(key)) {
            turnContext.getTurnState().add(key, service);
        } else {
            turnContext.getTurnState().add(service);
        }
        return next.next();
    }
}
