package com.microsoft.bot.builder;

/**
 * Thrown to indicate a service is already registered in a {@link ITurnContextServiceCollection} under the specified key.
 */
//[Serializable]
public class ServiceKeyAlreadyRegisteredException extends Throwable  {
    public ServiceKeyAlreadyRegisteredException(String key) {
        super("A services is already registered with the specified key: " + key);
    }
}
