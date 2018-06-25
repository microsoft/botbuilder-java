package com.microsoft.bot.builder;

import com.microsoft.bot.builder.ServiceKeyAlreadyRegisteredException;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface NextDelegate {
     CompletableFuture next() throws Exception, ServiceKeyAlreadyRegisteredException;
}
