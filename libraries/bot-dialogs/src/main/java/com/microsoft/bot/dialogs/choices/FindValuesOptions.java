// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Locale;

/**
 * Contains options used to control how choices are recognized in a users utterance.
 */
public class FindValuesOptions {
    @JsonProperty(value = "allowPartialMatches")
    private boolean allowPartialMatches;

    @JsonProperty(value = "locale")
    private String locale = Locale.ENGLISH.getDisplayName();

    @JsonProperty(value = "maxTokenDistance")
    private int maxTokenDistance = 2;

    @JsonProperty(value = "tokenizer")
    private TokenizerFunction tokenizer;

    /**
     * Gets value indicating whether only some of the tokens in a value need to exist to be
     * considered.
     * @return true if only some of the tokens in a value need to exist to be considered;
     * otherwise false.
     */
    public boolean getAllowPartialMatches() {
        return allowPartialMatches;
    }

    /**
     * Sets value indicating whether only some of the tokens in a value need to exist to be
     * considered.
     * @param withAllowPartialMatches true if only some of the tokens in a value need to exist
     *                                to be considered; otherwise false.
     */
    public void setAllowPartialMatches(boolean withAllowPartialMatches) {
        allowPartialMatches = withAllowPartialMatches;
    }

    /**
     * Gets the locale/culture code of the utterance. The default is `en-US`. This is optional.
     * @return The locale/culture code of the utterance.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the locale/culture code of the utterance. The default is `en-US`. This is optional.
     * @param withLocale The locale/culture code of the utterance.
     */
    public void setLocale(String withLocale) {
        locale = withLocale;
    }

    /**
     * Gets the maximum tokens allowed between two matched tokens in the utterance. So with
     * a max distance of 2 the value "second last" would match the utterance "second from the last"
     * but it wouldn't match "Wait a second. That's not the last one is it?".
     * The default value is "2".
     * @return The maximum tokens allowed between two matched tokens in the utterance.
     */
    public int getMaxTokenDistance() {
        return maxTokenDistance;
    }

    /**
     * Gets the maximum tokens allowed between two matched tokens in the utterance. So with
     * a max distance of 2 the value "second last" would match the utterance "second from the last"
     * but it wouldn't match "Wait a second. That's not the last one is it?".
     * The default value is "2".
     * @param withMaxTokenDistance The maximum tokens allowed between two matched tokens in the
     *                             utterance.
     */
    public void setMaxTokenDistance(int withMaxTokenDistance) {
        maxTokenDistance = withMaxTokenDistance;
    }

    /**
     * Gets the tokenizer to use when parsing the utterance and values being recognized.
     * @return The tokenizer to use when parsing the utterance and values being recognized.
     */
    public TokenizerFunction getTokenizer() {
        return tokenizer;
    }

    /**
     * Sets the tokenizer to use when parsing the utterance and values being recognized.
     * @param withTokenizer The tokenizer to use when parsing the utterance and values being
     *                      recognized.
     */
    public void setTokenizer(TokenizerFunction withTokenizer) {
        tokenizer = withTokenizer;
    }
}
