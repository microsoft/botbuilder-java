// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.microsoft.bot.dialogs.prompts.PromptCultureModel;

public class TestLocale {

    private String validLocale;

    private String capEnding;

    private String titleEnding;

    private String capTwoLetter;

    private String lowerTwoLetter;

    private String expectedPrompt;

    private String inputThatResultsInOne;

    private String inputThatResultsInZero;

    private PromptCultureModel culture;

    public TestLocale(
        PromptCultureModel cultureModel,
        String expectedPrompt,
        String inputThatResultsInOne,
        String inputThatResultsInZero) {
        if (cultureModel.getLocale().length() != 5) {
            throw new IllegalArgumentException("validLocale must be in format: es-es");
        }

        this.culture = cultureModel;
        this.validLocale = cultureModel.getLocale().toString().toLowerCase();
        this.expectedPrompt = expectedPrompt;
        this.inputThatResultsInOne = inputThatResultsInOne;
        this.inputThatResultsInZero = inputThatResultsInZero;

        // es-ES
        capEnding = getCapEnding(validLocale);

        // es-Es
        titleEnding = getTitleEnding(validLocale);

        // ES
        capTwoLetter = getCapTwoLetter(validLocale);

        // es
        lowerTwoLetter = getLowerTwoLetter(validLocale);
    }

    public String getSeparator() {
        if (culture != null) {
            return culture.getSeparator();
        } else {
            return "";
        }
    }

    public String getInlineOr() {
        if (culture != null) {
            return culture.getInlineOr();
        } else {
            return "";
        }
    }

    public String getInlineOrMore() {
        if (culture != null) {
            return culture.getInlineOrMore();
        } else {
            return "";
        }
    }


    private String getCapEnding(String locale) {
        return String.format("%s%s-%s%s",
                        locale.substring(0, 1),
                        locale.substring(1, 2),
                        locale.substring(3, 4).toUpperCase(),
                        locale.substring(4, 5).toUpperCase());
    }

    private String getTitleEnding(String locale) {
        return String.format("%s%s-%s%s",
        locale.substring(0, 1),
        locale.substring(1, 2),
        locale.substring(3, 4).toUpperCase(),
        locale.substring(4, 5));
    }

    private String getCapTwoLetter(String locale) {
        return String.format("%s%s",
        locale.substring(0, 1).toUpperCase(),
        locale.substring(1, 2).toUpperCase());
    }

    private String getLowerTwoLetter(String locale) {
        return String.format("%s%s",
        locale.substring(0, 1),
        locale.substring(1, 2));
    }

    /**
     * @return the ValidLocale value as a String.
     */
    public String getValidLocale() {
        return this.validLocale;
    }

    /**
     * @return the CapEnding value as a String.
     */
    public String getCapEnding() {
        return this.capEnding;
    }

    /**
     * @return the TitleEnding value as a String.
     */
    public String getTitleEnding() {
        return this.titleEnding;
    }

    /**
     * @return the CapTwoLetter value as a String.
     */
    public String getCapTwoLetter() {
        return this.capTwoLetter;
    }

    /**
     * @return the LowerTwoLetter value as a String.
     */
    public String getLowerTwoLetter() {
        return this.lowerTwoLetter;
    }

    /**
     * @return the ExpectedPrompt value as a String.
     */
    public String getExpectedPrompt() {
        return this.expectedPrompt;
    }

    /**
     * @return the InputThatResultsInOne value as a String.
     */
    public String getInputThatResultsInOne() {
        return this.inputThatResultsInOne;
    }

    /**
     * @return the InputThatResultsInZero value as a String.
     */
    public String getInputThatResultsInZero() {
        return this.inputThatResultsInZero;
    }

    /**
     * @return the Culture value as a PromptCultureModel.
     */
    public PromptCultureModel getCulture() {
        return this.culture;
    }
}
