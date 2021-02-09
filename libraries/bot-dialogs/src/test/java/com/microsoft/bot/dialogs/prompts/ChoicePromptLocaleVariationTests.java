// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.microsoft.bot.dialogs.TestLocale;
import com.microsoft.bot.dialogs.choices.Choice;
import com.microsoft.bot.dialogs.choices.ChoiceFactory;
import com.microsoft.bot.dialogs.choices.ChoiceFactoryOptions;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ChoicePromptLocaleVariationTests {

    String testCulture;
    String inlineOr;
    String inlineOrMore;
    String separator;

    public ChoicePromptLocaleVariationTests(String testCulture, String inlineOr, String inlineOrMore, String separator) {
        this.testCulture = testCulture;
        this.inlineOr = inlineOr;
        this.inlineOrMore = inlineOrMore;
        this.separator  = separator;
    }
    public static List<Object[]> getLocaleVariationTest() {
        TestLocale[] testLocales = new TestLocale[2];
        testLocales[0] = new TestLocale(PromptCultureModels.ENGLISH, null, null, null);
        testLocales[1] = new TestLocale(PromptCultureModels.SPANISH, null, null, null);
        // testLocales[2] = new TestLocale(PromptCultureModels.DUTCH, null, null, null);
        // testLocales[3] = new TestLocale(PromptCultureModels.BULGARIAN, null, null, null);
        // testLocales[4] = new TestLocale(PromptCultureModels.FRENCH, null, null, null);
        // testLocales[5] = new TestLocale(PromptCultureModels.HINDI, null, null, null);
        // testLocales[6] = new TestLocale(PromptCultureModels.ITALIAN, null, null, null);
        // testLocales[7] = new TestLocale(PromptCultureModels.JAPANESE, null, null, null);
        // testLocales[8] = new TestLocale(PromptCultureModels.KOREAN, null, null, null);
        // testLocales[9] = new TestLocale(PromptCultureModels.PORTUGUESE, null, null, null);
        // testLocales[10] = new TestLocale(PromptCultureModels.CHINESE, null, null, null);
        // testLocales[11] = new TestLocale(PromptCultureModels.SWEDISH, null, null, null);
        // testLocales[12] = new TestLocale(PromptCultureModels.TURKISH, null, null, null);

        List<Object[]> resultList = new ArrayList<Object[]>();
        for (TestLocale testLocale : testLocales) {
            resultList.add(new Object[] {testLocale.getValidLocale(), testLocale.getInlineOr(),
                    testLocale.getInlineOrMore(), testLocale.getSeparator() });
            resultList.add(new Object[] {testLocale.getCapEnding(), testLocale.getInlineOr(),
                    testLocale.getInlineOrMore(), testLocale.getSeparator() });
            resultList.add(new Object[] {testLocale.getTitleEnding(), testLocale.getInlineOr(),
                    testLocale.getInlineOrMore(), testLocale.getSeparator() });
            resultList.add(new Object[] {testLocale.getCapTwoLetter(), testLocale.getInlineOr(),
                    testLocale.getInlineOrMore(), testLocale.getSeparator() });
            resultList.add(new Object[] {testLocale.getLowerTwoLetter(), testLocale.getInlineOr(),
                    testLocale.getInlineOrMore(), testLocale.getSeparator() });
            }

        return resultList;
    }


    @Parameterized.Parameters
    public static List<Object[]> data() {
        return getLocaleVariationTest();

    }

    private static List<Choice> colorChoices = Arrays.asList(new Choice("red"), new Choice("green"),
            new Choice("blue"));

    @Test
    public void testShouldRecognizeLocaleVariationsOfCorrectLocales() {
        Assert.assertEquals(1, 1);
        System.out.println("Testing: " + testCulture);
        ShouldRecognizeLocaleVariationsOfCorrectLocales(this.testCulture, this.inlineOr,
                                                        this.inlineOrMore, this.separator);
    }

    public void ShouldRecognizeLocaleVariationsOfCorrectLocales(String testCulture, String inlineOr,
            String inlineOrMore, String separator) {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        // Create and add custom activity prompt to DialogSet.
        ChoicePrompt listPrompt = new ChoicePrompt("ChoicePrompt", null, testCulture);

        dialogs.add(listPrompt);

        Activity helloLocale = MessageFactory.text("hello");
        helloLocale.setLocale(testCulture);

        new TestFlow(adapter, (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();
            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("favorite color?"));
                options.setChoices(colorChoices);
                dc.prompt("ChoicePrompt", options).join();
            }
            return CompletableFuture.completedFuture(null);
        }).send(helloLocale).assertReply((activity) -> {
            // Use ChoiceFactory to build the expected answer, manually
            ChoiceFactoryOptions testChoiceOption = new ChoiceFactoryOptions();
            testChoiceOption.setInlineOr(inlineOr);
            testChoiceOption.setInlineOrMore(inlineOrMore);
            testChoiceOption.setInlineSeparator(separator);

            String expectedChoices = ChoiceFactory.inline(colorChoices, null, null, testChoiceOption).getText();
            Assert.assertEquals(String.format("favorite color?%s", expectedChoices), activity.getText());
        }).startTest().join();
    }

}
