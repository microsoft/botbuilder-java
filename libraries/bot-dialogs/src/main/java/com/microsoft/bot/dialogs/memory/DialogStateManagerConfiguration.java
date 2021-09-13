// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;

/**
 * Configures the path resolvers and memory scopes for the dialog state manager.
 */
public class DialogStateManagerConfiguration {

    private List<PathResolver> pathResolvers = new ArrayList<PathResolver>();

    private List<MemoryScope> memoryScopes = new ArrayList<MemoryScope>();


    /**
     * @return Returns the list of PathResolvers.
     */
    public List<PathResolver> getPathResolvers() {
        return this.pathResolvers;
    }


    /**
     * @param withPathResolvers Set the list of PathResolvers.
     */
    public void setPathResolvers(List<PathResolver> withPathResolvers) {
        this.pathResolvers = withPathResolvers;
    }


    /**
     * @return Returns the list of MemoryScopes.
     */
    public List<MemoryScope> getMemoryScopes() {
        return this.memoryScopes;
    }


    /**
     * @param withMemoryScopes Set the list of MemoryScopes.
     */
    public void setMemoryScopes(List<MemoryScope> withMemoryScopes) {
        this.memoryScopes = withMemoryScopes;
    }


}
