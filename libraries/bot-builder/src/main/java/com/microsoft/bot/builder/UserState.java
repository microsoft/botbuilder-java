package com.microsoft.bot.builder;

import com.microsoft.bot.builder.StateSettings;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.TurnContext;


import java.util.function.Supplier;

/**
 * Handles persistence of a user state object using the user ID as part of the key.
 * @param TState The type of the user state object.
 */
public class UserState<TState> extends BotState<TState>
{
    /**
     * The key to use to read and write this conversation state object to storage.
     */
    // Note: Hard coded to maintain compatibility with C#
    // "UserState:{typeof(UserState<TState>).Namespace}.{typeof(UserState<TState>).Name}"
    public static String PropertyName() {
        return String.format("UserState:Microsoft.Bot.Builder.Core.Extensions.UserState`1");
    }

    /**
     * Creates a new {@link UserState{TState}} object.
     * @param storage The storage provider to use.
     * @param settings The state persistance options to use.
     */
    public UserState(Storage storage, Supplier<? extends TState> ctor) {
        this(storage, ctor, null);
    }
    public UserState(Storage storage, Supplier<? extends TState> ctor, StateSettings settings) {
        super(storage, PropertyName(),
                (context) -> {
                    return String.format("user/%s/%s", context.getActivity().channelId(), context.getActivity().conversation().id());
                },
                ctor,
                settings);
    }

    /**
     * Gets the user state object from turn context.
     * @param context The context object for this turn.
     * @return The user state object.
     */
    public static <TState> TState Get(TurnContext context) throws IllegalArgumentException {
        return context.getServices().<TState>Get(PropertyName());
    }
}
