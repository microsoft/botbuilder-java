// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
import com.microsoft.bot.dialogs.choices.ChoiceFactoryOptions;
import com.microsoft.bot.dialogs.choices.ListStyle;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.recognizers.text.Culture;

import org.javatuples.Triplet;
import org.junit.Test;

public class ConfirmPromptLocTests {

    @Test
    public void ConfirmPrompt_Activity_Locale_Default() {
        ConfirmPrompt_Locale_Impl(null, Culture.English, "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl(null, Culture.English, "(1) Yes or (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl(null, Culture.Spanish, "(1) Sí o (2) No", "Sí", "1");
        ConfirmPrompt_Locale_Impl(null, Culture.Spanish, "(1) Sí o (2) No", "No", "0");
    }

    @Test
    public void ConfirmPrompt_Activity_Locale_Illegal_Default() {
        ConfirmPrompt_Locale_Impl(null, null, "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl(null, "", "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl(null, "not-supported", "(1) Yes or (2) No", "Yes", "1");
    }

    @Test
    public void ConfirmPrompt_Activity_Locale_Default_Number() {
        ConfirmPrompt_Locale_Impl(null, Culture.English, "(1) Yes or (2) No", "1", "1");
        ConfirmPrompt_Locale_Impl(null, Culture.English, "(1) Yes or (2) No", "2", "0");
        ConfirmPrompt_Locale_Impl(null, Culture.Spanish, "(1) Sí o (2) No", "1", "1");
        ConfirmPrompt_Locale_Impl(null, Culture.Spanish, "(1) Sí o (2) No", "2", "0");
    }

    @Test
    public void ConfirmPrompt_Activity_Locale_Illegal_Default_Number() {
        ConfirmPrompt_Locale_Impl(null, null, "(1) Yes or (2) No", "1", "1");
        ConfirmPrompt_Locale_Impl(null, "", "(1) Yes or (2) No", "1", "1");
        ConfirmPrompt_Locale_Impl(null, "not-supported", "(1) Yes or (2) No", "1", "1");
    }

    @Test
    public void ConfirmPrompt_Activity_Locale_Activity() {
        ConfirmPrompt_Locale_Impl(Culture.English, null, "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl(Culture.English, null, "(1) Yes or (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl(Culture.Spanish, null, "(1) Sí o (2) No", "Sí", "1");
        ConfirmPrompt_Locale_Impl(Culture.Spanish, null, "(1) Sí o (2) No", "No", "0");
    }

    @Test
    public void ConfirmPrompt_Activity_Locale_Illegal_Activity() {
        ConfirmPrompt_Locale_Impl(null, null, "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl("", null, "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl("not-supported", null, "(1) Yes or (2) No", "Yes", "1");
    }

    @Test
    public void ConfirmPrompt_Locale_Variations_English() {
        ConfirmPrompt_Locale_Impl("en-us", "en-us", "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl("en-us", "en-us", "(1) Yes or (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("en-US", "en-US", "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl("en-US", "en-US", "(1) Yes or (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("en-Us", "en-Us", "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl("en-Us", "en-Us", "(1) Yes or (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("EN", "EN", "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl("EN", "EN", "(1) Yes or (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("en", "en", "(1) Yes or (2) No", "Yes", "1");
        ConfirmPrompt_Locale_Impl("en", "en", "(1) Yes or (2) No", "No", "0");
    }

    @Test
    public void ConfirmPrompt_Locale_Variations_Spanish() {
        ConfirmPrompt_Locale_Impl("es-es", "es-es", "(1) Sí o (2) No", "Sí", "1");
        ConfirmPrompt_Locale_Impl("es-es", "es-es", "(1) Sí o (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("es-ES", "es-ES", "(1) Sí o (2) No", "Sí", "1");
        ConfirmPrompt_Locale_Impl("es-ES", "es-ES", "(1) Sí o (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("es-Es", "es-Es", "(1) Sí o (2) No", "Sí", "1");
        ConfirmPrompt_Locale_Impl("es-Es", "es-Es", "(1) Sí o (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("ES", "ES", "(1) Sí o (2) No", "Sí", "1");
        ConfirmPrompt_Locale_Impl("ES", "ES", "(1) Sí o (2) No", "No", "0");
        ConfirmPrompt_Locale_Impl("es", "es", "(1) Sí o (2) No", "Sí", "1");
        ConfirmPrompt_Locale_Impl("es", "es", "(1) Sí o (2) No", "No", "0");
    }

    @Test
    public void ConfirmPrompt_Locale_Override_ChoiceDefaults() {
        ConfirmPrompt_Locale_Override_ChoiceDefaults("custom-custom", "(1) customYes customOr (2) customNo",
                "customYes", "1");
        ConfirmPrompt_Locale_Override_ChoiceDefaults("custom-custom", "(1) customYes customOr (2) customNo", "customNo",
                "0");
    }

    public void ConfirmPrompt_Locale_Override_ChoiceDefaults(String defaultLocale, String prompt, String utterance,
            String expectedResponse) {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, Culture.English);
        listPrompt.setStyle(ListStyle.NONE);

        PromptCultureModel culture = new PromptCultureModel();
        culture.setInlineOr(" customOr ");
        culture.setInlineOrMore(" customOrMore ");
        culture.setLocale("custom-custom");
        culture.setSeparator("customSeparator");
        culture.setNoInLanguage("customNo");
        culture.setYesInLanguage("customYes");

        Map<String, Triplet<Choice, Choice, ChoiceFactoryOptions>> customDict = new HashMap<String, Triplet<Choice, Choice, ChoiceFactoryOptions>>();
        customDict.put(culture.getLocale(),
                new Triplet<Choice, Choice, ChoiceFactoryOptions>(new Choice(culture.getYesInLanguage()),
                        new Choice(culture.getNoInLanguage()), new ChoiceFactoryOptions(culture.getSeparator(),
                                culture.getInlineOr(), culture.getInlineOrMore(), true)));
        // Prompt should default to English if locale is a non-supported value
        dialogs.add(new ConfirmPrompt("ConfirmPrompt", customDict, null, defaultLocale));
        new TestFlow(adapter, (turnContext) -> {
            turnContext.getActivity().setLocale(culture.getLocale());
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Prompt."));
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("1")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("0")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        }).send("hello").assertReply("Prompt. " + prompt).send(utterance).assertReply(expectedResponse).startTest()
                .join();
    }

    private void ConfirmPrompt_Locale_Impl(String activityLocale, String defaultLocale, String prompt, String utterance,
            String expectedResponse) {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference("ConfirmPrompt_Locale_Impl", "testuser", "testbot"))
                        .use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Prompt should default to English if locale is a non-supported value
        dialogs.add(new ConfirmPrompt("ConfirmPrompt", null, defaultLocale));

        new TestFlow(adapter, (turnContext) -> {
            turnContext.getActivity().setLocale(activityLocale);

            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("Prompt."));
                dc.prompt("ConfirmPrompt", options).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if ((Boolean) results.getResult()) {
                    turnContext.sendActivity(MessageFactory.text("1")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("0")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        }).send("hello").assertReply("Prompt. " + prompt).send(utterance).assertReply(expectedResponse).startTest().join();
    }
}
