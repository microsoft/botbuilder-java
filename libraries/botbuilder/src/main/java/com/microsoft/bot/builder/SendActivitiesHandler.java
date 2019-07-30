package com.microsoft.bot.builder;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ResourceResponse;

import java.util.List;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface SendActivitiesHandler {
    ResourceResponse[] handle(TurnContext context, List<Activity> activities, Callable<ResourceResponse[]> next) throws Exception;
}
