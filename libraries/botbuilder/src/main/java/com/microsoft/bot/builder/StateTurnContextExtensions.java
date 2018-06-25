package com.microsoft.bot.builder;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;

/// <summary>
/// Provides helper methods for getting state objects from the turn context.
/// </summary>
public class StateTurnContextExtensions
{
    /// <summary>
    /// Gets a conversation state object from the turn context.
    /// </summary>
    /// <typeparam name="TState">The type of the state object to get.</typeparam>
    /// <param name="context">The context object for this turn.</param>
    /// <returns>The state object.</returns>
    public static <TState extends Object> TState GetConversationState(TurnContext context) throws IllegalArgumentException {

        return ConversationState.<TState>Get(context);
    }

    /// <summary>
    /// Gets a user state object from the turn context.
    /// </summary>
    /// <typeparam name="TState">The type of the state object to get.</typeparam>
    /// <param name="context">The context object for this turn.</param>
    /// <returns>The state object.</returns>
    public static <TState> TState GetUserState(TurnContext context) throws IllegalArgumentException {
        return UserState.<TState>Get(context);
    }
}
