// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

/**
 * Can convert from a generic recognizer result to a strongly typed one.
 */
public interface RecognizerConvert {
    /**
     * Convert recognizer result.
     * 
     * @param result Result to convert.
     */
    void convert(Object result);
}
