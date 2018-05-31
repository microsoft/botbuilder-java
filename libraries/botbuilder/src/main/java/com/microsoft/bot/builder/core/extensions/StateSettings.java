//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.core.extensions;

public class StateSettings
{
    private boolean lastWriterWins = true;
    public boolean getLastWriterWins() {
        return this.lastWriterWins;
    }
    public void setLast(boolean lastWriterWins) {
        this.lastWriterWins = lastWriterWins;
    }
}
