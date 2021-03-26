// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.prompts.PromptOptions;
import com.microsoft.bot.dialogs.prompts.PromptValidator;
import com.microsoft.bot.dialogs.prompts.TextPrompt;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

import org.junit.Test;

public class PromptValidatorContextTests {

    @Test
    public void PromptValidatorContextEnd() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        DialogSet dialogs = new DialogSet(dialogState);

        PromptValidator<String> validator = (promptContext) -> {
                return CompletableFuture.completedFuture(true);
        };

        dialogs.add(new TextPrompt("namePrompt", validator));

        WaterfallStep[] steps = new WaterfallStep[]
        {
             new WaterfallStep1(),
             new WaterfallStep2()
        };

        dialogs.add(new WaterfallDialog("nameDialog", Arrays.asList(steps)));

         new TestFlow(adapter,  (turnContext) -> {
                DialogContext dc =  dialogs.createContext(turnContext).join();
                dc.continueDialog().join();
                if (!turnContext.getResponded()) {
                     dc.beginDialog("nameDialog").join();
                }
                return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please type your name.")
        .send("John")
        .assertReply("John is a great name!")
        .send("Hi again")
        .assertReply("Please type your name.")
        .send("1")
        .assertReply("1 is a great name!")
        .startTest()
        .join();
    }

    private class WaterfallStep1 implements WaterfallStep {

        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            PromptOptions prompt = new PromptOptions();
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setText("Please type your name.");
            prompt.setPrompt(activity);
            return stepContext.prompt("namePrompt", prompt);
        }

    }

    private class WaterfallStep2 implements WaterfallStep {

        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            String name = (String)stepContext.getResult();
            stepContext.getContext().sendActivity(
                    MessageFactory.text(String.format("%s is a great name!", name))).join();
           return  stepContext.endDialog();
        }
    }

    @Test
    public void PromptValidatorContextRetryEnd() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        DialogSet dialogs = new DialogSet(dialogState);

        PromptValidator<String> validator = (promptContext) -> {
                String result = promptContext.getRecognized().getValue();
                if (result.length() > 3) {
                    return CompletableFuture.completedFuture(true);
                } else {
                     promptContext.getContext().sendActivity(
                         MessageFactory.text("Please send a name that is longer than 3 characters.")).join();
                }

                return CompletableFuture.completedFuture(false);
            };

        dialogs.add(new TextPrompt("namePrompt", validator));


        // Create TextPrompt with dialogId "namePrompt" and custom validator
        WaterfallStep[] steps = new WaterfallStep[]
        {
             new WaterfallStep1(),
             new WaterfallStep2()
        };

        dialogs.add(new WaterfallDialog("nameDialog", Arrays.asList(steps)));

        new TestFlow(adapter,  (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            dc.continueDialog().join();
            if (!turnContext.getResponded()) {
                    dc.beginDialog("nameDialog").join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please type your name.")
        .send("hi")
        .assertReply("Please send a name that is longer than 3 characters.")
        .send("John")
        .assertReply("John is a great name!")
        .startTest()
        .join();
    }

    @Test
    public void PromptValidatorNumberOfAttempts() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        DialogSet dialogs = new DialogSet(dialogState);

        PromptValidator<String> validator = (promptContext) -> {
            String result = promptContext.getRecognized().getValue();
            if (result.length() > 3) {
                Activity succeededMessage = MessageFactory.text(String.format("You got it at the %dth try!",
                                                                promptContext.getAttemptCount()));
                return promptContext.getContext().sendActivity(succeededMessage).thenApply(resourceResponse -> true);
            } else {
                    return promptContext.getContext()
                        .sendActivity(MessageFactory.text(String.format("Please send a name that is longer than 3 characters. %d", promptContext.getAttemptCount())))
                        .thenApply(resourceResponse -> false);
            }
        };

        dialogs.add(new TextPrompt("namePrompt", validator));

        WaterfallStep[] steps = new WaterfallStep[]
        {
             new WaterfallStep1(),
             new WaterfallStep2()
        };

        dialogs.add(new WaterfallDialog("nameDialog", Arrays.asList(steps)));

        new TestFlow(adapter,  (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            dc.continueDialog().join();
            if (!turnContext.getResponded()) {
                    dc.beginDialog("nameDialog").join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Please type your name.")
        .send("hi")
        .assertReply("Please send a name that is longer than 3 characters. 1")
        .send("hi")
        .assertReply("Please send a name that is longer than 3 characters. 2")
        .send("hi")
        .assertReply("Please send a name that is longer than 3 characters. 3")
        .send("John")
        .assertReply("You got it at the 4th try!")
        .assertReply("John is a great name!")
        .startTest()
        .join();
    }
}

