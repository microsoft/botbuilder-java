// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

package com.microsoft.bot.dialogs;

/**
 * These are the keys which are persisted.
 */
public class PersistedStateKeys {
    /**
     * The key for the user state.
     */
    private String userState;

    /**
     * The conversation state.
     */
    private String conversationState;

    /**
     * @return Gets the user state;
     */
    public String getUserState() {
        return this.userState;
    }

    /**
     * @param withUserState Sets the user state.
     */
    public void setUserState(String withUserState) {
        this.userState = withUserState;
    }

    /**
     * @return Gets the conversation state.
     */
    public String getConversationState() {
        return this.conversationState;
    }

    /**
     * @param withConversationState Sets the conversation state.
     */
    public void setConversationState(String withConversationState) {
        this.conversationState = withConversationState;
    }

}
