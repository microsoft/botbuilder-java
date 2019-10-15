// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.inspection;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.ClasspathPropertiesConfiguration;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.ConfigurationChannelProvider;
import com.microsoft.bot.integration.ConfigurationCredentialProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * This provides the default dependency creation for a Bot application.
 *
 * <p>This class should be subclassed by a class with the {@link org.springframework.context.annotation.Configuration}
 * annotation (or SpringBootApplication annotation).</p>
 *
 * <p>The Bot should be annotated with @Component, possibly including @AutoWired to indicate which
 * constructor to use.</p>
 */
public abstract class BotDependencyConfiguration {
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
     * Returns the CredentialProvider for the application.
     *
     * By default, it uses the {@link ConfigurationCredentialProvider} class.
     * Default scope of Singleton.
     *
     * @return A CredentialProvider object.
     */
    @Autowired
    @Bean
    public CredentialProvider getCredentialProvider(Configuration configuration) {
        return new ConfigurationCredentialProvider(configuration);
    }

    /**
     * Returns the ChannelProvider for the application.
     *
     * By default, it uses the {@link ConfigurationChannelProvider} class.
     * Default scope of Singleton.
     *
     * @return A ChannelProvider object.
     */
    @Autowired
    @Bean
    public ChannelProvider getChannelProvider(Configuration configuration) {
        return new ConfigurationChannelProvider(configuration);
    }

    /**
     * Returns the BotFrameworkHttpAdapter for the application.
     *
     * By default, it uses the {@link BotFrameworkHttpAdapter} class.
     * Default scope of Singleton.
     *
     * @return A BotFrameworkHttpAdapter object.
     */
    @Autowired
    @Bean
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new BotFrameworkHttpAdapter(configuration);
    }

    /**
     * Returns a {@link Storage} object.
     * @return A Storage object.
     */
    @Bean
    public Storage getStorage() {
        return new MemoryStorage();
    }

    /**
     * Returns a ConversationState object.
     * @param storage The Storage object to use.
     * @return A ConversationState object.
     */
    @Autowired
    @Bean
    public ConversationState getConversationState(Storage storage) {
        return new ConversationState(storage);
    }

    /**
     * Returns a UserState object.
     * @param storage The Storage object to use.
     * @return A UserState object.
     */
    @Autowired
    @Bean
    public UserState getUserState(Storage storage) {
        return new UserState(storage);
    }
}
