// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

public interface StoreItem {
    /**
     * eTag for concurrency
     */
    String getETag();

    void setETag(String eTag);
}
