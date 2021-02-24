// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.multilingual;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.integration.AdapterWithErrorHandler;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import com.microsoft.bot.sample.multilingual.translation.MicrosoftTranslator;
import com.microsoft.bot.sample.multilingual.translation.TranslationMiddleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

//
// This is the starting point of the Sprint Boot Bot application.
//
@SpringBootApplication

// Use the default BotController to receive incoming Channel messages. A custom
// controller could be used by eliminating this import and creating a new
// org.springframework.web.bind.annotation.RestController.
// The default controller is created by the Spring Boot container using
// dependency injection. The default route is /api/messages.
@Import({BotController.class})

/**
 * This class extends the BotDependencyConfiguration which provides the default
 * implementations for a Bot application.  The Application class should
 * override methods in order to provide custom implementations.
 */
public class Application extends BotDependencyConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Returns the Bot for this application.
     *
     * <p>
     *     The @Component annotation could be used on the Bot class instead of this method
     *     with the @Bean annotation.
     * </p>
     *
     * @return The Bot implementation for this application.
     */
    @Bean
    public Bot getBot(UserState userState) {
        return new MultiLingualBot(userState);
    }

    /**
     * Returns a custom Adapter that provides error handling.
     *
     * @param configuration The Configuration object to use.
     * @return An error handling BotFrameworkHttpAdapter.
     */
    @Override
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        Storage storage = this.getStorage();
        ConversationState conversationState = this.getConversationState(storage);

        BotFrameworkHttpAdapter adapter = new AdapterWithErrorHandler(configuration, conversationState);
        TranslationMiddleware translationMiddleware = this.getTranslationMiddleware(configuration);
        adapter.use(translationMiddleware);
        return adapter;
    }

    /**
     * Create the Microsoft Translator responsible for making calls to the Cognitive Services translation service.
     * @param configuration The Configuration object to use.
     * @return MicrosoftTranslator
     */
    @Bean
    public MicrosoftTranslator getMicrosoftTranslator(Configuration configuration) {
        return new MicrosoftTranslator(configuration);
    }

    /**
     * Create the Translation Middleware that will be added to the middleware pipeline in the AdapterWithErrorHandler.
     * @param configuration The Configuration object to use.
     * @return TranslationMiddleware
     */
    @Bean
    public TranslationMiddleware getTranslationMiddleware(Configuration configuration) {
        Storage storage = this.getStorage();
        UserState userState = this.getUserState(storage);
        MicrosoftTranslator microsoftTranslator = this.getMicrosoftTranslator(configuration);
        return new TranslationMiddleware(microsoftTranslator, userState);
    }
}
