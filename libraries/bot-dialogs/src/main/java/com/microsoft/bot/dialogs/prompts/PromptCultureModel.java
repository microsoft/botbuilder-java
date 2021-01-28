// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

/**
 * Culture model used in Choice and Confirm Prompts.
 */
public class PromptCultureModel {

    private String locale;
    private String separator;
    private String inlineOr;
    private String inlineOrMore;
    private String yesInLanguage;
    private String noInLanguage;

    /**
     * Creates a PromptCultureModel.
     */
    public PromptCultureModel() {

    }

    /**
     * Gets Culture Model's Locale.
     *
     * @return Ex: Locale. Example: "en-US".
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Sets Culture Model's Locale.
     *
     * @param locale Ex: Locale. Example: "en-US".
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * GetsCulture Model's Inline Separator.
     *
     * @return Example: ", ".
     */
    public String getSeparator() {
        return this.separator;
    }

    /**
     * Sets Culture Model's Inline Separator.
     *
     * @param separator Example: ", ".
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * Gets Culture Model's InlineOr.
     *
     * @return Example: " or ".
     */
    public String getInlineOr() {
        return this.inlineOr;
    }

    /**
     * Sets Culture Model's InlineOr.
     *
     * @param inlineOr Example: " or ".
     */
    public void setInlineOr(String inlineOr) {
        this.inlineOr = inlineOr;
    }

    /**
     * Gets Culture Model's InlineOrMore.
     *
     * @return Example: ", or ".
     */
    public String getInlineOrMore() {
        return this.inlineOrMore;
    }

    /**
     * Sets Culture Model's InlineOrMore.
     *
     * @param inlineOrMore Example: ", or ".
     */
    public void setInlineOrMore(String inlineOrMore) {
        this.inlineOrMore = inlineOrMore;
    }

    /**
     * Gets Equivalent of "Yes" in Culture Model's Language.
     *
     * @return Example: "Yes".
     */
    public String getYesInLanguage() {
        return this.yesInLanguage;
    }

    /**
     * Sets Equivalent of "Yes" in Culture Model's Language.
     *
     * @param yesInLanguage Example: "Yes".
     */
    public void setYesInLanguage(String yesInLanguage) {
        this.yesInLanguage = yesInLanguage;
    }

    /**
     * Gets Equivalent of "No" in Culture Model's Language.
     *
     * @return Example: "No".
     */
    public String getNoInLanguage() {
        return this.noInLanguage;
    }

    /**
     * Sets Equivalent of "No" in Culture Model's Language.
     *
     * @param noInLanguage Example: "No".
     */
    public void setNoInLanguage(String noInLanguage) {
        this.noInLanguage = noInLanguage;
    }

}
