package com.microsoft.bot.builder;

@FunctionalInterface
public interface NextDelegate {
     void next() throws Exception;
}
