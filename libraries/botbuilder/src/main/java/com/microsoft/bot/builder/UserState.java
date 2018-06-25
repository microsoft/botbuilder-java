package com.microsoft.bot.builder;

import com.microsoft.bot.builder.StateSettings;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.TurnContext;


import java.util.function.Supplier;

/// <summary>
/// Handles persistence of a user state object using the user ID as part of the key.
/// </summary>
/// <typeparam name="TState">The type of the user state object.</typeparam>
public class UserState<TState> extends BotState<TState>
{
    /// <summary>
    /// The key to use to read and write this conversation state object to storage.
    /// </summary>
    // Note: Hard coded to maintain compatibility with C#
    // "UserState:{typeof(UserState<TState>).Namespace}.{typeof(UserState<TState>).Name}"
    public static String PropertyName() {
        return String.format("UserState:Microsoft.Bot.Builder.Core.Extensions.UserState`1");
    }

    /// <summary>
    /// Creates a new <see cref="UserState{TState}"/> object.
    /// </summary>
    /// <param name="storage">The storage provider to use.</param>
    /// <param name="settings">The state persistance options to use.</param>
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

    /// <summary>
    /// Gets the user state object from turn context.
    /// </summary>
    /// <param name="context">The context object for this turn.</param>
    /// <returns>The user state object.</returns>
    public static <TState> TState Get(TurnContext context) throws IllegalArgumentException {
        return context.getServices().<TState>Get(PropertyName());
    }
}
