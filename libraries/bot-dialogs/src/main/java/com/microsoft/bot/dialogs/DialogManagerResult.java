package com.microsoft.bot.dialogs;

import com.microsoft.bot.schema.Activity;

/**
 * Represents the result of the Dialog Manager turn.
 */
public class DialogManagerResult {
        /// <summary>
        /// Gets or sets the result returned to the caller.
        /// </summary>
        /// <value>The result returned to the caller.</value>
        private DialogTurnResult turnResult;

        /// <summary>
        /// Gets or sets the array of resulting activities.
        /// </summary>
        /// <value>The array of resulting activities.</value>
        private Activity[] activities;

        /// <summary>
        /// Gets or sets the resulting new state.
        /// </summary>
        /// <value>The resulting new state.</value>
        private PersistedState newState;

}
