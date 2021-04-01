// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.activation.UnsupportedDataTypeException;

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

public class NumberPromptTests {

    @Test
    public void NumberPromptWithEmptyIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new NumberPrompt<Integer>("", Integer.class));
    }

    @Test
    public void NumberPromptWithNullIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new NumberPrompt<Integer>(null, Integer.class));
    }

    @Test
    public void NumberPromptWithUnsupportedTypeShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new NumberPrompt<Short>("prompt", Short.class));
    }

    @Test
    public void NumberPromptWithNullTurnContextShouldFail() {
         Assert.assertThrows(IllegalArgumentException.class, () -> {
             try {
                NumberPromptMock numberPromptMock = new NumberPromptMock("NumberPromptMock", null, null);

                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Please send a number.");
                options.setPrompt(activity);
                numberPromptMock.onPromptNullContext(options).join();
             } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });
    }

    @Test
    public void OnPromptErrorsWithNullOptions() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
            ConversationState convoState = new ConversationState(new MemoryStorage());
            StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

            TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

            DialogSet dialogs = new DialogSet(dialogState);
            // Create and add custom activity prompt to DialogSet.
            NumberPromptMock numberPromptMock = new NumberPromptMock("NumberPromptMock", null, null);

            dialogs.add(numberPromptMock);
            new TestFlow(adapter, (turnContext) -> {
                DialogContext dc = dialogs.createContext(turnContext).join();
                numberPromptMock.onPromptNullOptions(dc).join();
                return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .startTest()
            .join();
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });
    }

    @Test
    public void OnRecognizeWithNullTurnContextShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
            NumberPromptMock numberPromptMock = new NumberPromptMock("NumberPromptMock", null, null);
            numberPromptMock.onRecognizeNullContext();
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });
    }

    @Test
    public void NumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Integer> numberPrompt = new NumberPrompt<Integer>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Integer.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                int numberResult = (int) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%d'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("42")
        .assertReply("Bot received the number '42'.")
        .startTest()
        .join();
    }

    @Test
    public void NumberPromptRetry() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Integer> numberPrompt = new NumberPrompt<Integer>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Integer.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                Activity retryActivity = new Activity(ActivityTypes.MESSAGE);
                retryActivity.setText("You must enter a number.");
                options.setRetryPrompt(retryActivity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                int numberResult = (int) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%d'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("hello")
        .assertReply("You must enter a number.")
        .send("64")
        .assertReply("Bot received the number '64'.")
        .startTest()
        .join();
    }

    @Test
    public void NumberPromptValidator() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        PromptValidator<Integer> validator = (promptContext) -> {
            Integer result = promptContext.getRecognized().getValue();

            if (result < 100 && result > 0) {
                return CompletableFuture.completedFuture(true);
            }

            return CompletableFuture.completedFuture(false);
        };

        // Create and add number prompt to DialogSet.
        NumberPrompt<Integer> numberPrompt = new NumberPrompt<Integer>("NumberPrompt", validator,
                                                    PromptCultureModels.ENGLISH_CULTURE, Integer.class);
        dialogs.add(numberPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                Activity retryActivity = new Activity(ActivityTypes.MESSAGE);
                retryActivity.setText("You must enter a positive number less than 100.");
                options.setRetryPrompt(retryActivity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                int numberResult = (int) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%d'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("150")
        .assertReply("You must enter a positive number less than 100.")
        .send("64")
        .assertReply("Bot received the number '64'.")
        .startTest()
        .join();
    }

    @Test
    public void FloatNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Float> numberPrompt = new NumberPrompt<Float>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Float.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Float numberResult = (Float) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%.2f'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("3.14")
        .assertReply("Bot received the number '3.14'.")
        .startTest()
        .join();
    }

    @Test
    public void LongNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Long> numberPrompt = new NumberPrompt<Long>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Long.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Long numberResult = (Long) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%d'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("42")
        .assertReply("Bot received the number '42'.")
        .startTest()
        .join();
    }

    @Test
    public void DoubleNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Double.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%.2f'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("3.14")
        .assertReply("Bot received the number '3.14'.")
        .startTest()
        .join();
    }

    @Test
    public void CurrencyNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Double.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%.0f'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })        .send("hello")
        .assertReply("Enter a number.")
        .send("$500")
        .assertReply("Bot received the number '500'.")
        .startTest()
        .join();
    }

    @Test
    public void AgeNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Double.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%.0f'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("i am 18 years old")
        .assertReply("Bot received the number '18'.")
        .startTest()
        .join();
    }

    @Test
    public void DimensionNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Double.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%.0f'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("I've run 5km")
        .assertReply("Bot received the number '5'.")
        .startTest()
        .join();
    }

    @Test
    public void TemperatureNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null,
                                                    PromptCultureModels.ENGLISH_CULTURE, Double.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%.0f'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("The temperature is 32C")
        .assertReply("Bot received the number '32'.")
        .startTest()
        .join();
    }

    @Test
    public void CultureThruNumberPromptCtor() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null,
                                                    PromptCultureModels.DUTCH_CULTURE, Double.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                Assert.assertTrue(3.14 == numberResult);
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("3,14")
        .startTest()
        .join();
    }

    @Test
    public void CultureThruActivityNumberPrompt() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null,
                                                    PromptCultureModels.DUTCH_CULTURE, Double.class);
        dialogs.add(numberPrompt);
        Activity activityToSend = new Activity(ActivityTypes.MESSAGE);
        activityToSend.setText("3,14");
        activityToSend.setLocale(PromptCultureModels.DUTCH_CULTURE);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                Assert.assertTrue(3.14 == numberResult);
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send(activityToSend)
        .startTest()
        .join();
    }

    @Test
    public void NumberPromptDefaultsToEnUsLocale() throws UnsupportedDataTypeException {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Create and add number prompt to DialogSet.
        NumberPrompt<Double> numberPrompt = new NumberPrompt<Double>("NumberPrompt", null, null, Double.class);
        dialogs.add(numberPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("Enter a number.");
                options.setPrompt(activity);
                dc.prompt("NumberPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                Double numberResult = (Double) results.getResult();
                turnContext.sendActivity(
                    MessageFactory.text(String.format("Bot received the number '%.2f'.", numberResult))).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("Enter a number.")
        .send("3.14")
        .assertReply("Bot received the number '3.14'.")
        .startTest()
        .join();
    }
}
