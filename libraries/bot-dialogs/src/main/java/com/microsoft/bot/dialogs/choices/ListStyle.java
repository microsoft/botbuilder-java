// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

/**
 * Controls the way that choices for a `ChoicePrompt` or yes/no options for a `ConfirmPrompt` are
 * presented to a user.
 */
public enum ListStyle {
    /// Don't include any choices for prompt.
    NONE,

    /// Automatically select the appropriate style for the current channel.
    AUTO,

    /// Add choices to prompt as an inline list.
    INLINE,

    /// Add choices to prompt as a numbered list.
    LIST,

    /// Add choices to prompt as suggested actions.
    SUGGESTED_ACTION,

    /// Add choices to prompt as a HeroCard with buttons.
    HEROCARD
}
