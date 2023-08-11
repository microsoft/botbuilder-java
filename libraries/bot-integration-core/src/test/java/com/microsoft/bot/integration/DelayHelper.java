// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DelayHelper {

    public static CompletableFuture<Void> test(BotAdapter adapter) {
        TurnContextImpl turnContext = new TurnContextImpl(adapter, new Activity(ActivityTypes.MESSAGE));

        Activity activity1 = new Activity(ActivityTypes.DELAY);
        activity1.setValue(275);
        Activity activity2 = new Activity(ActivityTypes.DELAY);
        activity2.setValue(275L);
        Activity activity3 = new Activity(ActivityTypes.DELAY);
        activity3.setValue(275F);
        Activity activity4 = new Activity(ActivityTypes.DELAY);
        activity4.setValue(275D);
        List<Activity> activities = Arrays.asList(
            activity1,
            activity2,
            activity3,
            activity4
        );

        StopWatch sw = new StopWatch();

        sw.start();

        adapter.sendActivities(turnContext, activities).join();

        sw.stop();

        Assert.assertTrue("Delay only lasted " + sw.getTime(), sw.getTime() > 1);
        return CompletableFuture.completedFuture(null);
    }
}
