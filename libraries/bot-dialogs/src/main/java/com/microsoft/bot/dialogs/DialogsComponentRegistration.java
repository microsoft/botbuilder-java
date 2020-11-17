package com.microsoft.bot.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.bot.builder.ComponentRegistration;
import com.microsoft.bot.dialogs.memory.ComponentMemoryScopes;
import com.microsoft.bot.dialogs.memory.ComponentPathResolvers;
import com.microsoft.bot.dialogs.memory.PathResolver;
import com.microsoft.bot.dialogs.memory.PathResolvers.AtAtPathResolver;
import com.microsoft.bot.dialogs.memory.PathResolvers.AtPathResolver;
import com.microsoft.bot.dialogs.memory.PathResolvers.DollarPathResolver;
import com.microsoft.bot.dialogs.memory.PathResolvers.HashPathResolver;
import com.microsoft.bot.dialogs.memory.PathResolvers.PercentPathResolver;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.TurnMemoryScope;

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
        return listToReturn;
    }

}
