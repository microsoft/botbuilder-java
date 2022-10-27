// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.dialogs.ScopePath;

/**
 * MemoryScope represents a named memory scope abstract class.
 */
public class UserMemoryScope extends BotStateMemoryScope<UserState> {
    /**
     * DialogMemoryScope maps "this" to dc.ActiveDialog.State.
     */
    public UserMemoryScope() {
        super(UserState.class, ScopePath.USER);
    }
}
