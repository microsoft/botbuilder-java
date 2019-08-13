package com.microsoft.bot.builder;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;

/**
 * Provides helper methods for getting state objects from the turn context.
 */
public class StateTurnContextExtensions
{
    /**
     * Gets a conversation state object from the turn context.
     * @param TState The type of the state object to get.
     * @param context The context object for this turn.
     * @return The state object.
     */
    public static <TState extends Object> TState GetConversationState(TurnContext context) throws IllegalArgumentException {

        return ConversationState.<TState>Get(context);
    }

    /**
     * Gets a user state object from the turn context.
     * @param TState The type of the state object to get.
     * @param context The context object for this turn.
     * @return The state object.
     */
    public static <TState> TState GetUserState(TurnContext context) throws IllegalArgumentException {
        return UserState.<TState>Get(context);
    }
}
