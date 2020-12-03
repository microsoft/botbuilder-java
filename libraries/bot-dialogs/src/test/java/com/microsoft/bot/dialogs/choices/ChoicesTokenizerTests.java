// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ChoicesTokenizerTests {
    @Test
    public void shouldBreakOnSpaces() {
        List<Token> tokens = new Tokenizer().tokenize("how now brown cow", null);
        Assert.assertEquals(4, tokens.size());
        assertToken(tokens.get(0), 0, 2, "how");
        assertToken(tokens.get(1), 4, 6, "now");
        assertToken(tokens.get(2), 8, 12, "brown");
        assertToken(tokens.get(3), 14, 16, "cow");
    }

    @Test
    public void shouldBreakOnPunctuation() {
        List<Token> tokens = new Tokenizer().tokenize("how-now.brown:cow ?", null);
        Assert.assertEquals(4, tokens.size());
        assertToken(tokens.get(0), 0, 2, "how");
        assertToken(tokens.get(1), 4, 6, "now");
        assertToken(tokens.get(2), 8, 12, "brown");
        assertToken(tokens.get(3), 14, 16, "cow");
    }

    @Test
    public void shouldTokenizeSingleCharacterTokens()
    {
        List<Token> tokens = new Tokenizer().tokenize("a b c d", null);
        Assert.assertEquals(4, tokens.size());
        assertToken(tokens.get(0), 0, 0, "a");
        assertToken(tokens.get(1), 2, 2, "b");
        assertToken(tokens.get(2), 4, 4, "c");
        assertToken(tokens.get(3), 6, 6, "d");
    }

    @Test
    public void shouldReturnASingleToken() {
        List<Token> tokens = new Tokenizer().tokenize("food", null);
        Assert.assertEquals(1, tokens.size());
        assertToken(tokens.get(0), 0, 3, "food");
    }

    @Test
    public void shouldReturnNoTokens() {
        List<Token> tokens = new Tokenizer().tokenize(".?; -()", null);
        Assert.assertEquals(0, tokens.size());
    }

    @Test
    public void shouldReturnTheNormalizedAndOriginalTextForAToken() {
        List<Token> tokens = new Tokenizer().tokenize("fOoD", null);
        Assert.assertEquals(1, tokens.size());
        assertToken(tokens.get(0), 0, 3, "fOoD", "food");
    }

    @Test
    public void shouldBreakOnEmojis() {
        List<Token> tokens = new Tokenizer().tokenize("food \uD83D\uDCA5\uD83D\uDC4D\uD83D\uDE00", null);
        Assert.assertEquals(4, tokens.size());
        assertToken(tokens.get(0), 0, 3, "food");
        assertToken(tokens.get(1), 5, 6, "\uD83D\uDCA5");
        assertToken(tokens.get(2), 7, 8, "\uD83D\uDC4D");
        assertToken(tokens.get(3), 9, 10, "\uD83D\uDE00");
    }

    private static void assertToken(Token token, int start, int end, String text) {
        assertToken(token, start, end, text, null);
    }

    private static void assertToken(Token token, int start, int end, String text, String normalized) {
        Assert.assertEquals(start, token.getStart());
        Assert.assertEquals(end, token.getEnd());
        Assert.assertEquals(text, token.getText());
        Assert.assertEquals(normalized == null ? text : normalized, token.getNormalized());
    }
}
