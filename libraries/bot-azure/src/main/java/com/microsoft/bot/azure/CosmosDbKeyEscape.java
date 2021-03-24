// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to escape CosmosDB keys.
 */
public final class CosmosDbKeyEscape {

    private CosmosDbKeyEscape() {
        // not called
    }

    private static final Integer ESCAPE_LENGTH = 3;

    /**
     * Older libraries had a max key length of 255. The limit is now 1023. In this
     * library, 255 remains the default for backwards compat. To override this
     * behavior, and use the longer limit set
     * CosmosDbPartitionedStorageOptions.CompatibilityMode to false.
     * https://docs.microsoft.com/en-us/azure/cosmos-db/concepts-limits#per-item-limits.
     */
    public static final Integer MAX_KEY_LENGTH = 255;

    /**
     * The list of illegal characters for Cosmos DB Keys comes from this list on the
     * CosmostDB docs:
     * https://docs.microsoft.com/dotnet/api/microsoft.azure.documents.resource.id?view=azure-dotnet#remarks
     *
     * Note: We are also escaping the "*" character, as that what we're using as our
     * escape character.
     *
     * Note: The Java version escapes more than .NET since otherwise it errors out.
     * The additional characters are quote, single quote, semi-colon.
     */
    private static final char[] ILLEGAL_KEYS = new char[] {'\\', '?', '/', '#', '*', ';', '\"', '\''};

    /**
     * We are escaping illegal characters using a "*{AsciiCodeInHex}" pattern. This
     * means a key of "?test?" would be escaped as "*3ftest*3f".
     */
    private static final Map<Character, String> ILLEGAL_KEY_CHARACTER_REPLACEMENT_MAP = Arrays
        .stream(ArrayUtils.toObject(ILLEGAL_KEYS))
        .collect(Collectors.toMap(c -> c, c -> "*" + String.format("%02x", (int) c)));

    /**
     * Converts the key into a DocumentID that can be used safely with Cosmos DB.
     *
     * @param key The key to escape.
     * @return An escaped key that can be used safely with CosmosDB.
     *
     * @see #ILLEGAL_KEYS
     */
    public static String escapeKey(String key) {
        return escapeKey(key, new String(), true);
    }

    /**
     * Converts the key into a DocumentID that can be used safely with Cosmos DB.
     *
     * @param key               The key to escape.
     * @param suffix            The string to add at the end of all row keys.
     * @param compatibilityMode True if running in compatability mode and keys
     *                          should be truncated in order to support previous
     *                          CosmosDb max key length of 255. This behavior can be
     *                          overridden by setting
     *                          {@link CosmosDbPartitionedStorage.compatibilityMode}
     *                          to false. *
     * @return An escaped key that can be used safely with CosmosDB.
     */
    public static String escapeKey(String key, String suffix, Boolean compatibilityMode) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key");
        }

        suffix = suffix == null ? new String() : suffix;

        Integer firstIllegalCharIndex = StringUtils.indexOfAny(key, new String(ILLEGAL_KEYS));

        // If there are no illegal characters, and the key is within length costraints,
        // return immediately and avoid any further processing/allocations
        if (firstIllegalCharIndex == -1) {
            return truncateKeyIfNeeded(key.concat(suffix), compatibilityMode);
        }

        // Allocate a builder that assumes that all remaining characters might be
        // replaced
        // to avoid any extra allocations
        StringBuilder sanitizedKeyBuilder =
            new StringBuilder(key.length() + ((key.length() - firstIllegalCharIndex) * ESCAPE_LENGTH));

        // Add all good characters up to the first bad character to the builder first
        for (Integer index = 0; index < firstIllegalCharIndex; index++) {
            sanitizedKeyBuilder.append(key.charAt(index));
        }

        Map<Character, String> illegalCharacterReplacementMap = ILLEGAL_KEY_CHARACTER_REPLACEMENT_MAP;

        // Now walk the remaining characters, starting at the first known bad character,
        // replacing any bad ones with
        // their designated replacement value from the
        for (Integer index = firstIllegalCharIndex; index < key.length(); index++) {
            Character ch = key.charAt(index);

            // Check if this next character is considered illegal and, if so, append its
            // replacement;
            // otherwise just append the good character as is
            if (illegalCharacterReplacementMap.containsKey(ch)) {
                sanitizedKeyBuilder.append(illegalCharacterReplacementMap.get(ch));
            } else {
                sanitizedKeyBuilder.append(ch);
            }
        }

        if (StringUtils.isNotBlank(key)) {
            sanitizedKeyBuilder.append(suffix);
        }

        return truncateKeyIfNeeded(sanitizedKeyBuilder.toString(), compatibilityMode);
    }

    private static String truncateKeyIfNeeded(String key, Boolean truncateKeysForCompatibility) {
        if (!truncateKeysForCompatibility) {
            return key;
        }

        if (key.length() > MAX_KEY_LENGTH) {
            String hash = String.format("%x", key.hashCode());
            key = key.substring(0, MAX_KEY_LENGTH - hash.length()) + hash;
        }

        return key;
    }
}
