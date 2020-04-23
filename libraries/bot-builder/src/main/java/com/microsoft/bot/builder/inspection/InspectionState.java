// CHECKSTYLE:OFF
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.inspection;

import com.microsoft.bot.builder.BotState;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.TurnContext;

public class InspectionState extends BotState {
    /**
     * Initializes a new instance of the BotState class.
     *
     * @param withStorage The storage provider to use.
     */
    public InspectionState(Storage withStorage) {
        super(withStorage, InspectionState.class.getSimpleName());
    }

    @Override
    public String getStorageKey(TurnContext turnContext) {
        return InspectionState.class.getSimpleName();
    }
}
