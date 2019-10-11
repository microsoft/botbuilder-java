// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.echo;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.ClasspathPropertiesConfiguration;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.ConfigurationChannelProvider;
import com.microsoft.bot.integration.ConfigurationCredentialProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * This provides the default dependency creation for a Bot application.
 *
 * This class should be subclassed by a class with the {@link org.springframework.context.annotation.Configuration}
 * annotation (or SpringBootApplication annotation).
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
     * Returns the {@link Bot} object for the application.  A subclass must implement
     * this method.
     *
     * While configuration as scope Prototype, it's lifetime is managed by the
     * BotFrameworkHttpAdapter, which is singleton; effectively making this object
     * singleton as well.  The Bot object should not needlessly retain state.
     *
     * @return A {@link Bot} object.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public abstract Bot getBot();
}
