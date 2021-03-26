// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.activation.UnsupportedDataTypeException;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.Severity;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.prompts.DateTimePrompt;
import com.microsoft.bot.dialogs.prompts.NumberPrompt;
import com.microsoft.bot.dialogs.prompts.PromptCultureModels;
import com.microsoft.bot.dialogs.prompts.PromptOptions;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.junit.Assert;

public class WaterfallTests {

    public WaterfallDialog Create_Waterfall3() {
        WaterfallStep[] steps = new WaterfallStep[]
        {
            new Waterfall3_Step1(),
            new Waterfall3_Step2()
        };
        return new WaterfallDialog("test-waterfall-a", Arrays.asList(steps));
    }

    public WaterfallDialog Create_Waterfall4() {
        WaterfallStep[] steps = new WaterfallStep[]
        {
            new Waterfall4_Step1(),
            new Waterfall4_Step2()
        };
        return new WaterfallDialog("test-waterfall-b", Arrays.asList(steps));
    }

    public WaterfallDialog Create_Waterfall5() {
        WaterfallStep[] steps = new WaterfallStep[]
        {
            new Waterfall5_Step1(),
            new Waterfall5_Step2()
        };
        return new WaterfallDialog("test-waterfall-c", Arrays.asList(steps));
    }

    @Test
    public void Waterfall() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");
        DialogSet dialogs = new DialogSet(dialogState);
        WaterfallStep[] steps = new WaterfallStep[]
            {(step) -> {
                step.getContext().sendActivity("step1").join();
                return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
            }, (step) -> {
                step.getContext().sendActivity("step2").join();
                return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
            }, (step) -> {
                step.getContext().sendActivity("step3").join();
                return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
            },
        };
        dialogs.add(new WaterfallDialog("test", Arrays.asList(steps)));

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            dc.continueDialog().join();
            if (!turnContext.getResponded()) {
                dc.beginDialog("test", null).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("step1")
        .send("hello")
        .assertReply("step2")
        .send("hello")
        .assertReply("step3")
        .startTest()
        .join();
    }

    @Test
    public void WaterfallStepParentIsWaterfallParent() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");
        DialogSet dialogs = new DialogSet(dialogState);
        final String WATERFALL_PARENT_D = "waterfall-parent-test-dialog";
        ComponentDialog waterfallParent = new ComponentDialog(WATERFALL_PARENT_D);

        WaterfallStep[] steps = new WaterfallStep[] {(step) -> {
            Assert.assertEquals(step.getParent().getActiveDialog().getId(), waterfallParent.getId());
            step.getContext().sendActivity("verified").join();
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        } };

        waterfallParent.addDialog(new WaterfallDialog("test", Arrays.asList(steps)));
        waterfallParent.setInitialDialogId("test");
        dialogs.add(waterfallParent);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            dc.continueDialog().join();
            if (!turnContext.getResponded()) {
                dc.beginDialog(WATERFALL_PARENT_D, null).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("verified")
        .startTest()
        .join();
    }

    @Test
    public void WaterfallWithCallback() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");
        DialogSet dialogs = new DialogSet(dialogState);
        WaterfallStep[] steps = new WaterfallStep[] {(step) -> {
            step.getContext().sendActivity("step1").join();
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        }, (step) -> {
            step.getContext().sendActivity("step2").join();
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        }, (step) -> {
            step.getContext().sendActivity("step3").join();
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        }, };
        WaterfallDialog waterfallDialog = new WaterfallDialog("test", Arrays.asList(steps));

        dialogs.add(waterfallDialog);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            dc.continueDialog().join();
            if (!turnContext.getResponded()) {
                dc.beginDialog("test", null).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("step1")
        .send("hello")
        .assertReply("step2")
        .send("hello")
        .assertReply("step3")
        .startTest()
        .join();
    }

    @Test
    public void WaterfallWithStepsNull() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new WaterfallDialog("test", null).addStep(null));
    }

    @Test
    public void WaterfallWithClass() {
        ConversationState convoState = new ConversationState(new MemoryStorage());

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");
        DialogSet dialogs = new DialogSet(dialogState);

        dialogs.add(new MyWaterfallDialog("test"));

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            dc.continueDialog().join();
            if (!turnContext.getResponded()) {
                dc.beginDialog("test", null).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("step1")
        .send("hello")
        .assertReply("step2")
        .send("hello")
        .assertReply("step3")
        .startTest()
        .join();
    }

    @Test
    public void WaterfallPrompt() throws UnsupportedDataTypeException{
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        new TestFlow(adapter,  (turnContext) -> {
            DialogState state =  dialogState.get(turnContext, () -> new DialogState()).join();
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(Create_Waterfall2());
             NumberPrompt<Integer> numberPrompt = null;
             try {
                 numberPrompt = new NumberPrompt<Integer>("number", null, PromptCultureModels.ENGLISH_CULTURE,
                         Integer.class);
             } catch (Throwable t) {
                 // TODO Auto-generated catch block
                 t.printStackTrace();
             }
            dialogs.add(numberPrompt);

            DialogContext dc =  dialogs.createContext(turnContext).join();

             dc.continueDialog().join();

            if (!turnContext.getResponded()) {
                 dc.beginDialog("test-waterfall").join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("step1")
        .assertReply("Enter a number.")
        .send("hello again")
        .assertReply("It must be a number")
        .send("42")
        .assertReply("Thanks for '42'")
        .assertReply("step2")
        .assertReply("Enter a number.")
        .send("apple")
        .assertReply("It must be a number")
        .send("orange")
        .assertReply("It must be a number")
        .send("64")
        .assertReply("Thanks for '64'")
        .assertReply("step3")
        .startTest()
        .join();
    }

    @Test
    public void WaterfallNested() {
        ConversationState convoState = new ConversationState(new MemoryStorage());

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        new TestFlow(adapter, (turnContext) -> {
            StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(Create_Waterfall3());
            dialogs.add(Create_Waterfall4());
            dialogs.add(Create_Waterfall5());

            DialogContext dc = dialogs.createContext(turnContext).join();

            dc.continueDialog().join();

            if (!turnContext.getResponded()) {
                dc.beginDialog("test-waterfall-a").join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("step1")
        .assertReply("step1.1")
        .send("hello")
        .assertReply("step1.2")
        .send("hello")
        .assertReply("step2")
        .assertReply("step2.1")
        .send("hello")
        .assertReply("step2.2")
        .startTest()
        .join();
    }

    @Test
    public void WaterfallDateTimePromptFirstInvalidThenValidInput() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        DialogSet dialogs = new DialogSet(dialogState);

        dialogs.add(new DateTimePrompt("dateTimePrompt", null, PromptCultureModels.ENGLISH_CULTURE));

        WaterfallStep[] steps = new WaterfallStep[] {
             (stepContext) -> {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Provide a date"));
                return stepContext.prompt("dateTimePrompt", options);
            },
             (stepContext) -> {
                Assert.assertNotNull(stepContext);
                return  stepContext.endDialog();
            },
        };

        dialogs.add(new WaterfallDialog("test-dateTimePrompt", Arrays.asList(steps)));

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

         new TestFlow(adapter,  (turnContext) -> {
            DialogState state =  dialogState.get(turnContext, () -> new DialogState()).join();

            DialogContext dc =  dialogs.createContext(turnContext).join();

             dc.continueDialog().join();

            if (!turnContext.getResponded()) {
                 dc.beginDialog("test-dateTimePrompt", null).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Provide a date")
        .send("hello again")
        .assertReply("Provide a date")
        .send("Wednesday 4 oclock")
        .startTest()
        .join();
    }

    @Test
    public void WaterfallCancel() {
        final String id = "waterfall";
        final int index = 1;

        MyWaterfallDialog dialog = new MyWaterfallDialog(id);
        MyBotTelemetryClient telemetryClient = new MyBotTelemetryClient("Waterfall2_Step2");
        dialog.setTelemetryClient(telemetryClient);

        DialogInstance dialogInstance = new DialogInstance();
        dialogInstance.setId(id);
        Map<String, Object> stateMap = new HashMap<String, Object>();
        stateMap.put("stepIndex", index);
        stateMap.put("instanceId", "(guid)");
        dialogInstance.setState(stateMap);

         dialog.endDialog(
            new TurnContextImpl(new TestAdapter(), new Activity(ActivityTypes.MESSAGE)),
            dialogInstance,
            DialogReason.CANCEL_CALLED).join();

        Assert.assertTrue(telemetryClient.trackEventCallCount > 0);
    }

    private WaterfallDialog Create_Waterfall2() {
        WaterfallStep[] steps = new WaterfallStep[]
        {
            new Waterfall2_Step1(),
            new Waterfall2_Step2(),
            new Waterfall2_Step3()
        };
        return new WaterfallDialog("test-waterfall", Arrays.asList(steps));
    }

    private class Waterfall2_Step1 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            stepContext.getContext().sendActivity("step1").join();
            PromptOptions options = new PromptOptions();
            options.setPrompt(MessageFactory.text("Enter a number."));
            options.setRetryPrompt(MessageFactory.text("It must be a number"));
            return  stepContext.prompt("number", options);
           }
    }

    private class Waterfall2_Step2 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            if (stepContext.getValues() != null) {
                int numberResult = (int) stepContext.getResult();
                stepContext.getContext().sendActivity(String.format("Thanks for '%d'", numberResult)).join();
            }

            stepContext.getContext().sendActivity("step2").join();
            PromptOptions options = new PromptOptions();
            options.setPrompt(MessageFactory.text("Enter a number."));
            options.setRetryPrompt(MessageFactory.text("It must be a number"));
            return stepContext.prompt("number", options);
            }
    }

    private class Waterfall2_Step3 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            if (stepContext.getValues() != null) {
                int numberResult = (int) stepContext.getResult();
                stepContext.getContext().sendActivity(String.format("Thanks for '%d'", numberResult)).join();
            }

            stepContext.getContext().sendActivity("step3").join();
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("Value", "All Done!");
            return  stepContext.endDialog(resultMap);
            }
    }
    private class Waterfall3_Step1 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            stepContext.getContext().sendActivity(MessageFactory.text("step1")).join();
            return stepContext.beginDialog("test-waterfall-b", null);
        }
    }

    private class Waterfall3_Step2 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            stepContext.getContext().sendActivity(MessageFactory.text("step2")).join();
            return stepContext.beginDialog("test-waterfall-c", null);
        }
    }

    private class Waterfall4_Step1 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            stepContext.getContext().sendActivity(MessageFactory.text("step1.1")).join();
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        }
    }

    private class Waterfall4_Step2 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            stepContext.getContext().sendActivity(MessageFactory.text("step1.2")).join();
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        }
    }

    private class Waterfall5_Step1 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            stepContext.getContext().sendActivity(MessageFactory.text("step2.1")).join();
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        }
    }

    private class Waterfall5_Step2 implements WaterfallStep {
        @Override
        public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
            stepContext.getContext().sendActivity(MessageFactory.text("step2.2")).join();
            return  stepContext.endDialog();
            }
    }

    private final class MyBotTelemetryClient implements BotTelemetryClient {

        private int trackEventCallCount = 0;
        private String stepNameToMatch = "";

        private MyBotTelemetryClient(String stepNameToMatch) {
            this.stepNameToMatch = stepNameToMatch;
        }

        @Override
        public void trackAvailability(String name, OffsetDateTime timeStamp, Duration duration, String runLocation,
                boolean success, String message, Map<String, String> properties, Map<String, Double> metrics) {
            throw new NotImplementedException("trackAvailability is not implemented");
        }

        @Override
        public void trackDependency(String dependencyTypeName, String target, String dependencyName, String data,
                OffsetDateTime startTime, Duration duration, String resultCode, boolean success) {
            throw new NotImplementedException("trackDependency is not implemented");
        }

        @Override
        public void trackEvent(String eventName, Map<String, String> properties, Map<String, Double> metrics) {
            String stepName = properties.get("StepName");
            if (stepName != null && stepName.equals(stepNameToMatch)) {
                trackEventCallCount++;
            }
        }

        @Override
        public void trackException(Exception exception, Map<String, String> properties, Map<String, Double> metrics) {
            throw new NotImplementedException("trackException is not implemented");
        }

        @Override
        public void trackTrace(String message, Severity severityLevel, Map<String, String> properties) {
            throw new NotImplementedException("trackTrace is not implemented");
        }

        @Override
        public void trackDialogView(String dialogName, Map<String, String> properties, Map<String, Double> metrics) {
            throw new NotImplementedException("trackDialogView is not implemented");
        }

        @Override
        public void flush() {
            throw new NotImplementedException("flush is not implemented");
        }
    }

    private class MyWaterfallDialog extends WaterfallDialog {
        public MyWaterfallDialog(String id) {
            super(id, null);
            addStep(new Waterfall2_Step1());
            addStep(new Waterfall2_Step2());
            addStep(new Waterfall2_Step3());
        }

        private class Waterfall2_Step1 implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                stepContext.getContext().sendActivity("step1").join();
                return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
            }
        }

        private class Waterfall2_Step2 implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                stepContext.getContext().sendActivity("step2").join();
                return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
            }
        }
        private class Waterfall2_Step3 implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                stepContext.getContext().sendActivity("step3").join();
                return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
            }
        }
    }
}

