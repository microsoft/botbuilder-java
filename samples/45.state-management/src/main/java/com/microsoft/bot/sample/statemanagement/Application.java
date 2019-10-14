// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.statemanagement;

import com.microsoft.bot.integration.AdapterWithErrorHandler;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is the starting point of the Sprint Boot Bot application.
 *
 * This class also provides overrides for dependency injections.  A class that extends the
 * {@link com.microsoft.bot.builder.Bot} interface should be annotated with @Component.
 *
 * @see StateManagementBot
 */
@SpringBootApplication
public class Application extends BotDependencyConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Returns a custom Adapter that provides error handling.
     *
     * @param configuration The Configuration object to use.
     * @return An error handling BotFrameworkHttpAdapter.
     */
    @Override
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new AdapterWithErrorHandler(configuration);
    }
}
