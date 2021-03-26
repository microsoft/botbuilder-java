// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import org.junit.Assert;
import org.junit.Test;

public class ChoicesChannelTests {
    @Test
    public void shouldReturnTrueForSupportsSuggestedActionsWithLineAnd13() {
        boolean supports = Channel.supportsSuggestedActions(Channels.LINE, 13);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnFalseForSupportsSuggestedActionsWithLineAnd14() {
        boolean supports = Channel.supportsSuggestedActions(Channels.LINE, 14);
        Assert.assertFalse(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsSuggestedActionsWithSkypeAnd10() {
        boolean supports = Channel.supportsSuggestedActions(Channels.SKYPE, 10);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnFalseForSupportsSuggestedActionsWithSkypeAnd11() {
        boolean supports = Channel.supportsSuggestedActions(Channels.SKYPE, 11);
        Assert.assertFalse(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsSuggestedActionsWithKikAnd20() {
        boolean supports = Channel.supportsSuggestedActions(Channels.KIK, 20);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnFalseForSupportsSuggestedActionsWithKikAnd21() {
        boolean supports = Channel.supportsSuggestedActions(Channels.SKYPE, 21);
        Assert.assertFalse(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsSuggestedActionsWithEmulatorAnd100() {
        boolean supports = Channel.supportsSuggestedActions(Channels.EMULATOR, 100);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnFalseForSupportsSuggestedActionsWithEmulatorAnd101() {
        boolean supports = Channel.supportsSuggestedActions(Channels.EMULATOR, 101);
        Assert.assertFalse(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsSuggestedActionsWithDirectLineSpeechAnd100() {
        boolean supports = Channel.supportsSuggestedActions(Channels.DIRECTLINESPEECH, 100);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsCardActionsWithDirectLineSpeechAnd99() {
        boolean supports = Channel.supportsCardActions(Channels.DIRECTLINESPEECH, 99);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsCardActionsWithLineAnd99() {
        boolean supports = Channel.supportsCardActions(Channels.LINE, 99);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnFalseForSupportsCardActionsWithLineAnd100() {
        boolean supports = Channel.supportsCardActions(Channels.LINE, 100);
        Assert.assertFalse(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsCardActionsWithCortanaAnd100() {
        boolean supports = Channel.supportsCardActions(Channels.CORTANA, 100);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsCardActionsWithSlackAnd100() {
        boolean supports = Channel.supportsCardActions(Channels.SLACK, 100);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnTrueForSupportsCardActionsWithSkypeAnd100() {
        boolean supports = Channel.supportsCardActions(Channels.SKYPE, 3);
        Assert.assertTrue(supports);
    }

    @Test
    public void shouldReturnFalseForSupportsCardActionsWithSkypeAnd5() {
        boolean supports = Channel.supportsCardActions(Channels.SKYPE, 5);
        Assert.assertFalse(supports);
    }

    @Test
    public void shouldReturnFalseForHasMessageFeedWithCortana() {
        boolean supports = Channel.hasMessageFeed(Channels.CORTANA);
        Assert.assertFalse(supports);
    }

    @Test
    public void shouldReturnChannelIdFromContextActivity() {
        Activity testActivity = new Activity(ActivityTypes.MESSAGE);
        testActivity.setChannelId(Channels.FACEBOOK);
        TurnContext testContext = new TurnContextImpl(new BotFrameworkAdapter(new SimpleCredentialProvider()), testActivity);
        String channelId = Channel.getChannelId(testContext);
        Assert.assertEquals(Channels.FACEBOOK, channelId);
    }

    @Test
    public void shouldReturnEmptyFromContextActivityMissingChannel() {
        Activity testActivity = new Activity(ActivityTypes.MESSAGE);
        testActivity.setChannelId(null);
        TurnContext testContext = new TurnContextImpl(new BotFrameworkAdapter(new SimpleCredentialProvider()), testActivity);
        String channelId = Channel.getChannelId(testContext);
        Assert.assertNull(channelId);
    }
}
