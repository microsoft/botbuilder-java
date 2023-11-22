// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration.spring;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.inspection.InspectionState;
import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.ClasspathPropertiesConfiguration;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.ConfigurationChannelProvider;
import com.microsoft.bot.integration.ConfigurationCredentialProvider;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;

/**
 * This provides the default dependency creation for a Bot application.
 *
 * <p>
 * This class should be subclassed by a class with the
 * {@link org.springframework.context.annotation.Configuration} annotation (or
 * SpringBootApplication annotation).
 * </p>
 *
 * <p>
 * The Bot should be annotated with @Component, possibly including @AutoWired to
 * indicate which constructor to use.
 * </p>
 */
public abstract class BotDependencyConfiguration {
    /**
     * Returns an ExecutorService.
     *
     * <p>
     * For now, return the bot-connector ExecutorService. This is an area of
     * consideration. The goal here is to have a common ExecutorService to avoid
     * multiple thread pools.
     * </p>
     *
     * @return An ExecutorService.
     */
    @Bean
    public ExecutorService getExecutorService() {
        return ExecutorFactory.getExecutor();
    }

    /**
     * Returns the Configuration for the application.
     *
     * By default, it uses the {@link ClasspathPropertiesConfiguration} class.
     * Default scope of Singleton.
     *
     * @return A Configuration object.
     */
    @Bean
    public Configuration getConfiguration() {
        return new ClasspathPropertiesConfiguration();
    }


    /**
     * Returns the AuthenticationConfiguration for the application.
     *
     * By default, it uses the {@link AuthenticationConfiguration} class.
     * Default scope of Singleton.
     * @param configuration The Configuration object to read from.
     * @return An AuthenticationConfiguration object.
     */
    @Bean
    public AuthenticationConfiguration getAuthenticationConfiguration(Configuration configuration) {
        return new AuthenticationConfiguration();
    }

    /**
     * Returns the CredentialProvider for the application.
     *
     * By default, it uses the {@link ConfigurationCredentialProvider} class.
     * Default scope of Singleton.
     *
     * @param configuration The Configuration object to read from.
     * @return A CredentialProvider object.
     *
     * @see #getConfiguration()
     */
    @Bean
    public CredentialProvider getCredentialProvider(Configuration configuration) {
        return new ConfigurationCredentialProvider(configuration);
    }

    /**
     * Returns the ChannelProvider for the application.
     *
     * By default, it uses the {@link ConfigurationChannelProvider} class. Default
     * scope of Singleton.
     *
     * @param configuration The Configuration object to read from.
     * @return A ChannelProvider object.
     *
     * @see #getConfiguration()
     */
    @Bean
    public ChannelProvider getChannelProvider(Configuration configuration) {
        return new ConfigurationChannelProvider(configuration);
    }

    /**
     * Returns the BotFrameworkHttpAdapter for the application.
     *
     * By default, it uses the {@link BotFrameworkHttpAdapter} class. Default scope
     * of Singleton.
     *
     * @param configuration The Configuration object to read from.
     * @return A BotFrameworkHttpAdapter object.
     *
     * @see #getConfiguration()
     */
    @Bean
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new BotFrameworkHttpAdapter(configuration);
    }

    /**
     * Returns a {@link Storage} object. Default scope of Singleton.
     *
     * @return A Storage object.
     */
    @Bean
    public Storage getStorage() {
        return new MemoryStorage();
    }

    /**
     * Returns a ConversationState object. Default scope of Singleton.
     *
     * @param storage The Storage object to use.
     * @return A ConversationState object.
     */
    @Bean
    public ConversationState getConversationState(Storage storage) {
        return new ConversationState(storage);
    }

    /**
     * Returns a UserState object. Default scope of Singleton.
     *
     * @param storage The Storage object to use.
     * @return A UserState object.
     */
    @Bean
    public UserState getUserState(Storage storage) {
        return new UserState(storage);
    }

    /**
     * Creates an InspectionState used by
     * {@link com.microsoft.bot.builder.inspection.InspectionMiddleware}. Default
     * scope of Singleton.
     *
     * @param storage The Storage to use.
     *                {@link BotDependencyConfiguration#getStorage()}
     * @return An InspectionState object that uses the specified storage.
     */
    @Bean
    public InspectionState getInspectionState(Storage storage) {
        return new InspectionState(storage);
    }
}
