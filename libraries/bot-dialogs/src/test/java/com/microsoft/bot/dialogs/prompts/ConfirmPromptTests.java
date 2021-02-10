// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
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
import com.microsoft.bot.dialogs.choices.ChoiceFactoryOptions;
import com.microsoft.bot.dialogs.choices.ListStyle;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.recognizers.text.Culture;

import org.junit.Assert;
import org.junit.Test;

public class ConfirmPromptTests {

    @Test
    public void ChoicePromptWithEmptyIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new ConfirmPrompt(""));
    }

    @Test
    public void ConfirmPromptWithNullIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new ConfirmPrompt(null));
    }

    @Test
    public void ConfirmPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Please confirm."));
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("Confirmed.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Not confirmed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please confirm. (1) Yes or (2) No")
        .send("yes")
        .assertReply("Confirmed.")
        .startTest()
        .join();
    }

    @Test
    public void ConfirmPromptRetry() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity prompt = new Activity(ActivityTypes.MESSAGE);
                prompt.setText("Please confirm.");
                options.setPrompt(prompt);
                Activity retryPrompt = new Activity(ActivityTypes.MESSAGE);
                retryPrompt.setText("Please confirm, say 'yes' or 'no' or something like that.");
                options.setRetryPrompt(retryPrompt);
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("Confirmed.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Not confirmed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please confirm. (1) Yes or (2) No")
        .send("lala")
        .assertReply("Please confirm, say 'yes' or 'no' or something like that. (1) Yes or (2) No")
        .send("no")
        .assertReply("Not confirmed.")
        .startTest()
        .join();
    }

    @Test
    public void ConfirmPromptNoOptions() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("Confirmed.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Not confirmed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply(" (1) Yes or (2) No")
        .send("lala")
        .assertReply(" (1) Yes or (2) No")
        .send("no")
        .assertReply("Not confirmed.")
        .startTest()
        .join();
    }

    @Test
    public void ConfirmPromptChoiceOptionsNumbers() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        ChoiceFactoryOptions choiceOptions = new ChoiceFactoryOptions();
        choiceOptions.setIncludeNumbers(true);
        eventPrompt.setChoiceOptions(choiceOptions);
        eventPrompt.setStyle(ListStyle.INLINE);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity prompt = new Activity(ActivityTypes.MESSAGE);
                prompt.setText("Please confirm.");
                options.setPrompt(prompt);
                Activity retryPrompt = new Activity(ActivityTypes.MESSAGE);
                retryPrompt.setText("Please confirm, say 'yes' or 'no' or something like that.");
                options.setRetryPrompt(retryPrompt);
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("Confirmed.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Not confirmed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please confirm. (1) Yes or (2) No")
        .send("lala")
         .assertReply("Please confirm, say 'yes' or 'no' or something like that. (1) Yes or (2) No")
         .send("2")
         .assertReply("Not confirmed.")
         .startTest()
         .join();
    }

    @Test
    public void ConfirmPromptChoiceOptionsMultipleAttempts() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        ChoiceFactoryOptions choiceOptions = new ChoiceFactoryOptions();
        choiceOptions.setIncludeNumbers(true);
        eventPrompt.setChoiceOptions(choiceOptions);
        eventPrompt.setStyle(ListStyle.INLINE);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity prompt = new Activity(ActivityTypes.MESSAGE);
                prompt.setText("Please confirm.");
                options.setPrompt(prompt);
                Activity retryPrompt = new Activity(ActivityTypes.MESSAGE);
                retryPrompt.setText("Please confirm, say 'yes' or 'no' or something like that.");
                options.setRetryPrompt(retryPrompt);
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("Confirmed.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Not confirmed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please confirm. (1) Yes or (2) No")
        .send("lala")
        .assertReply("Please confirm, say 'yes' or 'no' or something like that. (1) Yes or (2) No")
        .send("what")
        .assertReply("Please confirm, say 'yes' or 'no' or something like that. (1) Yes or (2) No")
        .send("2")
         .assertReply("Not confirmed.")
         .startTest()
         .join();
    }

    @Test
    public void ConfirmPromptChoiceOptionsNoNumbers() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        ChoiceFactoryOptions choiceOptions = new ChoiceFactoryOptions();
        choiceOptions.setIncludeNumbers(false);
        choiceOptions.setInlineSeparator("~");
        eventPrompt.setChoiceOptions(choiceOptions);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity prompt = new Activity(ActivityTypes.MESSAGE);
                prompt.setText("Please confirm.");
                options.setPrompt(prompt);
                Activity retryPrompt = new Activity(ActivityTypes.MESSAGE);
                retryPrompt.setText("Please confirm, say 'yes' or 'no' or something like that.");
                options.setRetryPrompt(retryPrompt);
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("Confirmed.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Not confirmed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please confirm. Yes or No")
        .send("2")
        .assertReply("Please confirm, say 'yes' or 'no' or something like that. Yes or No")
        .send("no")
        .assertReply("Not confirmed.")
        .startTest()
        .join();
    }

    @Test
    public void ShouldUsePromptClassStyleProperty() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        eventPrompt.setStyle(ListStyle.INLINE);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("is it true?");
                options.setPrompt(activity);

                dc.prompt("ConfirmPrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("is it true? (1) Yes or (2) No")
        .startTest()
        .join();
    }

    @Test
    public void PromptOptionsStyleShouldOverridePromptClassStyleProperty() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ConfirmPrompt eventPrompt = new ConfirmPrompt("ConfirmPrompt", null, Culture.English);
        eventPrompt.setStyle(ListStyle.INLINE);
        dialogs.add(eventPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("is it true?");
                options.setPrompt(activity);
                options.setStyle(ListStyle.NONE);
                dc.prompt("ConfirmPrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("is it true?")
        .startTest()
        .join();
    }
}
