// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to escape CosmosDB keys.
 */
public final class CosmosDbKeyEscape {
    private static final int ESCAPE_LENGTH = 3;

    private CosmosDbKeyEscape() {

    }

    /**
     * Per the CosmosDB Docs, there is a max key length of 255.
     */
    static final int MAX_LENGTH = 255;

    /**
     * The list of illegal characters for Cosmos DB Keys comes from this list on the CosmostDB docs:
     *    https://docs.microsoft.com/dotnet/api/microsoft.azure.documents.resource.id?view=azure-dotnet#remarks
     *
     * <p>Note: We are also escaping the "*" character, as that what we're using
     * as our escape character.</p>
     *
     * <p>Note: The Java version escapes more than .NET since otherwise it errors out.  The additional characters are
     * quote, single quote, semi-colon.</p>
     */
    private static final Character[] ILLEGAL_CHARS = new Character[] {'\\', '?', '/', '#', '*', ';', '\"', '\''};

    /**
     * We are escaping illegal characters using a "*{AsciiCodeInHex}" pattern. This
     * means a key of "?test?" would be escaped as "*3ftest*3f".
     */
    private static final Map<Character, String> ESCAPE_REPLACEMENTS =
        Arrays.stream(ILLEGAL_CHARS).collect(Collectors.toMap(c -> c, c -> "*" + String.format("%02x", (int) c)));

    /**
     * Converts the key into a DocumentID that can be used safely with Cosmos DB.
     *
     * @param key The key to escape.
     * @return An escaped key that can be used safely with CosmosDB.
     *
     * @see #ILLEGAL_CHARS
     */
    public static String escapeKey(String key) {
        if (StringUtils.isEmpty(key) || StringUtils.isWhitespace(key)) {
            throw new IllegalArgumentException("key");
        }

        StringBuilder escKey = new StringBuilder(key.length() * ESCAPE_LENGTH);
        for (char c : key.toCharArray()) {
            String escaped = ESCAPE_REPLACEMENTS.get(c);
            if (escaped != null) {
                escKey.append(escaped);
            } else {
                escKey.append(c);
            }
        }

        return truncateKeyIfNeeded(escKey.toString());
    }

    private static String truncateKeyIfNeeded(String key) {
        if (key.length() > MAX_LENGTH) {
            String hash = String.format("%x", key.hashCode());
            key = key.substring(0, MAX_LENGTH - hash.length()) + hash;
        }

        return key;
    }
}
