package com.microsoft.bot.builder;

public interface PropertyManager {
   <T> StatePropertyAccessor<T> createProperty(String name);
}
