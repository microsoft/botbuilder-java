package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ResourceResponse;

import java.util.List;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface SendActivitiesHandler {
    ResourceResponse[] handle(TurnContext context, List<Activity> activities, Callable<ResourceResponse[]> next) throws Exception;
}
