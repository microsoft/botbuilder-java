package com.microsoft.bot.builder;

import com.microsoft.bot.builder.TurnContext;

import java.util.function.Supplier;

/**
 * Handles persistence of a conversation state object using the conversation ID as part of the key.
 * @param TState The type of the conversation state object.
 */
public class ConversationState<TState> extends BotState<TState>
{
    /**
     * The key to use to read and write this conversation state object to storage.
     */
    //
    // Note: Hard coded to maintain compatibility with C#
    // "ConversationState:{typeof(ConversationState<TState>).Namespace}.{typeof(ConversationState<TState>).Name}"
    public static String PropertyName() {
      return String.format("ConversationState:Microsoft.Bot.Builder.Core.Extensions.ConversationState`1");
    }

    /**
     * Creates a new {@link ConversationState{TState}} object.
     * @param storage The storage provider to use.
     * @param settings The state persistance options to use.
     */
    public ConversationState(Storage storage, Supplier<? extends TState> ctor) {
        this(storage, null, ctor);
    }

    public ConversationState(Storage storage, StateSettings settings, Supplier<? extends TState> ctor)  {
        super(storage, PropertyName(),
                (context) -> {
                    return String.format("conversation/%s/%s", context.getActivity().getChannelId(), context.getActivity().getConversation().getId());
                },
                ctor,
                settings);
    }

    /**
     * Gets the conversation state object from turn context.
     * @param context The context object for this turn.
     * @return The coversation state object.
     */
    public static <TState> TState Get(TurnContext context) throws IllegalArgumentException {
        return context.getServices().Get(PropertyName());
    }
}
