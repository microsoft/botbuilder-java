// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.inspection;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.inspection.InspectionState;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * This is the starting point of the Sprint Boot Bot application.
 *
 * This class also provides overrides for dependency injections.  A class that extends the
 * {@link com.microsoft.bot.builder.Bot} interface should be annotated with @Component.
 *
 * @see EchoBot
 */
@SpringBootApplication
public class Application extends BotDependencyConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Create an adapter with InspectionMiddleware.
     *
     * <p>NOTE: This is marked as @Primary to override the default Bean.</p>
     *
     * @param configuration The configuration.  {@link BotDependencyConfiguration#getConfiguration()}
     * @param inspectionState The InspectionState.  {@link Application#getInspectionState(Storage)}
     * @param userState The UserState.  {@link BotDependencyConfiguration#getUserState(Storage)}
     * @param conversationState The ConversationState. {@link BotDependencyConfiguration#getConversationState(Storage)}
     * @return An AdapterWithInspection object.
     *
     * @see BotController#BotController(BotFrameworkHttpAdapter, Bot)
     */
    @Bean
    @Primary
    public BotFrameworkHttpAdapter getInspectionBotFrameworkHttpAdapter(Configuration configuration,
                                                                        InspectionState inspectionState,
                                                                        UserState userState,
                                                                        ConversationState conversationState) {
        return new AdapterWithInspection(configuration, inspectionState, userState, conversationState);
    }

    /**
     * Creates an InspectionState object.
     * @param storage The Storage to use. {@link BotDependencyConfiguration#getStorage()}
     * @return An InspectionState object that uses the specified storage.
     */
    @Bean
    public InspectionState getInspectionState(Storage storage) {
        return new InspectionState(storage);
    }
}
