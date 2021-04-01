// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

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

import org.junit.Assert;
import org.junit.Test;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

public class ChoicePromptTests {
    private static List<Choice> colorChoices = Arrays.asList(new Choice("red"), new Choice("green"),
            new Choice("blue"));

    @Test
    public void ChoicePromptWithEmptyIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new ChoicePrompt("", null, null));
    }

    @Test
    public void ChoicePromptWithNullIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new ChoicePrompt(null, null, null));
    }

    @Test
    public void ChoicePromptWithCardActionAndNoValueShouldNotFail() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt eventPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        dialogs.add(eventPrompt);

         new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                Choice choice = new Choice();
                CardAction action = new CardAction();
                action.setType(ActionTypes.IM_BACK);
                action.setValue("value");
                action.setTitle("title");
                choice.setAction(action);

                PromptOptions options = new PromptOptions();
                List<Choice> choiceList = new ArrayList<Choice>();
                choiceList.add(choice);
                options.setChoices(choiceList);

                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
         })
         .send("hello")
         .assertReply(new Validators().validedStartsWith(" (1) title"))
         .startTest()
         .join();
    }

    @Test
    public void ShouldSendPrompt() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt eventPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        dialogs.add(eventPrompt);

         new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validedStartsWith("favorite color?"))
            .startTest()
            .join();
    }

    @Test
    public void ShouldSendPromptAsAnInlineList() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt eventPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        dialogs.add(eventPrompt);

         new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply("favorite color? (1) red, (2) green, or (3) blue")
            .startTest()
            .join();
    }

    @Test
    public void ShouldSendPromptAsANumberedList() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.LIST);

        dialogs.add(listPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply("favorite color?\n\n   1. red\n   2. green\n   3. blue")
            .startTest()
            .join();
    }

    @Test
    public void ShouldSendPromptUsingSuggestedActions() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.SUGGESTED_ACTION);

        dialogs.add(listPrompt);

        CardAction red = new CardAction();
        red.setType(ActionTypes.IM_BACK);
        red.setValue("red");
        red.setTitle("red");
        CardAction green = new CardAction();
        green.setType(ActionTypes.IM_BACK);
        green.setValue("green");
        green.setTitle("green");
        CardAction blue = new CardAction();
        blue.setType(ActionTypes.IM_BACK);
        blue.setValue("blue");
        blue.setTitle("blue");
        CardAction[] suggestedActions = new CardAction[] { red, green, blue };
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validateSuggestedActions("favorite color?", new SuggestedActions(suggestedActions)))
            .startTest()
            .join();
    }

    @Test
    public void ShouldSendPromptUsingHeroCard() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.HEROCARD);

        dialogs.add(listPrompt);

        CardAction red = new CardAction();
        red.setType(ActionTypes.IM_BACK);
        red.setValue("red");
        red.setTitle("red");
        CardAction green = new CardAction();
        green.setType(ActionTypes.IM_BACK);
        green.setValue("green");
        green.setTitle("green");
        CardAction blue = new CardAction();
        blue.setType(ActionTypes.IM_BACK);
        blue.setValue("blue");
        blue.setTitle("blue");
        ArrayList<CardAction> buttons = new ArrayList<CardAction>();
        buttons.add(red);
        buttons.add(green);
        buttons.add(blue);
        HeroCard card = new HeroCard();
        card.setText("favorite color?");
        card.setButtons(buttons);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validateHeroCard(card, 0))
            .startTest().join();
    }

    @Test
    public void ShouldSendPromptUsingAppendedHeroCard() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.HEROCARD);

        dialogs.add(listPrompt);
        HeroCard card = new HeroCard();
        card.setText("favorite color?");
        CardAction red = new CardAction();
        red.setType(ActionTypes.IM_BACK);
        red.setValue("red");
        red.setTitle("red");
        CardAction green = new CardAction();
        green.setType(ActionTypes.IM_BACK);
        green.setValue("green");
        green.setTitle("green");
        CardAction blue = new CardAction();
        blue.setType(ActionTypes.IM_BACK);
        blue.setValue("blue");
        blue.setTitle("blue");
        ArrayList<CardAction> buttons = new ArrayList<CardAction>();
        buttons.add(red);
        buttons.add(green);
        buttons.add(blue);
        card.setButtons(buttons);


        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                Attachment attachment = new Attachment();
                attachment.setContent("some content");
                attachment.setContentType("text/plain");

                PromptOptions options = new PromptOptions();
                Activity activity = MessageFactory.text("favorite color?");
                activity.setAttachments(Arrays.asList(attachment));
                options.setPrompt(activity);
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validateHeroCard(card, 1))
            .startTest().join();
    }

    @Test
    public void ShouldSendPromptWithoutAddingAList() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.NONE);

        dialogs.add(listPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply("favorite color?")
            .startTest()
            .join();
    }

    @Test
    public void ShouldSendPromptWithoutAddingAListButAddingSsml() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.NONE);

        dialogs.add(listPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = MessageFactory.text("favorite color?");
                activity.setSpeak("spoken prompt");
                options.setPrompt(activity);
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply(new Validators().validateSpeak("favorite color?", "spoken prompt"))
        .startTest()
        .join();
    }

    @Test
    public void ShouldRecognizeAChoice() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.NONE);

        dialogs.add(listPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                FoundChoice choiceResult = (FoundChoice) results.getResult();
                turnContext.sendActivities(MessageFactory.text(choiceResult.getValue())).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply(new Validators().validedStartsWith("favorite color?"))
        .send("red")
        .assertReply("red")
        .startTest()
        .join();
    }

    // This is being left out for now due to it failing due to an issue in the Text Recognizers library.
    // It should be worked out in the recognizers and then this test should be enabled again.
    @Test
    public void ShouldNotRecognizeOtherText() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.NONE);

        dialogs.add(listPrompt);
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setRetryPrompt(MessageFactory.text("your favorite color, please?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply(new Validators().validedStartsWith("favorite color?"))
        .send("what was that?")
        .assertReply("your favorite color, please?")
        .startTest()
        .join();
    }

    @Test
    public void ShouldCallCustomValidator() {

        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        PromptValidator<FoundChoice> validator =  (promptContext) -> {
            promptContext.getContext().sendActivity(MessageFactory.text("validator called")).join();
           return CompletableFuture.completedFuture(true);
       };

        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", validator, Culture.English);
        listPrompt.setStyle(ListStyle.NONE);

        dialogs.add(listPrompt);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validedStartsWith("favorite color?"))
            .send("I'll take the red please.")
            .assertReply("validator called")
            .startTest()
            .join();
    }

    @Test
    public void ShouldUseChoiceStyleIfPresent() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.HEROCARD);

        dialogs.add(listPrompt);
        CardAction red = new CardAction();
        red.setType(ActionTypes.IM_BACK);
        red.setValue("red");
        red.setTitle("red");
        CardAction green = new CardAction();
        green.setType(ActionTypes.IM_BACK);
        green.setValue("green");
        green.setTitle("green");
        CardAction blue = new CardAction();
        blue.setType(ActionTypes.IM_BACK);
        blue.setValue("blue");
        blue.setTitle("blue");
        CardAction[] suggestedActions = new CardAction[]{red, green, blue};

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                options.setStyle(ListStyle.SUGGESTED_ACTION);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply(new Validators().validateSuggestedActions("favorite color?", new SuggestedActions(suggestedActions)))
        .startTest()
        .join();
    }

    @Test
    public void ShouldDefaultToEnglishLocaleNull() {
        PerformShouldDefaultToEnglishLocale(null);
    }

    @Test
    public void ShouldDefaultToEnglishLocaleEmptyString() {
        PerformShouldDefaultToEnglishLocale("");
    }

    @Test
    public void ShouldDefaultToEnglishLocaleNotSupported() {
        Assert.assertThrows(IllformedLocaleException.class, () -> {
            try {
                    PerformShouldDefaultToEnglishLocale("not-supported");
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });
    }

    public void PerformShouldDefaultToEnglishLocale(String locale) {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);

        dialogs.add(listPrompt);

        Activity helloLocale = MessageFactory.text("hello");
        helloLocale.setLocale(locale);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send(helloLocale)
        .assertReply((activity) -> {
            // Use ChoiceFactory to build the expected answer, manually
            ChoiceFactoryOptions testChoiceOption = new ChoiceFactoryOptions();
            testChoiceOption.setInlineOr(PromptCultureModels.ENGLISH.getInlineOr());
            testChoiceOption.setInlineOrMore(PromptCultureModels.ENGLISH.getInlineOrMore());
            testChoiceOption.setInlineSeparator(PromptCultureModels.ENGLISH.getSeparator());

            String expectedChoices = ChoiceFactory.inline(colorChoices, null, null, testChoiceOption).getText();
            Assert.assertEquals(String.format("favorite color?%s", expectedChoices), activity.getText());
        })
        .startTest()
        .join();
    }

    @Test
    public void ShouldAcceptAndRecognizeCustomLocaleDict() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));
        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        PromptCultureModel culture = new PromptCultureModel();
        culture.setInlineOr(" customOr ");
        culture.setInlineOrMore(" customOrMore ");
        culture.setLocale("custom-custom");
        culture.setSeparator("customSeparator");
        culture.setNoInLanguage("customNo");
        culture.setYesInLanguage("customYes");

        Map<String, ChoiceFactoryOptions> customDict = new HashMap<String, ChoiceFactoryOptions>();
        ChoiceFactoryOptions choiceOption = new ChoiceFactoryOptions(culture.getSeparator(),
                                                                    culture.getInlineOr(),
                                                                    culture.getInlineOrMore(),
                                                                    true);
        customDict.put(culture.getLocale(), choiceOption);

        dialogs.add(new ChoicePrompt("ChoicePrompt", customDict, null, culture.getLocale()));

        Activity helloLocale = MessageFactory.text("hello");
        helloLocale.setLocale(culture.getLocale());
        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = MessageFactory.text("favorite color?");
                activity.setLocale(culture.getLocale());
                options.setPrompt(activity);
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        })
        .send(helloLocale)
        .assertReply((activity) -> {
            // Use ChoiceFactory to build the expected answer, manually
            ChoiceFactoryOptions testChoiceOption = new ChoiceFactoryOptions(culture.getSeparator(),
            culture.getInlineOr(),
            culture.getInlineOrMore(),
            true);

            String expectedChoices = ChoiceFactory.inline(colorChoices, null, null, testChoiceOption).getText();
            Assert.assertEquals(String.format("favorite color?%s", expectedChoices), activity.getText());
        })
        .startTest()
        .join();
    }

    class Validators {
        public Validators() {

        }
        private Consumer<Activity> validedStartsWith(String expected) {
            return activity -> {
                //Assert.IsAssignableFrom<MessageActivity>(activity);
                Activity msg = (Activity) activity;
                Assert.assertTrue(msg.getText().startsWith(expected));
            };
        }

        private Consumer<Activity> validateSuggestedActions(String expectedText,
                                        SuggestedActions expectedSuggestedActions) {
            return activity -> {
                //Assert.IsAssignableFrom<MessageActivity>(activity);
                Assert.assertEquals(expectedText, activity.getText());
                Assert.assertEquals(expectedSuggestedActions.getActions().size(),
                activity.getSuggestedActions().getActions().size());

                for (int i = 0; i < expectedSuggestedActions.getActions().size(); i++) {
                    Assert.assertEquals(expectedSuggestedActions.getActions().get(i).getType(),
                        activity.getSuggestedActions().getActions().get(i).getType());
                    Assert.assertEquals(expectedSuggestedActions.getActions().get(i).getValue(),
                        activity.getSuggestedActions().getActions().get(i).getValue());
                    Assert.assertEquals(expectedSuggestedActions.getActions().get(i).getTitle(),
                        activity.getSuggestedActions().getActions().get(i).getTitle());
                }
            };
        }

        private Consumer<Activity> validateHeroCard(HeroCard expectedHeroCard, int index) {
            return activity -> {
                HeroCard attachedHeroCard = (HeroCard) activity.getAttachments().get(index).getContent();

                Assert.assertEquals(expectedHeroCard.getTitle(), attachedHeroCard.getTitle());
                Assert.assertEquals(expectedHeroCard.getButtons().size(), attachedHeroCard.getButtons().size());
                for (int i = 0; i < expectedHeroCard.getButtons().size(); i++) {
                    Assert.assertEquals(expectedHeroCard.getButtons().get(i).getType(),
                                        attachedHeroCard.getButtons().get(i).getType());
                    Assert.assertEquals(expectedHeroCard.getButtons().get(i).getValue(),
                                        attachedHeroCard.getButtons().get(i).getValue());
                    Assert.assertEquals(expectedHeroCard.getButtons().get(i).getTitle(),
                                        attachedHeroCard.getButtons().get(i).getTitle());
                }
            };
        }

        private Consumer<Activity> validateSpeak(String expectedText, String expectedSpeak) {
            return activity -> {
                Activity msg = (Activity) activity;
                Assert.assertEquals(expectedText, msg.getText());
                Assert.assertEquals(expectedSpeak, msg.getSpeak());
            };
        }

    }
}
