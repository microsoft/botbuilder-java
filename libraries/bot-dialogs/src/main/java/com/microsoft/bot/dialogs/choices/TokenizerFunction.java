// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import java.util.List;

/**
 * Represents a callback method that can break a string into its component tokens.
 */
@FunctionalInterface
public interface TokenizerFunction {

    /**
     * The callback method that can break a string into its component tokens.
     * @param text The input text.
     * @param locale Optional, identifies the locale of the input text.
     * @return The list of the found Token objects.
     */
    List<Token> tokenize(String text, String locale);
}
