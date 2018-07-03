package com.microsoft.bot.builder;

import com.microsoft.bot.builder.ServiceKeyAlreadyRegisteredException;

import java.util.Map;

/**
 * Represents a set of collection of services associated with the {@link ITurnContext}.
 *
 * TODO: add more details on what kind of services can/should be stored here, by whom and what the lifetime semantics are, etc.
 *
 */
public interface TurnContextServiceCollection extends Iterable<Map.Entry<String, Object>>, AutoCloseable {
    /**
     * Add a service with a specified key.
     * @param TService The type of service to be added.
     * @param key The key to store the service under.
     * @param service The service to add.
     * @throws ServiceKeyAlreadyRegisteredException Thrown when a service is already registered with the specified {@code key}
     */
     <TService extends Object> void Add(String key, TService service) throws IllegalArgumentException, ServiceKeyAlreadyRegisteredException;

    /**
     * Get a service by its key.
     * @param TService The type of service to be retrieved.
     * @param key The key of the service to get.
     * @return The service stored under the specified key.
     */
    <TService extends Object> TService Get(String key) throws IllegalArgumentException;

}



