// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TraceTranscriptLogger;
import com.microsoft.bot.builder.TranscriptLoggerMiddleware;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogSet;
import com.microsoft.bot.dialogs.DialogState;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.DialogTurnStatus;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;

import org.junit.Assert;
import org.junit.Test;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


public class AttachmentPromptTests {
    @Test
    public void AttachmentPromptWithEmptyIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new AttachmentPrompt(""));
    }

    @Test
    public void AttachmentPromptWithNullIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new AttachmentPrompt(null));
    }

    @Test
    public void BasicAttachmentPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter(TestAdapter.createConversationReference("BasicAttachmentPrompt","",""))
            .use(new AutoSaveStateMiddleware(convoState))
            .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add attachment prompt to DialogSet.
        AttachmentPrompt attachmentPrompt = new AttachmentPrompt("AttachmentPrompt");
        dialogs.add(attachmentPrompt);

        // Create mock attachment for testing.
        Attachment attachment = new Attachment();
        attachment.setContent("some content");
        attachment.setContentType("text/plain");

        // Create incoming activity with attachment.
        Activity activityWithAttachment = new Activity(ActivityTypes.MESSAGE);
        ArrayList<Attachment> attachmentList = new ArrayList<Attachment>();
        attachmentList.add(attachment);
        activityWithAttachment.setAttachments(attachmentList);

        BotCallbackHandler botLogic = (turnContext -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();

            DialogTurnResult results =  dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("please add an attachment."));
                dc.prompt("AttachmentPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                ArrayList<Attachment> attachments = (ArrayList<Attachment>) results.getResult();
                Activity content = MessageFactory.text((String) attachments.get(0).getContent());
                 turnContext.sendActivity(content).join();
            }

            return CompletableFuture.completedFuture(null);
        });


        new TestFlow(adapter, botLogic)
        .send("hello")
        .assertReply("please add an attachment.")
        .send(activityWithAttachment)
        .assertReply("some content")
        .startTest()
        .join();
    }

    @Test
    public void RetryAttachmentPrompt() {

        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter(TestAdapter.createConversationReference("RetryAttachmentPrompt","",""))
            .use(new AutoSaveStateMiddleware(convoState))
            .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add attachment prompt to DialogSet.
        AttachmentPrompt attachmentPrompt = new AttachmentPrompt("AttachmentPrompt");
        dialogs.add(attachmentPrompt);

        // Create mock attachment for testing.
        Attachment attachment = new Attachment();
        attachment.setContent("some content");
        attachment.setContentType("text/plain");

        // Create incoming activity with attachment.
        Activity activityWithAttachment = new Activity(ActivityTypes.MESSAGE);
        ArrayList<Attachment> attachmentList = new ArrayList<Attachment>();
        attachmentList.add(attachment);
        activityWithAttachment.setAttachments(attachmentList);
        BotCallbackHandler botLogic = (turnContext -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();

            DialogTurnResult results =  dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("please add an attachment."));
                dc.prompt("AttachmentPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                ArrayList<Attachment> attachments = (ArrayList<Attachment>) results.getResult();
                Activity content = MessageFactory.text((String) attachments.get(0).getContent());
                 turnContext.sendActivity(content).join();
            }
            return CompletableFuture.completedFuture(null);
        });

         new TestFlow(adapter, botLogic)
        .send("hello")
        .assertReply("please add an attachment.")
        .send("hello again")
        .assertReply("please add an attachment.")
        .send(activityWithAttachment)
        .assertReply("some content")
        .startTest()
         .join();
    }
}
