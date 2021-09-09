// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import com.microsoft.bot.dialogs.DialogContext;
import java.util.concurrent.CompletableFuture;

/**
 * MemoryScope represents a named memory scope abstract class.
 */
public abstract class MemoryScope {
        /**
         * Initializes a new instance of the class.
         *
         * @param name Name of the scope.
         * @param includeInSnapshot Value indicating whether this memory should be included in snapshot.
         */
        public MemoryScope(String name, Boolean includeInSnapshot) {
            this.includeInSnapshot = includeInSnapshot;
            this.name = name;
        }

        /**
         * Name of the scope.
         */
        private String name;

        /**
         * Value indicating whether this memory should be included in snapshot.
         */
        private Boolean includeInSnapshot;

        /**
         * @return String Gets the name of the scope.
         */
        public String getName() {
            return this.name;
        }

        /**
         * @param withName Sets the name of the scope.
         */
        public void setName(String withName) {
            this.name = withName;
        }

        /**
         * @return Boolean Returns the value indicating whether this memory should be included in snapshot.
         */
        public Boolean getIncludeInSnapshot() {
            return this.includeInSnapshot;
        }


        /**
         * @param withIncludeInSnapshot Sets the value indicating whether this memory should be included in snapshot.
         */
        public void setIncludeInSnapshot(Boolean withIncludeInSnapshot) {
            this.includeInSnapshot = withIncludeInSnapshot;
        }

        /**
         *  Get the backing memory for this scope.
         *
         * @param dialogContext The DialogContext to get from the memory store.
         * @return Object The memory for this scope.
         */
        public abstract Object getMemory(DialogContext dialogContext);

        /**
         * Changes the backing object for the memory scope.
         *
         * @param dialogContext The DialogContext to set in memory store.
         * @param memory The memory to set the DialogContext to.
         */
        public abstract void setMemory(DialogContext dialogContext, Object memory);

        /**
         * Populates the state cache for this  from the storage layer.
         *
         * @param dialogContext The dialog context object for this turn.
         * @param force True to overwrite any existing state cache or false to load state from storage only
         * if the cache doesn't already exist.
         * @return CompletableFuture  A future that represents the work queued to execute.
         */
        public CompletableFuture<Void> load(DialogContext dialogContext, Boolean force) {
            return CompletableFuture.completedFuture(null);
        }


        /**
         * Writes the state cache for this to the storage layer.
         *
         * @param dialogContext The dialog context Object for this turn.
         * @param force True to save the state cache to storage. or false to save state to storage only
         * if a property in the cache has changed.
         * @return CompletableFuture A future that represents the work queued to execute.
         */
        public CompletableFuture<Void> saveChanges(DialogContext dialogContext, Boolean force) {
            return CompletableFuture.completedFuture(null);
        }

        /**
         * Deletes any state in storage and the cache for this.
         *
         * @param dialogContext The dialog context Object for this turn.
         * @return CompletableFuture A future that represents the work queued to execute.
         */
        public CompletableFuture<Void> delete(DialogContext dialogContext) {
            return CompletableFuture.completedFuture(null);
        }

}
