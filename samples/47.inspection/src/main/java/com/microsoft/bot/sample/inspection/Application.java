// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.inspection;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.inspection.InspectionState;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.AdapterWithInspection;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * This is the starting point of the Sprint Boot Bot application.
 *
 * <p>
 * This class could provide overrides for dependency injections. A class that
 * extends the {@link com.microsoft.bot.builder.Bot} interface should be
 * annotated with @Component.
 * </p>
 *
 * <p>
 * See README.md for details on using the InspectionMiddleware.
 * </p>
 *
 * @see BotDependencyConfiguration
 * @see EchoBot
 */
@SpringBootApplication

// Use the default BotController to receive incoming Channel messages. A custom
// controller could be used by eliminating this import and creating a new
// RestController.
// The default controller is created by the Spring Boot container using
// dependency injection. The default route is /api/messages.
@Import({ BotController.class })

public class Application extends BotDependencyConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Create an adapter with InspectionMiddleware.
     *
     * <p>
     * NOTE: This is marked as @Primary to override the default Bean.
     * </p>
     *
     * @param configuration     The configuration.
     *                          {@link BotDependencyConfiguration#getConfiguration()}
     * @param inspectionState   The InspectionState.
     *                          {@link BotDependencyConfiguration#getInspectionState(Storage)}
     * @param userState         The UserState.
     *                          {@link BotDependencyConfiguration#getUserState(Storage)}
     * @param conversationState The ConversationState.
     *                          {@link BotDependencyConfiguration#getConversationState(Storage)}
     * @return An AdapterWithInspection object.
     */
    @Bean
    @Primary
    public BotFrameworkHttpAdapter getInspectionBotFrameworkHttpAdapter(
        Configuration configuration,
        InspectionState inspectionState,
        UserState userState,
        ConversationState conversationState
    ) {
        return new AdapterWithInspection(
            configuration,
            inspectionState,
            userState,
            conversationState
        );
    }
}
