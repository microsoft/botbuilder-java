package com.microsoft.bot.dialogs.prompts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import com.microsoft.bot.dialogs.TestLocale;
import com.microsoft.bot.dialogs.choices.Choice;
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

    /**
     * Generates an Enumerable of variations on all supported locales.
     *
     * @return An iterable collection of Objects.
     */
    public static List<Object[]> getLocaleVariationTest() {
        TestLocale[] testLocales = new TestLocale[13];
        testLocales[0] = new TestLocale(PromptCultureModels.BULGARIAN, null, null, null);
        testLocales[1] = new TestLocale(PromptCultureModels.CHINESE, null, null, null);
        testLocales[2] = new TestLocale(PromptCultureModels.DUTCH, null, null, null);
        testLocales[3] = new TestLocale(PromptCultureModels.ENGLISH, null, null, null);
        testLocales[4] = new TestLocale(PromptCultureModels.FRENCH, null, null, null);
        testLocales[5] = new TestLocale(PromptCultureModels.HINDI, null, null, null);
        testLocales[6] = new TestLocale(PromptCultureModels.ITALIAN, null, null, null);
        testLocales[7] = new TestLocale(PromptCultureModels.JAPANESE, null, null, null);
        testLocales[8] = new TestLocale(PromptCultureModels.KOREAN, null, null, null);
        testLocales[9] = new TestLocale(PromptCultureModels.PORTUGUESE, null, null, null);
        testLocales[10] = new TestLocale(PromptCultureModels.SPANISH, null, null, null);
        testLocales[11] = new TestLocale(PromptCultureModels.SWEDISH, null, null, null);
        testLocales[12] = new TestLocale(PromptCultureModels.TURKISH, null, null, null);

        List<Object[]> resultList = new ArrayList<Object[]>();
        for (TestLocale testLocale : testLocales) {
            resultList.add(new Object[] { testLocale.getValidLocale(), testLocale.InlineOr, testLocale.InlineOrMore,
                    testLocale.Separator });
            resultList.add(new Object[] { testLocale.getCapEnding(), testLocale.InlineOr, testLocale.InlineOrMore,
                    testLocale.Separator });
            resultList.add(new Object[] { testLocale.getCapEnding(), testLocale.InlineOr, testLocale.InlineOrMore,
                    testLocale.Separator });
            resultList.add(new Object[] { testLocale.getCapEnding(), testLocale.InlineOr, testLocale.InlineOrMore,
                    testLocale.Separator });
        }

        return resultList;
    }

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
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                options.setPrompt(activity);
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
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                options.setPrompt(activity);
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
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                options.setPrompt(activity);
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply("favorite color?\n\n   1. red\n   2. green\n   3. blue")
            .startTest();
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

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                options.setPrompt(activity);
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validateSuggestedActions("favorite color?", new SuggestedActions(new
            CardAction[]
            {
                new CardAction() {
                    {
                        setType(ActionTypes.IM_BACK);
                        setValue("red");
                        setTitle("red");
                    }
                },
                new CardAction() {
                    {
                        setType(ActionTypes.IM_BACK);
                        setValue("green");
                        setTitle("green");
                    }
                },
                new CardAction() {
                    {
                        setType(ActionTypes.IM_BACK);
                        setValue("blue");
                        setTitle("blue");
                    }
                }
            })))
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

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                options.setPrompt(activity);
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validateHeroCard(
                new HeroCard() {
                    {
                        setText("favorite color?");
                        setButtons(new ArrayList<CardAction>() {
                            {
                                add(new CardAction() {
                                        {
                                            setType(ActionTypes.IM_BACK);
                                            setValue("red");
                                            setTitle("red");
                                        }
                                    });
                                    add(new CardAction() {
                                        {
                                            setType(ActionTypes.IM_BACK);
                                            setValue("green");
                                            setTitle("green");
                                        }
                                    });
                                    add(new CardAction() {
                                        {
                                            setType(ActionTypes.IM_BACK);
                                            setValue("blue");
                                            setTitle("blue");
                                        }
                                    });
                            }
                        });
                    }
                }, 0))
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

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                Attachment attachment = new Attachment();
                attachment.setContent("some content");
                attachment.setContentType("text/plain");

                PromptOptions options = new PromptOptions();
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                activity.setAttachments(new ArrayList<Attachment>() { { add(attachment); } });
                options.setPrompt(activity);
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
            })
            .send("hello")
            .assertReply(new Validators().validateHeroCard(
                new HeroCard() {
                    {
                        setText("favorite color?");
                        setButtons(new ArrayList<CardAction>() {
                            {
                                add(new CardAction() {
                                        {
                                            setType(ActionTypes.IM_BACK);
                                            setValue("red");
                                            setTitle("red");
                                        }
                                    });
                                    add(new CardAction() {
                                        {
                                            setType(ActionTypes.IM_BACK);
                                            setValue("green");
                                            setTitle("green");
                                        }
                                    });
                                    add(new CardAction() {
                                        {
                                            setType(ActionTypes.IM_BACK);
                                            setValue("blue");
                                            setTitle("blue");
                                        }
                                    });
                            }
                        });
                    }
                }, 1))
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
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                options.setPrompt(activity);
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
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
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
                Activity activity = new Activity(ActivityTypes.MESSAGE);
                activity.setText("favorite color?");
                options.setPrompt(activity);
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

    // @Test
    // public void ShouldNotRecognizeOtherText() {
    //     var convoState = new ConversationState(new MemoryStorage());
    //     var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //     var adapter = new TestAdapter()
    //         .Use(new AutoSaveStateMiddleware(convoState));

    //     var dialogs = new DialogSet(dialogState);
    //     var listPrompt = new ChoicePrompt("ChoicePrompt", defaultLocale: Culture.English) {
    //         Style = ListStyle.None,
    //     };
    //     dialogs.Add(listPrompt);

    //      new TestFlow(adapter,  (turnContext) -> {
    //             var dc =  dialogs.CreateContext(turnContext);

    //             var results =  dc.ContinueDialog(cancellationToken);
    //             if (results.Status == DialogTurnStatus.Empty) {
    //                  dc.Prompt(
    //                     "ChoicePrompt",
    //                     new PromptOptions {
    //                         Prompt = new Activity { Type = ActivityTypes.Message, Text = "favorite color?" },
    //                         RetryPrompt = new Activity { Type = ActivityTypes.Message, Text = "your favorite color, please?" },
    //                         Choices = _colorChoices,
    //                     },
    //                     cancellationToken);
    //             }
    //         })
    //         .send("hello")
    //         .assertReply(StartsWithValidator("favorite color?"))
    //         .send("what was that?")
    //         .assertReply("your favorite color, please?")
    //         .startTest();
    // }

    // @Test
    // public void ShouldCallCustomValidator() {
    //     var convoState = new ConversationState(new MemoryStorage());
    //     var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //     var adapter = new TestAdapter()
    //         .Use(new AutoSaveStateMiddleware(convoState));

    //     var dialogs = new DialogSet(dialogState);

    //     PromptValidator<FoundChoice> validator =  (promptContext) -> {
    //          promptContext.Context.SendActivity(MessageFactory.Text("validator called"));
    //         return true;
    //     };
    //     var listPrompt = new ChoicePrompt("ChoicePrompt", validator, Culture.English) {
    //         Style = ListStyle.None,
    //     };
    //     dialogs.Add(listPrompt);

    //      new TestFlow(adapter,  (turnContext) -> {
    //             var dc =  dialogs.CreateContext(turnContext);

    //             var results =  dc.ContinueDialog(cancellationToken);
    //             if (results.Status == DialogTurnStatus.Empty) {
    //                  dc.Prompt(
    //                     "ChoicePrompt",
    //                     new PromptOptions {
    //                         Prompt = new Activity { Type = ActivityTypes.Message, Text = "favorite color?" },
    //                         Choices = _colorChoices,
    //                     },
    //                     cancellationToken);
    //             }
    //         })
    //         .send("hello")
    //         .assertReply(StartsWithValidator("favorite color?"))
    //         .send("I'll take the red please.")
    //         .assertReply("validator called")
    //         .startTest();
    // }

    // @Test
    // public void ShouldUseChoiceStyleIfPresent() {
    //     var convoState = new ConversationState(new MemoryStorage());
    //     var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //     var adapter = new TestAdapter()
    //         .Use(new AutoSaveStateMiddleware(convoState));

    //     var dialogs = new DialogSet(dialogState);
    //     dialogs.Add(new ChoicePrompt("ChoicePrompt", defaultLocale: Culture.English) { Style = ListStyle.HeroCard });

    //      new TestFlow(adapter,  (turnContext) -> {
    //             var dc =  dialogs.CreateContext(turnContext);

    //             var results =  dc.ContinueDialog(cancellationToken);
    //             if (results.Status == DialogTurnStatus.Empty) {
    //                  dc.Prompt(
    //                     "ChoicePrompt",
    //                     new PromptOptions {
    //                         Prompt = new Activity { Type = ActivityTypes.Message, Text = "favorite color?" },
    //                         Choices = _colorChoices,
    //                         Style = ListStyle.SuggestedAction,
    //                     },
    //                     cancellationToken);
    //             }
    //         })
    //         .send("hello")
    //         .assertReply(SuggestedActionsValidator(
    //             "favorite color?",
    //             new SuggestedActions {
    //                 Actions new ArrayList<CardAction> {
    //                     new CardAction { Type = "imBack", Value = "red", Title = "red" },
    //                     new CardAction { Type = "imBack", Value = "green", Title = "green" },
    //                     new CardAction { Type = "imBack", Value = "blue", Title = "blue" },
    //                 },
    //             }))
    //         .startTest();
    // }

    // public void ShouldRecognizeLocaleVariationsOfCorrectLocales(String testCulture, String inlineOr, String inlineOrMore, String separator) {
    //     var convoState = new ConversationState(new MemoryStorage());
    //     var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //     var adapter = new TestAdapter()
    //         .Use(new AutoSaveStateMiddleware(convoState));

    //     // Create new DialogSet.
    //     var dialogs = new DialogSet(dialogState);
    //     dialogs.Add(new ChoicePrompt("ChoicePrompt", defaultLocale: testCulture));

    //     var helloLocale = MessageFactory.Text("hello");
    //     helloLocale.Locale = testCulture;

    //      new TestFlow(adapter,  (turnContext) -> {
    //         var dc =  dialogs.CreateContext(turnContext);

    //         var results =  dc.ContinueDialog(cancellationToken);
    //         if (results.Status == DialogTurnStatus.Empty) {
    //              dc.Prompt(
    //                 "ChoicePrompt",
    //                 new PromptOptions {
    //                     Prompt = new Activity { Type = ActivityTypes.Message, Text = "favorite color?", Locale = testCulture },
    //                     Choices = _colorChoices,
    //                 },
    //                 cancellationToken);
    //         }
    //     })
    //         .send(helloLocale)
    //         .assertReply((activity) -> {
    //             // Use ChoiceFactory to build the expected answer, manually
    //             var expectedChoices = ChoiceFactory.Inline(_colorChoices, null, null, new ChoiceFactoryOptions() {
    //                 InlineOr = inlineOr,
    //                 InlineOrMore = inlineOrMore,
    //                 InlineSeparator = separator,
    //             }).Text;
    //             Assert.Equal($"favorite color?{expectedChoices}", activity.AsMessageActivity().Text);
    //         })
    //         .startTest();
    // }

    // public void ShouldDefaultToEnglishLocale(String activityLocale) {
    //     var convoState = new ConversationState(new MemoryStorage());
    //     var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //     var adapter = new TestAdapter()
    //         .Use(new AutoSaveStateMiddleware(convoState));

    //     // Create new DialogSet.
    //     var dialogs = new DialogSet(dialogState);
    //     dialogs.Add(new ChoicePrompt("ChoicePrompt", defaultLocale: activityLocale));

    //     var helloLocale = MessageFactory.Text("hello");
    //     helloLocale.Locale = activityLocale;

    //      new TestFlow(adapter,  (turnContext) -> {
    //         var dc =  dialogs.CreateContext(turnContext);

    //         var results =  dc.ContinueDialog(cancellationToken);
    //         if (results.Status == DialogTurnStatus.Empty) {
    //              dc.Prompt(
    //                 "ChoicePrompt",
    //                 new PromptOptions {
    //                     Prompt = new Activity { Type = ActivityTypes.Message, Text = "favorite color?", Locale = activityLocale },
    //                     Choices = _colorChoices,
    //                 },
    //                 cancellationToken);
    //         }
    //     })
    //         .send(helloLocale)
    //         .assertReply((activity) -> {
    //             // Use ChoiceFactory to build the expected answer, manually
    //             var expectedChoices = ChoiceFactory.Inline(_colorChoices, null, null, new ChoiceFactoryOptions() {
    //                 InlineOr = English.InlineOr,
    //                 InlineOrMore = English.InlineOrMore,
    //                 InlineSeparator = English.Separator,
    //             }).Text;
    //             Assert.Equal($"favorite color?{expectedChoices}", activity.AsMessageActivity().Text);
    //         })
    //         .startTest();
    // }

    // @Test
    // public void ShouldAcceptAndRecognizeCustomLocaleDict() {
    //     var convoState = new ConversationState(new MemoryStorage());
    //     var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //     var adapter = new TestAdapter()
    //         .Use(new AutoSaveStateMiddleware(convoState));

    //     // Create new DialogSet.
    //     var dialogs = new DialogSet(dialogState);

    //     var culture = new PromptCultureModel() {
    //         InlineOr = " customOr ",
    //         InlineOrMore = " customOrMore ",
    //         Locale = "custom-custom",
    //         Separator = "customSeparator",
    //         NoInLanguage = "customNo",
    //         YesInLanguage = "customYes",
    //     };

    //     var customDict = new Dictionary<String, ChoiceFactoryOptions>() {
    //         { culture.Locale, new ChoiceFactoryOptions(culture.Separator, culture.InlineOr, culture.InlineOrMore, true) },
    //     };

    //     dialogs.Add(new ChoicePrompt("ChoicePrompt", customDict, null, culture.Locale));

    //     var helloLocale = MessageFactory.Text("hello");
    //     helloLocale.Locale = culture.Locale;

    //      new TestFlow(adapter,  (turnContext) -> {
    //         var dc =  dialogs.CreateContext(turnContext);

    //         var results =  dc.ContinueDialog(cancellationToken);
    //         if (results.Status == DialogTurnStatus.Empty) {
    //              dc.Prompt(
    //                 "ChoicePrompt",
    //                 new PromptOptions {
    //                     Prompt = new Activity { Type = ActivityTypes.Message, Text = "favorite color?", Locale = culture.Locale },
    //                     Choices = _colorChoices,
    //                 },
    //                 cancellationToken);
    //         }
    //     })
    //         .send(helloLocale)
    //         .assertReply((activity) -> {
    //             // Use ChoiceFactory to build the expected answer, manually
    //             var expectedChoices = ChoiceFactory.Inline(_colorChoices, null, null, new ChoiceFactoryOptions() {
    //                 InlineOr = culture.InlineOr,
    //                 InlineOrMore = culture.InlineOrMore,
    //                 InlineSeparator = culture.Separator,
    //             }).Text;
    //             Assert.Equal($"favorite color?{expectedChoices}", activity.AsMessageActivity().Text);
    //         })
    //         .startTest();
    // }

    /*
     * @Test public void ShouldHandleAnUndefinedRequest() { var convoState = new
     * ConversationState(new MemoryStorage()); var testProperty =
     * convoState.CreateProperty<Dictionary<String, Object>>("test");
     *
     * var adapter = new TestAdapter() .Use(convoState);
     *
     * PromptValidator<FoundChoice> validator = (context, promptContext) -> {
     * Assert.IsTrue(false); return CompletableFuture.completedFuture(null); };
     *
     * new TestFlow(adapter, (turnContext) -> { var state =
     * testProperty.Get(turnContext, () -> new Dictionary<String, Object>()); var
     * prompt = new ChoicePrompt(Culture.English, validator); prompt.Style =
     * ListStyle.None;
     *
     * var dialogCompletion = prompt.ContinueDialog(turnContext, state); if
     * (!dialogCompletion.IsActive && !dialogCompletion.IsCompleted) {
     * prompt.Begin(turnContext, state, new ChoicePromptOptions { PromptString =
     * "favorite color?", Choices = ChoiceFactory.ToChoices(colorChoices) }); } else
     * if (dialogCompletion.IsActive && !dialogCompletion.IsCompleted) { if
     * (dialogCompletion.Result == null) {
     * turnContext.SendActivity("NotRecognized"); } } }) .send("hello")
     * .assertReply(StartsWithValidator("favorite color?"))
     * .send("value shouldn't have been recognized.") .assertReply("NotRecognized")
     * .startTest(); }
     */

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

    // private Action<Activity> validateSpeak(String expectedText, String expectedSpeak) {
    //     return activity -> {
    //         Assert.IsAssignableFrom<MessageActivity>(activity);
    //         var msg = (MessageActivity)activity;
    //         Assert.Equal(expectedText, msg.Text);
    //         Assert.Equal(expectedSpeak, msg.Speak);
    //     };
    // }
}
