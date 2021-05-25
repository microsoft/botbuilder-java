// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import org.junit.Assert;
import org.junit.Test;

public class LuisApplicationTests {
    String validUUID = "b31aeaf3-3511-495b-a07f-571fc873214b";
    String invalidUUID = "0000";
    String validEndpoint = "https://www.test.com";
    String invalidEndpoint = "www.test.com";

    @Test
    public void invalidSubscriptionKey() {

        Exception exception = Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisApplication lA = new LuisApplication(
                validUUID,
                invalidUUID,
                validEndpoint);
        });

        String expectedMessage = String.format("%s is not a valid LUIS subscription key.", invalidUUID);
        String actualMessage = exception.getMessage();

        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void invalidApplicationId () {

        Exception exception = Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisApplication lA = new LuisApplication(
                invalidUUID,
                validUUID,
                validEndpoint);
        });

        String expectedMessage = String.format("%s is not a valid LUIS application id.", invalidUUID);
        String actualMessage = exception.getMessage();

        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void invalidEndpoint() {

        Exception exception = Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisApplication lA = new LuisApplication(
                validUUID,
                validUUID,
                invalidEndpoint);
        });

        String expectedMessage = String.format("%s is not a valid LUIS endpoint.", invalidEndpoint);
        String actualMessage = exception.getMessage();

        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void createsNewLuisApplication() {

        LuisApplication lA = new LuisApplication(
            validUUID,
            validUUID,
            validEndpoint
        );

        Assert.assertTrue(lA.getApplicationId().equals(validUUID));
        Assert.assertTrue(lA.getEndpointKey().equals(validUUID));
        Assert.assertTrue(lA.getEndpoint().equals(validEndpoint));
    }

    @Test
    public void createsNewLuisApplicationFromURL() {
        String url = "https://westus.api.cognitive.microsoft.com/luis/prediction/v3.0/apps/b31aeaf3-3511-495b-a07f-571fc873214b/slots/production/predict?verbose=true&timezoneOffset=-360&subscription-key=048ec46dc58e495482b0c447cfdbd291";
        LuisApplication lA = new LuisApplication(url);

        Assert.assertTrue(lA.getApplicationId().equals("b31aeaf3-3511-495b-a07f-571fc873214b"));
        Assert.assertTrue(lA.getEndpointKey().equals("048ec46dc58e495482b0c447cfdbd291"));
        Assert.assertTrue(lA.getEndpoint().equals("https://westus.api.cognitive.microsoft.com"));
    }

    @Test
    public void listApplicationFromLuisEndpointBadArguments() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisApplication lA = new LuisApplication("this.is.not.a.uri");
        });
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisApplication lA = new LuisApplication("https://westus.api.cognitive.microsoft.com/luis/v3.0/apps/b31aeaf3-3511-495b-a07f-571fc873214b?verbose=true&timezoneOffset=-360&q=");
        });
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            LuisApplication lA = new LuisApplication("https://westus.api.cognitive.microsoft.com?verbose=true&timezoneOffset=-360&subscription-key=048ec46dc58e495482b0c447cfdbd291&q=");
        });
    }
}
