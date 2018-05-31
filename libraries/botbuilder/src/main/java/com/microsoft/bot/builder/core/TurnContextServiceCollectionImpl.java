package com.microsoft.bot.builder.core;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class TurnContextServiceCollectionImpl implements TurnContextServiceCollection, AutoCloseable
{
    private final HashMap<String, Object> _services = new HashMap<String, Object>();

    public TurnContextServiceCollectionImpl() throws IllegalArgumentException {
    }



    public <TService extends Object> TService Get(String key) throws IllegalArgumentException {
        if (key == null)
            throw new IllegalArgumentException("key");

        TService service = (TService) _services.get(key);
        // TODO: log that we didn't find the requested service
        return (TService) service;
    }
    /// <summary>
    /// Get a service by type using its full type name as the key.
    /// </summary>
    /// <typeparam name="TService">The type of service to be retrieved.</typeparam>
    /// <returns>The service stored under the specified key.</returns>
    public <TService> TService Get(Class<TService> type) throws IllegalArgumentException {
        return this.Get(type.getName());
    }

    @Override
    public <TService extends Object> void Add(String key, TService service) throws IllegalArgumentException, ServiceKeyAlreadyRegisteredException {
        if (key == null) throw new IllegalArgumentException("key");
        if (service == null) throw new IllegalArgumentException("service");

        if (_services.containsKey(key))
            throw new ServiceKeyAlreadyRegisteredException(key);
        _services.put(key, service);
    }
    /// <summary>
    /// Add a service using its full type name as the key.
    /// </summary>
    /// <typeparam name="TService">The type of service to be added.</typeparam>
    /// <param name="service">The service to add.</param>

    public <TService> void Add(TService service, Class<TService> type) throws ServiceKeyAlreadyRegisteredException, IllegalArgumentException {
        Add(type.getName(), service);
    }


    public Iterator<Map.Entry<String, Object>> iterator() {
        return _services.entrySet().iterator();
    }

    @Override
    public void close() throws Exception {
        for (Map.Entry entry : this._services.entrySet()) {
            if (entry.getValue() instanceof AutoCloseable) {
                ((AutoCloseable) entry.getValue()).close();
            }
        }
    }
}

