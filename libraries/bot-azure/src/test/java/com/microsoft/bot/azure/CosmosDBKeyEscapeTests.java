// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure;

import org.junit.Assert;
import org.junit.Test;

public class CosmosDBKeyEscapeTests {
    @Test(expected = IllegalArgumentException.class)
    public void Sanitize_Key_Should_Fail_With_Null_Key() {
        // Null key should throw
        CosmosDbKeyEscape.escapeKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Sanitize_Key_Should_Fail_With_Empty_Key() {
        // Empty string should throw
        CosmosDbKeyEscape.escapeKey(new String());
    }

    @Test(expected = IllegalArgumentException.class)
    public void Sanitize_Key_Should_Fail_With_Whitespace_Key() {
        // Whitespace key should throw
        CosmosDbKeyEscape.escapeKey("    ");
    }

    @Test
    public void Sanitize_Key_Should_Not_Change_A_Valid_Key() {
        String validKey = "Abc12345";
        String sanitizedKey = CosmosDbKeyEscape.escapeKey(validKey);
        Assert.assertEquals(validKey, sanitizedKey);
    }

    @Test
    public void Long_Key_Should_Be_Truncated() {
        StringBuilder tooLongKey = new StringBuilder();
        for (int i = 0; i < CosmosDbKeyEscape.MAX_KEY_LENGTH + 1; i++) {
            tooLongKey.append("a");
        }

        String sanitizedKey = CosmosDbKeyEscape.escapeKey(tooLongKey.toString());
        Assert.assertTrue(sanitizedKey.length() <= CosmosDbKeyEscape.MAX_KEY_LENGTH);

        // The resulting key should be:
        String hash = String.format("%x", tooLongKey.toString().hashCode());
        String correctKey = sanitizedKey.substring(0, CosmosDbKeyEscape.MAX_KEY_LENGTH - hash.length()) + hash;

        Assert.assertEquals(correctKey, sanitizedKey);
    }

    @Test
    public void Long_Key_With_Illegal_Characters_Should_Be_Truncated() {
        StringBuilder tooLongKey = new StringBuilder();
        for (int i = 0; i < CosmosDbKeyEscape.MAX_KEY_LENGTH + 1; i++) {
            tooLongKey.append("a");
        }

        String tooLongKeyWithIllegalCharacters = "?test?" + tooLongKey.toString();
        String sanitizedKey = CosmosDbKeyEscape.escapeKey(tooLongKeyWithIllegalCharacters);

        // Verify the key ws truncated
        Assert.assertTrue(sanitizedKey.length() <= CosmosDbKeyEscape.MAX_KEY_LENGTH);

        // Make sure the escaping still happened
        Assert.assertTrue(sanitizedKey.startsWith("*3ftest*3f"));
    }

    @Test
    public void Sanitize_Key_Should_Escape_Illegal_Character()
    {
        // Ascii code of "?" is "3f".
        String sanitizedKey = CosmosDbKeyEscape.escapeKey("?test?");
        Assert.assertEquals("*3ftest*3f", sanitizedKey);

        // Ascii code of "/" is "2f".
        String sanitizedKey2 = CosmosDbKeyEscape.escapeKey("/test/");
        Assert.assertEquals("*2ftest*2f", sanitizedKey2);

        // Ascii code of "\" is "5c".
        String sanitizedKey3 = CosmosDbKeyEscape.escapeKey("\\test\\");
        Assert.assertEquals("*5ctest*5c", sanitizedKey3);

        // Ascii code of "#" is "23".
        String sanitizedKey4 = CosmosDbKeyEscape.escapeKey("#test#");
        Assert.assertEquals("*23test*23", sanitizedKey4);

        // Ascii code of "*" is "2a".
        String sanitizedKey5 = CosmosDbKeyEscape.escapeKey("*test*");
        Assert.assertEquals("*2atest*2a", sanitizedKey5);

        // Check a compound key
        String compoundSanitizedKey = CosmosDbKeyEscape.escapeKey("?#/");
        Assert.assertEquals("*3f*23*2f", compoundSanitizedKey);
    }

    @Test
    public void Collisions_Should_Not_Happen()
    {
        String validKey = "*2atest*2a";
        String validKey2 = "*test*";

        // If we failed to esacpe the "*", then validKey2 would
        // escape to the same value as validKey. To prevent this
        // we makes sure to escape the *.

        // Ascii code of "*" is "2a".
        String escaped1 = CosmosDbKeyEscape.escapeKey(validKey);
        String escaped2 = CosmosDbKeyEscape.escapeKey(validKey2);

        Assert.assertNotEquals(escaped1, escaped2);
    }

    @Test
    public void Long_Key_Should_Not_Be_Truncated_With_False_CompatibilityMode() {
        StringBuilder tooLongKey = new StringBuilder();
        for (int i = 0; i < CosmosDbKeyEscape.MAX_KEY_LENGTH + 1; i++) {
            tooLongKey.append("a");
        }

        String sanitizedKey = CosmosDbKeyEscape.escapeKey(tooLongKey, new String(), false);
        Assert.assertEquals(CosmosDbKeyEscape.MAX_KEY_LENGTH + 1, sanitizedKey.length());

        // The resulting key should be identical
        Assert.assertEquals(tooLongKey, sanitizedKey);
    }

    @Test
    public void Long_Key_With_Illegal_Characters_Should_Not_Be_Truncated_With_False_CompatibilityMode()
    {
        StringBuilder tooLongKey = new StringBuilder();
        for (int i = 0; i < CosmosDbKeyEscape.MAX_KEY_LENGTH + 1; i++) {
            tooLongKey.append("a");
        }

        String longKeyWithIllegalCharacters = "?test?" + tooLongKey.toString();
        String sanitizedKey = CosmosDbKeyEscape.escapeKey(longKeyWithIllegalCharacters, new String(), false);

        // Verify the key was NOT truncated
        Assert.assertEquals(longKeyWithIllegalCharacters.length() + 4, sanitizedKey.length());

        // Make sure the escaping still happened
        Assert.assertTrue(sanitizedKey.startsWith("*3ftest*3f"));
    }

    @Test
    public void KeySuffix_Is_Added_To_End_of_Key()
    {
        String suffix = "test suffix";
        String key = "this is a test";
        String sanitizedKey = CosmosDbKeyEscape.escapeKey(key, suffix, false);

        // Verify the suffix was added to the end of the key
        Assert.assertEquals(sanitizedKey, key + suffix);
    }
}
