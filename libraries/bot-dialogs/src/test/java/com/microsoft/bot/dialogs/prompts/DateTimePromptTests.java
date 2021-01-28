// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.ArrayList;
import java.util.List;
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
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.recognizers.text.Culture;

import org.junit.Test;

public class DateTimePromptTests {

    @Test
    public void BasicDateTimePrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        DateTimePrompt dateTimePrompt = new DateTimePrompt("DateTimePrompt",  null, Culture.English);
        // Create and add number prompt to DialogSet.
        dialogs.add(dateTimePrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("What date would you like?");
                options.setPrompt(activity);
                dc.prompt("DateTimePrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                ArrayList<DateTimeResolution> resolution = (ArrayList<DateTimeResolution>) results.getResult();
                //Activity reply = MessageFactory.text($"Timex:'{resolution.Timex}' Value:'{resolution.Value}'");
                Activity reply = MessageFactory.text(String.format("Timex:'%s' Value:'%s'",
                                                        resolution.get(0).getTimex(),
                                                        resolution.get(0).getValue()));
                turnContext.sendActivity(reply).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("What date would you like?")
        .send("5th December 2018 at 9am")
        .assertReply("Timex:'2018-12-05T09' Value:'2018-12-05 09:00:00'")
        .startTest()
        .join();
    }

    @Test
    public void MultipleResolutionsDateTimePrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        DateTimePrompt dateTimePrompt = new DateTimePrompt("DateTimePrompt",  null, Culture.English);
        // Create and add number prompt to DialogSet.
        dialogs.add(dateTimePrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("What date would you like?");
                options.setPrompt(activity);
                dc.prompt("DateTimePrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                ArrayList<DateTimeResolution> resolution = (ArrayList<DateTimeResolution>) results.getResult();

                List<String> elements = new ArrayList<String>();

                for (DateTimeResolution dateTimeResolution : resolution) {
                    if (!elements.contains(dateTimeResolution.getTimex())) {
                        elements.add(dateTimeResolution.getTimex());
                    }
                }
                Activity reply = MessageFactory.text(String.join(" ", elements));
                turnContext.sendActivity(reply).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("What date would you like?")
        .send("Wednesday 4 oclock")
        .assertReply("XXXX-WXX-3T04 XXXX-WXX-3T16")
        .startTest()
        .join();
    }

    @Test
    public void DateTimePromptWithValidator() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("DateTimePromptWithValidator", "testuser", "testbot"))
                .use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add custom activity prompt to DialogSet.
        DateTimePrompt dateTimePrompt = new DateTimePrompt("DateTimePrompt",
                                        new DateTimeValidator(), Culture.English);
        // Create and add number prompt to DialogSet.
        dialogs.add(dateTimePrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("What date would you like?");
                options.setPrompt(activity);
                dc.prompt("DateTimePrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                ArrayList<DateTimeResolution> resolution = (ArrayList<DateTimeResolution>) results.getResult();
                //Activity reply = MessageFactory.text($"Timex:'{resolution.Timex}' Value:'{resolution.Value}'");
                Activity reply = MessageFactory.text(String.format("Timex:'%s' Value:'%s'",
                                                        resolution.get(0).getTimex(),
                                                        resolution.get(0).getValue()));
                turnContext.sendActivity(reply).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("What date would you like?")
        .send("5th December 2018 at 9am")
        .assertReply("Timex:'2018-12-05' Value:'2018-12-05'")
        .startTest()
        .join();
    }

    class DateTimeValidator implements PromptValidator<List<DateTimeResolution>> {

        DateTimeValidator(){
        }

        @Override
        public CompletableFuture<Boolean> promptValidator(PromptValidatorContext<List<DateTimeResolution>> prompt) {
            if (prompt.getRecognized().getSucceeded()) {
                DateTimeResolution resolution = prompt.getRecognized().getValue().get(0);

                // re-write the resolution to just include the date part.
                DateTimeResolution rewrittenResolution = new DateTimeResolution();
                rewrittenResolution.setTimex(resolution.getTimex().split("T")[0]);
                rewrittenResolution.setValue(resolution.getValue().split(" ")[0]);

                List<DateTimeResolution> valueList = new ArrayList<DateTimeResolution>();
                valueList.add(rewrittenResolution);
                prompt.getRecognized().setValue(valueList);
                return CompletableFuture.completedFuture(true);
            }

            return CompletableFuture.completedFuture(false);
        }
    }
}

