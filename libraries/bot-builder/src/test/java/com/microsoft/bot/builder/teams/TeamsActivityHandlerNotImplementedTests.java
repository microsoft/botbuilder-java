// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.SimpleAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.teams.AppBasedLinkQuery;
import com.microsoft.bot.schema.teams.FileConsentCardResponse;
import com.microsoft.bot.schema.teams.FileUploadInfo;
import com.microsoft.bot.schema.teams.MessagingExtensionAction;
import com.microsoft.bot.schema.teams.MessagingExtensionActionResponse;
import com.microsoft.bot.schema.teams.MessagingExtensionQuery;
import com.microsoft.bot.schema.teams.O365ConnectorCardActionQuery;
import com.microsoft.bot.schema.teams.TaskModuleRequest;
import com.microsoft.bot.schema.teams.TaskModuleRequestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class TeamsActivityHandlerNotImplementedTests {
    @Test
    public void TestInvoke() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("gibberish");

        assertNotImplemented(activity);
    }

    @Test
    public void TestFileConsentAccept() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("fileConsent/invoke");
        FileUploadInfo fileInfo = new FileUploadInfo();
        fileInfo.setUniqueId("uniqueId");
        fileInfo.setFileType("fileType");
        fileInfo.setUploadUrl("uploadUrl");
        FileConsentCardResponse response = new FileConsentCardResponse();
        response.setAction("accept");
        response.setUploadInfo(fileInfo);
        activity.setValue(response);

        assertNotImplemented(activity);
    }

    @Test
    public void TestFileConsentDecline() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("fileConsent/invoke");
        FileUploadInfo fileInfo = new FileUploadInfo();
        fileInfo.setUniqueId("uniqueId");
        fileInfo.setFileType("fileType");
        fileInfo.setUploadUrl("uploadUrl");
        FileConsentCardResponse response = new FileConsentCardResponse();
        response.setAction("decline");
        response.setUploadInfo(fileInfo);
        activity.setValue(response);

        assertNotImplemented(activity);
    }

    @Test
    public void TestActionableMessageExecuteAction() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("actionableMessage/executeAction");
        activity.setValue(new O365ConnectorCardActionQuery());

        assertNotImplemented(activity);
    }

    @Test
    public void TestComposeExtensionQueryLink() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/queryLink");
        activity.setValue(new AppBasedLinkQuery());

        assertNotImplemented(activity);
    }

    @Test
    public void TestComposeExtensionQuery() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/query");
        activity.setValue(new MessagingExtensionQuery());

        assertNotImplemented(activity);
    }

    @Test
    public void TestMessagingExtensionSelectItem() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/selectItem");
        activity.setValue(new O365ConnectorCardActionQuery());

        assertNotImplemented(activity);
    }

    @Test
    public void TestMessagingExtensionSubmitAction() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        activity.setValue(new MessagingExtensionQuery());

        assertNotImplemented(activity);
    }

    @Test
    public void TestMessagingExtensionSubmitActionPreviewActionEdit() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setBotMessagePreviewAction("edit");
        activity.setValue(action);

        assertNotImplemented(activity);
    }

    @Test
    public void TestMessagingExtensionSubmitActionPreviewActionSend() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setBotMessagePreviewAction("send");
        activity.setValue(action);

        assertNotImplemented(activity);
    }

    @Test
    public void TestMessagingExtensionFetchTask() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/fetchTask");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setCommandId("testCommand");
        activity.setValue(action);

        assertNotImplemented(activity);
    }

    @Test
    public void TestMessagingExtensionConfigurationQuerySettingsUrl() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/querySettingsUrl");

        assertNotImplemented(activity);
    }

    @Test
    public void TestMessagingExtensionConfigurationSetting() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/setting");

        assertNotImplemented(activity);
    }

    @Test
    public void TestTaskModuleFetch() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("task/fetch");
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("key", "value");
        data.put("type", "task / fetch");
        TaskModuleRequestContext context = new TaskModuleRequestContext();
        context.setTheme("default");
        TaskModuleRequest request = new TaskModuleRequest();
        request.setData(data);
        request.setContext(context);
        activity.setValue(request);
        assertNotImplemented(activity);
    }

    @Test
    public void TestTaskModuleSubmit() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("task/submit");
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("key", "value");
        data.put("type", "task / fetch");
        TaskModuleRequestContext context = new TaskModuleRequestContext();
        context.setTheme("default");
        TaskModuleRequest request = new TaskModuleRequest();
        request.setData(data);
        request.setContext(context);
        activity.setValue(request);
        assertNotImplemented(activity);
    }

    @Test
    public void TestFileConsentAcceptImplemented() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("fileConsent/invoke");
        FileUploadInfo fileInfo = new FileUploadInfo();
        fileInfo.setUniqueId("uniqueId");
        fileInfo.setFileType("fileType");
        fileInfo.setUploadUrl("uploadUrl");
        FileConsentCardResponse response = new FileConsentCardResponse();
        response.setAction("accept");
        response.setUploadInfo(fileInfo);
        activity.setValue(response);

        assertImplemented(activity, new TestActivityHandlerFileConsent());
    }

    @Test
    public void TestFileConsentDeclineImplemented() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("fileConsent/invoke");
        FileUploadInfo fileInfo = new FileUploadInfo();
        fileInfo.setUniqueId("uniqueId");
        fileInfo.setFileType("fileType");
        fileInfo.setUploadUrl("uploadUrl");
        FileConsentCardResponse response = new FileConsentCardResponse();
        response.setAction("decline");
        response.setUploadInfo(fileInfo);
        activity.setValue(response);

        assertImplemented(activity, new TestActivityHandlerFileConsent());
    }

    @Test
    public void TestMessagingExtensionSubmitActionPreviewActionEditImplemented() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        MessagingExtensionAction action = new MessagingExtensionAction();
        action.setBotMessagePreviewAction("edit");
        activity.setValue(action);

        assertImplemented(activity, new TestActivityHandlerPrevieAction());
    }

    @Test
    public void TestMessagingExtensionSubmitActionPreviewActionSendImplemented() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName("composeExtension/submitAction");
        MessagingExtensionAction action = new MessagingExtensionAction();
                action.setBotMessagePreviewAction("send");
        activity.setValue(action);

        assertImplemented(activity, new TestActivityHandlerPrevieAction());
    }

    private void assertNotImplemented(Activity activity) {
        assertInvokeResponse(activity, new TestActivityHandler(), 501);
    }

    private void assertImplemented(Activity activity, ActivityHandler bot) {
        assertInvokeResponse(activity, bot, 200);
    }

    private void assertInvokeResponse(Activity activity, ActivityHandler bot, int expectedStatus) {
        AtomicReference<List<Activity>> activitiesToSend = new AtomicReference<>();

        TurnContext turnContext = new TurnContextImpl(
            new SimpleAdapter(activitiesToSend::set),
            activity
        );

        bot.onTurn(turnContext).join();

        Assert.assertNotNull(activitiesToSend.get());
        Assert.assertEquals(1, activitiesToSend.get().size());
        Assert.assertTrue(activitiesToSend.get().get(0).getValue() instanceof InvokeResponse);
        Assert.assertEquals(
            expectedStatus,
            ((InvokeResponse) activitiesToSend.get().get(0).getValue()).getStatus()
        );
    }

    private static class TestActivityHandler extends TeamsActivityHandler {

    }

    private static class TestActivityHandlerFileConsent extends TeamsActivityHandler {
        @Override
        protected CompletableFuture<Void> onTeamsFileConsentAccept(
            TurnContext turnContext,
            FileConsentCardResponse fileConsentCardResponse
        ) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<Void> onTeamsFileConsentDecline(
            TurnContext turnContext,
            FileConsentCardResponse fileConsentCardResponse
        ) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class TestActivityHandlerPrevieAction extends TeamsActivityHandler {

        @Override
        protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewEdit(
            TurnContext turnContext,
            MessagingExtensionAction action
        ) {
            return CompletableFuture.completedFuture(new MessagingExtensionActionResponse());
        }

        @Override
        protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewSend(
            TurnContext turnContext,
            MessagingExtensionAction action
        ) {
            return CompletableFuture.completedFuture(new MessagingExtensionActionResponse());
        }
    }
}
