// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.SimpleAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.teams.FileConsentCardResponse;
import com.microsoft.bot.schema.teams.FileUploadInfo;
import com.microsoft.bot.schema.teams.MessagingExtensionAction;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TeamsActivityHandlerBadRequestTests {
    @Test
    public void TestFileConsentBadAction() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("fileConsent/invoke");
        FileUploadInfo fileInfo = new FileUploadInfo();
        fileInfo.setUniqueId("uniqueId");
        fileInfo.setFileType("fileType");
        fileInfo.setUploadUrl("uploadUrl");
        FileConsentCardResponse fileConsentCard = new FileConsentCardResponse();
        fileConsentCard.setAction("this.is.a.bad.action");
        fileConsentCard.setUploadInfo(fileInfo);
        activity.setValue(fileConsentCard);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TeamsActivityHandler bot = new TeamsActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            400,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    @Test
    public void TestMessagingExtensionSubmitActionPreviewBadAction() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setBotMessagePreviewAction("this.is.a.bad.action");
        activity.setValue(action);

        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        TeamsActivityHandler bot = new TeamsActivityHandler();
        bot.onTurn(turnContext).join();

        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            400,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }
}
