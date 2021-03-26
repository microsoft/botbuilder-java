// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

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
import com.microsoft.bot.dialogs.choices.Choice;
import com.microsoft.bot.dialogs.choices.ChoiceFactory;
import com.microsoft.bot.dialogs.choices.ChoiceFactoryOptions;
import com.microsoft.bot.dialogs.choices.FoundChoice;
import com.microsoft.bot.dialogs.choices.ListStyle;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.HeroCard;
import com.microsoft.bot.schema.SuggestedActions;
import com.microsoft.recognizers.text.Culture;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class TextPromptTests {

    @Test
    public void TextPromptWithEmptyIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new TextPrompt(""));
    }

    @Test
    public void TextPromptWithNullIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new TextPrompt(null));
    }

    @Test
    public void TextPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("textPrompt");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        TextPrompt textPrompt = new TextPrompt("TextPrompt");

        dialogs.add(textPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter some text.");
                options.setPrompt(activity);
                dc.prompt("TextPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                String textResult = (String) results.getResult();
                Activity reply = MessageFactory.text(String.format("Bot received the text '%s'.", textResult));
                turnContext.sendActivity(reply).join();
            }
            return CompletableFuture.completedFuture(null);
        }).send("hello").assertReply("Enter some text.").send("some text")
                .assertReply("Bot received the text 'some text'.").startTest().join();
    }

    @Test
    public void TextPromptWithNaughtyStrings() throws FileNotFoundException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("textPrompt");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        TextPrompt textPrompt = new TextPrompt("TextPrompt");

        dialogs.add(textPrompt);
        File f = new File(ClassLoader.getSystemClassLoader().getResource("naughtyStrings.txt").getFile());

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String naughtyString = "";
        do {
            naughtyString = GetNextNaughtyString(br);
            try  {
                new TestFlow(adapter, (turnContext) -> {
                    DialogContext dc = dialogs.createContext(turnContext).join();
                    DialogTurnResult results = dc.continueDialog().join();

                    if (results.getStatus() == DialogTurnStatus.EMPTY) {
                        PromptOptions options = new PromptOptions();
                        Activity activity = new Activity(ActivityTypes.MESSAGE);
                        activity.setText("Enter some text.");
                        options.setPrompt(activity);
                        dc.prompt("TextPrompt", options).join();
                    } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                        String textResult = (String) results.getResult();
                        Activity reply = MessageFactory.text(textResult);
                        turnContext.sendActivity(reply).join();
                    }
                    return CompletableFuture.completedFuture(null);
                })
                .send("hello")
                .assertReply("Enter some text.")
                .send(naughtyString)
                .assertReply(naughtyString)
                .startTest()
                .join();
            }
            catch (Exception e) {
                // If the input message is empty after a .Trim() operation, character the comparison will fail
                // because the reply message will be a Message Activity with null as Text, this is expected behavior
                String message = e.getMessage();
                boolean messageIsBlank = e.getMessage()
                        .contains("should match expected")
                        && naughtyString.equals(" ");
                boolean messageIsEmpty = e.getMessage().contains("should match expected")
                    && StringUtils.isBlank(naughtyString);
                if (!(messageIsBlank || messageIsEmpty)) {
                    throw e;
                }
            }
        }
        while (!StringUtils.isEmpty(naughtyString));
        try {
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TextPromptValidator() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("TextPromptValidator");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        PromptValidator<String> validator = (promptContext) -> {
            String value = promptContext.getRecognized().getValue();
            if (value.length() <= 3) {
                promptContext.getContext()
                        .sendActivity(MessageFactory.text("Make sure the text is greater than three characters.")).join();
                return CompletableFuture.completedFuture(false);
            } else {
                return CompletableFuture.completedFuture(true);
            }
        };

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        TextPrompt textPrompt = new TextPrompt("TextPrompt", validator);
        // Create and add number prompt to DialogSet.
        dialogs.add(textPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter some text.");
                options.setPrompt(activity);
                dc.prompt("TextPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                String textResult = (String) results.getResult();
                Activity reply = MessageFactory.text(String.format("Bot received the text '%s'.", textResult));
                turnContext.sendActivity(reply).join();
            }
            return CompletableFuture.completedFuture(null);
        }).send("hello").assertReply("Enter some text.").send("hi")
                .assertReply("Make sure the text is greater than three characters.").send("hello")
                .assertReply("Bot received the text 'hello'.").startTest().join();
    }

    @Test
    public void TextPromptWithRetryPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("TextPromptWithRetryPrompt");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        PromptValidator<String> validator = (promptContext) -> {
            String value = promptContext.getRecognized().getValue();
            if (value.length() >= 3) {
                return CompletableFuture.completedFuture(true);
            } else {
                return CompletableFuture.completedFuture(false);
            }
        };

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        TextPrompt textPrompt = new TextPrompt("TextPrompt", validator);
        // Create and add number prompt to DialogSet.
        dialogs.add(textPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter some text.");
                options.setPrompt(activity);
                Activity retryActivity = new Activity(ActivityTypes.MESSAGE);
                retryActivity.setText("Make sure the text is greater than three characters.");
                options.setRetryPrompt(retryActivity);
                dc.prompt("TextPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                String textResult = (String) results.getResult();
                Activity reply = MessageFactory.text(String.format("Bot received the text '%s'.", textResult));
                turnContext.sendActivity(reply).join();
            }
            return CompletableFuture.completedFuture(null);
        }).send("hello").assertReply("Enter some text.").send("hi")
                .assertReply("Make sure the text is greater than three characters.").send("hello")
                .assertReply("Bot received the text 'hello'.").startTest().join();
    }

    @Test
    public void TextPromptValidatorWithMessageShouldNotSendRetryPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState
                .createProperty("TextPromptValidatorWithMessageShouldNotSendRetryPrompt");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        PromptValidator<String> validator = (promptContext) -> {
            String value = promptContext.getRecognized().getValue();
            if (value.length() <= 3) {
                promptContext.getContext()
                        .sendActivity(MessageFactory.text("The text should be greater than 3 chars.")).join();
                return CompletableFuture.completedFuture(false);
            } else {
                return CompletableFuture.completedFuture(true);
            }
        };

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        TextPrompt textPrompt = new TextPrompt("TextPrompt", validator);
        // Create and add number prompt to DialogSet.
        dialogs.add(textPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter some text.");
                options.setPrompt(activity);
                Activity retryActivity = new Activity(ActivityTypes.MESSAGE);
                retryActivity.setText("Make sure the text is greater than three characters.");
                options.setRetryPrompt(retryActivity);
                dc.prompt("TextPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                String textResult = (String) results.getResult();
                Activity reply = MessageFactory.text(String.format("Bot received the text '%s'.", textResult));
                turnContext.sendActivity(reply).join();
            }
            return CompletableFuture.completedFuture(null);
        }).send("hello").assertReply("Enter some text.").send("hi")
                .assertReply("The text should be greater than 3 chars.").send("hello")
                .assertReply("Bot received the text 'hello'.").startTest().join();
    }

    private static String GetNextNaughtyString(BufferedReader reader) {
        String textLine;
        try {
            while ((textLine = reader.readLine()) != null) {
                if (!textLine.startsWith("#")) {
                    return textLine;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}

