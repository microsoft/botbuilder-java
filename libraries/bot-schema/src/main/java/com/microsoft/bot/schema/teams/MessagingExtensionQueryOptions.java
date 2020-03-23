// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessagingExtensionQueryOptions {
    @JsonProperty(value = "skip")
    private int skip;

    @JsonProperty(value = "count")
    private int count;

    public int getSkip() {
        return skip;
    }

    public void setSkip(int withSkip) {
        skip = withSkip;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int withCount) {
        count = withCount;
    }
}
