// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 *  Defines path for available dialogs.
 */
public final class DialogPath {

        private DialogPath() {

        }

        /**
         * Counter of emitted events.
         */
        public static final String EVENTCOUNTER = "dialog.eventCounter";

        /**
         * Currently expected properties.
         */
        public static final String EXPECTEDPROPERTIES = "dialog.expectedProperties";

        /**
         * Default operation to use for entities where there is no identified operation entity.
         */
        public static final String DEFAULTOPERATION = "dialog.defaultOperation";

        /**
         * Last surfaced entity ambiguity event.
         */
        public static final String LASTEVENT = "dialog.lastEvent";

        /**
         * Currently required properties.
         */
        public static final String REQUIREDPROPERTIES = "dialog.requiredProperties";

        /**
         * Number of retries for the current Ask.
         */
        public static final String RETRIES = "dialog.retries";

        /**
         * Last intent.
         */
        public static final String LASTINTENT = "dialog.lastIntent";

        /**
         * Last trigger event: defined in FormEvent, ask, clarifyEntity etc.
         */
        public static final String LASTTRIGGEREVENT = "dialog.lastTriggerEvent";

        /**
         * Utility function to get just the property name without the memory scope prefix.
         * @param property Memory scope property path.
         * @return Name of the property without the prefix.
         */
        public static String getPropertyName(String property) {
            return property.replace("dialog.", "");
        }

}
