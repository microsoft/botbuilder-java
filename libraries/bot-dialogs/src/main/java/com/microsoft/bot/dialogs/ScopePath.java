// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Defines paths for the available scopes.
 */
public final class ScopePath {

    private ScopePath() {
    }

        /**
         * User memory scope root path.
         */
        public static final String USER = "user";

        /**
         * Conversation memory scope root path.
         */
        public static final String CONVERSATION = "conversation";

        /**
         * Dialog memory scope root path.
         */
        public static final String DIALOG = "dialog";

        /**
         * DialogClass memory scope root path.
         */
        public static final String DIALOG_CLASS = "dialogclass";

        /**
         * DialogContext memory scope root path.
         */
        public static final String DIALOG_CONTEXT = "dialogContext";

        /**
         * This memory scope root path.
         */
        public static final String THIS = "this";

        /**
         * Class memory scope root path.
         */
        public static final String CLASS = "class";

        /**
         * Settings memory scope root path.
         */
        public static final String SETTINGS = "settings";

        /**
         * Turn memory scope root path.
         */
        public static final String TURN = "turn";
}
