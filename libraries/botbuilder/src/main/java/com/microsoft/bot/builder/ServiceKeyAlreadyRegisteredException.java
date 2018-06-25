package com.microsoft.bot.builder;

/// <summary>
/// Thrown to indicate a service is already registered in a <see cref="ITurnContextServiceCollection"/> under the specified key.
/// </summary>
//[Serializable]
public class ServiceKeyAlreadyRegisteredException extends Throwable  {
    public ServiceKeyAlreadyRegisteredException(String key) {
        super("A services is already registered with the specified key: " + key);
    }
}
