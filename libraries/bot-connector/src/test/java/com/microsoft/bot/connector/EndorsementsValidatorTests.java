// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.EndorsementsValidator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class EndorsementsValidatorTests {

    @Test
    public void NullChannelIdParameterShouldPass() {
        boolean isEndorsed = EndorsementsValidator.validate(null, Collections.emptyList());
        Assert.assertTrue(isEndorsed);
    }

    @Test
    public void NullEndorsementsParameterShouldThrow() {
        try {
            EndorsementsValidator.validate("foo", null);
            Assert.fail("Should have failed with IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("endorsements"));
        }
    }

    @Test
    public void UnendorsedChannelIdShouldFail() {
        boolean isEndorsed = EndorsementsValidator.validate("channelOne", Collections.emptyList());
        Assert.assertFalse(isEndorsed);
    }

    @Test
    public void MismatchedEndorsementsChannelIdShouldFail() {
        boolean isEndorsed = EndorsementsValidator.validate("right", Arrays.asList("wrong"));
        Assert.assertFalse(isEndorsed);
    }

    @Test
    public void EndorsedChannelIdShouldPass() {
        boolean isEndorsed = EndorsementsValidator.validate("right", Arrays.asList("right"));
        Assert.assertTrue(isEndorsed);
    }

    @Test
    public void EndorsedChannelIdShouldPassWithTwoEndorsements() {
        boolean isEndorsed = EndorsementsValidator.validate("right", Arrays.asList("right", "wrong"));
        Assert.assertTrue(isEndorsed);
    }

    @Test
    public void UnaffinitizedActivityShouldPass() {
        boolean isEndorsed = EndorsementsValidator.validate("", Arrays.asList("right", "wrong"));
        Assert.assertTrue(isEndorsed);
    }
}
