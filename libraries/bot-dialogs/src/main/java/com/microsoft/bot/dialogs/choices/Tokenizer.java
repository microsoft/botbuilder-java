// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the default Tokenizer implementation.
 */
public class Tokenizer implements TokenizerFunction {

    /**
     * Simple tokenizer that breaks on spaces and punctuation. The only normalization
     * done is to lowercase.
     * @param text The input text.
     * @param locale Optional, identifies the locale of the input text.
     * @return The list of the found Token objects.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    @Override
    public List<Token> tokenize(String text, String locale) {
        List<Token> tokens = new ArrayList<>();
        Token token = null;

        int length = text == null ? 0 : text.length();
        int i = 0;

        while (i < length) {
            int codePoint = text.codePointAt(i);

            String chr = new String(Character.toChars(codePoint));

            if (isBreakingChar(codePoint)) {
                appendToken(tokens, token, i - 1);
                token = null;
            } else if (codePoint > 0xFFFF) {
                appendToken(tokens, token, i - 1);
                token = null;

                Token t = new Token();
                t.setStart(i);
                t.setEnd(i + chr.length() - 1);
                t.setText(chr);
                t.setNormalized(chr);

                tokens.add(t);
            } else if (token == null) {
                token = new Token();
                token.setStart(i);
                token.setText(chr);
            } else {
                token.appendText(chr);
            }

            i += chr.length();
        }

        appendToken(tokens, token, length - 1);
        return tokens;
    }

    private void appendToken(List<Token> tokens, Token token, int end) {
        if (token != null) {
            token.setEnd(end);
            token.setNormalized(token.getText().toLowerCase());
            tokens.add(token);
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static boolean isBreakingChar(int codePoint) {
        return isBetween(codePoint, 0x0000, 0x002F)
            || isBetween(codePoint, 0x003A, 0x0040)
            || isBetween(codePoint, 0x005B, 0x0060)
            || isBetween(codePoint, 0x007B, 0x00BF)
            || isBetween(codePoint, 0x02B9, 0x036F)
            || isBetween(codePoint, 0x2000, 0x2BFF)
            || isBetween(codePoint, 0x2E00, 0x2E7F);
    }

    private static boolean isBetween(int value, int from, int to) {
        return value >= from && value <= to;
    }
}
