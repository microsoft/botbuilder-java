package com.microsoft.bot.builder;

import com.microsoft.bot.builder.TurnContext;

import java.util.function.Supplier;

/// <summary>
/// Handles persistence of a conversation state object using the conversation ID as part of the key.
/// </summary>
/// <typeparam name="TState">The type of the conversation state object.</typeparam>
public class ConversationState<TState> extends BotState<TState>
{
    /// <summary>
    /// The key to use to read and write this conversation state object to storage.
    /// </summary>
    //
    // Note: Hard coded to maintain compatibility with C#
    // "ConversationState:{typeof(ConversationState<TState>).Namespace}.{typeof(ConversationState<TState>).Name}"
    public static String PropertyName() {
      return String.format("ConversationState:Microsoft.Bot.Builder.Core.Extensions.ConversationState`1");
    }

    /// <summary>
    /// Creates a new <see cref="ConversationState{TState}"/> object.
    /// </summary>
    /// <param name="storage">The storage provider to use.</param>
    /// <param name="settings">The state persistance options to use.</param>
    public ConversationState(Storage storage, Supplier<? extends TState> ctor) {
        this(storage, null, ctor);
    }

    public ConversationState(Storage storage, StateSettings settings, Supplier<? extends TState> ctor)  {
        super(storage, PropertyName(),
                (context) -> {
                    return String.format("conversation/%s/%s", context.getActivity().channelId(), context.getActivity().conversation().id());
                },
                ctor,
                settings);
    }

    /// <summary>
    /// Gets the conversation state object from turn context.
    /// </summary>
    /// <param name="context">The context object for this turn.</param>
    /// <returns>The coversation state object.</returns>
    public static <TState> TState Get(TurnContext context) throws IllegalArgumentException {
        return context.getServices().Get(PropertyName());
    }
}
