package com.microsoft.bot.builder;

import com.microsoft.bot.builder.ServiceKeyAlreadyRegisteredException;

import java.util.Map;

/// <summary>
/// Represents a set of collection of services associated with the <see cref="ITurnContext"/>.
/// </summary>
/// <remarks>
/// TODO: add more details on what kind of services can/should be stored here, by whom and what the lifetime semantics are, etc.
/// </remarks>
public interface TurnContextServiceCollection extends Iterable<Map.Entry<String, Object>>, AutoCloseable {
    /// <summary>
    /// Add a service with a specified key.
    /// </summary>
    /// <typeparam name="TService">The type of service to be added.</typeparam>
    /// <param name="key">The key to store the service under.</param>
    /// <param name="service">The service to add.</param>
    /// <exception cref="ServiceKeyAlreadyRegisteredException">Thrown when a service is already registered with the specified <paramref name="key"/></exception>
     <TService extends Object> void Add(String key, TService service) throws IllegalArgumentException, ServiceKeyAlreadyRegisteredException;

    /// <summary>
    /// Get a service by its key.
    /// </summary>
    /// <typeparam name="TService">The type of service to be retrieved.</typeparam>
    /// <param name="key">The key of the service to get.</param>
    /// <returns>The service stored under the specified key.</returns>
    <TService extends Object> TService Get(String key) throws IllegalArgumentException;

}



