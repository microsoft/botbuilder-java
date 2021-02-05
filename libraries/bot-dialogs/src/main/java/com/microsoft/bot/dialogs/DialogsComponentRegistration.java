// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.bot.builder.ComponentRegistration;
import com.microsoft.bot.dialogs.memory.ComponentMemoryScopes;
import com.microsoft.bot.dialogs.memory.ComponentPathResolvers;
import com.microsoft.bot.dialogs.memory.PathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.PercentPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.AtAtPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.AtPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.HashPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.DollarPathResolver;
import com.microsoft.bot.dialogs.memory.scopes.ClassMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.ConversationMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.DialogClassMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.DialogContextMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.DialogMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.SettingsMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.ThisMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.TurnMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.UserMemoryScope;

/**
 * Makes Dialogs components available to the system registering functionality.
 */
public class DialogsComponentRegistration extends ComponentRegistration
        implements ComponentMemoryScopes, ComponentPathResolvers {

    /**
     * Gets the Dialogs Path Resolvers.
     */
    @Override
    public Iterable<PathResolver> getPathResolvers() {
        List<PathResolver> listToReturn = new ArrayList<PathResolver>();
        listToReturn.add((PathResolver) new DollarPathResolver());
        listToReturn.add((PathResolver) new HashPathResolver());
        listToReturn.add((PathResolver) new AtAtPathResolver());
        listToReturn.add((PathResolver) new AtPathResolver());
        listToReturn.add((PathResolver) new PercentPathResolver());
        return listToReturn;
    }

    /**
     * Gets the Dialogs Memory Scopes.
     */
    @Override
    public Iterable<MemoryScope> getMemoryScopes() {
        List<MemoryScope> listToReturn = new ArrayList<MemoryScope>();
        listToReturn.add(new TurnMemoryScope());
        listToReturn.add(new SettingsMemoryScope());
        listToReturn.add(new DialogMemoryScope());
        listToReturn.add(new DialogContextMemoryScope());
        listToReturn.add(new DialogClassMemoryScope());
        listToReturn.add(new ClassMemoryScope());
        listToReturn.add(new ThisMemoryScope());
        listToReturn.add(new ConversationMemoryScope());
        listToReturn.add(new UserMemoryScope());
        return listToReturn;
    }

}
