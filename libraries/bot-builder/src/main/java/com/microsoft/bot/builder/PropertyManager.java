// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

public interface PropertyManager {
    <T> StatePropertyAccessor<T> createProperty(String name);
}
