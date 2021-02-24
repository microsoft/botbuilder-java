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
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.Severity;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TraceTranscriptLogger;
import com.microsoft.bot.builder.TranscriptLoggerMiddleware;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.prompts.NumberPrompt;
import com.microsoft.bot.dialogs.prompts.PromptCultureModels;
import com.microsoft.bot.dialogs.prompts.PromptOptions;


import org.junit.Assert;
import org.junit.Test;

public class ComponentDialogTests {

    @Test
    public void CallDialogInParentComponent() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState))
                .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        new TestFlow(adapter, (turnContext) -> {
            // DialogState state = dialogState.get(turnContext, () -> new
            // DialogState()).join();
            DialogSet dialogs = new DialogSet(dialogState);

            ComponentDialog childComponent = new ComponentDialog("childComponent");

            class Step1 implements WaterfallStep {
                public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                    return stepContext.getContext()
                        .sendActivity("Child started.")
                        .thenCompose(resourceResponse -> stepContext.beginDialog("parentDialog", "test"));
                }
            }

            class Step2 implements WaterfallStep {
                public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                    return stepContext.getContext()
                        .sendActivity(String.format("Child finished. Value: %s", stepContext.getResult()))
                        .thenCompose(resourceResponse -> stepContext.endDialog());
                }
            }

            WaterfallStep[] childStep = new WaterfallStep[] {new Step1(), new Step2() };

            childComponent.addDialog(new WaterfallDialog("childDialog", Arrays.asList(childStep)));

            ComponentDialog parentComponent = new ComponentDialog("parentComponent");
            parentComponent.addDialog(childComponent);

            class ParentStep implements WaterfallStep {
                public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                    return stepContext.getContext()
                        .sendActivity(String.format("Parent called.", stepContext.getResult()))
                        .thenCompose(resourceResponse -> stepContext.endDialog(stepContext.getOptions()));
                }
            }
            WaterfallStep[] parentStep = new WaterfallStep[] {new ParentStep() };

            parentComponent.addDialog(new WaterfallDialog("parentDialog", Arrays.asList(parentStep)));

            dialogs.add(parentComponent);

            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.beginDialog("parentComponent", null).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                int value = (int) results.getResult();
                turnContext.sendActivity(MessageFactory.text(String.format("Bot received the number '%d'.", value))).join();
            }

            return CompletableFuture.completedFuture(null);
        }).send("Hi").assertReply("Child started.").assertReply("Parent called.")
                .assertReply("Child finished. Value: test").startTest().join();
    }

    @Test
    public void BasicWaterfallTest() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("BasicWaterfallTest", "testuser", "testbot"))
                        .use(new AutoSaveStateMiddleware(convoState))
                        .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        new TestFlow(adapter, (turnContext) -> {
            DialogState state = dialogState.get(turnContext, () -> new DialogState()).join();
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(createWaterfall());
            try {
                dialogs.add(new NumberPrompt<Integer>("number", Integer.class));
            } catch (Throwable t) {
                t.printStackTrace();
            }

            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.beginDialog("test-waterfall", null).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                int value = (int) results.getResult();
                turnContext.sendActivity(MessageFactory.text(String.format("Bot received the number '%d'.", value))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("42")
        .assertReply("Thanks for '42'")
        .assertReply("Enter another number.")
        .send("64")
        .assertReply("Bot received the number '64'.")
        .startTest()
        .join();
    }

    @Test
    public void TelemetryBasicWaterfallTest() throws UnsupportedDataTypeException {
        TestComponentDialog testComponentDialog = new TestComponentDialog();
        Assert.assertEquals(NullBotTelemetryClient.class, testComponentDialog.getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                                testComponentDialog.findDialog("test-waterfall").getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                                testComponentDialog.findDialog("number").getTelemetryClient().getClass());

        testComponentDialog.setTelemetryClient(new MyBotTelemetryClient());
        Assert.assertEquals(MyBotTelemetryClient.class, testComponentDialog.getTelemetryClient().getClass());
        Assert.assertEquals(MyBotTelemetryClient.class,
                            testComponentDialog.findDialog("test-waterfall").getTelemetryClient().getClass());
        Assert.assertEquals(MyBotTelemetryClient.class,
                            testComponentDialog.findDialog("number").getTelemetryClient().getClass());
    }

    @Test
    public void TelemetryHeterogeneousLoggerTest() throws UnsupportedDataTypeException {
        TestComponentDialog testComponentDialog = new TestComponentDialog();
        Assert.assertEquals(NullBotTelemetryClient.class, testComponentDialog.getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("test-waterfall").getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("number").getTelemetryClient().getClass());

        testComponentDialog.findDialog("test-waterfall").setTelemetryClient(new MyBotTelemetryClient());

        Assert.assertEquals(MyBotTelemetryClient.class,
                            testComponentDialog.findDialog("test-waterfall").getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("number").getTelemetryClient().getClass());
    }

    @Test
    public void TelemetryAddWaterfallTest() throws UnsupportedDataTypeException {
        TestComponentDialog testComponentDialog = new TestComponentDialog();
        Assert.assertEquals(NullBotTelemetryClient.class, testComponentDialog.getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("test-waterfall").getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("number").getTelemetryClient().getClass());

        testComponentDialog.setTelemetryClient(new MyBotTelemetryClient());
        testComponentDialog.addDialog(new WaterfallDialog("C", null));

        Assert.assertEquals(MyBotTelemetryClient.class,
                            testComponentDialog.findDialog("C").getTelemetryClient().getClass());
    }

    @Test
    public void TelemetryNullUpdateAfterAddTest() throws UnsupportedDataTypeException {
        TestComponentDialog testComponentDialog = new TestComponentDialog();
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("test-waterfall").getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("number").getTelemetryClient().getClass());

        testComponentDialog.setTelemetryClient(new MyBotTelemetryClient());
        testComponentDialog.addDialog(new WaterfallDialog("C", null));

        Assert.assertEquals(MyBotTelemetryClient.class,
                            testComponentDialog.findDialog("C").getTelemetryClient().getClass());
        testComponentDialog.setTelemetryClient(null);

        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("test-waterfall").getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("number").getTelemetryClient().getClass());
        Assert.assertEquals(NullBotTelemetryClient.class,
                            testComponentDialog.findDialog("C").getTelemetryClient().getClass());
    }

    @Test
    public void BasicComponentDialogTest() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("BasicComponentDialogTest", "testuser", "testbot"))
                        .use(new AutoSaveStateMiddleware(convoState))
                        .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        new TestFlow(adapter, (turnContext) -> {
            DialogState state = dialogState.get(turnContext, () -> new DialogState()).join();
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(new TestComponentDialog());

            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.beginDialog("TestComponentDialog", null).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                int value = (int) results.getResult();
                turnContext.sendActivity(MessageFactory.text(String.format("Bot received the number '%d'.", value))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("42")
        .assertReply("Thanks for '42'")
        .assertReply("Enter another number.")
        .send("64")
        .assertReply("Bot received the number '64'.")
        .startTest()
        .join();
    }

    @Test
    public void NestedComponentDialogTest() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("BasicComponentDialogTest", "testuser", "testbot"))
                        .use(new AutoSaveStateMiddleware(convoState))
                        .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        new TestFlow(adapter, (turnContext) -> {
            DialogState state = dialogState.get(turnContext, () -> new DialogState()).join();
            DialogSet dialogs = new DialogSet(dialogState);

            dialogs.add(new TestNestedComponentDialog());

            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.beginDialog("TestNestedComponentDialog", null).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                int value = (int) results.getResult();
                turnContext.sendActivity(MessageFactory.text(String.format("Bot received the number '%d'.", value))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")

        // step 1
        .assertReply("Enter a number.")

        // step 2
        .send("42")
        .assertReply("Thanks for '42'")
        .assertReply("Enter another number.")

        // step 3 and step 1 again (nested component)
        .send("64")
        .assertReply("Got '64'.")
        .assertReply("Enter a number.")

        // step 2 again (from the nested component)
        .send("101")
        .assertReply("Thanks for '101'")
        .assertReply("Enter another number.")

        // driver code
        .send("5")
        .assertReply("Bot received the number '5'.")
        .startTest()
        .join();
    }

    @Test
    public void CallDialogDefinedInParentComponent() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        Map<String, String> options = new HashMap<String, String>();
        options.put("value", "test");

        ComponentDialog childComponent = new ComponentDialog("childComponent");

        class Step1 implements WaterfallStep {
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                return stepContext.getContext().sendActivity("Child started.")
                    .thenCompose(resourceResponse -> stepContext.beginDialog("parentDialog", options));
            }
        }

        class Step2 implements WaterfallStep {
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                Assert.assertEquals("test", (String) stepContext.getResult());
                return stepContext.getContext().sendActivity("Child finished.")
                    .thenCompose(resourceResponse -> stepContext.endDialog());
            }
        }

        WaterfallStep[] childActions = new WaterfallStep[] {new Step1(), new Step2() };

        childComponent.addDialog(new WaterfallDialog("childDialog", Arrays.asList(childActions)));

        ComponentDialog parentComponent = new ComponentDialog("parentComponent");
        parentComponent.addDialog(childComponent);

        class ParentAction implements WaterfallStep {
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                Map<String, String> stepOptions = (Map<String, String>) stepContext.getOptions();
                Assert.assertNotNull(stepOptions);
                Assert.assertTrue(stepOptions.containsKey("value"));
                return stepContext.getContext()
                    .sendActivity(String.format("Parent called with: %s", stepOptions.get("value")))
                    .thenCompose(resourceResponse -> stepContext.endDialog(stepOptions.get("value")));
            }
        }

        WaterfallStep[] parentActions = new WaterfallStep[] {
            new ParentAction()
        };

        parentComponent.addDialog(new WaterfallDialog("parentDialog", Arrays.asList(parentActions)));
        new TestFlow(adapter, (turnContext) -> {
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(parentComponent);

            DialogContext dc =  dialogs.createContext(turnContext).join();

            DialogTurnResult results =  dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                 dc.beginDialog("parentComponent", null).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                 turnContext.sendActivity(MessageFactory.text("Done")).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("Hi")
        .assertReply("Child started.")
        .assertReply("Parent called with: test")
        .assertReply("Child finished.")
        .startTest()
        .join();
    }

    // private static TestFlow CreateTestFlow(WaterfallDialog waterfallDialog) {
    //     var convoState = new ConversationState(new MemoryStorage());
    //     var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //     var adapter = new TestAdapter()
    //         .Use(new AutoSaveStateMiddleware(convoState));

    //     var testFlow = new TestFlow(adapter,  (turnContext) -> {
    //         var state =  dialogState.Get(turnContext, () -> new DialogState());
    //         var dialogs = new DialogSet(dialogState);

    //         dialogs.Add(new CancelledComponentDialog(waterfallDialog));

    //         var dc =  dialogs.CreateContext(turnContext);

    //         var results =  dc.ContinueDialog(cancellationToken);
    //         if (results.Status == DialogTurnStatus.Empty) {
    //             results =  dc.BeginDialog("TestComponentDialog", null);
    //         }

    //         if (results.Status == DialogTurnStatus.Cancelled) {
    //              turnContext.SendActivity(MessageFactory.Text($"Component dialog cancelled (result value is {results.Result?.toString()})."));
    //         } else if (results.Status == DialogTurnStatus.Complete) {
    //             var value = (int)results.Result;
    //              turnContext.SendActivity(MessageFactory.Text($"Bot received the number '{value}'."));
    //         }
    //     });
    //     return testFlow;
    // }

    private static WaterfallDialog createWaterfall() {
        class WaterfallStep1 implements WaterfallStep {

            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Enter a number."));
                return stepContext.prompt("number", options);
            }
        }

        class WaterfallStep2 implements WaterfallStep {

            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                if (stepContext.getValues() != null) {
                    int numberResult = (int) stepContext.getResult();
                    stepContext.getContext()
                            .sendActivity(MessageFactory.text(String.format("Thanks for '%d'", numberResult))).join();
                }
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Enter another number."));
                return stepContext.prompt("number", options);
            }
        }

        WaterfallStep[] steps = new WaterfallStep[] {new WaterfallStep1(), new WaterfallStep2() };
        return new WaterfallDialog("test-waterfall", Arrays.asList(steps));
    }

    private class MyBotTelemetryClient implements BotTelemetryClient {
        private MyBotTelemetryClient() {
        }

        @Override
        public void trackAvailability(String name, OffsetDateTime timeStamp, Duration duration, String runLocation,
                boolean success, String message, Map<String, String> properties, Map<String, Double> metrics) {

        }

        @Override
        public void trackDependency(String dependencyTypeName, String target, String dependencyName, String data,
                OffsetDateTime startTime, Duration duration, String resultCode, boolean success) {

        }

        @Override
        public void trackEvent(String eventName, Map<String, String> properties, Map<String, Double> metrics) {

        }

        @Override
        public void trackException(Exception exception, Map<String, String> properties, Map<String, Double> metrics) {

        }

        @Override
        public void trackTrace(String message, Severity severityLevel, Map<String, String> properties) {

        }

        @Override
        public void trackDialogView(String dialogName, Map<String, String> properties, Map<String, Double> metrics) {

        }

        @Override
        public void flush() {

        }
    }

    private class TestComponentDialog extends ComponentDialog {
        private TestComponentDialog() {
            super("TestComponentDialog");
            addDialog(createWaterfall());
            addDialog(new NumberPrompt<Integer>("number", null, PromptCultureModels.ENGLISH_CULTURE, Integer.class));
        }
    }

    private final class TestNestedComponentDialog extends ComponentDialog {
        private TestNestedComponentDialog() {
        super("TestNestedComponentDialog");
            WaterfallStep[] steps = new WaterfallStep[] {
                new WaterfallStep1(),
                new WaterfallStep2(),
                new WaterfallStep3(),
            };
            addDialog(new WaterfallDialog("test-waterfall", Arrays.asList(steps)));
            addDialog(new NumberPrompt<Integer>("number", null, PromptCultureModels.ENGLISH_CULTURE, Integer.class));
            addDialog(new TestComponentDialog());
        }
        class WaterfallStep1 implements WaterfallStep {

            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Enter a number."));
                return stepContext.prompt("number", options);
            }
        }

        class WaterfallStep2 implements WaterfallStep {

            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                if (stepContext.getValues() != null) {
                    int numberResult = (int) stepContext.getResult();
                    stepContext.getContext()
                            .sendActivity(MessageFactory.text(String.format("Thanks for '%d'", numberResult))).join();
                }
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Enter another number."));
                return stepContext.prompt("number", options);
            }
        }
        class WaterfallStep3 implements WaterfallStep {

            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                if (stepContext.getValues() != null) {
                    int numberResult = (int) stepContext.getResult();
                     stepContext.getContext().sendActivity(
                                              MessageFactory.text(String.format("Got '%d'.", numberResult)));
                }
                return  stepContext.beginDialog("TestComponentDialog", null);
            }
        }

    }

    // private class CancelledComponentDialog : ComponentDialog {
    //     public CancelledComponentDialog(Dialog waterfallDialog) {
    //     super("TestComponentDialog");
    //         AddDialog(waterfallDialog);
    //         AddDialog(new NumberPrompt<int>("number", defaultLocale: Culture.English));
    //     }
    // }
}

