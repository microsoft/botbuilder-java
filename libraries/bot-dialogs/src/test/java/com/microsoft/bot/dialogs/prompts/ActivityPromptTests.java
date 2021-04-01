// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogReason;
import com.microsoft.bot.dialogs.DialogSet;
import com.microsoft.bot.dialogs.DialogState;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.DialogTurnStatus;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

import org.junit.Assert;
import org.junit.Test;

public class ActivityPromptTests {

    public ActivityPromptTests(){

    }

    @Test
    public void ActivityPromptWithEmptyIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new EventActivityPrompt("",
                                                                        new BasicActivityPromptValidator()));
    }

    @Test
    public void ActivityPromptWithNullIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new EventActivityPrompt(null,
                                                                    new BasicActivityPromptValidator()));
    }

    @Test
    public void ActivityPromptWithNullValidatorShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new EventActivityPrompt("EventActivityPrompt", null));
    }

    @Test
    public void BasicActivityPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add custom activity prompt to DialogSet.
        EventActivityPrompt eventPrompt = new EventActivityPrompt("EventActivityPrompt",
                                                    new BasicActivityPromptValidator());
        dialogs.add(eventPrompt);

        // Create mock Activity for testing.
        Activity eventActivity = new Activity(ActivityTypes.EVENT);
        eventActivity.setValue(2);

        BotCallbackHandler botLogic = (turnContext -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();

            DialogTurnResult results =  dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("please send an event.");
                options.setPrompt(activity);
                dc.prompt("EventActivityPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Activity content = (Activity) results.getResult();
                 turnContext.sendActivity(content).join();
            }
            return CompletableFuture.completedFuture(null);
        });

        // ActivityPromptHandler handler = new ActivityPromptHandler();
        // handler.setDialogs(dialogs);

        new TestFlow(adapter, botLogic).send("hello").assertReply("please send an event.")
                    .send(eventActivity).assertReply("2").startTest().join();
    }

    @Test
    public void ActivityPromptShouldSendRetryPromptIfValidationFailed() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        DialogSet dialogs = new DialogSet(dialogState);


        EventActivityPrompt eventPrompt = new EventActivityPrompt("EventActivityPrompt", new EmptyPromptValidator());
        dialogs.add(eventPrompt);

        Activity eventActivity = new Activity(ActivityTypes.EVENT);
        eventActivity.setValue(2);

        BotCallbackHandler botLogic = (turnContext -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();

            DialogTurnResult results =  dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("please send an event."));
                options.setRetryPrompt(MessageFactory.text("Retrying - please send an event."));
                dc.prompt("EventActivityPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Activity content = (Activity) results.getResult();
                 turnContext.sendActivity(content).join();
            } else if (results.getStatus() == DialogTurnStatus.WAITING) {
                 turnContext.sendActivity("Test complete.").join();
            }
            return CompletableFuture.completedFuture(null);
        });

         new TestFlow(adapter, botLogic)
        .send("hello")
        .assertReply("please send an event.")
        .send("test")
        .assertReply("Retrying - please send an event.")
        .startTest()
         .join();
    }

    @Test
    public void ActivityPromptResumeDialogShouldPromptNotRetry()
    {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        DialogSet dialogs = new DialogSet(dialogState);

        EventActivityPrompt eventPrompt = new EventActivityPrompt("EventActivityPrompt", new EmptyPromptValidator());
        dialogs.add(eventPrompt);

        BotCallbackHandler botLogic = (turnContext -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();

            switch (turnContext.getActivity().getText()) {
                case "begin":
                    PromptOptions options = new PromptOptions();
                    options.setPrompt(MessageFactory.text("please send an event."));
                    options.setRetryPrompt(MessageFactory.text("Retrying - please send an event."));
                    dc.prompt("EventActivityPrompt", options).join();
                    break;
                case "continue":
                    eventPrompt.continueDialog(dc).join();
                    break;
                case "resume":
                    eventPrompt.resumeDialog(dc, DialogReason.NEXT_CALLED).join();
                    break;
                default:
                    break;
            }
            return CompletableFuture.completedFuture(null);
        });
        new TestFlow(adapter, botLogic)
        .send("begin")
        .assertReply("please send an event.")
        .send("continue")
        .assertReply("Retrying - please send an event.")
        .send("resume")
        // 'ResumeDialogAsync' of ActivityPrompt does NOT cause a Retry
        .assertReply("please send an event.")
        .startTest()
        .join();
    }

    @Test
    public void OnPromptOverloadWithoutIsRetryParamReturnsBasicActivityPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        DialogSet dialogs = new DialogSet(dialogState);

        EventActivityPrompt eventPrompt = new EventActivityPrompt("EventActivityWithoutRetryPrompt",
                                                    new BasicActivityPromptValidator());
        dialogs.add(eventPrompt);

        // Create mock Activity for testing.
        Activity eventActivity = new Activity(ActivityTypes.EVENT);
        eventActivity.setValue(2);
        BotCallbackHandler botLogic = (turnContext -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("please send an event."));
                dc.prompt("EventActivityWithoutRetryPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Activity content = (Activity) results.getResult();
                turnContext.sendActivity(content).join();
            }
            return CompletableFuture.completedFuture(null);
        });
        new TestFlow(adapter, botLogic)
        .send("hello")
        .assertReply("please send an event.")
        .send(eventActivity)
        .assertReply("2")
        .startTest()
        .join();
    }

    @Test
    public void OnPromptErrorsWithNullContext() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
                EventActivityPrompt eventPrompt = new EventActivityPrompt("EventActivityPrompt",
                                                            new BasicActivityPromptValidator());

                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("please send an event.");
                options.setPrompt(activity);
                eventPrompt.onPromptNullContext(options).join();
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });
    }

    @Test
    public void OnPromptErrorsWithNullOptions() {
        Assert.assertThrows(
            IllegalArgumentException.class, () -> {
                try {
                    ConversationState convoState = new ConversationState(new MemoryStorage());
                    StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

                    TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

                    DialogSet dialogs = new DialogSet(dialogState);

                    EventActivityPrompt eventPrompt = new EventActivityPrompt("EventActivityWithoutRetryPrompt",
                                                                new BasicActivityPromptValidator());
                    dialogs.add(eventPrompt);

                    BotCallbackHandler botLogic = (turnContext -> {
                        DialogContext dc =  dialogs.createContext(turnContext).join();
                        return eventPrompt.onPromptNullOptions(dc);
                    });

                    new TestFlow(adapter, botLogic)
                    .send("hello")
                    .startTest().join();

                } catch (CompletionException ex) {
                    throw ex.getCause().getCause();
                }
        });
    }

    class BasicActivityPromptValidator implements PromptValidator<Activity> {

        BasicActivityPromptValidator(){

        }

        @Override
        public CompletableFuture<Boolean> promptValidator(PromptValidatorContext<Activity> promptContext) {
            Assert.assertTrue(promptContext.getAttemptCount() > 0);

            Activity activity = promptContext.getRecognized().getValue();
            if (activity.getType().equals(ActivityTypes.EVENT)) {
                if ((int) activity.getValue() == 2) {
                    promptContext.getRecognized().setValue(MessageFactory.text(activity.getValue().toString()));
                    return CompletableFuture.completedFuture(true);
                }
            } else {
                 promptContext.getContext().sendActivity("Please send an 'event'-type Activity with a value of 2.");
            }

            return CompletableFuture.completedFuture(false);
        }
    }

    class EmptyPromptValidator implements PromptValidator<Activity> {

        EmptyPromptValidator(){

        }

        @Override
        public CompletableFuture<Boolean> promptValidator(PromptValidatorContext<Activity> promptContext) {
            return CompletableFuture.completedFuture(false);
        }
    }

}
