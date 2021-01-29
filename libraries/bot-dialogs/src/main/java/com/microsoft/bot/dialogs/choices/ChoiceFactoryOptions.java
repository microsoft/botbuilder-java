// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * Contains formatting options for presenting a list of choices.
 */
public class ChoiceFactoryOptions {
    public static final String DEFAULT_INLINE_SEPERATOR = ", ";
    public static final String DEFAULT_INLINE_OR = " or ";
    public static final String DEFAULT_INLINE_OR_MORE = ", or ";
    public static final boolean DEFAULT_INCLUDE_NUMBERS = true;

    /**
     * Creates default options.
     */
    public ChoiceFactoryOptions() {
        this(DEFAULT_INLINE_SEPERATOR, DEFAULT_INLINE_OR, DEFAULT_INLINE_OR_MORE);
    }

    /**
     * Clones another options object, and applies defaults if needed.
     *
     * @param options The options object to clone.
     */
    public ChoiceFactoryOptions(ChoiceFactoryOptions options) {
        this();

        if (options != null) {
            if (!StringUtils.isEmpty(options.getInlineSeparator())) {
                setInlineSeparator(options.getInlineSeparator());
            }

            if (!StringUtils.isEmpty(options.getInlineOr())) {
                setInlineOr(options.getInlineOr());
            }

            if (!StringUtils.isEmpty(options.getInlineOrMore())) {
                setInlineOrMore(options.getInlineOrMore());
            }

            setIncludeNumbers(options.getIncludeNumbers());
        }
    }

    /**
     * Creates options with the specified formatting values.
     * @param withInlineSeparator The inline seperator value.
     * @param withInlineOr The inline or value.
     * @param withInlineOrMore The inline or more value.
     */
    public ChoiceFactoryOptions(
        String withInlineSeparator,
        String withInlineOr,
        String withInlineOrMore
    ) {
        this(withInlineSeparator, withInlineOr, withInlineOrMore, DEFAULT_INCLUDE_NUMBERS);
    }

    /**
     * Initializes a new instance of the class.
     * Refer to the code in teh ConfirmPrompt for an example of usage.
     * @param withInlineSeparator The inline seperator value.
     * @param withInlineOr The inline or value.
     * @param withInlineOrMore The inline or more value.
     * @param withIncludeNumbers Flag indicating whether to include numbers as a choice.
     */
    public ChoiceFactoryOptions(
        String withInlineSeparator,
        String withInlineOr,
        String withInlineOrMore,
        boolean withIncludeNumbers
    ) {
        inlineSeparator = withInlineSeparator;
        inlineOr = withInlineOr;
        inlineOrMore = withInlineOrMore;
        includeNumbers = withIncludeNumbers;
    }

    @JsonProperty(value = "inlineSeparator")
    private String inlineSeparator;

    @JsonProperty(value = "inlineOr")
    private String inlineOr;

    @JsonProperty(value = "inlineOrMore")
    private String inlineOrMore;

    @JsonProperty(value = "includeNumbers")
    private Boolean includeNumbers;

    /**
     * Gets the character used to separate individual choices when there are more than 2 choices.
     * The default value is `", "`. This is optional.
     *
     * @return The seperator.
     */
    public String getInlineSeparator() {
        return inlineSeparator;
    }

    /**
     * Sets the character used to separate individual choices when there are more than 2 choices.
     * @param withSeperator The seperator.
     */
    public void setInlineSeparator(String withSeperator) {
        inlineSeparator = withSeperator;
    }

    /**
     * Gets the separator inserted between the choices when their are only 2 choices. The default
     * value is `" or "`. This is optional.
     *
     * @return The separator inserted between the choices when their are only 2 choices.
     */
    public String getInlineOr() {
        return inlineOr;
    }

    /**
     * Sets the separator inserted between the choices when their are only 2 choices.
     *
     * @param withInlineOr The separator inserted between the choices when their are only 2 choices.
     */
    public void setInlineOr(String withInlineOr) {
        this.inlineOr = withInlineOr;
    }

    /**
     * Gets the separator inserted between the last 2 choices when their are more than 2 choices.
     * The default value is `", or "`. This is optional.
     *
     * @return The separator inserted between the last 2 choices when their are more than 2 choices.
     */
    public String getInlineOrMore() {
        return inlineOrMore;
    }

    /**
     * Sets the separator inserted between the last 2 choices when their are more than 2 choices.
     *
     * @param withInlineOrMore The separator inserted between the last 2 choices when their
     *                         are more than 2 choices.
     */
    public void setInlineOrMore(String withInlineOrMore) {
        this.inlineOrMore = withInlineOrMore;
    }

    /**
     * Gets  a value indicating whether an inline and list style choices will be prefixed
     * with the index of the choice; as in "1. choice". If false, the list style will use a
     * bulleted list instead. The default value is true.
     *
     * @return If false, the list style will use a bulleted list.
     */
    public Boolean getIncludeNumbers() {
        return includeNumbers;
    }

    /**
     * Sets the value indicating whether an inline and list style choices will be prefixed
     * with the index of the choice.
     *
     * @param withIncludeNumbers If false, the list style will use a bulleted list instead.
     */
    public void setIncludeNumbers(Boolean withIncludeNumbers) {
        this.includeNumbers = withIncludeNumbers;
    }
}
