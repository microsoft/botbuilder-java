package com.microsoft.bot.builder;

import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Consumer;

public class BotFrameworkAdapterTest {
    @Test
    public void AdapterSingleUse()
    {
        SimpleAdapter a = new SimpleAdapter();
        a.Use(new CallCountingMiddleware());
    }

    @Test
    public void AdapterUseChaining()
    {
        SimpleAdapter a = new SimpleAdapter();
        a.Use(new CallCountingMiddleware()).Use(new CallCountingMiddleware());
    }

    @Test
    public void PassResourceResponsesThrough() throws Exception {
        Consumer<Activity[]> validateResponse =  (activities) -> {
            // no need to do anything.
        };

        SimpleAdapter a = new SimpleAdapter(validateResponse);
        TurnContextImpl c = new TurnContextImpl(a, new ActivityImpl());

        String activityId = UUID.randomUUID().toString();
        ActivityImpl activity = TestMessage.Message()
                .withId(activityId);

        ResourceResponse resourceResponse = c.SendActivity(activity);
        Assert.assertTrue("Incorrect response Id returned", resourceResponse.id() == activityId);
    }
}
